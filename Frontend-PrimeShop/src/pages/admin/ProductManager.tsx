import React, { useEffect, useState } from "react";
import "../../assets/css/admin.css";
import api from "../../api/api";
import styles from "../product/styles/ProductsPage.module.css";
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
  FormControl,
  InputLabel
} from "@mui/material";
import { useSearchParams } from "react-router-dom";
import { Category } from "../../types/category";
import Swal from "sweetalert2";
import { Product } from "../../types/product";
import { NameValuePair } from "../../types/NameValuePair";
import { ProductSpec } from "../../types/product";

const ProductManager: React.FC = () => {
  const [products, setProducts] = useState({content: [], totalPages: 0, number: 0});
  const [currentPage, setCurrentPage] = useState(0);
  const [filteredProducts, setFilteredProducts] = useState<any[]>([]);
  const [filters, setFilters] = useState({
    category: "all",
    search: "",
    minPrice: "",
    maxPrice: "",
    brand: "all"
  });
  const [showAddForm, setShowAddForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editProduct, setEditProduct] = useState<Product | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [newProduct, setNewProduct] = useState ({
    name: "",
    price: "",
    discountPercent: "",
    images: [""],
    description: "",
    categoryId: "",
    brand: "",
    stock: "",
    specs: [{name: "", value: ""}],
  });
  const search = searchParams.get("search") || "";
  const category = searchParams.get("category") || "all";
  const brand = searchParams.get("brand") || "all";
  const minPrice = searchParams.get("minPrice") || "all";
  const maxPrice = searchParams.get("maxPrice") || "all";
  const priceRanges = [
      { label: "- Tất cả -", minPrice: "", maxPrice: "" },
      { label: "1 triệu", minPrice: "1000000", maxPrice: "1000000" },
      { label: "2 triệu", minPrice: "2000000", maxPrice: "2000000" },
      { label: "5 triệu", minPrice: "50000000", maxPrice: "5000000" },
      { label: "10 triệu", minPrice: "10000000", maxPrice: "10000000" },
      { label: "20 triệu", minPrice: "20000000", maxPrice: "20000000" },
  ];
  const [categories, setCategories] = useState<Category[]>([]);
  const [categoriesSlug, setCategoriesSlug] = useState<string[]>([]);
  const [brands, setBrands] = useState<string[]>([]);
  const [images, setImages] = useState<string[]>([]);
  const [specs, setSpecs] = useState<ProductSpec[]>([{ name: "", value: "" }]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      const response = await api.post("/admin/product/add", newProduct);
      Swal.fire({
        title: 'Thêm sản phẩm thành công',
        icon: 'success',
        confirmButtonText: 'OK',
        allowOutsideClick: false,
        allowEscapeKey: false,
        allowEnterKey: false,
      }).then(() => {
        window.location.reload();
      });
      console.log("Thêm sản phẩm thành công:", response);     
    } catch (error) {
      console.error("Lỗi khi thêm sản phẩm:", error);
      console.log(JSON.stringify(newProduct, null, 2));
    }
  };

  const fetchProducts = async () => {
    try {
      const response = await api.get("/admin/product", {
        params: {
          category: category !== "all" ? category : undefined,
          search: search || undefined,
          minPrice: minPrice !== "all" ? minPrice : undefined,
          maxPrice: maxPrice !== "all" ? maxPrice : undefined,
          brand: brand !== "all" ? brand : undefined,
          page: currentPage,
          size: 10
        }
      });
      setProducts(response.data);
      console.log(response.data);
      setFilteredProducts(response.data);
    } catch (error) {
      console.error("Error fetching products:", error);
    }
  };

  const fetchImages = async (productSlug: string) => {
    const response = await api.get(`/product/images/${productSlug}`);
    setImages(response.data);
  }

  const handleAddDetailImage = async (productSlug: string, imageUrls: string[]) => {
    const response = await fetchImages(productSlug);
    if (imageUrls.length === 3) {
      const response = await api.post(`/product/add-images?productSlug=${productSlug}
      &imageUrls=${imageUrls[0]}&imageUrls=${imageUrls[1]}&imageUrls=${imageUrls[2]}`);
      console.log("response", response);
    } else if (imageUrls.length === 2) {
      const response = await api.post(`/product/add-images?productSlug=${productSlug}
      &imageUrls=${imageUrls[0]}&imageUrls=${imageUrls[1]}`);
      console.log("response", response);
    } else if (imageUrls.length === 1) {
      const response = await api.post(`/product/add-images?productSlug=${productSlug}
      &imageUrls=${imageUrls[0]}`);
      console.log("response", response);
    } else {
      console.log("Lỗi khi thêm ảnh chi tiết", imageUrls[0]);
    }
  }

  const handleDeleteDetailImage = async (productSlug: string, imageUrl: string) => {
    const response = await api.delete(`/product/delete-image?productSlug=${productSlug}
    &imageUrl=${imageUrl}`);
    console.log("response", response);
  }

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewProduct(prev => ({
      ...prev,
      [name]: value
    }));
  }

  const handleSpecChange = (index, field, value) => {
    const newSpecs = [...newProduct.specs];
    newSpecs[index][field] = value;
    setNewProduct(prev => ({
      ...prev,
      specs: newSpecs
    }));
  }

  const addSpecRow = () => {
    setNewProduct(prev => ({
      ...prev,
      specs: [...prev.specs, { name: '', value: '' }],
    }));
  };

  const removeSpecRow = (index) => {
    const newSpecs = newProduct.specs.filter((_, i) => i !== index);
    setNewProduct(prev => ({
      ...prev,
      specs: newSpecs,
    }));
  };

  const handleImageChange = (index, value) => {
    const newImages = [...newProduct.images];
    newImages[index] = value;
    setNewProduct(prev => ({
      ...prev,
      images: newImages
    }));
  };

  const addImageRow = () => {
    setNewProduct(prev => ({
      ...prev,
      images: [...prev.images, '']
    }));
  };

  const removeImageRow = (index) => {
    const newImages = newProduct.images.filter((_, i) => i !== index);
    setNewProduct(prev => ({
      ...prev,
      images: newImages
    }));
  };

  const handleUpdateProduct = async (productId, updatedProduct) => {
    try {
      await api.post(`/admin/products/update?id=${productId}`, {
        name: updatedProduct.name,
        price: updatedProduct.price,
        discountPercent: updatedProduct.discountPercent,
        images: updatedProduct.images,
        description: updatedProduct.description        
      });
    } catch (error) {
      console.error("Error updating product:", error);
    }
  };

  const [rows, setRows] = useState<NameValuePair[]>([
    { name: "", value: "" }
  ]);

  const handleAddRow = (index: number) => {
    const newRows = [...rows];
    newRows.splice(index + 1, 0, { name: "", value: "" });
    setRows(newRows);
  };

  const handleRemoveRow = (index: number) => {
    if (rows.length === 1) {
      alert("Phải có ít nhất 1 dòng.");
      return;
    }
    const newRows = [...rows];
    newRows.splice(index, 1);
    setRows(newRows);
  };

  const handleChangeRow = (index: number, field: keyof NameValuePair, value: string) => {
    const newRows = [...rows];
    newRows[index][field] = value;
    setRows(newRows);
  };

  function handleEditProduct(product) {
    Swal.fire({
      title: 'Sửa sản phẩm',
      width: '1200px',
      showCancelButton: true,
      confirmButtonText: 'Cập nhật',
      allowEnterKey: true,
      showLoaderOnConfirm: true,
      html: `
      <div class="form-group" style="width: 500px, align-items: flex-start;">
      <p><strong>Tên sản phẩm:</strong><input id="name" class="swal2-input" placeholder="Tên sản phẩm" value="${product.name}"></p>
      <p><strong>Giá:</strong><input id="price" class="swal2-input" type="number" placeholder="Giá" value="${product.price}"></p>
      <p><strong>Giảm giá:</strong><input id="discountPercent" class="swal2-input" type="number" placeholder="Giảm giá" value="${product.discountPercent}"></p>
      <p><strong>Thương hiệu:</strong><input id="brand" class="swal2-input" placeholder="Thương hiệu" value="${product.brand}"></p>
      <p><strong>Hàng tồn kho:</strong><input id="stock" class="swal2-input" type="number" placeholder="Hàng tồn kho" value="${product.stock}"></p>
      <p><strong>Ảnh đại diện:</strong><input id="imageUrl" class="swal2-input" type="text" placeholder="Ảnh đại diện" value="${product.imageUrl}"></p>
      <p><strong>Thêm ảnh chi tiết: (Bỏ trống nếu không thay đổi)</strong><input id="images" class="swal2-input" type="text" placeholder="Ảnh chi tiết" value="${product.images.join(',')}"></p>
      <p><strong>Xóa ảnh chi tiết: (1 ảnh)</strong><input id="deleteImage" class="swal2-input" type="text" placeholder="Xóa ảnh chi tiết" value=""></p>
      <p><strong>Mô tả:</strong><textarea id="description" class="swal2-textarea" placeholder="Mô tả">${product.description}</textarea></p>
      <p><strong>Thông số kỹ thuật: (Bỏ trống mặc định là xóa)</strong></p>
      <table id="specs">
        <thead>
          <tr>
            <th>Tên thông số</th>
            <th>Giá trị</th>
          </tr>
        </thead>
        <tbody id="specs">
          ${product.specs.map((spec) => `
            <tr>
              <td><input type="text" name="specName" value="${spec.name.replace(':', '')}" placeholder="Tên thông số"></td>
              <td><input type="text" name="specValue" value="${spec.value}" placeholder="Giá trị"></td>
            </tr>
          `).join('')}
        </tbody>        
      </table>   
      <div style={{display: 'flex', justifyContent: 'flex-end'}}>
          <button type="button" id="addSpecRow" className="btn btn-primary">
            Thêm dòng
          </button>
        </div>   
      `,
      didOpen: () => {
        const addSpecRow = document.getElementById('addSpecRow')?.addEventListener('click', () => {
          const tbody = document.querySelector('#specs tbody');
          if (tbody) {
            const newRow = document.createElement('tr');
            newRow.innerHTML = `
              <td><input type="text" name="specName" placeholder="Tên thông số"></td>
              <td><input type="text" name="specValue" placeholder="Giá trị"></td>
            `;
            tbody.appendChild(newRow);
          }
          else {
            console.log("Không tìm thấy tbody");
          }
        });
      },
      preConfirm: async () => {
        await fetchImages(product.slug);
        const name = document.getElementById('name')?.value;
        const price = document.getElementById('price')?.value;
        const discountPercent = document.getElementById('discountPercent')?.value;
        const brand = document.getElementById('brand')?.value;
        const description = document.getElementById('description')?.value;
        const stock = document.getElementById('stock')?.value;
        const imageUrl = document.getElementById('imageUrl')?.value;
        const images = document.getElementById('images')?.value;
        const imagesArray = images.split(',');
        const deleteImage = document.getElementById('deleteImage')?.value;
        const specs: ProductSpec[] = Array.from(document.querySelectorAll('#specs tbody tr'))
          .map(row => ({
            name: (row.querySelector('input[name="specName"]') as HTMLInputElement)?.value,
            value: (row.querySelector('input[name="specValue"]') as HTMLInputElement)?.value
          }))
          .filter(spec => spec.name && spec.value);
        console.log("specs", specs);

        

        try {
          const response = await api.patch(`/admin/product/update?id=${product.id}`, {
            name, price, discountPercent, brand, description, stock, imageUrl, images, specs
          });
          console.log("response", response);

          const newSlug = response.data.slug;
          try {
            const response2 = await handleAddDetailImage(newSlug, imagesArray);
          } catch (error) {
            console.error("Lỗi khi thêm ảnh chi tiết:", error);
          }

          if (deleteImage) {
            try {
              const response3 = await handleDeleteDetailImage(newSlug, deleteImage);
          } catch (error) {
              console.error("Lỗi khi xóa ảnh chi tiết:", error);
            }
          }

          return {
            name, price, discountPercent, brand, description, stock, imageUrl, images: imagesArray
          };
        } catch (error) {
          console.log(name, price, discountPercent, brand, description, stock, imageUrl, images);
          console.error("Lỗi khi cập nhật sản phẩm:", error);
        }
      },
      allowOutsideClick: () => !Swal.isLoading()
    }).then(result => {
      if (result.isConfirmed) {
        Swal.fire({
          title: 'Cập nhật sản phẩm thành công',
          icon: 'success',
          confirmButtonText: 'OK',
          allowOutsideClick: false,
          allowEscapeKey: false,
          allowEnterKey: false,
        }).then(() => {
          window.location.reload();
        });
        console.log(result.value);
      }
    });
  }

  function handleDeactivateProduct(productId) {
    Swal.fire({
      title: 'Xóa sản phẩm',
      text: 'Bạn có chắc chắn muốn xóa sản phẩm này không?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      allowOutsideClick: () => !Swal.isLoading()
    }).then(async (result) => {
      if (result.isConfirmed) {
        try {
          await api.post(`/admin/product/deactive?id=${productId}`);
          Swal.fire({
            title: 'Sản phẩm đã được xóa',
            icon: 'success',
            confirmButtonText: 'OK',
            allowOutsideClick: false,
            allowEscapeKey: false,
            allowEnterKey: false,
          }).then(() => {
            window.location.reload();
          });
        } catch (error) {
          console.error("Error deactivating product:", error);
        }
      }
    });
  } 

  useEffect(() => {
    const fetchFilters = async () => {
        try {
            const [resCategories, resBrands] = await Promise.all([
                api.get("/category/all").catch(console.log),
                api.get("/product/brands")
            ]);
            setCategories(["all", ...resCategories.data]);
            setCategoriesSlug(["all", ...resCategories.data.map((c: Category) => c.slug)]);
            setBrands(["all", ...resBrands.data]);
        } catch (err) {
            console.error("Lỗi khi tải filters:", err);
        }
    };
    fetchFilters();
}, []);

  useEffect(() => {
    fetchProducts();
  }, [search, category, brand, minPrice, maxPrice, currentPage]);

  const handleFilterChange = (field: string, value: string) => {
    setFilters(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const updateParam = (key: string, value: string) => {
    const params = new URLSearchParams(searchParams);
    if (value === "all" || value === "") {
        params.delete(key);
    } else {
        params.set(key, value);
    }
    setSearchParams(params);
};

  // return (
  //   <div className="admin-container">
  //     <h1 className="admin-title">Quản lý sản phẩm</h1>

  //     <div className="filters-container" style={{ marginBottom: 20, display: 'flex', gap: 20 }}>
  //       <FormControl style={{ minWidth: 120 }}>
  //         <InputLabel>Danh mục</InputLabel>
  //         <Select
  //           value={filters.category}
  //           onChange={(e) => handleFilterChange("category", e.target.value)}
  //         >
  //           <MenuItem value="all">Tất cả</MenuItem>
  //           <MenuItem value="laptop">Laptop</MenuItem>
  //           <MenuItem value="phone">Điện thoại</MenuItem>
  //           <MenuItem value="tablet">Máy tính bảng</MenuItem>
  //           <MenuItem value="console">Máy chơi game</MenuItem>
  //         </Select>
  //       </FormControl>

  //       <TextField
  //         label="Tìm kiếm"
  //         value={filters.search}
  //         onChange={(e) => handleFilterChange("search", e.target.value)}
  //       />

  //       <TextField
  //         label="Giá từ"
  //         type="number"
  //         value={filters.minPrice}
  //         onChange={(e) => handleFilterChange("minPrice", e.target.value)}
  //       />

  //       <TextField
  //         label="Giá đến"
  //         type="number"
  //         value={filters.maxPrice}
  //         onChange={(e) => handleFilterChange("maxPrice", e.target.value)}
  //       />

  //       <FormControl style={{ minWidth: 120 }}>
  //         <InputLabel>Thương hiệu</InputLabel>
  //         <Select
  //           value={filters.brand} 
  //           onChange={(e) => handleFilterChange("brand", e.target.value)}
  //         >
  //           <MenuItem value="all">Tất cả</MenuItem>
  //           <MenuItem value="Apple">ACER</MenuItem>
  //           <MenuItem value="Nokia">SONY</MenuItem>
            
  //         </Select>
  //       </FormControl>
  //     </div>

  //     <TableContainer component={Paper}>
  //       <Table>
  //         <TableHead>
  //           <TableRow>
  //             <TableCell>Mã SP</TableCell>
  //             <TableCell>Tên sản phẩm</TableCell>
  //             <TableCell>Danh mục</TableCell>
  //             <TableCell>Giá</TableCell>
  //             <TableCell>Số lượng</TableCell>
  //             <TableCell>Trạng thái</TableCell>
  //             <TableCell>Thao tác</TableCell>
  //           </TableRow>
  //         </TableHead>
  //         <TableBody>
  //           {filteredProducts.map((product) => (
  //             <TableRow key={product.productId}>
  //               <TableCell>{product.productId}</TableCell>
  //               <TableCell>{product.name}</TableCell>
  //               <TableCell>{product.category}</TableCell>
  //               <TableCell>
  //                 {product.price.toLocaleString('vi-VN', {
  //                   style: 'currency',
  //                   currency: 'VND'
  //                 })}
  //               </TableCell>
  //               <TableCell>{product.quantity}</TableCell>
  //               <TableCell>{product.status}</TableCell>
  //               <TableCell>
  //                 {/* Add action buttons here */}
  //               </TableCell>
  //             </TableRow>
  //           ))}
  //         </TableBody>
  //       </Table>
  //     </TableContainer>
  //   </div>
  // );

  return (
    <div>
      <h1>Quản lý sản phẩm</h1>
      <button
        onClick={() => setShowAddForm(!showAddForm)}
        className={`btn ${showAddForm ? 'btn-secondary' : 'btn-success'}`}
      >
        {showAddForm ? 'Hủy thêm' : 'Thêm sản phẩm'}
      </button>

      {showAddForm && (
        <div className="admin-form">
          <h2>Thêm sản phẩm mới</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Tên sản phẩm</label>
              <input
                type="text"
                name="name"
                value={newProduct.name}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Giá</label>
              <input
                type="number"
                name="price"
                value={newProduct.price}
                onChange={handleChange}
                required
                min="0"
              />
            </div>
            <div className="form-group">
              <label>Giảm giá (%)</label>
              <input
                type="number"
                name="discountPercent"
                value={newProduct.discountPercent}
                onChange={handleChange}
                min="0"
                max="100"
              />
            </div>
            
            <div className="form-group">
              <label>Hàng tồn kho</label>
              <input
                type="number"
                name="stock"
                value={newProduct.stock}
                onChange={handleChange}
                required
                min="0"
              />
            </div>
            {/* <div className="form-group">
              <label>Hình ảnh (Thêm trong phần sửa sau khi thêm sản phẩm)</label>
              {newProduct.images.map((img, index) => (
                index < 3 && (
                <div key={index} className="form-group">
                  <input
                    type="text"
                    placeholder="Link ảnh"
                    value={img}
                    onChange={(e) => handleImageChange(index, e.target.value)}
                  />
                  <button type="button" className="btn btn-danger" onMouseDown={() => removeImageRow(index)}>Xóa dòng</button>
                </div>
              )))}
              <button style={{width: 150}} type="button" className="btn btn-primary" onClick={addImageRow}>Thêm dòng</button>              
            </div> */}
            <div className="form-group">
              <label>Mô tả</label>
              <textarea
                name="description"
                value={newProduct.description}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Danh mục</label>
              <select
                  value={newProduct.categoryId}
                  name="categoryId"
                  onChange={handleChange}
                  className={styles.filterSelect}
                >
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.name}
                    </option>
                  ))}
                </select>
            </div>
            <div className="form-group">
              <label>Thương hiệu</label>
              <input
                type="text"
                value={newProduct.brand}
                name="brand"
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Đặc điểm kỹ thuật (Dòng 1: name, Dòng 2: value)</label>
              {newProduct.specs.map((spec, index) => (
                <div key={index} className="form-group">
                  <input
                    type="text"
                    value={spec.name}
                    onChange={(e) => handleSpecChange(index, 'name', e.target.value)}
                  />
                  <input
                    type="text"
                    value={spec.value}
                    onChange={(e) => handleSpecChange(index, 'value', e.target.value)}
                  />       
                  <button type="button" onMouseDown={() => removeSpecRow(index)} className="btn btn-danger">
                    Xóa dòng
                  </button>
                </div>
              ))}
              <div>
                <button onMouseDown={addSpecRow} className="btn btn-primary">
                  Thêm dòng
                </button>
              </div>
            </div>
            
            <div className="flex justify-start">
              <button type="submit" className="btn btn-primary">
                Thêm sản phẩm
              </button>
            </div>
          </form>
        </div>
      )}

          <div className={styles.filters}>
            <div className={styles.searchBar}>
              <input
                type="text"
                value={search}
                onChange={(e) => updateParam("search", e.target.value)}
                placeholder="Tìm kiếm sản phẩm..."
                className={styles.searchInput}
                aria-label="Tìm kiếm sản phẩm"
              />
            </div>
    
            <div className={styles.filterGroup}>
              <div className={styles.filter}>
                <label>Danh mục:</label>
                <select
                  value={category}
                  onChange={(e) => updateParam("category", e.target.value)}
                  className={styles.filterSelect}
                >
                  {categoriesSlug.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat === "all" ? "- Tất cả -" : categories.find(c => c.slug === cat)?.name}
                    </option>
                  ))}
                </select>
              </div>
    
              <div className={styles.filter}>
                <label>Thương hiệu:</label>
                <select
                  value={brand}
                  onChange={(e) => updateParam("brand", e.target.value)}
                  className={styles.filterSelect}
                >
                  {brands.map((b) => (
                    <option key={b} value={b}>
                      {b === "all" ? "- Tất cả -" : b}
                    </option>
                  ))}
                </select>
              </div>

              <div className={styles.filter}>
                <label>Giá từ:</label>
                <select
                    value={minPrice}
                    className={styles.filterSelect}
                    onChange={(e) => updateParam("minPrice", e.target.value)}
                >
                    {priceRanges.map((range, index) => (
                        <option key={index} value={range.minPrice}>
                            {range.label}
                        </option>
                    ))}
                </select>
              </div>

              <div className={styles.filter}>
                <label>Đến:</label>
                <select
                    value={maxPrice}
                    className={styles.filterSelect}
                    onChange={(e) => updateParam("maxPrice", e.target.value)}
                >
                    {priceRanges.map((range, index) => (
                        <option key={index} value={range.maxPrice}>
                            {range.label}
                        </option>
                    ))}
                </select>
              </div>
            </div>
      {loading ? (
        <div className="table-container">Đang tải...</div>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Tên sản phẩm</th>
                <th>Hình ảnh</th>
                <th>Giá</th>
                <th>Danh mục</th>
                <th>Hàng tồn kho</th>
                <th>Đã bán</th>
                <th>Đặc điểm kỹ thuật</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {products.content.map((product) => (
                <tr key={product.id}>
                  {editingId === product.id ? (
                    <>
                      <td>
                        <input
                          type="text"
                          value={editProduct?.name || ''}
                          onChange={(e) => {
                            const name = e.target.value;
                            setEditProduct({...editProduct, name});
                          }}
                          className="inline-input"
                          required
                        />
                      </td>
                      <td>
                        <textarea
                          value={""}
                          onChange={(e) => {}}
                          className="inline-textarea"
                          rows={2}
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          value={""}
                          onChange={(e) => {}}
                          className="inline-input"
                          min="0"
                          required
                        />
                      </td>
                      <td>
                        <input
                          type="text"
                          value={""}
                          onChange={(e) => {}}
                          className="inline-input"
                          required
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          value={""}
                          onChange={(e) => {}}
                          className="inline-input"
                          min="0"
                          required
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          value={""}
                          onChange={(e) => {}}
                          className="inline-input"
                          min="0"
                        />
                      </td>
                      <td>
                        <textarea
                          value={""}
                          onChange={(e) => {}}
                          className="inline-textarea"
                          rows={2}
                        />
                      </td>
                      <td>
                        <button onClick={() => {}} className="btn btn-primary btn-small">
                          Lưu
                        </button>
                        <button onClick={() => {}} className="btn btn-secondary btn-small">
                          Hủy
                        </button>
                      </td>
                    </>
                  ) : (
                    <>
                      <td>{product.id}</td>
                      <td>{product.name}</td>
                      <td>
                        <div className="image-list">
                          {(product.images || []).slice(0, 3).map((url: string, index: number) => (
                            <img
                              key={index}
                              src={url}
                              alt={`Sản phẩm ${product.name} - Ảnh ${index + 1}`}
                              onError={(e) => (e.currentTarget.src = 'https://via.placeholder.com/50')}
                            />
                          ))}
                          {(product.images || []).length === 0 && <span>Không có ảnh</span>}
                        </div>
                      </td>
                      <td>{product.price.toLocaleString()} VNĐ</td>
                      <td>{product.category}</td>
                      <td>{product.stock}</td>
                      <td>{product.sold}</td>
                      <td>{(product.specs || []).map((spec) => `${spec.name}: ${spec.value}`).join(', ') || 'Không có'}</td>
                      <td>
                        <button onClick={() => handleEditProduct(product)} className="btn btn-link">
                        {/* {Swal.fire({
                          title: 'Sửa sản phẩm',
                          width: '1200px',                          
                          html: `
                          <div class="form-group" style="width: 1000px, align-items: center;">
                            <label for="name">Tên sản phẩm</label>
                            <input type="text" id="name" value="${product.name}" class="swal2-input" readonly>
                          
                            <label for="price">Giá gốc</label>
                            <input type="number" id="price" value="${product.price}" class="swal2-input" onChange={(e) => {
                              setProduct({...product, price: e.target.value});
                            }}>
                          
                            <label for="discountPercent">Giảm giá</label>
                            <input type="number" id="discountPercent" value="${product.discountPercent}" class="swal2-input" onChange={(e) => {
                              setProduct({...product, discountPercent: e.target.value});
                            }}>
                            <label for="images">Hình ảnh</label>
                            <input type="text" id="images" value="${product.images}" class="swal2-input" onChange={(e) => {
                              setProduct({...product, images: e.target.value});
                            }}>
                            <label for="description">Mô tả</label>
                            <textarea id="description" class="swal2-textarea" onChange={(e) => {
                              setProduct({...product, description: e.target.value});
                            }}>${product.description}</textarea>
                          `,
                          confirmButtonText: 'Sửa',
                          showCancelButton: true,
                          cancelButtonText: 'Hủy',
                          allowOutsideClick: false,
                          allowEscapeKey: false,
                          allowEnterKey: false,
                          preConfirm: () => {
                            const price = document.getElementById('price')?.value;
                            const discountPercent = document.getElementById('discountPercent')?.value;
                            const images = document.getElementById('images')?.value;
                            const description = document.getElementById('description')?.value;
                            return {
                              price, discountPercent, images, description
                            };
                          }
                        }).then(async (result) => {
                          if (result.isConfirmed) {
                            try {
                              await api.put(`/admin/products/update?id=${product.id}`, {
                                price: document.getElementById('price')?.value,
                                discountPercent: document.getElementById('discountPercent')?.value,
                                images: document.getElementById('images')?.value,
                                description: document.getElementById('description')?.value,
                              });
                              
                              Swal.fire({
                                title: 'Sửa thành công',
                                icon: 'success'
                              });
                            } catch (error) {
                              Swal.fire({
                              title: 'Lỗi',
                              text: 'Lỗi khi sửa sản phẩm',
                              icon: 'error'
                              });
                            }
                          }
                        }); 
                        }} 
                        // {Swal.fire({
                        //   title: 'Sửa sản phẩm',
                        //   html: `
                        //     <input id="price" class="swal2-input" placeholder="Giá" value="${product.price}" />
                        //     <input id="discountPercent" class="swal2-input" placeholder="Giảm giá" value="${product.discountPercent}" />
                        //   `,
                        //   focusConfirm: false,
                        //   preConfirm: () => {
                        //     return {
                        //       price: document.getElementById('price')?.value,
                        //       discountPercent: document.getElementById('discountPercent')?.value
                        //     };
                        //   },
                        //   showCancelButton: true
                        // }).then(async (result) => {
                        //   if (result.isConfirmed && result.value) {
                        //     try {
                        //       await api.put(`/admin/products/update?id=${product.id}`, result.value);
                        //       Swal.fire('Sửa thành công', '', 'success');
                        //     } catch (error) {
                        //       Swal.fire('Lỗi khi sửa sản phẩm', '', 'error');
                        //     }
                        //   }
                        // });
                        // }}
                        
                        className="btn btn-link">
                          Sửa */}
                          Sửa
                        </button>
                        <button onClick={() => handleDeactivateProduct(product.id)} className="btn btn-link">
                          Xóa
                        </button>
                      </td>
                    </>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      </div>

      {deleteId && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>Xác nhận xóa</h2>
            <p>Bạn có chắc chắn muốn xóa sản phẩm này?</p>
            <div className="flex justify-end">
              <button onClick={() => setDeleteId(null)} className="btn btn-secondary">
                Hủy
              </button>
              <button onClick={() => {}} className="btn btn-danger">
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="pagination">
        <button
          onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
          disabled={currentPage === 0}
        >
          Trang trước
        </button>
        {Array.from({ length: products.totalPages }, (_, i) => (
          <button
            key={i}
            onClick={() => setCurrentPage(i)}
            className={currentPage === i ? "active" : ""}
          >
            {i + 1}
          </button>
        ))}
        <button
          onClick={() =>
            setCurrentPage((prev) => Math.min(prev + 1, products.totalPages - 1))
          }
          disabled={currentPage === products.totalPages - 1}
        >
          Trang sau
        </button>
      </div>
    </div>
  );
};

export default ProductManager;
