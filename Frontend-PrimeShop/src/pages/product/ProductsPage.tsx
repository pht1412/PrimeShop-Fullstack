import React, { useState, useEffect } from "react";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import ProductCard from "../../components/product-card/product-card";
import { mockProducts } from "../../mocks/mockData";
import styles from "./styles/ProductsPage.module.css";
import { Product } from "../../types/product";

const ProductsPage: React.FC = () => {
  // State quản lý sản phẩm, tìm kiếm, bộ lọc và loading
  const [products, setProducts] = useState<Product[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [selectedCategory, setSelectedCategory] = useState<string>("all");
  const [selectedBrand, setSelectedBrand] = useState<string>("all");
  const [isLoading, setIsLoading] = useState<boolean>(false);

  // Lấy danh sách danh mục và thương hiệu từ mock data
  const categories = ["all", ...new Set(mockProducts.map((p) => p.category))];
  const brands = ["all", ...new Set(mockProducts.map((p) => p.brand))];

  // Hàm tìm kiếm và lọc sản phẩm
  const filterProducts = () => {
    setIsLoading(true);
    setTimeout(() => {
      const filtered = mockProducts.filter((product) => {
        const matchesSearch = product.name
          .toLowerCase()
          .includes(searchTerm.toLowerCase());
        const matchesCategory =
          selectedCategory === "all" || product.category === selectedCategory;
        const matchesBrand =
          selectedBrand === "all" || product.brand === selectedBrand;
        return matchesSearch && matchesCategory && matchesBrand;
      });
      setProducts(filtered);
      setIsLoading(false);
    }, 500); // Giả lập độ trễ để thấy hiệu ứng loading
  };

  // Cập nhật danh sách sản phẩm khi thay đổi tìm kiếm hoặc bộ lọc
  useEffect(() => {
    filterProducts();
  }, [searchTerm, selectedCategory, selectedBrand]);

  // Xử lý tìm kiếm
  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  };

  return (
    <div className={styles.productsPage}>
      {/* Thông báo Toastify */}
      <ToastContainer position="top-right" autoClose={2000} />

      {/* Header */}
      <header className={styles.header}>
        <h1>Danh sách sản phẩm</h1>
      </header>

      {/* Bộ lọc và tìm kiếm */}
      <div className={styles.filters}>
        <div className={styles.searchBar}>
          <input
            type="text"
            value={searchTerm}
            onChange={handleSearch}
            placeholder="Tìm kiếm sản phẩm..."
            className={styles.searchInput}
            aria-label="Tìm kiếm sản phẩm"
          />
        </div>

        <div className={styles.filterGroup}>
          <div className={styles.filter}>
            <label htmlFor="category">Danh mục:</label>
            <select
              id="category"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className={styles.filterSelect}
            >
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category === "all" ? "Tất cả danh mục" : category}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.filter}>
            <label htmlFor="brand">Thương hiệu:</label>
            <select
              id="brand"
              value={selectedBrand}
              onChange={(e) => setSelectedBrand(e.target.value)}
              className={styles.filterSelect}
            >
              {brands.map((brand) => (
                <option key={brand} value={brand}>
                  {brand === "all" ? "Tất cả thương hiệu" : brand}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Trạng thái loading */}
      {isLoading && (
        <div className={styles.loading}>
          <p>Đang tải sản phẩm...</p>
        </div>
      )}

      {/* Danh sách sản phẩm */}
      {!isLoading && products.length === 0 ? (
        <div className={styles.empty}>
          <p>Không tìm thấy sản phẩm nào.</p>
        </div>
      ) : (
        <div className={styles.productsGrid}>
          {products.map((product) => (
            <ProductCard
              key={product.id}
              id={product.id}
              name={product.name}
              price={product.price}
              originalPrice={product.originalPrice}
              discount={product.discount}
              image={product.image}
              brand={product.brand}
              screenType={product.screenType}
              screenSize={product.screenSize}
              storageOptions={product.storageOptions}
              rating={product.rating}
              sold={product.sold}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default ProductsPage;