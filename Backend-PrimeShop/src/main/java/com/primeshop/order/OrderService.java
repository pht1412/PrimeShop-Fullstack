package com.primeshop.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.primeshop.cart.Cart;
import com.primeshop.cart.CartItem;
import com.primeshop.cart.CartItemRepo;
import com.primeshop.cart.CartRepo;
import com.primeshop.delivery.DeliveryTracking;
import com.primeshop.delivery.DeliveryTrackingRepository;
import com.primeshop.product.Product;
import com.primeshop.product.ProductRepo;
import com.primeshop.seller.SellerProfile;
import com.primeshop.seller.SellerRepo;
import com.primeshop.stock.RevenueService;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;
import com.primeshop.voucher.Voucher;
import com.primeshop.voucher.VoucherService;
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
    private final OrderRepo orderRepo;
    @Autowired
    private RevenueService revenueService;
    @Autowired
    private SellerRepo sellerRepo;

    @Autowired
    private VoucherService voucherService;
    @Autowired
    private DeliveryTrackingRepository trackingRepo;

    public OrderService(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Transactional
    public List<OrderResponse> createOrder(OrderRequest request) {
        if (request == null
                || request.getFullName() == null || request.getFullName().isBlank()
                || request.getPhoneNumber() == null || !request.getPhoneNumber().matches("\\d{10}")
                || request.getAddress() == null || request.getAddress().isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Vui lòng cung cấp họ tên, số điện thoại hợp lệ và địa chỉ giao hàng."
            );
        }
        Order.PaymentType paymentType = resolvePaymentType(request.getPaymentMethod());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));

        Cart cart = cartRepo.findByUser(user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng đang trống."));

        List<CartItem> cartItems = cartItemRepo.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng đang trống.");
        }

        Map<Long, SellerProfile> sellers = new LinkedHashMap<>();
        Map<Long, List<OrderItem>> itemsBySeller = new LinkedHashMap<>();
        Map<Long, BigDecimal> totalsBySeller = new LinkedHashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            SellerProfile seller = product.getSeller();

            if (!Boolean.TRUE.equals(product.getActive()) || product.getStatus() != Product.ProductStatus.APPROVED) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sản phẩm '" + product.getName() + "' hiện không thể đặt hàng."
                );
            }

            if (!SellerProfile.OFFICIAL_SHOP_NAME.equals(seller.getShopName())
                    && seller.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bạn không thể đặt mua sản phẩm do chính mình đăng."
                );
            }

            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho."
                );
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPrice());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            orderItem.setProductName(product.getName());
            orderItem.setProductImageUrl(product.getImageUrl());

            Long sellerId = seller.getId();
            sellers.put(sellerId, seller);
            itemsBySeller.computeIfAbsent(sellerId, ignored -> new ArrayList<>()).add(orderItem);
            totalsBySeller.merge(sellerId, orderItem.getTotalPrice(), BigDecimal::add);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepo.save(product);
        }

        List<Voucher> appliedVouchers = new ArrayList<>();
        List<String> voucherCodesToProcess = new ArrayList<>();
        if (request.getVoucherCodes() != null && !request.getVoucherCodes().isEmpty()) {
            voucherCodesToProcess.addAll(request.getVoucherCodes());
        }
        if (voucherCodesToProcess.isEmpty()) {
            List<Voucher> cartVouchers = cart.getVouchers();
            if (cartVouchers != null && !cartVouchers.isEmpty()) {
                voucherCodesToProcess = cartVouchers.stream()
                    .map(Voucher::getCode)
                    .collect(Collectors.toList());
            }
        }
        
        if (!voucherCodesToProcess.isEmpty()) {
            appliedVouchers = voucherService.processVouchersForOrder(
                voucherCodesToProcess,
                totalAmount.doubleValue()
            );
        }

        BigDecimal discountToAllocate = cart.getDiscount() == null
            ? BigDecimal.ZERO
            : cart.getDiscount().min(totalAmount);
        BigDecimal allocatedDiscount = BigDecimal.ZERO;
        List<OrderResponse> responses = new ArrayList<>();
        int groupIndex = 0;
        for (Map.Entry<Long, List<OrderItem>> entry : itemsBySeller.entrySet()) {
            groupIndex++;
            BigDecimal sellerTotal = totalsBySeller.get(entry.getKey());
            BigDecimal sellerDiscount;
            if (groupIndex == itemsBySeller.size()) {
                sellerDiscount = discountToAllocate.subtract(allocatedDiscount);
            } else {
                sellerDiscount = totalAmount.signum() == 0
                    ? BigDecimal.ZERO
                    : discountToAllocate.multiply(sellerTotal)
                        .divide(totalAmount, 2, RoundingMode.DOWN);
                allocatedDiscount = allocatedDiscount.add(sellerDiscount);
            }

            Order order = new Order();
            order.setSeller(sellers.get(entry.getKey()));
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentType(paymentType);
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setTotalAmount(sellerTotal);
            order.setDiscountAmount(sellerDiscount);
            order.setFinalAmount(sellerTotal.subtract(sellerDiscount).max(BigDecimal.ZERO));
            order.setVouchers(appliedVouchers);
            order.setOrderItems(entry.getValue());
            order.setFullName(request.getFullName());
            order.setPhoneNumber(request.getPhoneNumber());
            order.setAddress(request.getAddress());
            order.setNote(request.getNote() == null ? "Không có" : request.getNote());

            for (OrderItem orderItem : entry.getValue()) {
                orderItem.setOrder(order);
            }

            Order savedOrder = orderRepo.save(order);
            recordTracking(savedOrder, OrderStatus.PENDING, null, "Đơn hàng được tạo.");
            responses.add(new OrderResponse(savedOrder));
        }

        cartItemRepo.deleteAll(cartItems);

        return responses;
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
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng."));
        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse confirmOrderReceived(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để xác nhận nhận hàng.");
        }

        User user = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy người dùng."));
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng."));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xác nhận đơn hàng này.");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Chỉ có thể xác nhận đơn hàng đã được giao thành công.");
        }

        return updateOrderStatus(orderId, OrderStatus.COMPLETED, null, "Khách hàng xác nhận đã nhận hàng.");
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

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        return updateOrderStatus(orderId, status, null, null);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status, String location, String note) {
        return updateOrderStatus(orderId, status, location, note, false);
    }

    @Transactional
    public OrderResponse updateDeliveryOrderStatus(
            Long orderId, OrderStatus status, String location, String note, boolean paymentCollected) {
        return updateOrderStatus(orderId, status, location, note, true, paymentCollected);
    }

    private OrderResponse updateOrderStatus(
            Long orderId, OrderStatus status, String location, String note, boolean deliveryUnitUpdate) {
        return updateOrderStatus(orderId, status, location, note, deliveryUnitUpdate, false);
    }

    private OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus status,
            String location,
            String note,
            boolean deliveryUnitUpdate,
            boolean paymentCollected) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái đơn hàng không hợp lệ.");
        }
        if (note != null && note.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ghi chú không được vượt quá 1000 ký tự.");
        }
        if (location != null && location.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vị trí không được vượt quá 255 ký tự.");
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng."));
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == status) {
            if (status == OrderStatus.DELIVERED
                    && order.getEffectivePaymentType() == Order.PaymentType.CASH
                    && !paymentCollected
                    && order.getEffectivePaymentStatus() != PaymentStatus.PAID) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cần xác nhận đã thu tiền COD trước khi hoàn tất giao hàng.");
            }
            if (status == OrderStatus.DELIVERED
                    && order.getEffectivePaymentType() == Order.PaymentType.CASH
                    && paymentCollected
                    && order.getEffectivePaymentStatus() != PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.PAID);
                Order savedOrder = orderRepo.save(order);
                recordTracking(savedOrder, status, location, "Đã thu tiền COD từ khách hàng.");
                return new OrderResponse(savedOrder);
            }
            return new OrderResponse(order);
        }

        if (deliveryUnitUpdate
                && (currentStatus == null || !currentStatus.canBeChangedByDeliveryUnitTo(status))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn vị vận chuyển không thể chuyển trạng thái từ " + currentStatus + " sang " + status + ".");
        }

        boolean cashOrderProcessing = currentStatus == OrderStatus.CONFIRMED
                && status == OrderStatus.PROCESSING
                && order.getEffectivePaymentType() == Order.PaymentType.CASH;
        boolean refundingCancelledPaidOrder = currentStatus == OrderStatus.CANCELLED
                && status == OrderStatus.REFUNDED
                && order.getEffectivePaymentStatus() == PaymentStatus.PAID;
        if (currentStatus == null
                || (!currentStatus.canTransitionTo(status)
                && !cashOrderProcessing
                && !refundingCancelledPaidOrder)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không thể chuyển trạng thái đơn hàng từ " + currentStatus + " sang " + status + ".");
        }
        if (status == OrderStatus.DELIVERED
                && order.getEffectivePaymentType() == Order.PaymentType.CASH
                && !paymentCollected) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cần xác nhận đã thu tiền COD trước khi hoàn tất giao hàng.");
        }
        if ((status == OrderStatus.FAILED_DELIVERY || status == OrderStatus.RETURNED)
                && (note == null || note.isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng nhập lý do khi giao thất bại hoặc hoàn hàng.");
        }
        if (status == OrderStatus.REFUNDED
                && currentStatus != OrderStatus.RETURNED
                && !refundingCancelledPaidOrder) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Chỉ có thể hoàn tiền cho đơn đã trả hàng hoặc đã thanh toán bị hủy.");
        }
        
        order.setStatus(status);
        updatePaymentStatus(order, status);
        if (status == OrderStatus.DELIVERED && order.getEffectivePaymentType() == Order.PaymentType.CASH) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        Order savedOrder = orderRepo.save(order);
        recordTracking(savedOrder, status, location, note);
        
        // Tự động tính toán lợi nhuận khi đơn hàng đạt trạng thái DELIVERED
        if (status == OrderStatus.DELIVERED && order.getCreatedAt() != null) {
            BigDecimal orderRevenue = order.getTotalAmount();
            BigDecimal orderProfit = calculateOrderProfit(order);
            String period = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            revenueService.updateRevenueFromOrder(orderRevenue, orderProfit, period);
        }
        
        return new OrderResponse(savedOrder);
    }

    private void recordTracking(Order order, OrderStatus status, String location, String note) {
        DeliveryTracking tracking = new DeliveryTracking();
        tracking.setOrder(order);
        tracking.setStatus(status);
        tracking.setTimestamp(LocalDateTime.now());
        tracking.setLocation(location == null || location.isBlank() ? null : location.trim());
        tracking.setNote(note == null || note.isBlank() ? defaultTrackingNote(status) : note.trim());
        trackingRepo.save(tracking);
    }

    private String defaultTrackingNote(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Đơn hàng được tạo.";
            case CONFIRMED -> "Đơn hàng được xác nhận.";
            case PAID -> "Thanh toán thành công.";
            case PAYMENT_FAILED -> "Thanh toán thất bại.";
            case PROCESSING -> "Đơn hàng đang được xử lý.";
            case INVENTORY -> "Đơn hàng đang được kiểm kho.";
            case READY_TO_SHIP -> "Đơn hàng sẵn sàng bàn giao.";
            case SHIPPED -> "Đơn hàng đã bàn giao cho đơn vị vận chuyển.";
            case DELIVERED -> "Đơn hàng đã giao thành công.";
            case FAILED_DELIVERY, DELIVERED_FAILED -> "Giao hàng không thành công.";
            case RETURNED -> "Đơn hàng đã được hoàn trả.";
            case REFUNDED -> "Đơn hàng đã được hoàn tiền.";
            case CANCELLED -> "Đơn hàng đã bị hủy.";
            case SHIPPING -> "Đơn hàng đang được vận chuyển (trạng thái cũ).";
            case COMPLETED -> "Khách hàng xác nhận đã nhận hàng.";
        };
    }

    private void updatePaymentStatus(Order order, OrderStatus status) {
        switch (status) {
            case PAID -> order.setPaymentStatus(PaymentStatus.PAID);
            case PAYMENT_FAILED -> order.setPaymentStatus(PaymentStatus.FAILED);
            case REFUNDED -> order.setPaymentStatus(PaymentStatus.REFUNDED);
            default -> {
            }
        }
    }

    private Order.PaymentType resolvePaymentType(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return Order.PaymentType.CASH;
        }
        return switch (paymentMethod.trim().toLowerCase(Locale.ROOT)) {
            case "cod", "cash" -> Order.PaymentType.CASH;
            case "bnpl", "installment" -> Order.PaymentType.BNPL;
            case "bank", "card", "vnpay", "momo", "paypal", "wallet" -> Order.PaymentType.CARD;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Phương thức thanh toán không được hỗ trợ.");
        };
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
        return orderRepo.getTotalPurchase(List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED), userId);
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

    @Transactional
    public boolean updateOrderStatus(Long orderId, String status) {
        if (orderId == null || !orderRepo.existsById(orderId)) {
            return false;
        }

        OrderStatus requestedStatus;
        try {
            requestedStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        try {
            updateOrderStatus(orderId, requestedStatus);
            return true;
        } catch (ResponseStatusException exception) {
            return false;
        }
    }
}