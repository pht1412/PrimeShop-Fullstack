---Backend + Database (Spring Boot + SQL Server)---
1. Công nghệ sử dụng

  Java 17+
  
  Spring Boot
  
  Spring Security + JWT
  
  JPA / Hibernate
  
  Microsoft SQL Server
  
  Maven
2. Yêu cầu môi trường
  
  Cài sẵn:
  
  JDK 17+
  
  Maven 3.8+
  
  SQL Server 2019+
  
  SQL Server Management Studio (SSMS)
3. Database – Restore từ file .bak
3.1 Chuẩn bị

  File backup: primeshop.bak
3.2 Restore database

  1. Mở SQL Server Management Studio
  
  2. Chuột phải Databases → Restore Database…
  
  3. Source: Device
  
  4. Browse → chọn primeshop.bak
  
  5. Database name: PrimeShop

  6. Nhấn OK
4. Cấu hình Spring Boot
4.1 File cấu hình

  Mở:  src/main/resources/application.properties
  Sửa: username và password đăng nhập database bằng account đã có trên máy
5. Chạy Backend
  mvn clean install
  mvn spring-boot:run

6. Quy trình đơn hàng và vận chuyển
  - Đơn mới: PENDING -> CONFIRMED.
  - Đơn thanh toán điện tử: CONFIRMED -> PAID -> PROCESSING.
  - Đơn COD: CONFIRMED -> PROCESSING; chỉ đánh dấu đã thanh toán khi đơn vị vận chuyển xác nhận thu tiền.
  - Xử lý kho: PROCESSING -> INVENTORY -> READY_TO_SHIP.
  - Giao vận: READY_TO_SHIP -> SHIPPED -> DELIVERED -> COMPLETED (khách xác nhận đã nhận).
  - Giao thất bại: SHIPPED -> FAILED_DELIVERY -> READY_TO_SHIP (giao lại) hoặc RETURNED -> REFUNDED.
  - Mọi thay đổi trạng thái phải đi qua OrderService; API giao vận chỉ cho phép các bước của đơn vị vận chuyển.
  - Lịch sử tracking ghi nhận thời điểm, vị trí và ghi chú/lý do cho từng lần chuyển trạng thái.
  - PaymentStatus được lưu riêng với OrderStatus. Các giá trị trạng thái cũ SHIPPING và DELIVERED_FAILED được giữ để đọc dữ liệu đã tồn tại.
  - Endpoint khách xác nhận nhận hàng chỉ cho phép chủ đơn chuyển DELIVERED -> COMPLETED.

---Thẻ test VNPay---
Ngân hàng: NCB
Số thẻ: 9704198526191432198
Tên chủ thẻ: NGUYEN VAN A
Ngày phát hành:	07/15
Mật khẩu OTP: 123456

---Thẻ test MOMO---
No	Tên	            Số thẻ	                Hạn ghi trên thẻ	   OTP	    Trường hợp test
1	NGUYEN VAN A	9704000000000018	    03/07	               OTP	    Thành công
2	NGUYEN VAN A	9704000000000026	    03/07	               OTP	    Thẻ bị khóa
3	NGUYEN VAN A	9704000000000034	    03/07	               OTP	    Nguồn tiền không đủ
4	NGUYEN VAN A	9704000000000042	    03/07	               OTP	    Hạn mức thẻ
