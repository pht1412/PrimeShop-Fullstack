import React, { useState, useEffect } from 'react';
import {
    Box,
    Card,
    CardContent,
    Typography,
    Chip,
    Grid,
    CircularProgress,
    Alert,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button
} from '@mui/material';
import {
    Discount,
    AccessTime,
    People,
    CheckCircle,
    Info
} from '@mui/icons-material';
import axios from 'axios';

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

interface VoucherListProps {
    showAllVouchers?: boolean;
}

const VoucherList: React.FC<VoucherListProps> = ({
    showAllVouchers = true
}) => {
    const [vouchers, setVouchers] = useState<Voucher[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [selectedVoucherDetails, setSelectedVoucherDetails] = useState<Voucher | null>(null);
    const [dialogOpen, setDialogOpen] = useState(false);

    useEffect(() => {
        fetchVouchers();
    }, []);

    const fetchVouchers = async () => {
        try {
            setLoading(true);
            let response;
            if (showAllVouchers) {
                // Lấy tất cả voucher đang hoạt động
                response = await axios.get<Voucher[]>(
                    'https://localhost:8080/api/vouchers'
                );
            } else {
                // Lấy voucher cho đơn hàng cụ thể (nếu cần)
                response = await axios.get<Voucher[]>(
                    'https://localhost:8080/api/vouchers/valid-for-order?orderValue=0'
                );
            }
            setVouchers(response.data);
            setError('');
        } catch (err) {
            setError('Không thể tải danh sách mã giảm giá');
            console.error('Error fetching vouchers:', err);
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('vi-VN');
    };

    const formatDiscountText = (voucher: Voucher) => {
        if (voucher.discountType === 'PERCENT') {
            return `Giảm ${voucher.discountValue}%`;
        } else {
            return `Giảm ${voucher.discountValue.toLocaleString('vi-VN')} VNĐ`;
        }
    };

    const getRemainingDays = (endDate: string) => {
        const end = new Date(endDate);
        const now = new Date();
        const diffTime = end.getTime() - now.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays;
    };

    const handleVoucherClick = (voucher: Voucher) => {
        setSelectedVoucherDetails(voucher);
        setDialogOpen(true);
    };

    const isVoucherExpired = (voucher: Voucher) => {
        return new Date(voucher.endDate) < new Date();
    };

    const isVoucherOutOfUsage = (voucher: Voucher) => {
        return voucher.currentUsage >= voucher.maxUsage;
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return (
            <Alert severity="error" sx={{ mb: 2 }}>
                {error}
            </Alert>
        );
    }

    if (vouchers.length === 0) {
        return (
            <Box sx={{ textAlign: 'center', p: 3 }}>
                <Discount sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                <Typography variant="h6" color="text.secondary" gutterBottom>
                    Không có mã giảm giá khả dụng
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Hiện tại không có mã giảm giá nào đang hoạt động
                </Typography>
            </Box>
        );
    }

    return (
        <Box>
            <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                <Discount color="primary" />
                Danh sách mã giảm giá ({vouchers.length})
            </Typography>

            <Grid container spacing={2}>
                {vouchers.map((voucher) => {
                    const remainingDays = getRemainingDays(voucher.endDate);
                    const remainingUsage = voucher.remainingUsage;
                    const expired = isVoucherExpired(voucher);
                    const outOfUsage = isVoucherOutOfUsage(voucher);
                    const isUsable = !expired && !outOfUsage && voucher.isActive;

                    return (
                        <Grid item xs={12} sm={6} md={4} key={voucher.id}>
                            <Card
                                sx={{ 
                                    cursor: 'pointer',
                                    border: 1,
                                    borderColor: isUsable ? 'divider' : 'error.main',
                                    opacity: isUsable ? 1 : 0.7,
                                    '&:hover': {
                                        borderColor: isUsable ? 'primary.main' : 'error.main',
                                        boxShadow: isUsable ? 2 : 1
                                    }
                                }}
                                onClick={() => handleVoucherClick(voucher)}
                            >
                                <CardContent>
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                                        <Typography variant="h6" color="primary" fontWeight="bold">
                                            {voucher.code}
                                        </Typography>
                                        {!isUsable && (
                                            <Chip 
                                                label={expired ? "Hết hạn" : outOfUsage ? "Hết lượt" : "Không hoạt động"} 
                                                color="error" 
                                                size="small" 
                                            />
                                        )}
                                    </Box>

                                    <Typography variant="h5" color="error" fontWeight="bold" sx={{ mb: 1 }}>
                                        {formatDiscountText(voucher)}
                                    </Typography>

                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                                        Áp dụng cho đơn hàng từ {voucher.minOrderValue.toLocaleString('vi-VN')} VNĐ
                                    </Typography>

                                    <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                                        <Chip
                                            icon={<AccessTime />}
                                            label={`Còn ${remainingDays} ngày`}
                                            size="small"
                                            color={remainingDays <= 3 ? 'error' : 'default'}
                                        />
                                        <Chip
                                            icon={<People />}
                                            label={`Còn ${remainingUsage} lượt`}
                                            size="small"
                                            color={remainingUsage <= 5 ? 'warning' : 'default'}
                                        />
                                    </Box>

                                    <Typography variant="caption" color="text.secondary">
                                        HSD: {formatDate(voucher.endDate)}
                                    </Typography>
                                </CardContent>
                            </Card>
                        </Grid>
                    );
                })}
            </Grid>

            {/* Dialog chi tiết voucher */}
            <Dialog 
                open={dialogOpen} 
                onClose={() => setDialogOpen(false)}
                maxWidth="sm"
                fullWidth
            >
                {selectedVoucherDetails && (
                    <>
                        <DialogTitle>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                <Discount color="primary" />
                                Chi tiết mã giảm giá
                            </Box>
                        </DialogTitle>
                        <DialogContent>
                            <Box sx={{ mb: 2 }}>
                                <Typography variant="h4" color="primary" fontWeight="bold" gutterBottom>
                                    {selectedVoucherDetails.code}
                                </Typography>
                                <Typography variant="h5" color="error" fontWeight="bold" gutterBottom>
                                    {formatDiscountText(selectedVoucherDetails)}
                                </Typography>
                            </Box>

                            <Box sx={{ mb: 2 }}>
                                <Typography variant="body1" gutterBottom>
                                    <strong>Điều kiện áp dụng:</strong>
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    • Đơn hàng tối thiểu: {selectedVoucherDetails.minOrderValue.toLocaleString('vi-VN')} VNĐ
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    • Thời gian áp dụng: {formatDate(selectedVoucherDetails.startDate)} - {formatDate(selectedVoucherDetails.endDate)}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    • Số lượt sử dụng: {selectedVoucherDetails.currentUsage}/{selectedVoucherDetails.maxUsage}
                                </Typography>
                            </Box>

                            {isVoucherExpired(selectedVoucherDetails) ? (
                                <Alert severity="error" icon={<Info />}>
                                    Mã giảm giá này đã hết hạn
                                </Alert>
                            ) : isVoucherOutOfUsage(selectedVoucherDetails) ? (
                                <Alert severity="warning" icon={<Info />}>
                                    Mã giảm giá này đã hết lượt sử dụng
                                </Alert>
                            ) : !selectedVoucherDetails.isActive ? (
                                <Alert severity="warning" icon={<Info />}>
                                    Mã giảm giá này hiện không hoạt động
                                </Alert>
                            ) : (
                                <Alert severity="success" icon={<CheckCircle />}>
                                    Mã giảm giá này có thể sử dụng
                                </Alert>
                            )}
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={() => setDialogOpen(false)}>
                                Đóng
                            </Button>
                        </DialogActions>
                    </>
                )}
            </Dialog>
        </Box>
    );
};

export default VoucherList;