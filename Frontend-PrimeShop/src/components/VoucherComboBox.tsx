import React, { useState, useEffect } from 'react';
import {
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Box,
    Typography,
    Alert,
    Button,
    Chip,
    CircularProgress
} from '@mui/material';
import {
    Discount,
    Error
} from '@mui/icons-material';
import api from '../api/api';

interface Voucher {
    id: number;
    code: string;
    discountType: 'PERCENT' | 'FIXED' | 'FREESHIP';
    discountValue: number;
    minOrderValue: number;
    startDate: string;
    endDate: string;
    maxUsage: number;
    currentUsage: number;
    isActive: boolean;
    isValid: boolean;
    remainingUsage: number;
}

interface VoucherComboBoxProps {
    orderValue: number;
    onVoucherApplied: (voucher: Voucher) => void;
    onVoucherRemoved: (voucher: Voucher) => void;
    appliedVouchers: Voucher[];
}

const VoucherComboBox: React.FC<VoucherComboBoxProps> = ({
    orderValue,
    onVoucherApplied,
    onVoucherRemoved,
    appliedVouchers
}) => {
    const [vouchers, setVouchers] = useState<Voucher[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [selectedVoucherId, setSelectedVoucherId] = useState<string>('');
    const [voucherApplying, setVoucherApplying] = useState(false);

    useEffect(() => {
        fetchAvailableVouchers();
    }, [orderValue]);

    const fetchAvailableVouchers = async () => {
        try {
            setLoading(true);
            setError('');
            const response = await api.get<Voucher[]>(
                `/vouchers/valid-for-order?orderValue=${orderValue}`
            );
            setVouchers(response.data);
        } catch (err) {
            setError('Không thể tải danh sách mã giảm giá');
        } finally {
            setLoading(false);
        }
    };

    const handleVoucherSelect = async () => {
        if (!selectedVoucherId) return;
        const selectedVoucher = vouchers.find(v => v.id.toString() === selectedVoucherId);
        if (!selectedVoucher) return;
        setVoucherApplying(true);
        try {
            await onVoucherApplied(selectedVoucher);
            setSelectedVoucherId('');
            setError('');
        } catch (err) {
            setError('Không thể áp dụng voucher');
        } finally {
            setVoucherApplying(false);
        }
    };

    const handleRemoveVoucher = (voucher: Voucher) => {
        onVoucherRemoved(voucher);
    };

    const formatDiscountText = (voucher: Voucher) => {
        if (voucher.discountType === 'FREESHIP') {
            return 'Miễn phí vận chuyển';
        } else if (voucher.discountType === 'PERCENT') {
            return `Giảm ${voucher.discountValue}%`;
        } else {
            return `Giảm ${voucher.discountValue.toLocaleString('vi-VN')} VNĐ`;
        }
    };

    const formatVoucherLabel = (voucher: Voucher) => {
        const discountText = formatDiscountText(voucher);
        const minOrderText = `(Đơn từ ${voucher.minOrderValue.toLocaleString('vi-VN')} VNĐ)`;
        return `${voucher.code} - ${discountText} ${minOrderText}`;
    };

    return (
        <Box sx={{ mb: 2, width: '100%' }}>
            <Typography
                variant="h6"
                sx={{
                    mb: 1.5,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1,
                    fontWeight: 600,
                    color: '#2563eb',
                }}
            >
                <Discount color="primary" />
                Mã giảm giá
            </Typography>

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <FormControl fullWidth size="small">
                        <InputLabel sx={{ color: '#6b7280', '&.Mui-focused': { color: '#2563eb' } }}>
                            {loading ? 'Đang tải...' : 'Chọn mã giảm giá'}
                        </InputLabel>
                        <Select
                            value={selectedVoucherId}
                            onChange={(e) => setSelectedVoucherId(e.target.value)}
                            disabled={loading || vouchers.length === 0 || voucherApplying}
                            sx={{
                                '& .MuiOutlinedInput-notchedOutline': {
                                    borderColor: '#93c5fd',
                                },
                                '&:hover .MuiOutlinedInput-notchedOutline': {
                                    borderColor: '#2563eb',
                                },
                                '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                                    borderColor: '#2563eb',
                                    boxShadow: '0 0 0 3px rgba(37, 99, 235, 0.1)',
                                },
                                borderRadius: '8px',
                                backgroundColor: '#fff',
                            }}
                            startAdornment={
                                loading ? (
                                    <CircularProgress size={20} sx={{ mr: 1 }} />
                                ) : (
                                    <Discount sx={{ mr: 1, color: '#6b7280' }} />
                                )
                            }
                        >
                            {vouchers.length === 0 && !loading && (
                                <MenuItem disabled>
                                    Không có mã giảm giá khả dụng
                                </MenuItem>
                            )}
                            {vouchers
                                .filter(v => !appliedVouchers.some(av => av.id === v.id))
                                .map((voucher) => (
                                    <MenuItem key={voucher.id} value={voucher.id.toString()}>
                                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                                            <Typography variant="body2" fontWeight="bold">
                                                {voucher.code}
                                            </Typography>
                                            <Typography variant="caption" color="text.secondary">
                                                {formatVoucherLabel(voucher)}
                                            </Typography>
                                            <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5 }}>
                                                <Chip
                                                    label={`Còn ${voucher.remainingUsage} lượt`}
                                                    size="small"
                                                    color={voucher.remainingUsage <= 5 ? 'warning' : 'default'}
                                                />
                                            </Box>
                                        </Box>
                                    </MenuItem>
                                ))}
                        </Select>
                    </FormControl>
                    <Button
                        variant="contained"
                        onClick={handleVoucherSelect}
                        disabled={loading || !selectedVoucherId || voucherApplying}
                        sx={{
                            minWidth: 100,
                            borderRadius: '8px',
                            backgroundColor: '#2563eb',
                            '&:hover': { backgroundColor: '#1e40af' },
                            '&:disabled': { backgroundColor: '#93c5fd' },
                        }}
                    >
                        {voucherApplying ? <CircularProgress size={20} color="inherit" /> : 'Áp dụng'}
                    </Button>
                </Box>

                {appliedVouchers.length > 0 && (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                        {appliedVouchers.map(voucher => (
                            <Chip
                                key={voucher.id}
                                label={formatVoucherLabel(voucher)}
                                color={voucher.discountType === 'FREESHIP' ? 'success' : 'primary'}
                                onDelete={() => handleRemoveVoucher(voucher)}
                                sx={{ mb: 1, borderRadius: '16px', fontWeight: 500 }}
                            />
                        ))}
                    </Box>
                )}

                {error && (
                    <Alert severity="error" sx={{ mt: 1 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Error />
                            {error}
                        </Box>
                    </Alert>
                )}

                <Typography variant="caption" color="text.secondary">
                    * Chỉ hiển thị mã giảm giá có thể áp dụng cho đơn hàng từ {orderValue.toLocaleString('vi-VN')} VNĐ
                </Typography>
            </Box>
        </Box>
    );
};

export default VoucherComboBox;