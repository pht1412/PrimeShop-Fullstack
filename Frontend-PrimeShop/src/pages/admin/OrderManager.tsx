import React, { useEffect, useState } from "react";
import { useNavigate } from 'react-router-dom';
import "../../assets/css/admin.css";
import api from "../../api/api";
import { Order } from "../../types/order";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Select,
  MenuItem,
  Button
} from "@mui/material";
import Swal from "sweetalert2";
import dayjs from "dayjs";

const escapeHtml = (value: string | number | null | undefined): string =>
  String(value ?? "").replace(/[&<>"']/g, character => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  })[character] ?? character);

const OrderManager = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);
  const [filters, setFilters] = useState({
    orderId: "",
    status: "all",
    search: "",
    startDate: "",
    endDate: ""
  });

  useEffect(() => {
    fetchOrders();
  }, [filters]);

  const fetchOrders = async () => {
    try {
      const response = await api.get("/order/all-orders", {
        params: {
          orderId: filters.orderId || undefined,
          status: filters.status !== "all" ? filters.status : undefined,
          search: filters.search || undefined,
          startDate: filters.startDate ? dayjs(filters.startDate).format("YYYY-MM-DDTHH:mm:ss") : undefined,
          endDate: filters.endDate ? dayjs(filters.endDate).format("YYYY-MM-DDTHH:mm:ss") : undefined
        }
      });

      console.log("📦 Orders:", response.data);
      setOrders(response.data);
    } catch (error) {
      console.error("Error fetching orders:", error);
      Swal.fire("Lỗi", "Không thể tải danh sách đơn hàng", "error");
    }
  };

  // ============================
  // SHOW DETAILS POPUP
  // ============================
  const showDetails = (order: Order) => {
    const finalPrice = order.finalAmount ?? order.totalAmount;
    const discount = order.totalAmount - finalPrice;

    Swal.fire({
      title: `Chi tiết đơn hàng #${order.orderId}`,
      html: `
        <div style="text-align:left;font-size:0.95rem;line-height:1.6;">

            <div style="background:#f5f5f5;padding:10px;border-radius:8px;margin-bottom:10px;">
                <p><strong>Khách hàng:</strong> ${escapeHtml(order.fullName)}</p>
                <p><strong>SĐT:</strong> ${escapeHtml(order.phoneNumber)}</p>
                <p><strong>Địa chỉ:</strong> ${escapeHtml(order.address)}</p>
                <p><strong>Ngày đặt:</strong> ${new Date(order.createdAt).toLocaleString("vi-VN")}</p>
            </div>

            <p><strong>Sản phẩm:</strong></p>
            <ul style="padding-left:20px;margin-bottom:15px;">
              ${order.orderItems?.map(item =>
                `<li>${escapeHtml(item.productName)}
                    <span style="color:#666;font-size:0.9em">(x${escapeHtml(item.quantity)})</span>
                 </li>`
              ).join("") || "<li>Không có dữ liệu</li>"}
            </ul>

            <hr />
            <div style="display:flex;justify-content:space-between;">
                <span>Tổng tiền hàng:</span>
                <span>${order.totalAmount.toLocaleString("vi-VN")} đ</span>
            </div>

            ${discount > 0 ? `
            <div style="display:flex;justify-content:space-between;color:#2e7d32;">
                <span>Giảm giá (Voucher):</span>
                <span>-${discount.toLocaleString("vi-VN")} đ</span>
            </div>` : ""}

            <div style="display:flex;justify-content:space-between;font-size:1.2em;color:#d32f2f;margin-top:5px;">
                <strong>THỰC THU:</strong>
                <strong>${finalPrice.toLocaleString("vi-VN")} đ</strong>
            </div>
        </div>
      `,
      width: 500,
      confirmButtonText: "Đóng"
    });
  };

  // ============================
  // UPDATE STATUS
  // ============================
  const handleSetStatus = async (orderId: string, status: string, successMessage: string) => {
    try {
      await api.put(`/admin/order/update-status?id=${orderId}&status=${status}`);
      Swal.fire("Thành công", successMessage, "success");
      fetchOrders();
    } catch (error) {
      const responseError = error as {
        response?: { data?: { message?: string } };
      };
      Swal.fire(
        "Lỗi",
        responseError.response?.data?.message ?? "Không thể cập nhật đơn hàng",
        "error"
      );
    }
  };

  const handleFilterChange = (field: keyof typeof filters, value: string) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  // ============================
  // RENDER UI
  // ============================
  return (
    <div style={{ padding: "20px" }}>
      <h1 style={{ marginBottom: "20px", color: "#1976d2" }}>Quản lý đơn hàng</h1>

      {/* Filters */}
      <form style={{ display: 'flex', gap: '15px', marginBottom: '20px', flexWrap: 'wrap' }}>
        <Select
          value={filters.status}
          onChange={e => handleFilterChange("status", e.target.value)}
          size="small"
          style={{ minWidth: 150, background: "white" }}
        >
          <MenuItem value="all">Tất cả</MenuItem>
          <MenuItem value="PENDING">Chờ xác nhận</MenuItem>
          <MenuItem value="CONFIRMED">Đã xác nhận</MenuItem>
          <MenuItem value="PAID">Đã thanh toán</MenuItem>
          <MenuItem value="PROCESSING">Đang xử lý</MenuItem>
          <MenuItem value="INVENTORY">Đang kiểm kho</MenuItem>
          <MenuItem value="READY_TO_SHIP">Sẵn sàng bàn giao</MenuItem>
          <MenuItem value="SHIPPED">Đang vận chuyển</MenuItem>
          <MenuItem value="DELIVERED">Hoàn thành</MenuItem>
          <MenuItem value="FAILED_DELIVERY">Giao thất bại</MenuItem>
          <MenuItem value="PAYMENT_FAILED">Thanh toán thất bại</MenuItem>
          <MenuItem value="RETURNED">Đã hoàn trả</MenuItem>
          <MenuItem value="REFUNDED">Đã hoàn tiền</MenuItem>
          <MenuItem value="COMPLETED">Đã hoàn tất</MenuItem>
          <MenuItem value="CANCELLED">Đã hủy</MenuItem>
        </Select>

        <TextField
          label="Mã đơn"
          size="small"
          value={filters.orderId}
          onChange={e => handleFilterChange("orderId", e.target.value)}
          style={{ background: "white" }}
        />

        <TextField
          label="Ngày bắt đầu"
          type="date"
          size="small"
          value={filters.startDate}
          onChange={e => handleFilterChange("startDate", e.target.value)}
          InputLabelProps={{ shrink: true }}
          style={{ background: "white" }}
        />

        <TextField
          label="Ngày kết thúc"
          type="date"
          size="small"
          value={filters.endDate}
          onChange={e => handleFilterChange("endDate", e.target.value)}
          InputLabelProps={{ shrink: true }}
          style={{ background: "white" }}
        />
      </form>

      {/* Table */}
      <TableContainer component={Paper} elevation={3}>
        <Table>
          <TableHead style={{ background: "#f5f5f5" }}>
            <TableRow>
              <TableCell><strong>Mã đơn</strong></TableCell>
              <TableCell><strong>Khách hàng</strong></TableCell>
              <TableCell><strong>Tổng tiền hàng</strong></TableCell>
              <TableCell><strong>Thực thu</strong></TableCell>
              <TableCell><strong>Trạng thái</strong></TableCell>
              <TableCell><strong>Ngày tạo</strong></TableCell>
              <TableCell align="center"><strong>Thao tác</strong></TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">Không có dữ liệu</TableCell>
              </TableRow>
            ) : (
              orders.map(order => {
                const final = order.finalAmount ?? order.totalAmount;

                return (
                  <TableRow key={order.orderId} hover>
                    <TableCell>#{order.orderId}</TableCell>

                    <TableCell>
                      <div>{order.fullName}</div>
                      <div style={{ fontSize: "0.8em", color: "#666" }}>{order.phoneNumber}</div>
                    </TableCell>

                    {/* Total */}
                    <TableCell
                      style={{
                        textDecoration: final < order.totalAmount ? "line-through" : "none",
                        color: "#777"
                      }}
                    >
                      {order.totalAmount.toLocaleString("vi-VN")} đ
                    </TableCell>

                    {/* Final */}
                    <TableCell style={{ fontWeight: "bold", color: "#d32f2f" }}>
                      {final.toLocaleString("vi-VN")} đ
                    </TableCell>

                    <TableCell>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: 4,
                        background:
                          order.orderStatus === "PENDING" ? "#fff3e0" :
                          order.orderStatus === "DELIVERED" ? "#e8f5e9" : "#e3f2fd",
                        color:
                          order.orderStatus === "PENDING" ? "#ef6c00" :
                          order.orderStatus === "DELIVERED" ? "#2e7d32" : "#1565c0",
                        fontWeight: 500
                      }}>
                        {order.orderStatus}
                        {order.paymentStatus && (
                          <small style={{ display: "block", fontWeight: 400 }}>
                            Thanh toán: {order.paymentStatus}
                          </small>
                        )}
                      </span>
                    </TableCell>

                    <TableCell>
                      {new Date(order.createdAt).toLocaleDateString("vi-VN")}
                    </TableCell>

                    <TableCell align="center">
                      <div style={{ display: "flex", gap: 8, justifyContent: "center" }}>
                        <Button variant="outlined" size="small" onClick={() => showDetails(order)}>
                          Chi tiết
                        </Button>

                        {order.orderStatus === "PENDING" && (
                          <Button
                            variant="contained"
                            size="small"
                            onClick={() => handleSetStatus(order.orderId, "CONFIRMED", "Đã xác nhận đơn hàng")}
                          >
                            Xác nhận
                          </Button>
                        )}

                        {order.orderStatus === "PAID" && (
                          <Button
                            variant="contained"
                            color="secondary"
                            size="small"
                            onClick={() => handleSetStatus(order.orderId, "PROCESSING", "Đã bắt đầu xử lý đơn")}
                          >
                            Bắt đầu xử lý
                          </Button>
                        )}

                        {order.orderStatus === "CONFIRMED" && order.paymentType === "CASH" && (
                          <Button
                            variant="contained"
                            color="secondary"
                            size="small"
                            onClick={() => handleSetStatus(order.orderId, "PROCESSING", "Đã bắt đầu xử lý đơn COD")}
                          >
                            Bắt đầu xử lý COD
                          </Button>
                        )}

                        {order.orderStatus === "PROCESSING" && (
                          <Button
                            variant="contained"
                            size="small"
                            onClick={() => handleSetStatus(order.orderId, "INVENTORY", "Đơn hàng đang được kiểm kho")}
                          >
                            Kiểm kho
                          </Button>
                        )}

                        {order.orderStatus === "INVENTORY" && (
                          <Button
                            variant="contained"
                            size="small"
                            onClick={() => handleSetStatus(order.orderId, "READY_TO_SHIP", "Đơn hàng sẵn sàng bàn giao")}
                          >
                            Sẵn sàng bàn giao
                          </Button>
                        )}

                        {order.orderStatus === "RETURNED" && (
                          <Button
                            variant="contained"
                            size="small"
                            onClick={() => handleSetStatus(order.orderId, "REFUNDED", "Đã hoàn tiền đơn hàng")}
                          >
                            Hoàn tiền
                          </Button>
                        )}

                        {order.orderStatus === "CANCELLED" && order.paymentStatus === "PAID" && (
                          <Button
                            variant="contained"
                            size="small"
                            onClick={() => handleSetStatus(order.orderId, "REFUNDED", "Đã hoàn tiền đơn hàng")}
                          >
                            Hoàn tiền
                          </Button>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>

        </Table>
      </TableContainer>
    </div>
  );
};

export default OrderManager;
