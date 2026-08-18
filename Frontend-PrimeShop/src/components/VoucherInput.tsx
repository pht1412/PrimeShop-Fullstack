import React, { useState } from 'react';
import { 
    TextField, 
    Button, 
    Box, 
    Typography, 
    Alert, 
    Chip,
    CircularProgress
} from '@mui/material';
import { Discount, CheckCircle, Error, LocalShipping } from '@mui/icons-material';
import axios from 'axios';

interface VoucherInputProps {
    orderValue: number;
    onVoucherApplied: (discountAmount: number, voucherCode: string, discountType: 'PERCENT' | 'FIXED' | 'FREESHIP', discountValue: number) => void;
    onVoucherRemoved: () => void;
    appliedVoucher?: {
        code: string;
        discountAmount: number;
        discountType: 'PERCENT' | 'FIXED' | 'FREESHIP';
        discountValue: number;
    };
}

interface VoucherValidationResponse {
    valid: boolean;
    discountAmount: number;
    discountType: 'PERCENT' | 'FIXED' | 'FREESHIP';
    discountValue: number;
    message: string;
}

const VoucherInput: React.FC<VoucherInputProps> = ({
    orderValue,
    onVoucherApplied,
    onVoucherRemoved,
    appliedVoucher
}) => {
    const [voucherCode, setVoucherCode] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleApplyVoucher = async () => {
        if (!voucherCode.trim()) {
            setError('Vui lòng nhập mã giảm giá');
            return;
        }

        setLoading(true);
        setError('');
        setSuccess('');

        try {
            const response = await axios.post<VoucherValidationResponse>(
                'https://backend-primeshop.onrender.com/api/vouchers/validate',
                {
                    code: voucherCode.trim().toUpperCase(),
                    orderValue: orderValue
                }
            );

            const { valid, discountAmount, discountType, discountValue, message } = response.data;

            if (valid) {
                setSuccess(message);
                onVoucherApplied(discountAmount, voucherCode.trim().toUpperCase(), discountType, discountValue);
                setVoucherCode('');
            } else {
                setError(message);
            }
        } catch (err) {
            setError('Có lỗi xảy ra khi kiểm tra mã giảm giá');
            console.error('Voucher validation error:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleRemoveVoucher = () => {
        onVoucherRemoved();
        setSuccess('');
        setError('');
    };

    const formatDiscountText = () => {
        if (!appliedVoucher) return '';
        if (appliedVoucher.discountType === 'PERCENT') {
            return `Giảm ${appliedVoucher.discountValue}% (${appliedVoucher.discountAmount.toLocaleString('vi-VN')} VNĐ)`;
        } else if (appliedVoucher.discountType === 'FIXED') {
            return `Giảm ${appliedVoucher.discountAmount.toLocaleString('vi-VN')} VNĐ`;
        } else if (appliedVoucher.discountType === 'FREESHIP') {
            return `Miễn phí vận chuyển (${appliedVoucher.discountAmount.toLocaleString('vi-VN')} VNĐ)`;
        }
        return '';
    };

    return (
        <Box sx={{ mb: 2 }}>
            <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                <Discount color="primary" />
                Mã giảm giá
            </Typography>

            {appliedVoucher ? (
                <Box sx={{ mb: 2 }}>
                    <Alert 
                        severity="success" 
                        action={
                            <Button 
                                color="inherit" 
                                size="small" 
                                onClick={handleRemoveVoucher}
                            >
                                Xóa
                            </Button>
                        }
                    >
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {appliedVoucher.discountType === 'FREESHIP' ? <LocalShipping /> : <CheckCircle />}
                            <Typography variant="body2">
                                Đã áp dụng mã: <strong>{appliedVoucher.code}</strong>
                            </Typography>
                        </Box>
                        <Typography variant="body2" sx={{ mt: 0.5 }}>
                            {formatDiscountText()}
                        </Typography>
                    </Alert>
                </Box>
            ) : (
                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                    <TextField
                        fullWidth
                        size="small"
                        placeholder="Nhập mã giảm giá"
                        value={voucherCode}
                        onChange={(e) => setVoucherCode(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleApplyVoucher()}
                        disabled={loading}
                        InputProps={{
                            startAdornment: <Discount sx={{ mr: 1, color: 'text.secondary' }} />
                        }}
                    />
                    <Button
                        variant="contained"
                        onClick={handleApplyVoucher}
                        disabled={loading || !voucherCode.trim()}
                        sx={{ minWidth: 100 }}
                    >
                        {loading ? <CircularProgress size={20} /> : 'Áp dụng'}
                    </Button>
                </Box>
            )}

            {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Error />
                        {error}
                    </Box>
                </Alert>
            )}

            {success && !appliedVoucher && (
                <Alert severity="success" sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <CheckCircle />
                        {success}
                    </Box>
                </Alert>
            )}

            {/* Hiển thị thông tin đơn hàng tối thiểu */}
            <Typography variant="caption" color="text.secondary">
                * Áp dụng cho đơn hàng từ {orderValue.toLocaleString('vi-VN')} VNĐ
            </Typography>
        </Box>
    );
};

export default VoucherInput;