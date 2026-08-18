package com.primeshop.order;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.primeshop.cart.Cart;
import com.primeshop.cart.CartItem;
import com.primeshop.cart.CartItemRepo;
import com.primeshop.cart.CartRepo;
import com.primeshop.product.Product;
import com.primeshop.product.ProductRepo;
import com.primeshop.seller.SellerProfile;
import com.primeshop.seller.SellerRepo;
import com.primeshop.stock.RevenueService;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;
import com.primeshop.voucher.Voucher;
import com.primeshop.voucher.VoucherService;
import java.util.Optional;
import jakarta.transaction.Transactional;

@Service
public class OrderService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private CartItemRepo cartItemRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private RevenueService revenueService;
    @Autowired
    private SellerRepo sellerRepo;

    @Autowired
    private VoucherService voucherService;

    private final OrderRepo orderRepository;

    public OrderService(OrderRepo orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        //Lấy cart của user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));

        Cart cart = cartRepo.findByUser(user).orElseThrow(() -> new RuntimeException("Cart is empty!"));

        List<CartItem> cartItems = cartItemRepo.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("No items in cart to order!");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Product '" + product.getName() + "' is out of stock.");
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepo.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            orderItem.setProductName(product.getName()); // Lưu tên sản phẩm snapshot
            
            // ✅ FIX 3: Lưu URL ảnh vào chi tiết đơn hàng (Snapshot)
            // Điều này đảm bảo dù sau này sản phẩm đổi ảnh, đơn hàng vẫn giữ ảnh cũ
            orderItem.setProductImageUrl(product.getImageUrl());

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        // Xử lý voucher - LUÔN TĂNG USED_COUNT KHI ĐẶT HÀNG
        List<Voucher> appliedVouchers = new ArrayList<>();
        List<String> voucherCodesToProcess = new ArrayList<>();
        
        // Thu thập voucher codes từ cả request và cart
        if (request.getVoucherCodes() != null && !request.getVoucherCodes().isEmpty()) {
            voucherCodesToProcess.addAll(request.getVoucherCodes());
        }
        
        // Nếu không có voucher từ request, lấy từ cart
        if (voucherCodesToProcess.isEmpty()) {
            List<Voucher> cartVouchers = cart.getVouchers();
            if (cartVouchers != null && !cartVouchers.isEmpty()) {
                voucherCodesToProcess = cartVouchers.stream()
                    .map(Voucher::getCode)
                    .collect(Collectors.toList());
            }
        }
        
        // Xử lý tất cả voucher codes (tăng used_count)
        if (!voucherCodesToProcess.isEmpty()) {
            try {
                // Sử dụng method để tăng used_count
                appliedVouchers = voucherService.processVouchersForOrder(
                    voucherCodesToProcess, 
                    totalAmount.doubleValue()
                );
            } catch (RuntimeException e) {
                throw e; // Re-throw để rollback transaction
            }
        }

        // Tính discountAmount tổng hợp nếu cần
        BigDecimal discountAmount = cart.getDiscount() != null ? cart.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
        order.setVouchers(appliedVouchers); 
        order.setOrderItems(orderItems);
        order.setFullName(request.getFullName());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setAddress(request.getAddress());
        if (request.getNote() != null) {
            order.setNote(request.getNote());
        } else {
            order.setNote("Không có");
        }

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }

        orderRepo.save(order);
        cartItemRepo.deleteAll(cartItems);

        return new OrderResponse(order); 
    }

    public List<OrderResponse> getOrdersByUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));
        return orderRepo.findByUser(user).stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersBySeller() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));
        SellerProfile seller = sellerRepo.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Tài khoản hiện tại không phải là người bán!"));
        List<Order> orders = orderRepo.findBySellerId(seller.getId());
        return orders.stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId) {        
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found!"));
        return new OrderResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepo.findAll().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> searchOrders(OrderFilterRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));        
        
        Specification<Order> spec = OrderSpecification.filter(request, user);
        List<Order> orders = orderRepo.findAll(spec);
        return orders.stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found!"));
        
        OrderStatus currentStatus = order.getStatus();

        if (!isValidStatusTransition(currentStatus, status)) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + status);
        }
        
        order.setStatus(status);
        orderRepo.save(order);
        
        // Tự động tính toán lợi nhuận khi đơn hàng đạt trạng thái DELIVERED
        if (status == OrderStatus.DELIVERED) {
            BigDecimal orderRevenue = order.getTotalAmount();
            BigDecimal orderProfit = calculateOrderProfit(order);
            String period = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            revenueService.updateRevenueFromOrder(orderRevenue, orderProfit, period);
        }
        
        return new OrderResponse(order);
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING: return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED: return newStatus == OrderStatus.PAID || newStatus == OrderStatus.PAYMENT_FAILED || newStatus == OrderStatus.CANCELLED;
            case PAID: return newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PAYMENT_FAILED: return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case PROCESSING: return newStatus == OrderStatus.INVENTORY || newStatus == OrderStatus.CANCELLED;
            case INVENTORY: return newStatus == OrderStatus.READY_TO_SHIP || newStatus == OrderStatus.CANCELLED;
            case READY_TO_SHIP: return newStatus == OrderStatus.SHIPPING || newStatus == OrderStatus.CANCELLED;
            case SHIPPING: return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED: return newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED;
            case DELIVERED: return newStatus == OrderStatus.CANCELLED;
            case CANCELLED: return false;
            case FAILED_DELIVERY: return false;
            default: throw new IllegalArgumentException("Unknown status: " + currentStatus);
        }
    }

    public List<OrderResponse> filterOrders(OrderFilterRequest filterRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));
        Specification<Order> spec = OrderSpecification.filter(filterRequest, user);
        List<Order> orders = orderRepo.findAll(spec);
        return orders.stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    public Long countOrder() {
        return orderRepo.countByOrder();
    }

    public BigDecimal getTotalPurchase(Long userId) {
        return orderRepo.getTotalPurchase(OrderStatus.DELIVERED, userId);
    }

    public Long countByUser(Long userId) {
        return orderRepo.countByUser(userId);
    }
    
    private BigDecimal calculateOrderProfit(Order order) {
        BigDecimal totalRevenue = order.getTotalAmount();
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            BigDecimal unitCost = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            BigDecimal estimatedUnitCost = unitCost.multiply(BigDecimal.valueOf(0.7));
            BigDecimal itemCost = estimatedUnitCost.multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            totalCost = totalCost.add(itemCost);
        }
        
        return totalRevenue.subtract(totalCost);
    }

    public boolean updateOrderStatus(Long orderId, String status) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) return false;
        Order order = optionalOrder.get();
        order.setStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
        return true;
    }

    private boolean isValidStatus(String status) {
        List<String> allowedStatuses = List.of(
            "READY_TO_SHIP", "PROCESSING", "INVENTORY", "SHIPPING",
            "SHIPPED", "DELIVERED", "RETURNED", "REFUNDED",
            "FAILED_DELIVERY", "CANCELLED", "COMPLETED"
        );
        return allowedStatuses.contains(status);
    }
}