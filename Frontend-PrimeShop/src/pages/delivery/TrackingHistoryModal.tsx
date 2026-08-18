import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Chip,
  CircularProgress,
  Alert,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  useTheme // Import useTheme để xử lý màu an toàn
} from '@mui/material';
import {
  LocalShipping as LocalShippingIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Schedule as ScheduleIcon,
  LocationOn as LocationOnIcon,
  Storage as StorageIcon,
  Check as CheckIcon
} from '@mui/icons-material';
import api from '../../api/api';
import './delivery.css';

interface TrackingEvent {
  id: number;
  status: string;
  location: string;
  timestamp: string; 
}

interface TrackingHistoryModalProps {
  orderId: string | null;
  open: boolean;
  onClose: () => void;
}

// Định nghĩa type màu hợp lệ cho Chip của MUI
type MuiChipColor = 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning';

const TrackingHistoryModal: React.FC<TrackingHistoryModalProps> = ({
  orderId,
  open,
  onClose
}) => {
  const theme = useTheme(); // Hook để lấy theme
  const [trackingHistory, setTrackingHistory] = useState<TrackingEvent[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open && orderId) {
      fetchTrackingHistory();
    } else {
      setTrackingHistory([]);
      setError(null);
    }
  }, [open, orderId]);

  const fetchTrackingHistory = async () => {
    if (!orderId) return;
    try {
      setLoading(true);
      setError(null);
      const response = await api.get(`/delivery/orders/${orderId}/tracking`);
      if (Array.isArray(response.data)) {
        setTrackingHistory(response.data);
      } else {
        setTrackingHistory([]);
        setError('Dữ liệu lịch sử tracking không hợp lệ');
      }
    } catch (err) {
      console.error('Lỗi lấy lịch sử tracking:', err);
      setError('Không thể tải lịch sử vận chuyển');
      setTrackingHistory([]);
    } finally {
      setLoading(false);
    }
  };

  // FIX 1: Hàm trả về đúng Type màu của Chip (Thay 'grey' bằng 'default')
  const getChipColor = (status: string): MuiChipColor => {
    switch (status) {
      case 'PENDING':
        return 'default'; // Mặc định là màu xám nhạt
      case 'CONFIRMED':
      case 'SHIPPING':
        return 'info';
      case 'PAID':
        return 'primary';
      case 'PROCESSING':
      case 'READY_TO_SHIP':
        return 'warning';
      case 'INVENTORY':
        return 'secondary';
      case 'DELIVERED':
        return 'success';
      case 'FAILED_DELIVERY':
        return 'error';
      default:
        return 'default';
    }
  };

  // FIX 2: Hàm riêng để lấy mã Hex màu thực tế cho Icon (Tránh truy cập dynamic theme gây crash)
  const getIconHexColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return theme.palette.grey[500];
      case 'CONFIRMED':
      case 'SHIPPING':
        return theme.palette.info.main;
      case 'PAID':
        return theme.palette.primary.main;
      case 'PROCESSING':
      case 'READY_TO_SHIP':
        return theme.palette.warning.main;
      case 'INVENTORY':
        return theme.palette.secondary.main;
      case 'DELIVERED':
        return theme.palette.success.main;
      case 'FAILED_DELIVERY':
        return theme.palette.error.main;
      default:
        return theme.palette.grey[500];
    }
  };

  const getLabelByStatus = (status: string) => {
    switch (status) {
      case 'PENDING': return 'Chờ xử lý';
      case 'CONFIRMED': return 'Đã xác nhận';
      case 'PAID': return 'Đã thanh toán';
      case 'PROCESSING': return 'Đang xử lý';
      case 'INVENTORY': return 'Tồn kho';
      case 'READY_TO_SHIP': return 'Chờ giao';
      case 'SHIPPING': return 'Đang giao';
      case 'DELIVERED': return 'Đã giao';
      case 'FAILED_DELIVERY': return 'Giao thất bại';
      default: return status;
    }
  };

  const getIconByStatus = (status: string) => {
    switch (status) {
      case 'PENDING': return <ScheduleIcon />;
      case 'CONFIRMED': return <CheckIcon />;
      case 'PAID': return <CheckIcon />;
      case 'PROCESSING': return <CircularProgress size={20} color="inherit" />; // color inherit để ăn theo parent
      case 'INVENTORY': return <StorageIcon />;
      case 'READY_TO_SHIP': return <ScheduleIcon />;
      case 'SHIPPING': return <LocalShippingIcon />;
      case 'DELIVERED': return <CheckCircleIcon />;
      case 'FAILED_DELIVERY': return <ErrorIcon />;
      default: return <LocationOnIcon />;
    }
  };

  const formatDateTime = (timestamp: string) => {
    try {
        const dateObj = new Date(timestamp);
        return {
            date: dateObj.toLocaleDateString('vi-VN'),
            time: dateObj.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
        };
    } catch (e) {
        return { date: '-', time: '-' };
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      className="delivery-dialog"
    >
      <DialogTitle className="delivery-dialog-title">
        <Box display="flex" alignItems="center" gap={1}>
          <LocationOnIcon color="primary" />
          Lịch sử vận chuyển đơn hàng #{orderId}
        </Box>
      </DialogTitle>

      <DialogContent
        dividers
        className="delivery-dialog-content"
        style={{ minHeight: 300, maxHeight: '70vh' }}
      >
        {loading && (
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {!loading && !error && trackingHistory.length === 0 && (
          <Paper className="delivery-no-tracking" elevation={0}>
            <Typography variant="body1" color="textSecondary">
              Chưa có lịch sử vận chuyển cho đơn hàng này
            </Typography>
          </Paper>
        )}

        {!loading && !error && trackingHistory.length > 0 && (
          <List className="delivery-tracking-list">
            {trackingHistory.map((event, idx) => {
              if (!event || !event.status || !event.timestamp) return null;

              const { date, time } = formatDateTime(event.timestamp);
              const chipColor = getChipColor(event.status); // Màu cho Chip (Safe type)
              const iconColorHex = getIconHexColor(event.status); // Màu Hex cho Icon Box

              return (
                <React.Fragment key={event.id || idx}>
                  <ListItem alignItems="flex-start" className="delivery-tracking-item">
                    <ListItemIcon>
                        {/* FIX 3: Box style an toàn, dùng trực tiếp mã Hex */}
                      <Box
                        className="status-icon"
                        sx={{
                          width: 40,
                          height: 40,
                          borderRadius: '50%',
                          backgroundColor: iconColorHex, // Dùng hex trực tiếp
                          color: '#fff',
                          display: 'flex',
                          justifyContent: 'center',
                          alignItems: 'center',
                          boxShadow: `0 0 8px ${iconColorHex}66` // Thêm alpha cho shadow thủ công
                        }}
                      >
                        {getIconByStatus(event.status)}
                      </Box>
                    </ListItemIcon>
                    <ListItemText
                      primary={
                        <Box display="flex" alignItems="center" gap={1} mb={0.5}>
                          {/* FIX 4: Chip dùng đúng color type */}
                          <Chip
                            label={getLabelByStatus(event.status)}
                            color={chipColor} 
                            size="small"
                            variant="outlined" // Thêm outlined cho đẹp nếu muốn
                          />
                          <Typography variant="caption" color="textSecondary">
                            {date} lúc {time}
                          </Typography>
                        </Box>
                      }
                      secondary={
                        <Typography variant="body2" color="textSecondary">
                          {event.location || 'Không có thông tin vị trí'}
                        </Typography>
                      }
                    />
                  </ListItem>
                  {idx < trackingHistory.length - 1 && <Divider component="li" variant="inset" />}
                </React.Fragment>
              );
            })}
          </List>
        )}
      </DialogContent>

      <DialogActions className="delivery-dialog-actions">
        <Button onClick={onClose} className="btn-close">
          Đóng
        </Button>
        {trackingHistory.length > 0 && (
          <Button onClick={fetchTrackingHistory} disabled={loading} className="btn-refresh">
            Làm mới
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default TrackingHistoryModal;