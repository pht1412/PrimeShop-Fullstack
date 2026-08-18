# 🛒 PrimeShop - Platform Thương Mại Điện Tử Fullstack

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)
![React](https://img.shields.io/badge/React-18-61DAFB.svg)
![Vite](https://img.shields.io/badge/Vite-6.2-646CFF.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)

> **PrimeShop** là một nền tảng Thương mại Điện tử (E-Commerce Monorepo) hiện đại được thiết kế và phát triển theo kiến trúc Fullstack khép kín. Hệ thống tích hợp đầy đủ quy trình từ duyệt sản phẩm, quản lý giỏ hàng, đặt hàng, xử lý thanh toán trực tuyến (VNPay & PayPal), phân quyền người dùng (Customer, Seller, Admin) đến quản lý tồn kho và thông báo Real-time.

---

## 📌 Tổng Quan Kiến Trúc (Architecture Overview)

Dự án được tổ chức theo mô hình **Monorepo** giúp đồng bộ mã nguồn và dễ dàng triển khai:

```text
PrimeShop-Fullstack/
├── 📂 Frontend-PrimeShop/   # Giao diện người dùng (React, Vite, MUI, Zustand)
├── 📂 Backend-PrimeShop/    # RESTful API Service (Spring Boot 3, Spring Security, JPA)
├── 📄 .gitignore            # File cấu hình Git ignore chung
└── 📄 README.md             # Tài liệu dự án
```

---

## 🚀 Công Nghệ Sử Dụng (Tech Stack)

### 🔹 Backend (`/Backend-PrimeShop`)
- **Core Framework**: Java 17, Spring Boot 3.4.4
- **Security & Authentication**: Spring Security, JSON Web Token (JWT)
- **Database & Persistence**: Spring Data JPA, PostgreSQL / MS SQL Server, Flyway (Database Migration)
- **Real-time & Communication**: WebSocket, STOMP Messaging
- **Integration & Payment**: VNPay API, PayPal SDK, Cloudinary API (Media Management)
- **API Documentation**: OpenAPI 3.0 / Swagger UI (`springdoc-openapi`)
- **Utilities**: ZXing (Mã QR), Lombok, Jackson JSR310

### 🔹 Frontend (`/Frontend-PrimeShop`)
- **Core Framework**: React 18, TypeScript / JSX, Vite 6
- **State Management**: Zustand, TanStack React Query v5
- **UI Components & Styling**: Material UI (MUI), Ant Design Icons, Swiper, SweetAlert2
- **Real-time**: `@stomp/stompjs`, SockJS Client
- **Data Fetching & OpenAPI**: Axios, `openapi-typescript`

---

## ✨ Tính Năng Nổi Bật (Key Features)

### 👤 Người Dùng & Khách Hàng (Customer)
- **Xác thực & Phân quyền**: Đăng ký, đăng nhập bảo mật qua JWT Token & Refresh Token.
- **Duyệt & Tìm kiếm sản phẩm**: Lọc theo danh mục, mức giá, từ khóa và đánh giá (Rating).
- **Giỏ hàng & Đặt hàng**: Giỏ hàng realtime (Zustand + Local Storage), áp mã giảm giá (Voucher).
- **Thanh toán trực tuyến**: Tích hợp thanh toán an toàn qua cổng **VNPay** và **PayPal**.
- **Theo dõi đơn hàng**: Cập nhật trạng thái đơn hàng theo thời gian thực (Real-time WebSockets).

### 🏪 Quản Lý Cửa Hàng & Người Bán (Seller)
- **Quản lý sản phẩm**: Thêm, sửa, xóa sản phẩm, upload nhiều hình ảnh qua Cloudinary.
- **Quản lý tồn kho**: Cập nhật số lượng nhập kho, cảnh báo hết hàng.
- **Báo cáo doanh thu**: Thống kê đơn hàng và biểu đồ doanh thu theo thời gian.

### 🛡️ Quản Trị Hệ Thống (Admin)
- **Quản lý người dùng**: Phân quyền tài khoản (User, Seller, Admin), khóa/mở tài khoản.
- **Quản lý danh mục & Khuyến mãi**: Tạo và quản lý mã giảm giá, danh mục sản phẩm.

---

## 🛠️ Hướng Dẫn Cài Đặt & Chạy Local (Getting Started)

### Yêu Cầu Tiền Đề (Prerequisites)
- **Java**: JDK 17 trở lên
- **Node.js**: Node.js 18+ & npm
- **Database**: PostgreSQL / SQL Server

---

### 1️⃣ Chạy Backend (Spring Boot)

```bash
# Di chuyển vào thư mục Backend
cd Backend-PrimeShop

# Cấu hình file application.yaml hoặc .env chứa thông tin kết nối DB

# Chạy ứng dụng bằng Maven Wrapper
./mvnw spring-boot:run
```
> 🌐 **Backend Server**: `http://localhost:8080`  
> 📖 **Swagger API Docs**: `http://localhost:8080/swagger-ui.html`

---

### 2️⃣ Chạy Frontend (React + Vite)

```bash
# Di chuyển vào thư mục Frontend
cd Frontend-PrimeShop

# Cài đặt dependencies
npm install

# Khởi chạy môi trường phát triển (Dev server)
npm run dev
```
> 🌐 **Frontend Client**: `http://localhost:5173`

---

## 🎯 Bài Toán Kỹ Thuật Đã Giải Quyết (Engineering Challenges)

1. **Bảo mật RESTful API**: Áp dụng Filter Chain của Spring Security kết hợp với JWT Authentication để xác thực từng Request và phân quyền chi tiết cho từng Role (`ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`).
2. **Thanh toán An toàn (Payment Integration)**: Xử lý Checksum / Hashing SHA-256 đối với tham số VNPay và đồng bộ Callback (IPN/Return URL) để xác nhận trạng thái thanh toán chính xác.
3. **Đồng bộ Dữ liệu Real-time**: Sử dụng WebSocket + STOMP Broker để phát thông báo đơn hàng tức thì tới người mua và người bán mà không cần F5 trang.
4. **Tối ưu hóa Hiệu năng Frontend**: Áp dụng TanStack Query cho việc caching API response, nạp ảnh bất đồng bộ qua Cloudinary và phân chia code (Code splitting) bằng React Router.

---

## 📝 License
Dự án được phát triển cho mục đích học tập và phục vụ công việc. Bản quyền thuộc về **[pht1412](https://github.com/pht1412)**.
