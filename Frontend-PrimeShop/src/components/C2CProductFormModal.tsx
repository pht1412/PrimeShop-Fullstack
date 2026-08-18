// Vị trí: src/components/C2CProductFormModal.tsx
import React, { useState, useEffect } from 'react';
import { 
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Button, MenuItem, FormControl, InputLabel, Select, Grid,
  IconButton, Typography, Box, CircularProgress
} from '@mui/material';
import { SelectChangeEvent } from '@mui/material/Select';
// Nếu chưa cài @mui/icons-material, hãy cài hoặc thay bằng react-icons
import { Delete as DeleteIcon, CloudUpload as CloudUploadIcon } from '@mui/icons-material'; 
import '../assets/css/c2c-form-modal.css'; 

// Định nghĩa kiểu dữ liệu cho Tình trạng
type C2CProductCondition = 'new' | 'like_new' | 'used' | 'for_parts';

interface IC2CFormData {
  name: string;
  description: string;
  price: number;
  brand: string;
  images: string[];
  category_id: string;
  condition: C2CProductCondition;
  location: string;
  stock: number;
}

interface C2CFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (productData: any) => void;
  productToEdit: any | null; 
}

// Giá trị mặc định khi tạo mới
const defaultEmptyProduct: IC2CFormData = { 
  name: '', 
  description: '', 
  price: 0, 
  brand: '', 
  images: [], 
  category_id: '', 
  condition: 'used', // Mặc định là 'Đã qua sử dụng'
  location: '', 
  stock: 1,
};

const C2CProductFormModal: React.FC<C2CFormModalProps> = ({ isOpen, onClose, onSave, productToEdit }) => {
  const [formData, setFormData] = useState<IC2CFormData>(defaultEmptyProduct);
  const [isUploading, setIsUploading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      if (productToEdit) {
        // [QUAN TRỌNG] Merge dữ liệu edit vào form
        // Parent (C2CTab) đã xử lý việc bóc tách specs -> condition/location
        // Ta chỉ việc hiển thị nó ra
        setFormData({
            ...defaultEmptyProduct, // Giữ các default để tránh undefined
            ...productToEdit,       // Ghi đè bằng dữ liệu thật
            // Đảm bảo không bị null/undefined làm crash Select box
            condition: productToEdit.condition || 'used', 
            location: productToEdit.location || '',
            brand: productToEdit.brand || '',
        });
      } else {
        setFormData(defaultEmptyProduct);
      }
    }
  }, [productToEdit, isOpen]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: (name === 'price' || name === 'stock') ? parseFloat(value) : value,
    }));
  };

  const handleSelectChange = (e: SelectChangeEvent) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name as string]: value }));
  };

  // === UPLOAD ẢNH LÊN CLOUDINARY ===
  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const files = Array.from(e.target.files);
      setIsUploading(true);

      try {
        const uploadPromises = files.map(async (file) => {
          const data = new FormData();
          data.append("file", file);
          data.append("upload_preset", "primeshop_preset"); 
          data.append("folder", "prime/ecommerce");
          data.append("cloud_name", "dapsvdkmt"); 

          const res = await fetch("https://api.cloudinary.com/v1_1/dapsvdkmt/image/upload", {
            method: "POST",
            body: data
          });

          const uploadedImage = await res.json();
          if (uploadedImage.secure_url) {
              return uploadedImage.secure_url;
          } else {
              throw new Error(uploadedImage.error?.message || "Upload failed");
          }
        });

        const newImageUrls = await Promise.all(uploadPromises);

        setFormData(prev => ({
          ...prev,
          images: [...prev.images, ...newImageUrls] 
        }));
        
      } catch (error: any) {
        console.error("Upload error:", error);
        alert(`Lỗi upload ảnh: ${error.message}`);
      } finally {
        setIsUploading(false);
      }
    }
  };

  const handleRemoveImage = (indexToRemove: number) => {
    setFormData(prev => ({
      ...prev,
      images: prev.images.filter((_, index) => index !== indexToRemove)
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // [CLEAN CODE] Chỉ gửi dữ liệu form, không tự bịa ID hay sellerId
    // Logic đó thuộc về Parent (C2CTab) hoặc Backend
    onSave(formData);
  };

  const conditionOptions: { value: C2CProductCondition, label: string }[] = [
    { value: 'new', label: 'Mới 100%' },
    { value: 'like_new', label: 'Như mới 99%' },
    { value: 'used', label: 'Đã qua sử dụng' },
    { value: 'for_parts', label: 'Bán linh kiện/Hỏng' },
  ];

  return (
    <Dialog open={isOpen} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {productToEdit ? 'Chỉnh sửa tin đăng' : 'Tạo tin đăng mới'}
      </DialogTitle>
      <form onSubmit={handleSubmit}>
        <DialogContent className="c2c-form-content">
          <Grid container spacing={2}>
            
            {/* Tên sản phẩm */}
            <Grid item xs={12} md={8}>
              <TextField name="name" label="Tên sản phẩm" value={formData.name} onChange={handleChange} fullWidth required variant="outlined" />
            </Grid>

            {/* Giá bán */}
            <Grid item xs={12} md={4}>
              <TextField name="price" label="Giá bán (VND)" type="number" value={formData.price} onChange={handleChange} fullWidth required variant="outlined" />
            </Grid>

            {/* [QUAN TRỌNG] Tình trạng - Select Box */}
            <Grid item xs={12} md={6}>
              <FormControl fullWidth variant="outlined" required>
                <InputLabel id="condition-label">Tình trạng</InputLabel>
                <Select 
                    labelId="condition-label" 
                    name="condition" 
                    value={formData.condition} 
                    onChange={handleSelectChange} 
                    label="Tình trạng"
                >
                  {conditionOptions.map(opt => (
                    <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* [QUAN TRỌNG] Vị trí */}
            <Grid item xs={12} md={6}>
              <TextField name="location" label="Vị trí / Nơi bán" value={formData.location} onChange={handleChange} fullWidth required variant="outlined" />
            </Grid>

            {/* Thương hiệu */}
            <Grid item xs={12} md={4}>
               <TextField name="brand" label="Thương hiệu" value={formData.brand} onChange={handleChange} fullWidth variant="outlined" />
            </Grid>

            {/* Danh mục */}
            <Grid item xs={12} md={4}>
               <TextField 
                 name="category_id" 
                 label="Danh mục (ID)" 
                 value={formData.category_id} 
                 onChange={handleChange} 
                 fullWidth 
                 variant="outlined" 
                 helperText="VD: 1, 2, 3"
               />
            </Grid>

            {/* Tồn kho */}
            <Grid item xs={12} md={4}>
               <TextField name="stock" label="Số lượng" type="number" value={formData.stock} onChange={handleChange} fullWidth variant="outlined" required />
            </Grid>

            {/* Mô tả */}
            <Grid item xs={12}>
              <TextField name="description" label="Mô tả chi tiết" value={formData.description} onChange={handleChange} fullWidth multiline rows={4} required variant="outlined" />
            </Grid>

            {/* Khu vực chọn ảnh */}
            <Grid item xs={12}>
                <Box sx={{ border: '1px dashed #ccc', borderRadius: 2, p: 2, textAlign: 'center', backgroundColor: '#fafafa' }}>
                    <input
                        accept="image/*"
                        style={{ display: 'none' }}
                        id="raised-button-file"
                        multiple
                        type="file"
                        onChange={handleFileChange}
                    />
                    <label htmlFor="raised-button-file">
                        <Button 
                            variant="outlined" 
                            component="span" 
                            startIcon={isUploading ? <CircularProgress size={20} /> : <CloudUploadIcon />}
                            disabled={isUploading}
                        >
                            {isUploading ? 'Đang tải lên...' : 'Chọn ảnh từ thiết bị'}
                        </Button>
                    </label>
                    <Typography variant="caption" display="block" sx={{ mt: 1, color: '#666' }}>
                        Tải lên tối đa 5 ảnh. Ảnh đầu tiên sẽ là ảnh đại diện.
                    </Typography>

                    {/* Preview Image Grid */}
                    {formData.images.length > 0 && (
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, mt: 2, justifyContent: 'center' }}>
                            {formData.images.map((imgUrl, index) => (
                                <Box key={index} sx={{ position: 'relative', width: 100, height: 100 }}>
                                    <img 
                                        src={imgUrl} 
                                        alt={`preview-${index}`} 
                                        style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 8, border: '1px solid #ddd' }} 
                                    />
                                    <IconButton 
                                        size="small" 
                                        onClick={() => handleRemoveImage(index)}
                                        sx={{ 
                                            position: 'absolute', top: -8, right: -8, 
                                            backgroundColor: 'white', border: '1px solid #ccc',
                                            '&:hover': { backgroundColor: '#ffebee' } 
                                        }}
                                    >
                                        <DeleteIcon fontSize="small" color="error" />
                                    </IconButton>
                                </Box>
                            ))}
                        </Box>
                    )}
                </Box>
            </Grid>

          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} color="secondary">Huỷ bỏ</Button>
          <Button type="submit" variant="contained" color="primary" disabled={isUploading}>
            {productToEdit ? 'Lưu thay đổi' : 'Đăng tin'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default C2CProductFormModal;