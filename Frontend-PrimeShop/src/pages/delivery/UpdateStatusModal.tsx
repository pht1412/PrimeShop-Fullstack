import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Checkbox,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Typography,
  Alert,
  CircularProgress,
  FormControlLabel,
} from '@mui/material';
import api from '../../api/api';
import './delivery.css';

interface Order {
  orderId: string;
  fullName: string;
  orderStatus: string;
  paymentType?: string;
  allowedDeliveryStatuses?: string[];
}

interface UpdateStatusModalProps {
  order: Order | null;
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const STATUS_LABELS: Record<string, string> = {
  READY_TO_SHIP: 'Sẵn sàng bàn giao',
  SHIPPED: 'Đã bàn giao cho đơn vị vận chuyển',
  DELIVERED: 'Giao thành công',
  FAILED_DELIVERY: 'Giao thất bại',
  RETURNED: 'Hoàn trả',
};

const UpdateStatusModal: React.FC<UpdateStatusModalProps> = ({
  order,
  open,
  onClose,
  onSuccess,
}) => {
  const [status, setStatus] = useState('');
  const [location, setLocation] = useState('');
  const [note, setNote] = useState('');
  const [paymentCollected, setPaymentCollected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (order && open) {
      setStatus('');
      setLocation('');
      setNote('');
      setPaymentCollected(false);
      setError(null);
    }
  }, [order, open]);

  const requiresNote = status === 'FAILED_DELIVERY' || status === 'RETURNED';
  const allowedNextStatuses = order?.allowedDeliveryStatuses ?? [];

  const validate = () => {
    if (!allowedNextStatuses.includes(status)) return false;
    if (requiresNote && note.trim() === '') return false;
    if (status === 'DELIVERED' && order?.paymentType === 'CASH' && !paymentCollected) return false;
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

    const payload = {
      status,
      location: location.trim() || undefined,
      note: note.trim() || undefined,
      paymentCollected,
    };

    try {
      setLoading(true);
      setError(null);

      await api.put(`/delivery/orders/${order.orderId}/status`, payload);

      onSuccess();
      onClose();

      setStatus('');
      setLocation('');
      setNote('');
      setPaymentCollected(false);
    } catch (err) {
      console.error('Lỗi cập nhật trạng thái:', err);
      const responseError = err as {
        response?: { data?: { error?: string; message?: string } };
      };
      setError(
        responseError.response?.data?.error
          ?? responseError.response?.data?.message
          ?? 'Không thể cập nhật trạng thái đơn hàng'
      );
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
      setPaymentCollected(false);
      setError(null);
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'READY_TO_SHIP':
        return 'Sẵn sàng bàn giao';
      case 'SHIPPED':
        return 'Đã bàn giao cho đơn vị vận chuyển';
      case 'DELIVERED':
        return 'Giao thành công';
      case 'FAILED_DELIVERY':
        return 'Giao thất bại';
      case 'RETURNED':
        return 'Hoàn trả';
      default:
        return STATUS_LABELS[status] ?? status;
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
              setPaymentCollected(false);
              setError(null);
            }}
          >
            <MenuItem value="" disabled>Chọn trạng thái tiếp theo</MenuItem>
            {allowedNextStatuses.map(nextStatus => (
              <MenuItem key={nextStatus} value={nextStatus}>
                {getStatusLabel(nextStatus)}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {(status === 'SHIPPED' || status === 'DELIVERED') && (
          <TextField
            fullWidth
            label="Vị trí cập nhật"
            placeholder="Nhập vị trí hiện tại (không bắt buộc)"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            sx={{ mb: 2 }}
          />
        )}

        {requiresNote && (
          <TextField
            fullWidth
            label="Lý do"
            placeholder="Nhập lý do giao thất bại hoặc hoàn hàng"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            multiline
            rows={3}
            required
            sx={{ mb: 2 }}
          />
        )}

        {status === 'DELIVERED' && order?.paymentType === 'CASH' && (
          <FormControlLabel
            control={
              <Checkbox
                checked={paymentCollected}
                onChange={(event) => setPaymentCollected(event.target.checked)}
              />
            }
            label="Đã thu tiền COD từ khách hàng"
          />
        )}

        {allowedNextStatuses.length === 0 && (
          <Typography variant="body1" color="textSecondary" sx={{ mb: 2 }}>
            Đơn hàng này hiện không có bước vận chuyển tiếp theo.
          </Typography>
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
