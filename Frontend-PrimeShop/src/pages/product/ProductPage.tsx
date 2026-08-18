import React, { useState, useEffect } from "react";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import ProductCard from "../../components/product-card/product-card";
import styles from "./styles/ProductsPage.module.css";
import { Category } from "../../api/category.api";
import api from "../../api/api";
import { useSearchParams } from "react-router-dom";
// Import Icons
import { 
  FaSearch, FaFilter, FaCheck, FaTags, FaMoneyBillWave, 
  FaGem, FaDesktop, FaMobileAlt, FaLaptop, FaHeadphones, FaTabletAlt
} from "react-icons/fa";

const ProductsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  // State UI
  const [products, setProducts] = useState({ content: [], totalPages: 0, number: 0 });
  const [categories, setCategories] = useState<Category[]>([]);
  const [brands, setBrands] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  
  const pageParam = parseInt(searchParams.get("page") || "0");
  const [currentPage, setCurrentPage] = useState(pageParam);

  const search = searchParams.get("search") || "";
  const categorySlug = searchParams.get("category") || "all";
  const brand = searchParams.get("brand") || "all";
  const minPrice = searchParams.get("minPrice") || "all";
  const maxPrice = searchParams.get("maxPrice") || "all";

  // --- 1. ĐỊNH NGHĨA CÁC NHÓM LOGO (BRAND POOLS) ---
  // Lưu ý: Tên trong mảng phải khớp với tên file ảnh trong /public/logo/ (ví dụ: apple.png)
  const phoneBrands = ["apple", "samsung", "oppo", "xiaomi", "vivo", "realme", "nokia", "asus"];
  const laptopBrands = ["macbook", "dell", "hp", "asus", "lenovo", "msi", "acer", "lg", "surface"];
  const monitorBrands = ["lg", "samsung", "dell", "asus", "viewsonic", "aoc", "gigabyte", "benq", "msi"];
  const accessoryBrands = ["sony", "jbl", "anker", "logitech", "razer", "corsair", "apple", "samsung"];
  const tabletBrands = ["apple", "samsung", "lenovo", "xiaomi", "huawei"];
  const keyboardBrands = ["logitech", "razer", "corsair", "hyperx", "akko", "dareu","magicapple"];
  const mouseBrands = ["logitech", "hyperx", "dareu","apple mouse", "asus", "zadez", "rapoo", "msi", "corsair", "akko"];
  const headsetBrands = ["sony", "jbl", "logitech", "edifier", "hyperx", "apple", "marshall"];
  // Danh sách tổng hợp mặc định (Lấy đại diện mỗi loại một ít)
  const defaultBrands = ["apple", "asus", "hyperx", "zadez","rapoo","msi"];

  // --- 2. CẤU HÌNH BANNER & LOGO ĐỘNG THEO DANH MỤC ---
  const categoryConfig: any = {
    "man-hinh": {
      title: "Thế Giới Màn Hình",
      subtitle: "SẮC NÉT - CHÂN THỰC - SỐNG ĐỘNG",
      image: "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&q=80&w=2000",
      icon: <FaDesktop />,
      logos: monitorBrands // <--- List logo riêng cho màn hình
    },
    "ien-thoai": {
      title: "Smartphone Đỉnh Cao",
      subtitle: "CÔNG NGHỆ TRONG TẦM TAY",
      image: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&q=80&w=2000",
      icon: <FaMobileAlt />,
      logos: phoneBrands // <--- List logo riêng cho điện thoại
    },
    "laptop": {
      title: "Laptop Hiệu Năng Cao",
      subtitle: "HỌC TẬP - LÀM VIỆC - GIẢI TRÍ",
      image: "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&q=80&w=2000",
      icon: <FaLaptop />,
      logos: laptopBrands
    },
    "may-tinh-bang": {
        title: "Máy Tính Bảng",
        subtitle: "SÁNG TẠO MỌI LÚC MỌI NƠI",
        image: "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&q=80&w=2000",
        icon: <FaTabletAlt />,
        logos: tabletBrands
    },
    "phu-kien": {
        title: "Phụ Kiện Chính Hãng",
        subtitle: "NÂNG TẦM TRẢI NGHIỆM",
        image: "https://images.unsplash.com/photo-1600086827875-a63b01f1335c?auto=format&fit=crop&q=80&w=2000",
        icon: <FaHeadphones />,
        logos: accessoryBrands
    },
    "ban-phim": {
        title: "Bàn Phím Cơ",
        subtitle: "TRẢI NGHIỆM GÕ PHÍM ĐỈNH CAO",
        image: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&q=80&w=2000",
        icon: <FaGem />,
        logos: keyboardBrands
    },
    "chuot": {
        title: "Chuột Máy Tính",
        subtitle: "CHÍNH XÁC - NHANH NHẠY - BỀN BỈ",
        image: "https://images.unsplash.com/photo-1587825140408-6c4f3b3f3f4b?auto=format&fit=crop&q=80&w=2000",
        icon: <FaGem />,
        logos: mouseBrands
    },
    "tai-nghe": {
        title: "Tai Nghe Chất Lượng",
        subtitle: "ÂM THANH SỐNG ĐỘNG - CÔNG NGHỆ HIỆN ĐẠI",
        image: "https://images.unsplash.com/photo-1511367461989-f85a21fda167?auto=format&fit=crop&q=80&w=2000",
        icon: <FaGem />,
        logos: headsetBrands
    },
    // Cấu hình mặc định (khi chọn 'all' hoặc danh mục chưa define)
    "default": {
      title: "Đối Tác Uy Tín",
      subtitle: "NHÀ PHÂN PHỐI CHÍNH HÃNG",
      image: null,
      icon: <FaGem />,
      logos: defaultBrands
    }
  };

  // Xác định config hiện tại
  const currentConfig = categoryConfig[categorySlug] || categoryConfig["default"];
  // Xác định list logo cần chạy (Fallback về default nếu trong config không có logo)
  const currentLogos = currentConfig.logos || defaultBrands;  

  const priceRanges = [
    { label: "- Tất cả -", minPrice: "", maxPrice: "" },
    { label: "Dưới 1 triệu", minPrice: "0", maxPrice: "1000000" },
    { label: "1 - 5 triệu", minPrice: "1000000", maxPrice: "5000000" },
    { label: "5 - 10 triệu", minPrice: "5000000", maxPrice: "10000000" },
    { label: "10 - 20 triệu", minPrice: "10000000", maxPrice: "20000000" },
    { label: "Trên 20 triệu", minPrice: "20000000", maxPrice: "" },
  ];

  // Load Filters
  useEffect(() => {
    const fetchFilters = async () => {
      try {
        const [resCategories, resBrands] = await Promise.all([
          api.get("/category/all").catch(() => ({ data: [] })),
          api.get("/product/brands").catch(() => ({ data: [] }))
        ]);
        setCategories(resCategories.data);
        setBrands(["all", ...resBrands.data]);
      } catch (err) {
        console.error("Lỗi khi tải filters:", err);
      }
    };
    fetchFilters();
  }, []);

  // Load Products
  useEffect(() => {
    const fetchProducts = async () => {
      setIsLoading(true);
      try {
        window.scrollTo({ top: 0, behavior: 'smooth' });

        const res = await api.get("/product/all-products", {
          params: {
            search: search || undefined,
            category: categorySlug !== "all" ? categorySlug : undefined,
            brand: brand !== "all" ? brand : undefined,
            minPrice: minPrice !== "all" ? minPrice : undefined,
            maxPrice: maxPrice !== "all" ? maxPrice : undefined,
            page: currentPage,
            size: 12 
          },
        });
        setProducts(res.data);
      } catch (error) {
        console.error("Lỗi khi gọi API sản phẩm:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchProducts();
  }, [search, categorySlug, brand, minPrice, maxPrice, currentPage]);

  const updateParam = (key: string, value: string) => {
    const params = new URLSearchParams(searchParams);
    if (value === "all" || value === "") {
      params.delete(key);
    } else {
      params.set(key, value);
    }
    if (key !== "page") {
      params.set("page", "0");
      setCurrentPage(0);
    }
    setSearchParams(params);
  };

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
    const params = new URLSearchParams(searchParams);
    params.set("page", newPage.toString());
    setSearchParams(params);
  };

  const renderPagination = () => {
    const totalPages = products?.totalPages || 0; // Fix lỗi undefined
    const current = currentPage + 1;
    const delta = 2;
    const range = [];
    const rangeWithDots = [];

    for (let i = 1; i <= totalPages; i++) {
      if (i === 1 || i === totalPages || (i >= current - delta && i <= current + delta)) {
        range.push(i);
      }
    }

    let l;
    for (let i of range) {
      if (l) {
        if (i - l === 2) {
          rangeWithDots.push(l + 1);
        } else if (i - l !== 1) {
          rangeWithDots.push('...');
        }
      }
      rangeWithDots.push(i);
      l = i;
    }

    return (
      <div className={styles.pagination}>
        <button
          className={styles.pageBtn}
          onClick={() => handlePageChange(Math.max(currentPage - 1, 0))}
          disabled={currentPage === 0}
        >
          &lt; Trước
        </button>

        {rangeWithDots.map((page, index) => 
          page === '...' ? (
            <span key={`dots-${index}`} className={styles.dots}>...</span>
          ) : (
            <button
              key={index}
              onClick={() => handlePageChange((page as number) - 1)}
              className={`${styles.pageBtn} ${currentPage === (page as number) - 1 ? styles.active : ""}`}
            >
              {page}
            </button>
          )
        )}

        <button
          className={styles.pageBtn}
          onClick={() => handlePageChange(Math.min(currentPage + 1, totalPages - 1))}
          disabled={currentPage >= totalPages - 1}
        >
          Sau &gt;
        </button>
      </div>
    );
  };

  return (
    <div className={styles.productsPage}>
      <ToastContainer position="top-right" autoClose={2000} />
      
      <div className={styles.pageContainer}>
        
        {/* === SIDEBAR (BỘ LỌC) === */}
        <aside className={styles.sidebar}>
          
          <div className={styles.filterGroup}>
            <div className={styles.filterTitle}><FaFilter /> Bộ Lọc Tìm Kiếm</div>
          </div>

          {/* DANH MỤC */}
          <div className={styles.filterGroup}>
            <div className={styles.filterTitle}><FaTags /> Theo Danh Mục</div>
            <ul className={styles.filterList}>
              <li 
                className={`${styles.filterItem} ${categorySlug === 'all' ? styles.active : ''}`}
                onClick={() => updateParam("category", "all")}
              >
                Tất cả {categorySlug === 'all' && <FaCheck className={styles.checkIcon}/>}
              </li>
              {categories.map((cat) => (
                <li 
                  key={cat.id}
                  className={`${styles.filterItem} ${categorySlug === cat.slug ? styles.active : ''}`}
                  onClick={() => updateParam("category", cat.slug)}
                >
                  {cat.name} {categorySlug === cat.slug && <FaCheck className={styles.checkIcon}/>}
                </li>
              ))}
            </ul>
          </div>

          {/* THƯƠNG HIỆU */}
          <div className={styles.filterGroup}>
            <div className={styles.filterTitle}><FaGem /> Thương Hiệu</div>
            <ul className={styles.filterList}>
              <li 
                className={`${styles.filterItem} ${brand === 'all' ? styles.active : ''}`}
                onClick={() => updateParam("brand", "all")}
              >
                Tất cả {brand === 'all' && <FaCheck className={styles.checkIcon}/>}
              </li>
              {brands.map((b, idx) => (
                <li 
                  key={idx}
                  className={`${styles.filterItem} ${brand === b ? styles.active : ''}`}
                  onClick={() => updateParam("brand", b)}
                >
                  {b} {brand === b && <FaCheck className={styles.checkIcon}/>}
                </li>
              ))}
            </ul>
          </div>

          {/* KHOẢNG GIÁ */}
          <div className={styles.filterGroup}>
            <div className={styles.filterTitle}><FaMoneyBillWave /> Khoảng Giá</div>
            <ul className={styles.filterList}>
              {priceRanges.map((r, i) => {
                const isActive = minPrice === r.minPrice && maxPrice === r.maxPrice;
                return (
                  <li 
                    key={i}
                    className={`${styles.filterItem} ${isActive ? styles.active : ''}`}
                    onClick={() => {
                        updateParam("minPrice", r.minPrice);
                        updateParam("maxPrice", r.maxPrice);
                    }}
                  >
                    {r.label} {isActive && <FaCheck className={styles.checkIcon}/>}
                  </li>
                );
              })}
            </ul>
          </div>
        </aside>

        {/* === MAIN CONTENT === */}
        <main className={styles.mainContent}>
          
          {/* 1. SEARCH BAR */}
          <div className={styles.searchSection}>
            <FaSearch className={styles.searchIcon} />
            <input
              type="text"
              value={search}
              onChange={(e) => updateParam("search", e.target.value)}
              placeholder="Bạn muốn tìm sản phẩm gì hôm nay?"
              className={styles.searchInput}
            />
          </div>

          {/* 2. DYNAMIC BRAND SPOTLIGHT */}
          {/* Logic: Chỉ hiện khi danh mục khác 'all' hoặc có cấu hình */}
          {categorySlug !== 'all' && (
            <section 
                className={`${styles.brandSpotlight} ${currentConfig.image ? styles.hasImage : ''}`}
                style={currentConfig.image ? { backgroundImage: `url(${currentConfig.image})` } : {}}
            >
              <div className={styles.spotlightContent}>
                <div className={styles.spotlightHeader}>
                  <span className={styles.spotlightSubtitle}>
                    {currentConfig.icon} {currentConfig.subtitle}
                  </span>
                  <h2 className={styles.spotlightTitle}>
                    {currentConfig.title} <span className={styles.spotlightHighlight}>Uy Tín</span>
                  </h2>
                </div>
                
                {/* Marquee Logo chạy riêng theo danh mục */}
                <div className={styles.marqueeContainer}>
                  <div className={styles.marqueeTrack}>
                    {[...currentLogos, ...currentLogos].map((b, index) => (
                      <div className={styles.brandItem} key={index}>
                        <img 
                          src={`/logo/${b}.png`} 
                          alt={b} 
                          title={`Đối tác ${b}`}
                          onError={(e) => {
                            // Fallback: Nếu không có file local, thử dùng Clearbit logo API
                            e.currentTarget.src = `https://logo.clearbit.com/${b}.com`;
                            e.currentTarget.onerror = null; // Tránh loop vô hạn
                          }}
                        />
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </section>
          )}

          {/* 3. PRODUCT GRID */}
          {isLoading ? (
            <div className={styles.loading}><p>Đang tải dữ liệu...</p></div>
          ) : (!products?.content || products.content.length === 0) ? (
            <div className={styles.empty}><p>Không tìm thấy sản phẩm phù hợp.</p></div>
          ) : (
            <>
              <div className={styles.productsGrid}>
                {products?.content?.map((p: any) => (
                  <ProductCard key={p.id || p.slug} {...p} />
                ))}
              </div>
              
              {/* 4. PAGINATION */}
              {products.totalPages > 1 && renderPagination()}
            </>
          )}
        </main>
      </div>
    </div>
  );
};

export default ProductsPage;