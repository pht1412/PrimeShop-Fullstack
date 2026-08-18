import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import api from '../../api/api';
import './delivery.css';

interface Order {
  orderId: string;
  fullName: string;
  orderStatus: string;
  // Các trường khác ...
}

interface UpdateStatusModalProps {
  order: Order | null;
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

// Mảng trạng thái tương ứng nghiệp vụ
const STATUS_REQUIRING_LOCATION = ['INVENTORY'];
const STATUS_REQUIRING_NOTE = ['READY_TO_SHIP', 'PROCESSING', 'SHIPPING'];
const STATUS_REQUIRING_REASON = ['DELIVERED_FAILED', 'RETURNED', 'REFUNDED'];
const STATUS_NO_INPUT = ['DELIVERED'];

const UpdateStatusModal: React.FC<UpdateStatusModalProps> = ({
  order,
  open,
  onClose,
  onSuccess,
}) => {
  const [status, setStatus] = useState(order?.orderStatus || '');
  const [location, setLocation] = useState('');
  const [note, setNote] = useState('');
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (order && open) {
      setStatus(order.orderStatus || '');
      setLocation('');
      setNote('');
      setReason('');
      setError(null);
    }
  }, [order, open]);

  const requiresLocation = STATUS_REQUIRING_LOCATION.includes(status);
  const requiresNote = STATUS_REQUIRING_NOTE.includes(status);
  const requiresReason = STATUS_REQUIRING_REASON.includes(status);
  const noInput = STATUS_NO_INPUT.includes(status);

  const validate = () => {
    if (!status) return false;
    if (requiresLocation && location.trim() === '') return false;
    if (requiresNote && note.trim() === '') return false;
    if (requiresReason && reason.trim() === '') return false;
    return true;
  };

  const handleSubmit = async () => {
    if (!order || !status) {
      setError('Vui lòng chọn trạng thái đơn hàng');
      return;
    }
    if (!validate()) {
      setError('Vui lòng điền đầy đủ thông tin bắt buộc');
      return;
    }

    const payload: any = { status };
    if (requiresLocation) payload.location = location.trim();
    if (requiresNote) payload.note = note.trim();
    if (requiresReason) payload.reason = reason.trim();

    try {
      setLoading(true);
      setError(null);

      await api.put(`/delivery/orders/${order.orderId}/status`, payload);

      onSuccess();
      onClose();

      setStatus('');
      setLocation('');
      setNote('');
      setReason('');
    } catch (err) {
      console.error('Lỗi cập nhật trạng thái:', err);
      setError('Không thể cập nhật trạng thái đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      onClose();
      setStatus('');
      setLocation('');
      setNote('');
      setReason('');
      setError(null);
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'INVENTORY':
        return 'Trong kho kiểm kê';
      case 'READY_TO_SHIP':
        return 'Chờ giao';
      case 'PROCESSING':
        return 'Đang xử lý';
      case 'SHIPPING':
        return 'Đang giao';
      case 'DELIVERED':
        return 'Đã giao';
      case 'DELIVERED_FAILED':
        return 'Giao thất bại';
      case 'RETURNED':
        return 'Đã trả hàng';
      case 'REFUNDED':
        return 'Đã hoàn tiền';
      default:
        return status;
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth className="delivery-dialog">
      <DialogTitle className="delivery-dialog-title">
        Cập nhật trạng thái đơn hàng #{order?.orderId}
      </DialogTitle>

      <DialogContent className="delivery-dialog-content">
        <Box mb={2}>
          <Typography variant="body2" color="text.secondary">
            Khách hàng: <strong>{order?.fullName}</strong>
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel>Trạng thái mới</InputLabel>
          <Select
            value={status}
            label="Trạng thái mới"
            onChange={(e) => {
              setStatus(e.target.value);
              // Reset các input phụ thuộc trạng thái
              setLocation('');
              setNote('');
              setReason('');
              setError(null);
            }}
          >
            <MenuItem value="INVENTORY">{getStatusLabel('INVENTORY')}</MenuItem>
            <MenuItem value="READY_TO_SHIP">{getStatusLabel('READY_TO_SHIP')}</MenuItem>
            <MenuItem value="PROCESSING">{getStatusLabel('PROCESSING')}</MenuItem>
            <MenuItem value="SHIPPING">{getStatusLabel('SHIPPING')}</MenuItem>
            <MenuItem value="DELIVERED">{getStatusLabel('DELIVERED')}</MenuItem>
            <MenuItem value="DELIVERED_FAILED">{getStatusLabel('DELIVERED_FAILED')}</MenuItem>
            <MenuItem value="RETURNED">{getStatusLabel('RETURNED')}</MenuItem>
            <MenuItem value="REFUNDED">{getStatusLabel('REFUNDED')}</MenuItem>
          </Select>
        </FormControl>

        {requiresLocation && (
          <TextField
            fullWidth
            label="Vị trí kho"
            placeholder="Nhập vị trí kho (ví dụ: Kho Hà Nội - Kệ A3)"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            multiline
            rows={2}
            required
            sx={{ mb: 2 }}
          />
        )}

        {requiresNote && (
          <TextField
            fullWidth
            label="Ghi chú"
            placeholder="Nhập ghi chú (ví dụ: Tài xế sẽ đến lấy trong 1-3 ngày)"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            multiline
            rows={3}
            required
            sx={{ mb: 2 }}
          />
        )}

        {noInput && (
          <Typography variant="body1" color="textSecondary" sx={{ mb: 2 }}>
            Hoàn thành đơn hàng, vui lòng đợi khách xác nhận đã nhận.
          </Typography>
        )}

        {requiresReason && (
          <TextField
            fullWidth
            label="Lý do"
            placeholder="Nhập lý do (ví dụ: Khách từ chối nhận hàng)"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            multiline
            rows={3}
            required
            sx={{ mb: 2 }}
          />
        )}
      </DialogContent>

      <DialogActions className="delivery-dialog-actions">
        <Button onClick={handleClose} className="btn-close" disabled={loading}>
          Hủy
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading || !status || !validate()}
          startIcon={loading ? <CircularProgress size={16} /> : null}
          className="btn-refresh"
        >
          {loading ? 'Đang cập nhật...' : 'Cập nhật'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default UpdateStatusModal;
