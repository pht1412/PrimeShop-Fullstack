# Thiết kế hệ thống quản lý hàng hóa và doanh thu

## Tổng quan

Hệ thống được thiết kế để quản lý hai loại hàng hóa khác nhau và theo dõi doanh thu từ các nguồn khác nhau:

1. **Hàng tự sản xuất** (business_id = 1): Sản phẩm do công ty tự sản xuất
2. **Hàng từ nguồn khác** (business_id = 2): Sản phẩm nhập từ nhà cung cấp bên ngoài

## Các thực thể chính

### 1. Business
- **Mục đích**: Mô tả nguồn hàng hóa
- **business_id = 1**: Hàng tự sản xuất
- **business_id = 2**: Hàng từ nguồn khác
- **Quan hệ**: One-to-Many với Product

### 2. ImportTransaction
- **Mục đích**: Quản lý nhập hàng cho sản phẩm từ nguồn khác (business_id = 2)
- **Thông tin lưu trữ**:
  - Số lượng nhập
  - Giá vốn đơn vị
  - Tổng chi phí
  - Nhà cung cấp
  - Số hóa đơn
  - Ghi chú
- **Tác động**: Cập nhật stock của sản phẩm

### 3. ExportTransaction
- **Mục đích**: Quản lý xuất hàng cho sản phẩm tự sản xuất (business_id = 1)
- **Thông tin lưu trữ**:
  - Số lượng xuất
  - Giá bán đơn vị
  - Giá vốn đơn vị
  - Tổng doanh thu
  - Tổng chi phí
  - Lợi nhuận
  - Tỷ lệ lợi nhuận
  - Khách hàng
  - Số hóa đơn
- **Tác động**: Cập nhật stock và sold của sản phẩm

### 4. Revenue
- **Mục đích**: Tổng hợp doanh thu từ đơn hàng hoàn thành và xuất hàng
- **Thông tin lưu trữ**:
  - Doanh thu từ đơn hàng
  - Lợi nhuận từ đơn hàng
  - Doanh thu từ xuất hàng
  - Lợi nhuận từ xuất hàng
  - Tổng doanh thu
  - Tổng lợi nhuận
  - Tỷ lệ lợi nhuận
  - Số lượng đơn hàng
  - Số lượng xuất hàng

## Luồng hoạt động

### 1. Nhập hàng
- Chỉ áp dụng cho sản phẩm có business_id = 2 (hàng từ nguồn khác)
- Lưu thông tin nhập hàng vào ImportTransaction
- Cập nhật stock của sản phẩm
- Tính toán tổng chi phí và số vốn

### 2. Xuất hàng
- Chỉ áp dụng cho sản phẩm có business_id = 1 (hàng tự sản xuất)
- Kiểm tra stock trước khi xuất
- Lưu thông tin xuất hàng vào ExportTransaction
- Cập nhật stock và sold của sản phẩm
- Tính toán doanh thu, chi phí và lợi nhuận

### 3. Quản lý doanh thu
- Tự động cập nhật Revenue khi có xuất hàng
- Tích hợp với hệ thống đơn hàng để tính doanh thu từ đơn hàng hoàn thành
- Tính toán tổng doanh thu và lợi nhuận theo tháng

## API Endpoints

### Business
- `GET /api/business` - Lấy danh sách business
- `GET /api/business/{id}` - Lấy business theo ID
- `POST /api/business` - Tạo business mới
- `PUT /api/business/{id}` - Cập nhật business
- `DELETE /api/business/{id}` - Xóa business
- `POST /api/business/initialize` - Khởi tạo business mặc định

### ImportTransaction
- `POST /api/import-transactions` - Tạo giao dịch nhập hàng
- `GET /api/import-transactions/product/{productId}` - Lấy giao dịch nhập theo sản phẩm
- `GET /api/import-transactions/product/{productId}/total-cost` - Tổng chi phí theo sản phẩm
- `GET /api/import-transactions/date-range` - Lấy giao dịch theo khoảng thời gian

### ExportTransaction
- `POST /api/export-transactions` - Tạo giao dịch xuất hàng
- `GET /api/export-transactions/product/{productId}` - Lấy giao dịch xuất theo sản phẩm
- `GET /api/export-transactions/product/{productId}/total-revenue` - Tổng doanh thu theo sản phẩm
- `GET /api/export-transactions/product/{productId}/total-profit` - Tổng lợi nhuận theo sản phẩm
- `GET /api/export-transactions/date-range` - Lấy giao dịch theo khoảng thời gian

### Revenue
- `GET /api/revenue/period/{period}` - Lấy doanh thu theo tháng (YYYY-MM)
- `GET /api/revenue/year/{year}` - Lấy doanh thu theo năm
- `GET /api/revenue/all` - Lấy tất cả doanh thu
- `GET /api/revenue/year/{year}/total-revenue` - Tổng doanh thu theo năm
- `GET /api/revenue/year/{year}/total-profit` - Tổng lợi nhuận theo năm
- `POST /api/revenue/calculate/{period}` - Tính toán doanh thu theo tháng

## Khởi tạo dữ liệu

Hệ thống tự động khởi tạo 2 business mặc định khi khởi động:
1. **Hàng tự sản xuất** (ID: 1)
2. **Hàng từ nguồn khác** (ID: 2)

## Lưu ý quan trọng

1. **Phân quyền hàng hóa**: 
   - Chỉ có thể nhập hàng cho sản phẩm business_id = 2
   - Chỉ có thể xuất hàng cho sản phẩm business_id = 1

2. **Kiểm tra stock**: 
   - Khi xuất hàng phải kiểm tra stock đủ
   - Tự động cập nhật stock khi nhập/xuất

3. **Tính toán tự động**:
   - Tổng chi phí = Số lượng × Giá vốn đơn vị
   - Tổng doanh thu = Số lượng × Giá bán đơn vị
   - Lợi nhuận = Tổng doanh thu - Tổng chi phí
   - Tỷ lệ lợi nhuận = (Lợi nhuận / Tổng doanh thu) × 100

4. **Quản lý doanh thu**:
   - Tự động cập nhật Revenue khi có giao dịch xuất hàng
   - Tích hợp với hệ thống đơn hàng để tính doanh thu từ đơn hàng hoàn thành 