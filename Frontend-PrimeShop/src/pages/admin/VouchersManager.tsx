import React, { useEffect, useState } from "react";
import api from "../../api/api";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  CircularProgress,
  Alert,
} from "@mui/material";
import Swal from "sweetalert2";

export interface Voucher {
  id: number;
  code: string;
  discountType: "PERCENT" | "FIXED";
  discountValue: number;
  minOrderValue: number;
  startDate: string;
  endDate: string;
  maxUsage: number;
  currentUsage: number;
  isActive: boolean;
}

const defaultVoucher: Omit<Voucher, "id" | "currentUsage"> = {
  code: "",
  discountType: "PERCENT",
  discountValue: 0,
  minOrderValue: 0,
  startDate: "",
  endDate: "",
  maxUsage: 1,
  isActive: true,
};

const VouchersManager: React.FC = () => {
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>("");
  const [openDialog, setOpenDialog] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedVoucher, setSelectedVoucher] = useState<Voucher | null>(null);
  const [form, setForm] = useState<typeof defaultVoucher>(defaultVoucher);
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  const fetchVouchers = async () => {
    setLoading(true);
    try {
      const res = await api.get<Voucher[]>("/vouchers/all");
      setVouchers(res.data);
      setError("");
    } catch (e) {
      setError("Không thể tải danh sách voucher.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVouchers();
  }, []);

  const openAddDialog = () => {
    setForm(defaultVoucher);
    setEditMode(false);
    setOpenDialog(true);
  };

  const openEditDialog = (voucher: Voucher) => {
    setForm({ ...voucher });
    setSelectedVoucher(voucher);
    setEditMode(true);
    setOpenDialog(true);
  };

  const closeDialog = () => {
    setOpenDialog(false);
    setForm(defaultVoucher);
    setSelectedVoucher(null);
    setEditMode(false);
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSwitchChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    checked: boolean
  ) => {
    setForm((prev) => ({ ...prev, [e.target.name]: checked }));
  };

  const formatDate = (date: string) => (date ? `${date}T00:00:00` : "");

  const handleSubmit = async () => {
    if (!form.code.trim()) {
      Swal.fire("Lỗi", "Mã voucher không được để trống!", "error");
      return;
    }
    const payload = {
      ...form,
      startDate: formatDate(form.startDate),
      endDate: formatDate(form.endDate),
    };
    try {
      if (editMode && selectedVoucher) {
        await api.put(`/vouchers/${selectedVoucher.id}`, payload);
        Swal.fire("Thành công", "Voucher đã được cập nhật.", "success");
      } else {
        await api.post("/vouchers", payload);
        Swal.fire("Thành công", "Voucher mới đã được tạo.", "success");
      }
      closeDialog();
      fetchVouchers();
    } catch (err) {
      Swal.fire("Lỗi", "Không thể lưu voucher.", "error");
    }
  };

  const handleDelete = async (id: number) => {
    const result = await Swal.fire({
      title: "Xác nhận xóa?",
      text: "Bạn có chắc muốn xóa voucher này?",
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Xóa",
      cancelButtonText: "Hủy",
    });
    if (result.isConfirmed) {
      try {
        await api.delete(`/vouchers/${id}`);
        Swal.fire("Đã xóa", "Voucher đã bị xóa.", "success");
        fetchVouchers();
      } catch (err) {
        Swal.fire("Lỗi", "Không thể xóa voucher.", "error");
      }
    }
  };

  // Phân trang
  const pagedVouchers = vouchers.slice(
    currentPage * pageSize,
    (currentPage + 1) * pageSize
  );
  const totalPages = Math.ceil(vouchers.length / pageSize);

  return (
    <div style={{ padding: 24 }}>
      <h2>Quản lý mã giảm giá</h2>
      <Button variant="contained" onClick={openAddDialog} sx={{ mb: 2 }}>
        Thêm mã giảm giá
      </Button>

      {loading ? (
        <CircularProgress />
      ) : error ? (
        <Alert severity="error">{error}</Alert>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Mã</TableCell>
                <TableCell>Loại</TableCell>
                <TableCell>Giá trị</TableCell>
                <TableCell>Đơn tối thiểu</TableCell>
                <TableCell>Ngày bắt đầu</TableCell>
                <TableCell>Ngày kết thúc</TableCell>
                <TableCell>Lượt dùng</TableCell>
                <TableCell>Kích hoạt</TableCell>
                <TableCell>Hành động</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {pagedVouchers.map((v) => (
                <TableRow key={v.id}>
                  <TableCell>{v.code}</TableCell>
                  <TableCell>{v.discountType === "PERCENT" ? "Phần trăm" : "Cố định"}</TableCell>
                  <TableCell>
                    {v.discountType === "PERCENT"
                      ? `${v.discountValue}%`
                      : `${v.discountValue.toLocaleString()} VNĐ`}
                  </TableCell>
                  <TableCell>{v.minOrderValue.toLocaleString()} VNĐ</TableCell>
                  <TableCell>{new Date(v.startDate).toLocaleDateString()}</TableCell>
                  <TableCell>{new Date(v.endDate).toLocaleDateString()}</TableCell>
                  <TableCell>
                    {v.currentUsage} / {v.maxUsage}
                  </TableCell>
                  <TableCell>{v.isActive ? "Có" : "Không"}</TableCell>
                  <TableCell>
                    <Button size="small" onClick={() => openEditDialog(v)}>
                      Sửa
                    </Button>
                    <Button size="small" color="error" onClick={() => handleDelete(v.id)}>
                      Xóa
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <div style={{ display: "flex", justifyContent: "center", margin: 16 }}>
            <Button
              onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
              disabled={currentPage === 0}
            >
              Trang trước
            </Button>
            {Array.from({ length: totalPages }, (_, i) => (
              <Button
                key={i}
                onClick={() => setCurrentPage(i)}
                variant={currentPage === i ? "contained" : "outlined"}
                sx={{ mx: 0.5 }}
              >
                {i + 1}
              </Button>
            ))}
            <Button
              onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1))}
              disabled={currentPage === totalPages - 1}
            >
              Trang sau
            </Button>
          </div>
        </TableContainer>
      )}

      {/* Dialog thêm/sửa voucher */}
      <Dialog open={openDialog} onClose={closeDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editMode ? "Chỉnh sửa voucher" : "Thêm voucher"}</DialogTitle>
        <DialogContent dividers>
          <TextField
            label="Mã voucher"
            name="code"
            value={form.code}
            onChange={handleInputChange}
            fullWidth
            margin="normal"
          />
          <FormControl fullWidth margin="normal">
            <InputLabel id="discount-type">Loại giảm giá</InputLabel>
            <Select
              labelId="discount-type"
              name="discountType"
              value={form.discountType}
              label="Loại giảm giá"
              onChange={(e) =>
                setForm((prev) => ({
                  ...prev,
                  discountType: e.target.value as "PERCENT" | "FIXED",
                }))
              }
            >
              <MenuItem value="PERCENT">Phần trăm (%)</MenuItem>
              <MenuItem value="FIXED">Số tiền (VNĐ)</MenuItem>
            </Select>
          </FormControl>
          <TextField
            label="Giá trị giảm"
            name="discountValue"
            type="number"
            value={form.discountValue}
            onChange={handleInputChange}
            fullWidth
            margin="normal"
          />
          <TextField
            label="Đơn tối thiểu"
            name="minOrderValue"
            type="number"
            value={form.minOrderValue}
            onChange={handleInputChange}
            fullWidth
            margin="normal"
          />
          <TextField
            label="Ngày bắt đầu"
            name="startDate"
            type="date"
            value={form.startDate}
            onChange={handleInputChange}
            fullWidth
            margin="normal"
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            label="Ngày kết thúc"
            name="endDate"
            type="date"
            value={form.endDate}
            onChange={handleInputChange}
            fullWidth
            margin="normal"
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            label="Số lượt dùng tối đa"
            name="maxUsage"
            type="number"
            value={form.maxUsage}
            onChange={handleInputChange}
            fullWidth
            margin="normal"
          />
          <FormControlLabel
            control={
              <Switch checked={form.isActive} name="isActive" onChange={handleSwitchChange} />
            }
            label="Kích hoạt"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDialog}>Hủy</Button>
          <Button variant="contained" onClick={handleSubmit}>
            {editMode ? "Lưu" : "Thêm"}
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default VouchersManager;