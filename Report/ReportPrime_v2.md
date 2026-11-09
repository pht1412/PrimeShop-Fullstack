# PrimeShop — Báo cáo bàn giao phiên làm việc

> **Cập nhật lần cuối:** 09/11/2026, 16:04 (giờ Việt Nam, UTC+07:00)
> **Repository:** `pht1412/PrimeShop-Fullstack`
> **Thư mục local:** `C:\Users\lhp14\Prime`
> **Mục đích:** Cung cấp ngữ cảnh đầy đủ để AI/thành viên khác nhận diện hệ thống và tiếp tục làm việc mà không phải suy đoán lại các quyết định trong phiên hôm nay.
> **Nguồn sự thật:** Mã nguồn hiện tại và kết quả kiểm thử được ghi trong báo cáo. `ReportPrime_v1.md` là lịch sử/bối cảnh trước đó; những thông tin cũ mâu thuẫn với mã nguồn hoặc báo cáo này không được coi là trạng thái mới nhất. Không có bí mật, token hay thông tin đăng nhập được ghi trong tài liệu.

## 1. Tóm tắt nhanh

PrimeShop là monorepo thương mại điện tử gồm frontend React/Vite và backend Spring Boot. Sản phẩm được chia thành hai luồng:

- **B2C:** hàng của shop chính thức `PrimeShop Official`; admin quản lý sản phẩm.
- **C2C:** hàng do seller/người dùng đăng; seller cần được duyệt và sản phẩm cần được admin duyệt trước khi bán.

Trong phiên ngày 09/11/2026, người dùng yêu cầu sửa lỗi tạo đơn, tách khu vực C2C khỏi catalog B2C, sửa chi tiết đơn hàng admin và chuẩn hóa quy trình đơn hàng/vận chuyển. Các thay đổi đã được thực hiện trên cả frontend và backend, gồm:

1. Tạo đơn theo seller; một giỏ hàng có sản phẩm từ nhiều shop được tách thành nhiều đơn.
2. Ngăn người bán tự đặt mua sản phẩm C2C của mình ở backend; giao diện cũng ẩn nút mua của chủ sản phẩm.
3. Tạo trang `/c2c` và API danh sách/chi tiết sản phẩm C2C; trang sản phẩm B2C không đưa sản phẩm C2C vào catalog.
4. Đồng bộ DTO đơn hàng với admin: API trả `orderItems` và frontend hiện đọc `orderItems`.
5. Tập trung kiểm tra chuyển trạng thái tại `OrderService`; bổ sung trạng thái thanh toán riêng `PaymentStatus`, xác nhận COD, tracking và các bước giao thất bại/hoàn tiền.
6. Gỡ cấu hình Surefire vốn bỏ qua lỗi test để lần chạy sau trả kết quả thất bại đúng khi test lỗi.

Người dùng xác nhận đã tự kiểm thử và đặt hàng thành công sau các thay đổi xử lý lỗi tạo đơn. Mã nguồn hiện có thay đổi **chưa commit**. Chưa có bằng chứng kiểm thử end-to-end trên database đang chạy sau khi thêm `PaymentStatus`.

## 2. Cấu trúc và công nghệ

```text
Prime/
├── Backend-PrimeShop/       # Spring Boot REST API
├── Frontend-PrimeShop/      # React / TypeScript / Vite
├── Report/
│   ├── ReportPrime_v1.md    # Báo cáo trước phiên này; một số thông tin đã cũ
│   └── ReportPrime_v2.md    # Báo cáo bàn giao hiện tại
└── README.md
```

### Backend

- Spring Boot **3.4.4**, Maven Wrapper, Java release **17** (`Backend-PrimeShop/pom.xml`).
- Spring Security/JWT, Spring Data JPA/Hibernate, REST API.
- Các domain hiện có gồm auth/user, product/category, seller, cart/order, delivery/tracking, thanh toán (VNPay, VNPay trả góp, wallet và các phương thức khác trong repository), voucher, news, chat/WebSocket, minigame và các màn quản trị.
- Báo cáo v1 ghi nhận cấu hình local đã dùng profile `prod` với PostgreSQL/Supabase. Cấu hình thật lấy từ môi trường/`.env`; **không đọc, sao chép hoặc đưa giá trị bí mật vào báo cáo**.
- Trước phiên này, báo cáo v1 ghi `ddl-auto: update` trong profile prod và Flyway tắt. Sau thay đổi entity thanh toán/tracking, Hibernate cần được chạy để đồng bộ schema; việc đó **chưa được xác nhận trên DB thật trong phiên này**.
- Một số cấu hình nền hoặc tài liệu cũ có thể còn nhắc SQL Server; không dùng nội dung cũ đó để kết luận database đang chạy. Xác minh cấu hình profile đang bật trước khi thay đổi DB.

### Frontend

- React 18, TypeScript/JSX, Vite 6, Axios, React Router, MUI, SweetAlert2, Zustand và TanStack Query.
- Lệnh phổ biến tại `Frontend-PrimeShop/`: `npm run dev`, `npm run lint`, `npm run build`.
- Báo cáo v1 ghi frontend local thường chạy cổng `5174`, backend HTTP cổng `8080`, Vite proxy `/api` và `/ws`. Đây là thông tin môi trường từng quan sát được, không phải xác nhận các process đang chạy tại thời điểm đọc báo cáo v2.
- Router gốc: `src/App.jsx`; route khách hàng ở `src/routes/UserRoutes.jsx`, admin ở `src/routes/AdminRoutes.jsx`, giao vận ở `src/routes/DeliveryRoutes.jsx`.

### Quy tắc xác minh môi trường

- Đừng giả định backend đang dùng đúng source mới nhất chỉ vì compile thành công. Nếu cần kiểm thử UI/API, xác nhận backend đã được restart và process đúng đang nghe cổng dự kiến.
- Không in `.env`, mật khẩu, JWT, API/payment keys, dữ liệu cá nhân hoặc thông tin người mua/người bán.
- Không thao tác ghi/xóa dữ liệu Supabase để thử nghiệm khi chưa có xác nhận của người dùng.

## 3. Mô hình và nghiệp vụ sản phẩm

### Sản phẩm và seller

- `Product` có quan hệ seller bắt buộc (`seller_id` không null) và category; sản phẩm public phải được duyệt và active.
- Shop chính thức được nhận diện bằng `SellerProfile.OFFICIAL_SHOP_NAME` (`PrimeShop Official`).
- Sản phẩm admin tạo cho B2C được gắn với hồ sơ shop chính thức và tạo ở trạng thái `APPROVED`.
- Sản phẩm C2C do seller tạo ban đầu là `PENDING`; admin duyệt qua trang quản lý C2C.
- Hồ sơ seller có trạng thái như `PENDING_REVIEW`, `VERIFIED_SELLER`, `BANNED_SELLER`. Role seller trong code có spelling lịch sử `ROLE_BUSSINESS`; không tự ý đổi tên nếu chưa rà soát tương thích.
- Báo cáo v1 ghi nhận hồ sơ `PrimeShop Official` hiện có thể liên kết với admin tạo hồ sơ vì entity seller yêu cầu user. Về lâu dài có thể cần tài khoản service/shop hoặc mô hình seller hệ thống riêng, nhưng đó chưa phải thay đổi của phiên này.

### Luồng duyệt seller và sản phẩm C2C

1. User đăng nhập, vào `/account` và đăng ký Business.
2. `POST /api/seller/apply` tạo hồ sơ seller chờ duyệt.
3. Admin duyệt hồ sơ seller tại `/admin/business-manager`.
4. Seller đăng sản phẩm C2C qua `/api/seller/add-product?sellerId=...`; sản phẩm chờ duyệt.
5. Admin duyệt sản phẩm tại `/admin/c2c-management`.

Các màn seller và sản phẩm C2C là hai việc khác nhau: `business-manager` duyệt shop; `c2c-management` duyệt listing.

### Tách catalog B2C/C2C

- Trang B2C: `/all-products`; backend product thông thường lọc sản phẩm của shop chính thức.
- Trang cộng đồng C2C: `/c2c`; trang chi tiết C2C: `/c2c-product-detail/:id`.
- API C2C: `GET /api/product/c2c` và `GET /api/product/c2c/{productId}`. Danh sách/chi tiết lọc sản phẩm đã duyệt thuộc seller không phải shop chính thức; DTO C2C đánh dấu sản phẩm của user hiện tại.
- Giao diện chi tiết sản phẩm C2C không hiển thị CTA mua cho người đăng sản phẩm.
- Backend `OrderService.createOrder` vẫn là lớp bảo vệ cuối: từ chối đặt mua sản phẩm C2C nếu seller của sản phẩm là user đang checkout. Không dựa riêng vào ẩn nút ở frontend.
- Các sản phẩm trong giỏ được kiểm tra `active`, `APPROVED` và tồn kho ở backend lúc tạo đơn.

## 4. Giỏ hàng, checkout và tạo đơn

### Luồng tạo đơn hiện tại

1. Checkout gửi thông tin người nhận, giỏ hàng, mã voucher và phương thức thanh toán.
2. Backend kiểm tra thông tin nhận hàng, trạng thái/active sản phẩm, tồn kho và quyền mua C2C.
3. Các `CartItem` được gom theo seller. Mỗi seller nhận một `Order` riêng; `Order.seller` được gán rõ ràng, tránh lỗi lưu đơn do quan hệ seller bắt buộc bị bỏ trống.
4. Nếu có nhiều đơn con, tổng tiền và phần giảm giá được phân bổ theo tổng giá trị từng seller; phần chênh làm tròn được gán cho nhóm cuối để tổng giảm giá không bị thất thoát.
5. Backend lưu snapshot sản phẩm vào `OrderItem` (tên, ảnh, giá, số lượng) để hiển thị chi tiết đơn; sau khi tạo đơn, các item được xóa khỏi giỏ.
6. Đơn bắt đầu ở `PENDING`; lưu `paymentType`, `paymentStatus=PENDING` và tracking ban đầu.

Người dùng đã xác nhận đặt hàng hoạt động sau lần sửa HTTP 500. Lỗi gốc và biện pháp đã xử lý thuộc luồng tạo đơn: order cần được gắn seller phù hợp, và giỏ nhiều shop cần được chia đơn theo seller.

### Phương thức thanh toán

- `Order.PaymentType`: `CASH`, `CARD`, `BNPL`.
- `PaymentStatus` mới: `PENDING`, `PAID`, `FAILED`, `REFUNDED`.
- Checkout gửi phương thức thanh toán; backend map các lựa chọn COD/CASH thành `CASH`, trả góp/installment thành `BNPL`, và các lựa chọn thanh toán điện tử như bank/card/VNPay/MoMo/PayPal/wallet thành `CARD`.
- Nếu client cũ không truyền payment method, backend mặc định COD (`CASH`) để tương thích.
- Dữ liệu cũ thiếu payment type/status có accessor tương thích; trạng thái lịch sử `DELIVERED`/`COMPLETED` được suy diễn đã thanh toán, `PAYMENT_FAILED` suy diễn thất bại, `REFUNDED` suy diễn đã hoàn.
- Callback VNPay, VNPay trả góp và wallet được chuyển sang cập nhật qua `OrderService`. Không khẳng định các cổng thanh toán đã được kiểm thử trực tiếp trong phiên này.

## 5. Đơn hàng và quy trình trạng thái

### Hai khái niệm độc lập

- `OrderStatus` là tiến trình xử lý/giao đơn.
- `PaymentStatus` là tiến trình tiền đã thu/thất bại/hoàn tiền.

Không suy luận đã thanh toán chỉ từ trạng thái giao hàng cho các đơn mới; COD chỉ chuyển sang `PAID` khi xác nhận đã thu tiền. Với dữ liệu cũ, backend có logic tương thích riêng.

### Luồng trạng thái được triển khai

Các chuyển trạng thái hợp lệ được định nghĩa tập trung trong `OrderStatus` và được `OrderService` kiểm tra:

- Đơn mới: `PENDING → CONFIRMED`.
- Thanh toán điện tử: `PENDING`/`CONFIRMED → PAID → PROCESSING → INVENTORY → READY_TO_SHIP`.
- COD: `PENDING → CONFIRMED → PROCESSING → INVENTORY → READY_TO_SHIP`. Backend cho phép nhánh `CONFIRMED → PROCESSING` cho đơn COD.
- Giao vận: `READY_TO_SHIP → SHIPPED → DELIVERED`.
- COD: khi đơn vị giao vận cập nhật `DELIVERED`, request phải xác nhận `paymentCollected=true`; backend đặt `PaymentStatus=PAID`. Không xác nhận thu COD thì không được hoàn tất cập nhật giao thành công.
- Sau `DELIVERED`, khách hàng là chủ đơn xác nhận nhận hàng qua `PUT /api/order/{orderId}/confirm-received`, chuyển `DELIVERED → COMPLETED`.
- Giao thất bại: `SHIPPED → FAILED_DELIVERY → READY_TO_SHIP` để giao lại hoặc `FAILED_DELIVERY → RETURNED → REFUNDED`. Giao thất bại/hoàn hàng cần lý do.
- Hủy đơn đã thanh toán có nhánh hoàn tiền; hoàn tiền cho đơn đã trả hàng đi qua `RETURNED → REFUNDED`.

Đây là workflow mới trong source; dữ liệu trạng thái cũ vẫn cần được giữ tương thích:

- `SHIPPING` được giữ để đọc record cũ; luồng mới dùng `SHIPPED`.
- `DELIVERED_FAILED` được giữ để đọc record cũ; luồng mới dùng `FAILED_DELIVERY`.
- Không xóa hay đổi tên enum lịch sử nếu chưa có kế hoạch migration dữ liệu.

### API và vai trò từng lớp

- `OrderService` là nơi kiểm tra chuyển trạng thái, cập nhật payment status, xác nhận chủ đơn, kiểm tra COD và ghi tracking.
- API đơn hàng: `POST /api/order/create`, `GET /api/order/get`, `GET /api/order/all-orders`, `PUT /api/order/{orderId}/confirm-received`; admin dùng API cập nhật trạng thái tổng quát theo cấu hình bảo mật backend.
- API delivery riêng: `GET /api/delivery/orders`, `PUT /api/delivery/orders/{orderId}/status`, `GET /api/delivery/orders/{orderId}/tracking`, `GET /api/delivery/stats`, `GET /api/delivery/orders/{orderId}`.
- Delivery Service delegate cập nhật trạng thái về `OrderService`, không tự đặt status trực tiếp. Endpoint bàn giao trùng lặp `/delivery/send` của luồng cũ đã được loại bỏ.
- `OrderResponse` trả `allowedNextStatuses` và `allowedDeliveryStatuses`; UI giao vận chỉ dựa vào các bước dành cho delivery, không dùng quyền chuyển trạng thái chung của admin.
- Tracking lưu trạng thái, thời điểm, vị trí và ghi chú/lý do; lịch sử được đọc theo thứ tự thời gian.

### Vì sao không bỏ chức năng vận chuyển?

Chức năng giao vận có vai trò nghiệp vụ riêng: nhận đơn sẵn sàng giao, cập nhật giao thành công/thất bại, ghi vị trí/lý do và xác nhận thu COD. Vấn đề được phát hiện là các API/trạng thái/UI cũ chồng lấn, không phải việc bản thân chức năng vận chuyển không cần thiết. Thiết kế hiện tại giữ màn delivery riêng nhưng dùng chung nguồn quy tắc trạng thái ở backend.

## 6. Sửa chi tiết sản phẩm trong đơn admin

- API `OrderResponse` trả danh sách ở trường `orderItems`.
- Admin trước đó đọc `order.items`, gây hiển thị “Không có dữ liệu” dù đơn có sản phẩm.
- `OrderManager.tsx` và `types/order.ts` hiện đã đồng bộ với `orderItems`.
- API test đơn hàng kiểm tra response có thông tin sản phẩm; đây là regression test cho hợp đồng dữ liệu.

## 7. Các màn hình và API cần biết

### Frontend routes trọng điểm

- Khách hàng: `/`, `/all-products`, `/product-detail/:slug`, `/cart`, `/checkout`, `/account`, `/order-tracking`, `/c2c`, `/c2c-product-detail/:id`, `/payment-result`, `/payment/success`.
- Admin: `/admin/orders`, `/admin/products`, `/admin/business-manager`, `/admin/c2c-management` và các màn user/category/news/voucher/wallet/revenue.
- Giao vận: `/delivery-dashboard/` (các route con `dashboard`, `orders`, `history` dùng DeliveryDashboard).

### API trọng điểm

- Auth: `/api/auth/register`, `/api/auth/login`, `/api/auth/check-role`.
- Seller: `/api/seller/apply`, `/api/seller/me`, `/api/seller/add-product`.
- Product: `/api/product`, `/api/product/c2c`, `/api/product/c2c/{productId}`.
- Cart: `/api/cart/add`, `/api/cart/remove`, `/api/cart`, `/api/cart/count`.
- Order: `/api/order/create`, `/api/order/get`, `/api/order/all-orders`, `/api/order/{orderId}/confirm-received`.
- Delivery: `/api/delivery/orders`, `/api/delivery/orders/{orderId}/status`, `/api/delivery/orders/{orderId}/tracking`.
- Admin B2C tạo sản phẩm: `/api/admin/product/add`; shop chính thức được backend chọn/tạo, không tin `sellerId` do client tự gửi.
- Các endpoint admin phải tiếp tục được bảo vệ ở backend; frontend route guard không thay thế backend authorization.

## 8. Tệp nguồn trọng điểm

### Order/payment/delivery

- `Backend-PrimeShop/src/main/java/com/primeshop/order/Order.java` — entity order, payment type/status và giá trị tương thích dữ liệu cũ.
- `Backend-PrimeShop/src/main/java/com/primeshop/order/OrderStatus.java` — enum trạng thái, quy tắc chuyển và giới hạn chuyển trạng thái của delivery.
- `Backend-PrimeShop/src/main/java/com/primeshop/order/PaymentStatus.java` — trạng thái thanh toán mới.
- `Backend-PrimeShop/src/main/java/com/primeshop/order/OrderService.java` — tạo đơn theo seller, validation, COD, payment status, tracking, xác nhận nhận hàng.
- `Backend-PrimeShop/src/main/java/com/primeshop/order/OrderResponse.java` — DTO đơn, payment type/status, items và bước tiếp theo UI được phép.
- `Backend-PrimeShop/src/main/java/com/primeshop/order/OrderController.java` — API order.
- `Backend-PrimeShop/src/main/java/com/primeshop/delivery/DeliveryService.java` — danh sách đơn delivery và delegate đổi trạng thái.
- `Backend-PrimeShop/src/main/java/com/primeshop/delivery/DeliveryController.java` — API delivery.
- `Backend-PrimeShop/src/main/java/com/primeshop/delivery/DeliveryTracking.java` — tracking và ghi chú.
- `Backend-PrimeShop/src/main/java/com/primeshop/config/SecurityConfig.java` — quyền truy cập API.

### Product/C2C

- `Backend-PrimeShop/src/main/java/com/primeshop/product/ProductService.java` — lọc B2C/C2C, tạo và kiểm tra sản phẩm.
- `Backend-PrimeShop/src/main/java/com/primeshop/product/ProductController.java` — API product/C2C.
- `Backend-PrimeShop/src/main/java/com/primeshop/product/C2CProductResponse.java` — DTO có thông tin chủ listing.
- `Backend-PrimeShop/src/main/java/com/primeshop/cart/CartService.java` — quy tắc giỏ hàng theo product/seller.
- `Backend-PrimeShop/src/main/java/com/primeshop/seller/SellerProfile.java` — seller profile và tên shop chính thức.
- `Frontend-PrimeShop/src/pages/productC2C/C2CProductPage.tsx` — danh sách C2C.
- `Frontend-PrimeShop/src/pages/productC2C/C2CProductDetailPage.tsx` — chi tiết C2C và CTA theo chủ sở hữu.
- `Frontend-PrimeShop/src/api/c2c.api.ts` — client API C2C.

### Checkout/admin/delivery

- `Frontend-PrimeShop/src/pages/checkout/CheckoutPage.tsx` — tạo đơn và lựa chọn phương thức thanh toán.
- `Frontend-PrimeShop/src/pages/admin/OrderManager.tsx` — danh sách, chi tiết và thao tác order admin.
- `Frontend-PrimeShop/src/types/order.ts` — kiểu dữ liệu order frontend.
- `Frontend-PrimeShop/src/pages/delivery/DeliveryDashboard.tsx` — dashboard delivery.
- `Frontend-PrimeShop/src/pages/delivery/UpdateStatusModal.tsx` — nhập trạng thái/ghi chú/thu COD.
- `Frontend-PrimeShop/src/pages/delivery/TrackingHistoryModal.tsx` — lịch sử tracking.
- `Frontend-PrimeShop/src/api/delivery.api.ts` — client API delivery.
- `Frontend-PrimeShop/src/pages/AccountPage.tsx` — trạng thái thanh toán và xác nhận nhận hàng.

### Kiểm thử/tài liệu

- `Backend-PrimeShop/src/test/java/com/primeshop/service/OrderServiceTest.java` — test logic order.
- `Backend-PrimeShop/src/test/java/com/primeshop/api/OrderApiTest.java` — test API tạo đơn và payload items.
- `Backend-PrimeShop/pom.xml` — Maven/Surefire; cấu hình bỏ qua lỗi test đã được gỡ.
- `Backend-PrimeShop/README.txt` — tài liệu quy trình order/delivery đã cập nhật, nhưng phần đầu README còn nội dung cũ/encoding lỗi; ưu tiên báo cáo và mã nguồn hiện tại.

## 9. Kiểm chứng trong phiên và giới hạn

### Đã xác nhận

- Người dùng tự kiểm thử và xác nhận tạo đơn hoạt động sau khi xử lý lỗi.
- `npm run lint` tại frontend: **thành công**.
- `npm run build` tại frontend: **thành công**.
- Backend targeted test: `OrderServiceTest` **6/6 pass** và một test API `OrderApiTest#TC04_CreateOrder_Success` **1/1 pass**; tổng **7 test**, Maven trả `BUILD SUCCESS` (exit code 0).
- `git diff --check`: **thành công**.
- Test fixture xác nhận nhận hàng được sửa để dùng authentication có authorities, phù hợp với kiểm tra authenticated user.
- Đã gỡ `<testFailureIgnore>true</testFailureIgnore>` khỏi Maven Surefire; các lần test tiếp theo không còn được phép báo thành công nếu test thất bại.

### Cảnh báo/giới hạn chưa xử lý

- Build frontend báo CSS có cú pháp cũ `height: 3px;s` và bundle JS minified lớn hơn ngưỡng 500 kB. Build vẫn thành công; các cảnh báo này chưa được xử lý trong phạm vi phiên.
- Không chạy toàn bộ backend test suite. Các báo cáo Surefire cũ có thể chứa lỗi từ lần chạy suite trước; không suy luận suite tổng thể xanh từ 7 test targeted.
- Chưa xác nhận backend mới được restart cùng schema database thật sau khi thêm `payment_status`/tracking note.
- Chưa chạy end-to-end qua trình duyệt cho COD, thanh toán điện tử, giao thất bại/giao lại, hoàn tiền, hoặc kiểm tra quyền với account thật.
- Chưa kiểm thử webhook/payment gateway thật; chưa khẳng định cơ chế callback an toàn/idempotent.
- Chưa xác minh cách khôi phục tồn kho khi hủy/hoàn đơn hoặc việc mọi cổng thanh toán ngoài những callback đã rà soát đều đi qua `OrderService`.
- Không thực hiện thao tác ghi dữ liệu production/Supabase trong lần xác minh này.

## 10. Trạng thái Git và cách tiếp tục

- Thay đổi hôm nay vẫn nằm trong worktree và **chưa commit**.
- Có nhiều file đã sửa/thêm trong backend và frontend (bao gồm C2C, checkout/order, payment/delivery, admin và README); không reset, checkout, revert hoặc ghi đè các thay đổi này.
- Trước khi bắt đầu tác vụ mới, chạy `git status --short`, xem diff của đúng file cần làm và giữ nguyên phần không thuộc yêu cầu.
- Báo cáo này là tài liệu bổ sung do người dùng yêu cầu; không tạo commit nếu chưa được yêu cầu.

### Ưu tiên hợp lý cho phiên tiếp theo

1. Đọc báo cáo này, sau đó đối chiếu mã nguồn và `git status`; nếu có mâu thuẫn, mã nguồn mới nhất là chuẩn.
2. Nếu cần xác nhận DB, restart backend có chủ đích bằng profile đã cấu hình và kiểm tra Hibernate/schema mà không tiết lộ secrets.
3. Kiểm thử COD end-to-end: checkout → xác nhận → xử lý kho → bàn giao → xác nhận đã thu COD và giao thành công → khách xác nhận nhận hàng.
4. Kiểm thử giao thất bại có lý do → giao lại hoặc hoàn hàng → hoàn tiền; xác nhận payment status và tracking ở mỗi bước.
5. Chạy full backend test suite sau khi đã ghi nhận baseline; giờ Surefire không còn bỏ qua test failure nên exit code và số pass/fail phải được xem là kết quả thực.
6. Cân nhắc kiểm thử các quy tắc C2C bằng test: listing C2C không xuất hiện trong B2C, chủ sản phẩm không có CTA mua, API từ chối tự mua, khách khác vẫn có thể mua.
7. Chỉ xử lý các cảnh báo CSS/bundle hoặc mở rộng test nếu người dùng yêu cầu hoặc chúng cản trở công việc tiếp theo.

## 11. Quy tắc bảo vệ dữ liệu và vận hành

- Không đưa `.env`, DB URL/password, JWT, secret key, thông tin cá nhân seller/buyer hoặc credential thanh toán vào báo cáo, log gửi ra ngoài, câu trả lời hay commit.
- Không dùng dữ liệu production để thử nghiệm nếu chưa có xác nhận rõ ràng.
- Không xóa trạng thái enum cũ hoặc sửa dữ liệu trạng thái hàng loạt nếu chưa có migration/backfill được duyệt.
- Không kết luận một API/UI hoạt động chỉ từ compile; phân biệt rõ compile/test với runtime và kiểm tra trên DB thật.
- Không commit thay đổi nếu người dùng chưa yêu cầu.
