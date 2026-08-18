import React, { useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button, Alert, CircularProgress, Stack } from '@mui/material';
import { toast } from 'react-toastify';

interface SellerApplyModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: {
    shopName: string;
    phone: string;
    identityCard: string;
    description: string;
    address: string;
  }) => Promise<void>;
  isSubmitting: boolean;
}

const SellerApplyModal: React.FC<SellerApplyModalProps> = ({ open, onClose, onSubmit, isSubmitting }) => {
  const [formData, setFormData] = useState({
    shopName: '',
    phone: '',
    identityCard: '',
    description: '',
    address: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.shopName.trim()) {
      newErrors.shopName = 'Tên shop không được để trống';
    }
    if (!formData.phone.trim()) {
      newErrors.phone = 'Số điện thoại không được để trống';
    } else if (!/^\d{10,11}$/.test(formData.phone.replace(/\D/g, ''))) {
      newErrors.phone = 'Số điện thoại phải có 10-11 chữ số';
    }
    if (!formData.identityCard.trim()) {
      newErrors.identityCard = 'CCCD/CMND không được để trống';
    } else if (!/^\d{9,12}$/.test(formData.identityCard.replace(/\D/g, ''))) {
      newErrors.identityCard = 'CCCD/CMND phải có 9-12 chữ số';
    }
    if (!formData.description.trim()) {
      newErrors.description = 'Mô tả shop không được để trống';
    } else if (formData.description.trim().length < 10) {
      newErrors.description = 'Mô tả phải ít nhất 10 ký tự';
    }


    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
      await onSubmit(formData);
      setFormData({ shopName: '', phone: '', identityCard: '', description: '', address: '' });
      setErrors({});
      onClose();
    } catch (err: any) {
      console.error('Form submit error:', err);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle className="bg-gradient-to-r from-blue-600 to-blue-700 text-white font-bold text-lg">
        📝 Đăng ký Business Seller
      </DialogTitle>

      <DialogContent className="pt-6">
        <Alert severity="info" className="mb-4">
          Vui lòng cung cấp thông tin đầy đủ và chính xác. Admin sẽ kiểm duyệt trong 24-48 giờ.
        </Alert>

        {/* Bọc các input bằng Stack để giãn dòng đều và dễ điều chỉnh */}
        <Stack spacing={3}>
          <TextField
            fullWidth
            label="Tên Shop"
            name="shopName"
            value={formData.shopName}
            onChange={handleChange}
            placeholder="VD: Cửa hàng Minh XYZ"
            error={!!errors.shopName}
            helperText={errors.shopName || ''}
            disabled={isSubmitting}
            variant="outlined"
            size="small"
          />

          <TextField
            fullWidth
            label="Số Điện Thoại"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            placeholder="VD: 0912345678"
            error={!!errors.phone}
            helperText={errors.phone || ''}
            disabled={isSubmitting}
            variant="outlined"
            size="small"
            inputProps={{ maxLength: 11 }}
          />

          <TextField
            fullWidth
            label="CCCD / CMND"
            name="identityCard"
            value={formData.identityCard}
            onChange={handleChange}
            placeholder="VD: 123456789012"
            error={!!errors.identityCard}
            helperText={errors.identityCard || ''}
            disabled={isSubmitting}
            variant="outlined"
            size="small"
            inputProps={{ maxLength: 12 }}
          />

          

          <TextField
            fullWidth
            label="Mô Tả Shop"
            name="description"
            value={formData.description}
            onChange={handleChange}
            placeholder="VD: Chuyên bán điện thoại xịn, bảo hành 12 tháng..."
            error={!!errors.description}
            helperText={errors.description || `${formData.description.length}/500`}
            disabled={isSubmitting}
            variant="outlined"
            multiline
            rows={4}
            inputProps={{ maxLength: 500 }}
          />
        </Stack>
      </DialogContent>

      <DialogActions className="p-4 border-t">
        <button
          onClick={onClose}
          disabled={isSubmitting}
          className="px-4 py-2 text-gray-700 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50"
        >
          Hủy
        </button>
        <button
          onClick={handleSubmit}
          disabled={isSubmitting}
          className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 flex items-center gap-2 disabled:opacity-50"
        >
          {isSubmitting ? (
            <>
              <CircularProgress size={16} color="inherit" />
              Đang gửi...
            </>
          ) : (
            '✓ Gửi Đơn'
          )}
        </button>
      </DialogActions>
    </Dialog>
  );
};

export default SellerApplyModal;
