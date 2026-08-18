# Hệ thống tính phí vận chuyển - PrimeShop

## Tổng quan
Hệ thống tính phí vận chuyển cho PrimeShop với các tính năng:
- Tính phí vận chuyển theo khu vực (TPHCM nội thành/ngoại thành, các tỉnh khác)
- Phụ thu khi có nhiều sản phẩm (>5 sản phẩm)
- Miễn phí ship khi đơn hàng >= 500.000đ
- Hỗ trợ mã giảm giá FREESHIP

## Cấu trúc thư mục
```
shipping/
├── ShippingZone.java              # Enum phân loại khu vực giao hàng
├── ShippingAddress.java           # DTO thông tin địa chỉ
├── CartItemInfo.java              # DTO thông tin sản phẩm trong giỏ hàng
├── ShippingPreviewRequest.java    # DTO request cho API preview
├── ShippingPreviewResponse.java   # DTO response cho API preview
├── ShippingService.java           # Logic tính phí vận chuyển
├── ShippingController.java        # REST API controller
├── ShippingUtils.java             # Utility methods
└── README.md                      # File hướng dẫn này
```

## API Endpoints

### POST /api/checkout/preview-shipping
Tính toán và trả về phí vận chuyển dựa trên địa chỉ và giỏ hàng.

**Request Body:**
```json
{
  "address": {
    "province": "TPHCM",
    "district": "Quận 1",
    "ward": "Phường Bến Nghé"
  },
  "cartItems": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 2,
      "price": 25000000,
      "totalPrice": 50000000
    }
  ],
  "subtotal": 50000000,
  "voucherCode": "FREESHIP123"
}
```

**Response:**
```json
{
  "shippingFee": 20000,
  "shippingDiscount": 20000,
  "finalShippingFee": 0,
  "shippingZone": "HCMC_INNER",
  "zoneDescription": "TPHCM - Nội thành",
  "freeShipping": true,
  "freeShippingReason": "Mã giảm giá FREESHIP"
}
```

## Logic tính phí

### 1. Phí vận chuyển cơ bản
- **TPHCM - Nội thành**: 20.000đ
- **TPHCM - Ngoại thành**: 35.000đ  
- **Các tỉnh/thành khác**: 50.000đ

### 2. Phụ thu số lượng sản phẩm
- Nếu tổng số sản phẩm > 5: +10.000đ

### 3. Điều kiện miễn phí ship
- Đơn hàng >= 500.000đ
- Có mã giảm giá FREESHIP hợp lệ

### 4. Thứ tự ưu tiên
1. Kiểm tra subtotal >= 500.000đ
2. Kiểm tra mã FREESHIP (nếu chưa miễn phí)

## Cách sử dụng

### 1. Gọi API từ Frontend
```javascript
const response = await fetch('/api/checkout/preview-shipping', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    address: {
      province: 'TPHCM',
      district: 'Quận 1',
      ward: 'Phường Bến Nghé'
    },
    cartItems: cartItems,
    subtotal: 500000,
    voucherCode: 'FREESHIP123'
  })
});

const shippingInfo = await response.json();
```

### 2. Sử dụng trong Service
```java
@Autowired
private ShippingService shippingService;

public void calculateShipping() {
    ShippingAddress address = new ShippingAddress();
    address.setProvince("TPHCM");
    address.setDistrict("Quận 1");
    
    ShippingPreviewResponse result = shippingService.calculateShippingFee(
        address, cartItems, subtotal, voucherCode
    );
}
```

## Các khu vực giao hàng

### TPHCM - Nội thành
- Quận 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
- Quận Bình Tân, Bình Thạnh, Gò Vấp, Phú Nhuận, Tân Bình, Tân Phú, Thủ Đức

### TPHCM - Ngoại thành
- Các quận/huyện khác của TPHCM

### Các tỉnh/thành khác
- Tất cả các tỉnh/thành phố khác TPHCM

## Lưu ý
- Hệ thống hỗ trợ nhiều cách viết tên TPHCM: "TPHCM", "Thành phố Hồ Chí Minh", "Hồ Chí Minh"
- Mã giảm giá FREESHIP phải có discountType = FIXED và discountValue >= phí ship
- Phí vận chuyển được tính theo VND (đồng) 