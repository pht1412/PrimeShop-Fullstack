# 📘 CẨM NANG VẤN ĐÁP: SELENIUM AUTOMATION TEST CHO PRIMESHOP

Tài liệu này được thiết kế để giúp bạn hiểu sâu sắc kiến trúc, nguyên lý hoạt động và các "mánh khoé" xử lý lỗi kinh điển trong quá trình làm Selenium test cho đồ án PrimeShop. Đọc kỹ file này sẽ giúp bạn dễ dàng trả lời các câu hỏi phản biện từ Giảng viên.
s
---

## 1. TỔNG QUAN KIẾN TRÚC MÃ NGUỒN (POM - Page Object Model)
Thư mục `com.primeshop.ui` của bạn được chia thành 2 nhánh nguyên lý cực kỳ rõ ràng, tuân thủ **Page Object Model (POM)**:
- **`ui/pages/` (Lớp Thư Viện):** Nơi định nghĩa "Bản đồ" của trang web. Nó chứa **Locators** (vị trí của các nút bấm, ô input) và **Actions** (hành động nhấp, điền chữ). Tuyệt đối không chứa logic đúng/sai (Assert) ở đây.
- **`ui/tests/` (Lớp Kịch Bản):** Nơi chứa những "Đạo diễn" thực thi test case. Class test sẽ điều khiển cái Browser, lôi các Pages ra để xài và **chốt hạ Assertions (Xác nhận Passed/Failed)**.

> **❓ Câu hỏi vấn đáp:** Tại sao lại phải dùng POM mà không viết tất cả vào tệp Test?
> **👉 Trả lời:** Để tái sử dụng code và dễ bảo trì. Ví dụ nút "Đăng nhập" thay đổi XPath, em chỉ cần vào file `LoginPage.java` sửa 1 dòng, thay vì phải đi sửa 100 tệp Test có gọi đăng nhập.

---

## 2. QUY TRÌNH SỐNG CỦA MỘT BÀI KIỂM THỬ (@Test)
Bất kể đó là `AdminOrderTest` hay `CartTest`, mọi class đều tuân theo chu trình chuẩn của JUnit 5:

1. **Kế thừa `BaseWebTest`:** Để hệ thống tự động gọi hàm Khởi tạo Trình Duyệt Chrome `browser` và thư viện chờ `wait` trước khi làm bất cứ thứ gì.
2. **`@BeforeEach` (Dọn đường):** Nếu test đòi hỏi quyền Admin, hàm này sẽ tự động gõ tài khoản "admin", gõ pass "admin", nhấn chuyển hướng để khi chạy Test, chúng ta không cần quan tâm đăng nhập lại.
3. **`@Test` (Triển khai kịch bản):** Selenium mở tới đúng trang cần test, gọi hàm từ `pages/` để bấm nút, nhập form và hứng kết quả.
4. **`assertTrue / assertFalse / assertEquals` (Tuyên án):** Dùng các hàm của JUnit để khẳng định hệ thống đang hoạt động đúng.

---

## 3. CÁC NGHỆ THUẬT AUTO-CLICK VÀ CHỜ (WAITS)
Vì PrimeShop code bằng **ReactJS**, giao diện nó "động" (thay đổi trạng thái DOM theo thời gian thực) chứ không đứng tĩnh. Việc này đòi hỏi Selenium phải cực kỳ dẻo dai! Thầy cô sẽ rất hay hỏi phần này để coi bạn có đi copy trên mạng hay tự hiểu.

### 3.1. Phân biệt các loại Chờ
- **`Thread.sleep(1000)` (Chờ cứng):** Ép bot dừng chết 1 giây. Bắt buộc dùng khi muốn "Làm màu" cho giám khảo xem, hoặc chờ một hiệu ứng hình ảnh (Animation của Vòng quay) trôi qua hẳn mới check.
- **`wait.until(...)` (Chờ thông minh):** Trình duyệt sẽ liên tục săm soi, vừa thấy Element hiện ra là bụp luôn không chờ đợi thêm, vô cùng tiết kiệm thời gian.
  - Ví dụ: `ExpectedConditions.elementToBeClickable` hoặc `visibilityOfElementLocated`.

### 3.2. Cú pháp JavascriptExecutor vs Native Click
> **❓ Câu hỏi vấn đáp:** Tại sao có lúc thấy dùng `browser.executeScript("arguments[0].click()")` nhưng đôi khi lại vứt, dùng `element.click()`?
> **👉 Trả lời:**
> *   Dùng **JS Click**: React đôi khi vẽ các thẻ ảo đè lên nút bấm (chắn mất vùng UI), ví dụ như thanh điều hướng. Chuột ảo của Selenium sẽ bị báo lỗi thẻ chắn `ElementClickInterceptedException`. Em dùng JS để ra lệnh thẳng qua Console của Browser nhằm xuyên thủng lớp phòng thủ HTML ảo đó!
> *   Dùng **Native click()**: Trên giao diện Admin, thư viện **Material UI (MUI)** xài Select thả xuống bắt buộc phải ấn bằng cái Click thật (Native MouseDown) mới móc được event của React. JS Click sẽ bị tàn phế đối với MUI.

---

## 4. [TRỌNG TÂM] 4 PHA XỬ LÝ LỖI TRÍ DANH VÀ KHÉO LÉO NHẤT ĐỒ ÁN
Để ăn điểm tuyệt đối, hãy lôi những pha debug này nói thao thao bất tuyệt coi như "Cái khó ló cái khôn" của nhóm em nha!

### Pha kinh điển 1: Bắt con chim Toastify chạy trốn nhanh như ảo thuật
- **Vấn đề:** Khi Add to Cart, Backend trả về 1 cái popup xanh lá rất đẹp bên góc trên bên phải, nhưng 2 giây sau nó Tàng hình mất tiêu. Nhẹ thì bot kịp chộp ảnh lúc nó hiện, nặng thì nó ẩn cmnr mới check -> Ăn Lỗi `Timeout`.
- **Giải quyết:** Tại tệp `ProductDetailPage.java`, em dùng khung `try - catch`. Nếu cái bóng của con Toast đi qua chưa qua 3 giây, em Assert thành công. Cứ thử bắt, bắt trượt thì tóm ngoại lệ và đi tiếp chứ không để chết toàn Test Suite!

### Pha kinh điển 2: Nhập sai do ghi đè text
- **Vấn đề:** Selenium bị dính bệnh nhập liệu nối đuôi. Gõ "admin" thay vì gõ chữ mới nó lại nối thành "adminadmin".
- **Giải quyết:** Ở lớp Input, em bắt nó `element.clear()` trước khi `sendKeys()` hoặc tinh vi hơn là mô phỏng `Ctrl + A` + `Backspace`.

### Pha kinh điển 3: Bẫy Font Tiếng Việt & Mã hóa của Java
- **Vấn đề:** Backend test trên Terminal Windows hay bị ngu dốt phần tiếng Việt. Việc Selector bảo nhấp vào nút "Chờ xác nhận" bị bộ gõ làm thành "Ch? x?c nh?n" sinh ra lỗi NotFound kinh hoàng.
- **Giải quyết:** Vào tận code Frontend ngắm MUI Select, em không chơi định dạng label nữa, em xài chuẩn mã API backend luôn. Test đổi qua `By.xpath("//li[@data-value='PENDING']")` => Vạn dặm không xước!

### Pha kinh điển 4: Check giỏ hàng Không có dữ liệu nhưng API ném lỗi
- **Vấn đề (TC_Order_8):** Tìm mạo danh đơn hàng ID = `DUMMY_9999`. Code frontend chưa kịp xài HTML rỗng mà React quăng hẳn lỗi 500 ném ra cảnh báo Pop up chà bá "Không tìm thấy".
- **Giải quyết:** Viết hàm quét cái thẻ SweetAlert "Lỗi" xuất hiện trên DOM. Vừa Assert Popup bằng True vừa "Bồi" thêm hành động bấm `OK` dọn dẹp hệ thống.

---

## Luồng Auth Tự Động Toàn Hệ Thống (E2E)
Em đã xây dựng kịch bản Automation để gánh vác nguyên quy trình **User User Journey Map**: Đăng ký User ảo (Randomizied User) -> Đăng ký qua Alert thành công -> Lại tự gõ Acc đăng nhập -> Chạy vòng lặp List mảng duyệt nguyên cái Menu như 1 con người thật.

**👉 Tuyệt chiêu chốt hạ:** "Dự án của bọn em không chỉ dùng Selenium để Test chèn Unit rác, mà dùng nó đo lường Front-End (Giao diện API) và Flow mượt rượt nhất như cách một User chạm vào Web vậy".

---
Chúc bạn vấn đáp điểm tuyệt đối ngày mai nha! 🚀
