# PrimeShop — Báo cáo bàn giao phiên làm việc

> **Cập nhật lần cuối:** 04/11/2026 (giờ Việt Nam, UTC+07:00)  
> **Mục đích:** Tài liệu ngữ cảnh để AI hoặc thành viên khác tiếp tục làm việc trên dự án mà không phải suy đoán lại các quyết định và trạng thái đã xác nhận.  
> **Nguồn sự thật:** Mã nguồn hiện tại trong repository và trạng thái kiểm tra được ghi ở cuối tài liệu. Nếu tài liệu này khác với mã nguồn, ưu tiên mã nguồn mới nhất và cập nhật lại báo cáo.

## 1. Tóm tắt nhanh cho phiên tiếp theo

- Dự án là monorepo PrimeShop tại `C:\Users\lhp14\Prime`, gồm frontend React/Vite, backend Spring Boot và PostgreSQL trên Supabase.
- Mục tiêu triển khai hiện tại: **frontend local + backend local HTTP cổng 8080 + database Supabase**. Không còn dự định phụ thuộc Render/Vercel trong luồng local.
- PrimeShop hướng tới hai mô hình:
  - **B2C:** shop chính thức PrimeShop bán sản phẩm; admin tạo sản phẩm từ `/admin/products`.
  - **C2C:** khách hàng đăng ký seller, admin duyệt seller, seller đăng sản phẩm, admin duyệt sản phẩm.
- Người dùng đã xác nhận đã đăng ký/đăng nhập, tạo hồ sơ seller, duyệt shop, duyệt sản phẩm, và kiểm tra được một sản phẩm `APPROVED` cùng danh mục/shop trong Supabase.
- Lỗi đăng ký tài khoản đã xác định là CORS: frontend chạy cổng `5174`, backend chỉ cho origin cổng `5173`. Đã sửa để chấp nhận localhost/127.0.0.1 với cổng Vite linh hoạt.
- Đã sửa source để admin thêm hàng B2C: endpoint tự gắn sản phẩm với hồ sơ `PrimeShop Official`, tạo hồ sơ này nếu chưa có và đánh dấu sản phẩm `APPROVED`.
- **Điểm cần chú ý nhất:** backend source mới biên dịch thành công, nhưng lần chạy Maven mới nhất thất bại vì cổng 8080 đã có backend khác chiếm. Tiến trình đang chạy lúc kiểm tra là một Java process dùng profile `prod`; chưa xác nhận nó đã nạp các thay đổi B2C mới. Cần restart backend hiện hữu từ VS Code/Pleiades một cách có chủ đích rồi mới thử chức năng admin B2C.
- Không có sản phẩm B2C thử nghiệm nào được tạo trong phiên; không gọi endpoint tạo sản phẩm với dữ liệu thật để tránh thêm dữ liệu ngoài ý muốn.
- Không commit thay đổi nào trong phiên được ghi nhận. Worktree còn nhiều thay đổi chưa commit, một số là kết quả các bước local/Supabase trước đó; không xóa hoặc hoàn tác khi chưa xác định chủ sở hữu.

## 2. Cấu trúc và công nghệ

Repository: `pht1412/PrimeShop-Fullstack`  
Thư mục local: `C:\Users\lhp14\Prime`

```text
Prime/
├── Backend-PrimeShop/   # Java / Spring Boot REST API
├── Frontend-PrimeShop/  # React / TypeScript-JSX / Vite
├── Report/              # Báo cáo bàn giao
└── README.md
```

### Backend

- Spring Boot `3.4.4`, Maven Wrapper, source cấu hình Java release 17 trong `pom.xml`.
- Máy dùng để chạy đã có Java `21.0.6`; đây là runtime quan sát được từ log, không phải yêu cầu đã xác nhận của dự án.
- Spring Security + JWT, Spring Data JPA/Hibernate, PostgreSQL driver; có SQL Server driver và cấu hình SQL Server cũ trong cấu hình nền.
- REST API, WebSocket/STOMP, thanh toán VNPay/PayPal và các mô-đun khác.
- Cấu hình profile Supabase ở `Backend-PrimeShop/src/main/resources/application-prod.yml`.
- `Application.java` nạp `.env` cục bộ và profile `prod` dùng `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. Không sao chép giá trị bí mật vào báo cáo, chat, source hoặc commit.

### Frontend

- React 18, TypeScript/JSX, Vite, Axios, React Router, MUI, SweetAlert2, Zustand, TanStack Query.
- Script: `npm run dev`, `npm run build`, `npm run lint`.
- Vite proxy `/api` và `/ws` tới `http://localhost:8080`.
- PrimeShop được xác nhận ở `http://localhost:5174`; `5173` có thể đang được ứng dụng khác sử dụng, vì vậy không giả định cổng 5173 là PrimeShop.
- Router admin được mount tại `/admin/*`; các route admin được kiểm tra role ở frontend, API admin còn được bảo vệ ở backend.

### Database

- Database mới được dùng là PostgreSQL trên Supabase, schema mặc định `public`.
- Backend chạy profile `prod`; log đã xác nhận Hibernate kết nối PostgreSQL 17.11 thành công.
- Tên bảng theo entity đã sửa/đang dùng:
  - `public.users`
  - `public.category` (số ít; không phải `categories`)
  - `public.seller_profiles`
  - `public.product`
  - Bảng role/user-role và các bảng quan hệ khác do entity/JPA quản lý.
- Hibernate profile `prod` đang để `ddl-auto: update`; Flyway tắt. Không tự áp migration phá hủy dữ liệu.
- Danh mục `Điện thoại` đã được tạo; ID `1` được xác nhận trước đó. Truy vấn endpoint danh mục trả `["Điện thoại"]`.

## 3. Mô hình dữ liệu và luồng nghiệp vụ quan trọng

### Quan hệ bắt buộc của sản phẩm

Entity `Product` ánh xạ sang bảng `product`, trong đó:

- `seller_id` bắt buộc tham chiếu `seller_profiles.id`.
- `category_id` bắt buộc tham chiếu `category.id`.
- `slug` bắt buộc và duy nhất.
- Các trường chính gồm `name`, `brand`, `price`, `discountPercent`, `discountPrice`, `imageUrl`, `stock`, `sold`, `active`, `status`, `description`, `createdAt`, `updatedAt`.
- Trạng thái sản phẩm trong Java: `PENDING`, `APPROVED`, `REJECTED`, `DISABLED`.
- Sản phẩm public được lọc theo `status = APPROVED` và `active = true` trong `ProductSpecification`.

Vì `seller_id` là NOT NULL, admin không thể tạo product chỉ bằng payload tên/giá/danh mục; thao tác phải gắn với một hồ sơ seller hợp lệ. Đây là nguyên nhân gốc của form admin cũ.

### Role và trạng thái seller

- Role mặc định đăng ký tài khoản: `ROLE_USER`.
- Role seller trong code được viết chính tả là `ROLE_BUSSINESS` (giữ nguyên spelling để tương thích code hiện có).
- Role admin: `ROLE_ADMIN`.
- Gửi đơn seller tạo `seller_profiles` ở trạng thái `PENDING_REVIEW`, và gán role business vào user.
- Trạng thái seller: `PENDING_REVIEW`, `VERIFIED_SELLER`, `BANNED_SELLER`.
- JWT chứa role tại thời điểm đăng nhập; nếu role được thêm/sửa trực tiếp trong DB, cần đăng xuất và đăng nhập lại để có token mới.

### Luồng C2C đã xác nhận

1. Người dùng đăng ký tài khoản khách hàng và đăng nhập.
2. Tại `/account` → tab C2C → **Đăng ký Business**, gửi tên shop, số điện thoại, CCCD/CMND và mô tả.
3. API `POST /api/seller/apply` tạo seller profile `PENDING_REVIEW`.
4. Admin đăng nhập lại bằng tài khoản có `ROLE_ADMIN`, vào `/admin/business-manager`, tab **Đơn chờ duyệt**, duyệt shop.
5. Seller đăng nhập lại, vào `/account` → C2C, thêm tin. API `POST /api/seller/add-product?sellerId=...` tạo sản phẩm trạng thái `PENDING`.
6. Admin vào `/admin/c2c-management` để duyệt sản phẩm. Endpoint admin duyệt yêu cầu `sellerId` và `productId`.
7. Người dùng xác nhận đã duyệt shop, duyệt sản phẩm và truy vấn SQL cuối cho thấy product `APPROVED` liên kết đúng category/shop.

Hai trang admin có mục đích khác nhau:

- `/admin/business-manager`: duyệt hồ sơ seller.
- `/admin/c2c-management`: duyệt sản phẩm C2C.

Thông báo “Không thể tải danh sách sản phẩm” ở trang C2C không phải lỗi danh sách seller; để duyệt shop phải dùng đúng `business-manager`.

### Luồng B2C admin vừa được sửa

Quyết định người dùng đã chọn: **admin tạo hàng B2C thuộc shop chính thức PrimeShop; C2C tiếp tục do seller khách hàng đăng và admin duyệt**.

Source mới thực hiện:

- `POST /api/admin/product/add` không còn yêu cầu client gửi `sellerId`.
- Backend gọi `SellerService.getOrCreateOfficialSellerProfile()`.
- Hồ sơ mặc định có tên `PrimeShop Official`, trạng thái `VERIFIED_SELLER`, liên kết với tài khoản admin đầu tiên tạo hồ sơ.
- Nếu đã tồn tại shop `PrimeShop Official`, tái sử dụng hồ sơ đó.
- Nếu tài khoản admin hiện tại đã gắn với seller profile khác trong khi chưa tồn tại shop chính thức, trả lỗi rõ ràng thay vì tự gắn sai shop.
- Sản phẩm tạo qua hàm admin được đặt `APPROVED` để hàng B2C có thể bán ngay; C2C vẫn tạo `PENDING`.
- Form gửi `imageUrl`, số tiền dạng number, `categoryId` dạng number và chỉ gửi spec có đủ tên/giá trị. Form hiển thị thông báo lỗi từ API và trạng thái đang submit.
- Hình ảnh chi tiết chưa được thêm ở form tạo mới; hiện form có URL ảnh đại diện.

**Thiết kế cần lưu ý:** Hồ sơ PrimeShop Official hiện được liên kết với tài khoản admin tạo ra hồ sơ đó, do entity `SellerProfile` bắt buộc có `user_id`. Đây là phương án gọn cho local/hiện tại, nhưng về lâu dài nên tạo một tài khoản service/shop B2C chuyên biệt hoặc mô hình seller system-owned, không phụ thuộc vòng đời một admin cá nhân.

## 4. Các thay đổi đã thực hiện trong phiên

### Tương thích Supabase/PostgreSQL

- Đổi kiểu cột `NVARCHAR` không tương thích sang kiểu PostgreSQL phù hợp ở entity Category, SellerProfile, News và Conversation.
- Ánh xạ rõ bảng Category sang `category`, khớp tên entity/backend thay vì bảng `categories` của schema cũ.
- Kiểm tra schema; xác nhận ban đầu thiếu `category`/`seller_profiles`, và `categories` không có cột `active`.
- Hướng dẫn tạo category và thêm seller/product qua luồng ứng dụng thay vì điền `SELLER_ID`/`CATEGORY_ID` làm placeholder trong SQL.

### Local HTTP, frontend proxy và deploy cũ

- Chuyển Axios API frontend local sang base URL tương đối `/api`, để Vite proxy về backend local.
- Cập nhật Vite proxy `/api` và WebSocket `/ws` trỏ `http://localhost:8080`.
- Chuyển các URL backend local hard-code HTTPS/Render sang đường dẫn tương đối/proxy ở API, voucher, news, WebSocket và trang static liên quan.
- Thêm `server.ssl.enabled: false` trong `application-prod.yml`, đồng thời xác nhận backend chạy HTTP trong lần khởi động thành công trước đó.
- Gỡ file cấu hình triển khai khỏi source:
  - Xóa `Backend-PrimeShop/render.yaml`.
  - Xóa `Frontend-PrimeShop/vercel.json`.
- Đây chỉ là thay đổi source; chưa xác nhận dịch vụ cloud Render/Vercel đã bị xóa trên dashboard. Người dùng ưu tiên chạy local và giữ Supabase.

### Đăng ký tài khoản/CORS

- Frontend xử lý cả response lỗi dạng `error` và `message`; trường hợp không có response báo lỗi kết nối.
- CORS ban đầu chỉ cho `http://localhost:5173`, trong khi PrimeShop chạy `http://localhost:5174`.
- Cập nhật `CorsConfig` và `AuthController` để cho phép origin HTTP local trên localhost/127.0.0.1 với cổng linh hoạt.
- Đã xác minh trước đó: preflight từ origin 5174 đổi từ HTTP 403 sang HTTP 200.
- Backend sau đó được restart theo yêu cầu của người dùng; người dùng xác nhận đăng ký tài khoản và đăng nhập thành công, user xuất hiện trong Supabase.

### Admin B2C

- Sửa `AdminProductController`, `SellerService`, `SellerRepo`, `ProductService`, `ProductManager.tsx`.
- Fix quan hệ bắt buộc seller/category, trạng thái B2C `APPROVED`, request payload, ảnh đại diện, validation danh mục, feedback lỗi và trạng thái đang gửi.
- `ProductService` bỏ qua các spec trống/null và không gọi saveAll khi không có spec hợp lệ.
- Form admin tại `http://localhost:5174/admin/products` đổi nút thành **Thêm sản phẩm B2C** và có nhắc shop đích.

### Những file local khác đã bị chỉnh trước phần B2C

Worktree có các thay đổi local trước/đồng thời trong phiên ở:

- `Backend-PrimeShop/src/main/java/com/primeshop/category/Category.java`
- `Backend-PrimeShop/src/main/java/com/primeshop/seller/SellerProfile.java`
- `Backend-PrimeShop/src/main/java/com/primeshop/news/News.java`
- `Backend-PrimeShop/src/main/java/com/primeshop/chatc2c/Conversation.java`
- `Backend-PrimeShop/src/main/java/com/primeshop/config/CorsConfig.java`
- `Backend-PrimeShop/src/main/java/com/primeshop/product/ProductController.java`
- `Backend-PrimeShop/src/main/java/com/primeshop/user/AuthController.java`
- `Backend-PrimeShop/src/main/resources/application-prod.yml`
- `Backend-PrimeShop/src/main/resources/static/index.html`
- `Frontend-PrimeShop/src/api/api.ts`
- `Frontend-PrimeShop/src/pages/auth/RegisterPage.tsx`
- `Frontend-PrimeShop/src/pages/news/api.ts`
- `Frontend-PrimeShop/src/components/VoucherInput.tsx`
- `Frontend-PrimeShop/src/components/VoucherList.tsx`
- `Frontend-PrimeShop/src/components/chat/wsClient.ts`
- `Frontend-PrimeShop/vite.config.js`
- `Backend-PrimeShop/render.yaml` (đã xóa)
- `Frontend-PrimeShop/vercel.json` (đã xóa)

Các thay đổi trong danh sách này chưa được commit. Trước khi sửa tiếp, kiểm tra `git status` và đọc diff; không ghi đè hay hoàn tác nếu chưa rõ nguồn gốc.

## 5. Địa chỉ và endpoint hữu ích

### Local

- PrimeShop register: `http://localhost:5174/register`
- PrimeShop account: `http://localhost:5174/account`
- Admin products/B2C: `http://localhost:5174/admin/products`
- Admin duyệt seller: `http://localhost:5174/admin/business-manager`
- Admin duyệt sản phẩm C2C: `http://localhost:5174/admin/c2c-management`
- Backend: `http://localhost:8080`
- Danh mục kiểm tra: `GET http://localhost:8080/api/category`

### API auth/seller/product

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/check-role`
- `POST /api/seller/apply` (cần user đăng nhập)
- `GET /api/seller/me`
- `POST /api/seller/add-product?sellerId={id}` (seller; C2C, status `PENDING`)
- `GET /api/admin/seller/pending-registrations` (admin)
- `PATCH /api/admin/seller/approve-registration?sellerId={id}` (admin)
- `GET /api/admin/seller/pending-products` (admin)
- `PATCH /api/admin/seller/approve-products?sellerId={sellerId}&productId={productId}` (admin)
- `POST /api/admin/product/add` (admin; B2C shop PrimeShop Official, status `APPROVED`)

API admin được bảo vệ bằng `ROLE_ADMIN` trong SecurityConfig. Gọi endpoint admin không có token trả 403 là hành vi mong đợi, không phải bằng chứng rằng ứng dụng lỗi.

### SQL chỉ đọc để kiểm tra product

```sql
SELECT
    p.id AS product_id,
    p.name,
    p.brand,
    p.price,
    p.discount_price,
    p.stock,
    p.sold,
    p.status,
    p.active,
    p.slug,
    c.name AS category,
    sp.shop_name AS seller_shop,
    u.username AS seller_username,
    p.created_at
FROM public.product p
JOIN public.category c ON c.id = p.category_id
JOIN public.seller_profiles sp ON sp.id = p.seller_id
JOIN public.users u ON u.id = sp.user_id
ORDER BY p.id DESC
LIMIT 20;
```

### SQL chỉ đọc để xem seller

```sql
SELECT sp.id AS seller_id, sp.shop_name, sp.status, u.username
FROM public.seller_profiles sp
JOIN public.users u ON u.id = sp.user_id
ORDER BY sp.id DESC;
```

Không gửi user khác hoặc AI không đáng tin cậy các kết quả chứa CCCD, số điện thoại, email, password hash, JWT, DB URL/password, payment/API keys.

## 6. Kiểm tra đã chạy và kết quả

- Backend compile sau thay đổi B2C:
  - Lệnh: `Backend-PrimeShop\.mvnw.cmd -DskipTests compile`
  - Kết quả: `BUILD SUCCESS` (228 source files).
- Frontend production build:
  - Lệnh: `npm run build` tại `Frontend-PrimeShop`.
  - Kết quả: build thành công.
  - Có cảnh báo CSS cũ `height: 3px;s` và bundle JS minified lớn hơn 500 kB; không làm build thất bại.
- VS Code Problems cho các file backend B2C và ProductManager: không thấy lỗi.
- `git diff --check`: thành công.
- Backend Supabase/HTTP:
  - Đã xác nhận ở một lần chạy trước cổng 8080 trả HTTP 200 cho `/api/category`, nội dung `["Điện thoại"]`.
  - Trong lần kiểm tra cuối của thay đổi B2C, có một backend prod trên 8080 tiếp tục trả 200.
- CORS đăng ký 5174:
  - Đã xác minh preflight HTTP 200 sau khi sửa và user xác nhận đăng ký thành công.
- C2C:
  - User xác nhận đăng ký seller, duyệt shop, duyệt sản phẩm; SQL xác nhận product `APPROVED` và các quan hệ seller/category đúng.
- Không chạy test suite đầy đủ.
- Không thực hiện POST tạo một sản phẩm B2C mới để tránh ghi dữ liệu vào Supabase mà không có xác nhận.

### Lưu ý về backend restart hiện tại

- Một lần thử khởi chạy ứng dụng sau build B2C bằng Maven đã kết thúc `exit code 1` với thông báo `Port 8080 was already in use`.
- Tiến trình nghe cổng 8080 lúc đó là Java `com.primeshop.Application` chạy profile `prod`; nó được khởi chạy trước thay đổi B2C mới nhất. Không dừng tiến trình này tự động.
- Do đó, **compile thành công không đồng nghĩa process đang phục vụ đã nạp class mới**. Để xác minh tính năng B2C mới:
  1. Người dùng dừng/restart backend hiện hữu trong VS Code/Pleiades.
  2. Chạy bằng profile `prod` và `.env` hiện có; không hiển thị `.env`.
  3. Xác nhận log `Tomcat initialized with port 8080 (http)` và kết nối Postgres/Supabase.
  4. Đăng nhập lại admin để token chứa role mới nếu role vừa được sửa.
  5. Thử tạo product B2C có kiểm soát sau khi người dùng đồng ý, rồi kiểm tra bằng SQL chỉ đọc.

## 7. Việc nên làm tiếp theo

### Ưu tiên trước mắt

1. Restart backend source mới từ IDE, xác nhận port 8080 do đúng process mới giữ.
2. Mở `/admin/products`, tạo một sản phẩm B2C có dữ liệu hợp lệ sau khi người dùng đồng ý.
3. Xác minh row có:
   - seller shop `PrimeShop Official`;
   - `status = 'APPROVED'`;
   - `active = true`;
   - category và slug hợp lệ;
   - hiển thị được ở trang sản phẩm public.
4. Nếu lỗi, lấy status + response body của request `POST /api/admin/product/add`; không gửi Authorization header hoặc token.
5. Xác minh không có hai hồ sơ `PrimeShop Official` do nhiều admin đồng thời tạo (hiện chưa có ràng buộc unique theo shopName). Nếu có nhu cầu, thêm cơ chế idempotency/unique constraint/migration an toàn.

### Cải thiện thiết kế và vận hành

- Cân nhắc tạo tài khoản/hồ sơ service cho shop B2C thay vì gắn shop chính thức với admin cá nhân.
- Bổ sung test cho:
  - admin tạo B2C khi chưa có official profile;
  - admin tái sử dụng profile đã tồn tại;
  - từ chối/hiển thị lỗi nếu admin đã gắn seller profile khác;
  - C2C vẫn tạo `PENDING`;
  - guest/non-admin không được gọi API admin.
- Bổ sung upload/chọn nhiều ảnh vào form admin nếu nghiệp vụ cần; form hiện chỉ gửi URL ảnh đại diện.
- Cân nhắc thêm validation backend cho `ProductRequest` (giá, danh mục, tên, stock, discount range) và response lỗi chuẩn; hiện controller trả lỗi theo cấu hình exception handler hiện có.
- Review `application.properties`: còn cấu hình SQL Server/SSL cũ, đồng thời repo có thể chứa cấu hình nhạy cảm ngoài `.env`. Không đưa các giá trị đó vào tài liệu; nếu key trong source là key thật, chuyển sang environment secret và rotate theo chính sách.
- Cập nhật README cũ nếu cần: có thể đang mô tả Java 17/port 5173/deploy hoặc tính năng khác trạng thái hiện tại. Tài liệu này ghi nhận trạng thái local quan sát được.
- Render/Vercel chỉ bị gỡ khỏi cấu hình source local; chưa có bằng chứng dashboard/cloud đã bị xóa.

## 8. Bảo mật và quy tắc làm việc

- Không đọc/in toàn bộ `.env`, không đưa credentials, JWT, CCCD hoặc dữ liệu cá nhân vào report/log/commit.
- Người dùng từng nhắc lỗi SSL; local stack hiện cần HTTP giữa Vite và backend (`http://localhost:8080`), Supabase vẫn là dịch vụ database ngoài máy.
- Không giết process cổng 8080 nếu chưa xác minh chính xác PID và chưa được người dùng cho phép. PID thay đổi theo thời gian; luôn kiểm tra lại trước khi thao tác.
- Không chạy lệnh SQL INSERT/UPDATE/DELETE để thử nghiệm nếu chưa có xác nhận của người dùng. Dùng SELECT để xác minh.
- Không commit thay đổi nếu chưa được yêu cầu.
- Trước mọi chỉnh sửa mới, chạy `git status --short`, xem diff liên quan và bảo toàn thay đổi hiện hữu.

## 9. Tệp tham khảo chính

### Frontend

- `Frontend-PrimeShop/src/App.jsx` — router gốc.
- `Frontend-PrimeShop/src/routes/AdminRoutes.jsx` — routes admin.
- `Frontend-PrimeShop/src/components/AdminRoute.tsx` — guard `ROLE_ADMIN`.
- `Frontend-PrimeShop/src/pages/admin/ProductManager.tsx` — form/list sản phẩm admin (B2C).
- `Frontend-PrimeShop/src/components/C2CTab.tsx` — luồng seller đăng C2C.
- `Frontend-PrimeShop/src/pages/admin/BusinessManager.tsx` — quản lý/duyệt seller.
- `Frontend-PrimeShop/src/pages/admin/C2CManagement.tsx` — duyệt sản phẩm C2C.
- `Frontend-PrimeShop/src/api/api.ts` — Axios base URL và JWT header.
- `Frontend-PrimeShop/src/api/seller.api.ts` — API seller/admin seller.
- `Frontend-PrimeShop/vite.config.js` — proxy `/api`, `/ws`.

### Backend

- `Backend-PrimeShop/src/main/java/com/primeshop/Application.java` — khởi động, nạp `.env` theo triển khai hiện có.
- `Backend-PrimeShop/src/main/resources/application-prod.yml` — Supabase/PostgreSQL, cổng và HTTP.
- `Backend-PrimeShop/src/main/resources/application.properties` — cấu hình nền cũ; cần rà soát bảo mật/cấu hình.
- `Backend-PrimeShop/src/main/java/com/primeshop/config/SecurityConfig.java` — phân quyền.
- `Backend-PrimeShop/src/main/java/com/primeshop/config/CorsConfig.java` — CORS local.
- `Backend-PrimeShop/src/main/java/com/primeshop/user/AuthController.java` — register/login/check-role.
- `Backend-PrimeShop/src/main/java/com/primeshop/product/Product.java` — entity và quan hệ product.
- `Backend-PrimeShop/src/main/java/com/primeshop/product/ProductService.java` — tạo product C2C/B2C.
- `Backend-PrimeShop/src/main/java/com/primeshop/admin/AdminProductController.java` — API admin product.
- `Backend-PrimeShop/src/main/java/com/primeshop/seller/SellerController.java` — API seller.
- `Backend-PrimeShop/src/main/java/com/primeshop/seller/SellerService.java` — đăng ký/duyệt seller và hồ sơ official.
- `Backend-PrimeShop/src/main/java/com/primeshop/admin/AdminSellerController.java` — API admin duyệt seller/product C2C.
- `Backend-PrimeShop/src/main/java/com/primeshop/seller/SellerProfile.java` — entity `seller_profiles`.
- `Backend-PrimeShop/src/main/java/com/primeshop/category/Category.java` — entity `category`.

## 10. Checklist khi AI mới bắt đầu phiên

1. Đọc file này trước, sau đó xem mã nguồn hiện tại; không coi trạng thái tiến trình cũ là mặc định.
2. Chạy `git status --short`; bảo toàn mọi thay đổi chưa commit.
3. Xác nhận với người dùng mục tiêu của lượt mới nếu tác động dữ liệu Supabase, thanh toán, quyền admin hoặc xóa dữ liệu.
4. Đối với tính năng admin B2C, kiểm tra backend mới đã restart hay chưa trước khi chẩn đoán UI/API.
5. Không giả định product B2C đã được tạo: phiên trước chỉ xác nhận C2C; luồng B2C mới compile/build nhưng chưa submit dữ liệu thử.
6. Dùng trạng thái DB hiện tại làm cơ sở: người dùng cho biết sản phẩm C2C cuối đã được duyệt và SQL xác nhận `APPROVED`; dữ liệu này có thể thay đổi sau phiên.
7. Ghi rõ trong phản hồi nếu thay đổi DB hoặc cần restart backend; không nói đã xác minh điều chưa thực sự chạy.

