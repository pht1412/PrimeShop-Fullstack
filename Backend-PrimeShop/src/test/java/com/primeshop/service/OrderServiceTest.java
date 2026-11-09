package com.primeshop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.primeshop.cart.Cart;
import com.primeshop.cart.CartItem;
import com.primeshop.cart.CartItemRepo;
import com.primeshop.cart.CartRepo;
import com.primeshop.delivery.DeliveryTracking;
import com.primeshop.delivery.DeliveryTrackingRepository;
import com.primeshop.order.Order;
import com.primeshop.order.OrderRepo;
import com.primeshop.order.OrderRequest;
import com.primeshop.order.OrderResponse;
import com.primeshop.order.OrderService;
import com.primeshop.order.OrderStatus;
import com.primeshop.order.PaymentStatus;
import com.primeshop.product.Product;
import com.primeshop.product.ProductRepo;
import com.primeshop.seller.SellerProfile;
import com.primeshop.seller.SellerRepo;
import com.primeshop.stock.RevenueService;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;
import com.primeshop.voucher.VoucherService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private UserRepo userRepo;
    @Mock private CartRepo cartRepo;
    @Mock private CartItemRepo cartItemRepo;
    @Mock private ProductRepo productRepo;
    @Mock private OrderRepo orderRepo;
    @Mock private RevenueService revenueService;
    @Mock private SellerRepo sellerRepo;
    @Mock private VoucherService voucherService;
    @Mock private DeliveryTrackingRepository trackingRepo;
    private OrderService orderService;

    private User buyer;
    private Cart cart;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepo);
        ReflectionTestUtils.setField(orderService, "userRepo", userRepo);
        ReflectionTestUtils.setField(orderService, "cartRepo", cartRepo);
        ReflectionTestUtils.setField(orderService, "cartItemRepo", cartItemRepo);
        ReflectionTestUtils.setField(orderService, "productRepo", productRepo);
        ReflectionTestUtils.setField(orderService, "orderRepo", orderRepo);
        ReflectionTestUtils.setField(orderService, "revenueService", revenueService);
        ReflectionTestUtils.setField(orderService, "sellerRepo", sellerRepo);
        ReflectionTestUtils.setField(orderService, "voucherService", voucherService);
        ReflectionTestUtils.setField(orderService, "trackingRepo", trackingRepo);

        buyer = new User();
        buyer.setId(1L);
        buyer.setUsername("buyer");
        buyer.setRoles(new HashSet<>());
        cart = new Cart(buyer);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("buyer", "password", List.of())
        );
        lenient().when(userRepo.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        lenient().when(cartRepo.findByUser(buyer)).thenReturn(Optional.of(cart));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrderSplitsItemsBySellerAndSetsRequiredSellerRelation() {
        List<CartItem> items = List.of(
            cartItem(product(10L, "Seller A", 2L), 1, "100.00"),
            cartItem(product(11L, SellerProfile.OFFICIAL_SHOP_NAME, 3L), 2, "200.00")
        );
        when(cartItemRepo.findByCart(cart)).thenReturn(items);
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<OrderResponse> orders = orderService.createOrder(validRequest());

        assertEquals(2, orders.size());
        assertEquals(10L, orders.get(0).getSellerId());
        assertEquals(11L, orders.get(1).getSellerId());
        assertEquals(Order.PaymentType.CASH, orders.get(0).getPaymentType());
        assertEquals(PaymentStatus.PENDING, orders.get(0).getPaymentStatus());
        verify(orderRepo, times(2)).save(any(Order.class));
        verify(cartItemRepo).deleteAll(items);
    }

    @Test
    void createOrderRejectsTheSellerBuyingTheirOwnC2CProduct() {
        CartItem ownProduct = cartItem(product(10L, "My Shop", buyer.getId()), 1, "100.00");
        when(cartItemRepo.findByCart(cart)).thenReturn(List.of(ownProduct));

        assertThrows(ResponseStatusException.class, () -> orderService.createOrder(validRequest()));
    }

    @Test
    void codOrderCanStartProcessingBeforePaymentAndRecordsTheTransition() {
        Order order = order(99L, OrderStatus.CONFIRMED, Order.PaymentType.CASH);
        when(orderRepo.findById(99L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateOrderStatus(99L, OrderStatus.PROCESSING);

        assertEquals(OrderStatus.PROCESSING, response.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        verify(trackingRepo).save(any(DeliveryTracking.class));
    }

    @Test
    void rejectsSkippingOrderWorkflowSteps() {
        Order order = order(99L, OrderStatus.PENDING, Order.PaymentType.CASH);
        when(orderRepo.findById(99L)).thenReturn(Optional.of(order));

        assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrderStatus(99L, OrderStatus.DELIVERED));
        verify(orderRepo, never()).save(any(Order.class));
        verify(trackingRepo, never()).save(any(DeliveryTracking.class));
    }

    @Test
    void codDeliveryCannotCompleteUntilPaymentIsCollected() {
        Order order = order(99L, OrderStatus.SHIPPED, Order.PaymentType.CASH);
        when(orderRepo.findById(99L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateDeliveryOrderStatus(
                        99L, OrderStatus.DELIVERED, null, null, false));

        OrderResponse response = orderService.updateDeliveryOrderStatus(
                99L, OrderStatus.DELIVERED, "Hà Nội", null, true);

        assertEquals(OrderStatus.DELIVERED, response.getOrderStatus());
        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        assertFalse(response.getAllowedNextStatuses().isEmpty());
        assertEquals(List.of(OrderStatus.COMPLETED), response.getAllowedNextStatuses());
        assertEquals(List.of(), response.getAllowedDeliveryStatuses());
    }

    @Test
    void customerCanConfirmReceiptOnlyForTheirDeliveredOrder() {
        Order order = order(99L, OrderStatus.DELIVERED, Order.PaymentType.CASH);
        when(orderRepo.findById(99L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.confirmOrderReceived(99L);

        assertEquals(OrderStatus.COMPLETED, response.getOrderStatus());
        verify(trackingRepo).save(any(DeliveryTracking.class));
    }

    private OrderRequest validRequest() {
        OrderRequest request = new OrderRequest();
        request.setFullName("Buyer");
        request.setPhoneNumber("0123456789");
        request.setAddress("Hanoi");
        return request;
    }

    private Product product(Long sellerId, String shopName, Long ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        owner.setUsername("seller-" + ownerId);
        owner.setRoles(new HashSet<>());

        SellerProfile seller = new SellerProfile();
        seller.setId(sellerId);
        seller.setShopName(shopName);
        seller.setUser(owner);

        Product product = new Product();
        product.setId(sellerId);
        product.setName("Product " + sellerId);
        product.setSeller(seller);
        product.setActive(true);
        product.setStatus(Product.ProductStatus.APPROVED);
        product.setStock(5);
        product.setImageUrl("image.png");
        return product;
    }

    private Order order(Long id, OrderStatus status, Order.PaymentType paymentType) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setPaymentType(paymentType);
        order.setUser(buyer);
        return order;
    }

    private CartItem cartItem(Product product, int quantity, String unitPrice) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(new BigDecimal(unitPrice));
        item.setTotalPrice(new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}
