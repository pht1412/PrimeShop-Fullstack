import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import "../assets/css/home.css"; 
import ProductCard from "../components/product-card/product-card";
import api from "../api/api";
import { Product } from "../types/product";
import { News } from "../types/news";
import { 
  FaShippingFast, FaShieldAlt, FaHeadset, FaUndo, 
  FaBolt, FaFire, FaArrowRight
} from "react-icons/fa";

const HomePage: React.FC = () => {
  // --- STATE ---
  const [timeLeft, setTimeLeft] = useState(2 * 60 * 60); // 2 hours
  const [selectedCategory, setSelectedCategory] = useState("Tất cả");
  const [hotSaleProducts, setHotSaleProducts] = useState<Product[]>([]);
  const [discountProducts, setDiscountProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [news, setNews] = useState<News[]>([]);

  // LOGO THƯƠNG HIỆU
  const brandLogos = [
    "apple", "samsung", "xiaomi", "oppo", 
    "vivo", "sony", "asus", "dell", 
    "hp", "lenovo", "lg", "msi"
  ];

  // --- FETCH DATA ---
  const fetchCategories = async () => {
    try {
      const res = await api.get("/category");
      setCategories(["Tất cả", ...res.data]);
    } catch (error) { console.error("Error fetching categories", error); }
  };

  const fetchHotSaleProducts = async () => {
    try {
      const res = await api.get("/product/hot-sale");
      setHotSaleProducts(res.data);
    } catch (error) { console.error("Error fetching hot sale", error); }
  };

  const fetchDiscountProducts = async () => {
    try {
      const res = await api.get("/product/discount");
      setDiscountProducts(res.data);
    } catch (error) { console.error("Error fetching discount", error); }
  };

  const fetchNews = async () => {
    try {
      const res = await api.get("/news");
      setNews(res.data.content || []); 
    } catch (error) { console.error("Error fetching news", error); }
  };

  useEffect(() => {
    fetchCategories();
    fetchHotSaleProducts();
    fetchDiscountProducts();
    fetchNews();
  }, []);

  // --- LOGIC SCROLL REVEAL ---
  useEffect(() => {
    const reveal = () => {
      const reveals = document.querySelectorAll(".reveal");
      for (let i = 0; i < reveals.length; i++) {
        const windowHeight = window.innerHeight;
        const elementTop = reveals[i].getBoundingClientRect().top;
        const elementVisible = 150; 
        if (elementTop < windowHeight - elementVisible) {
          reveals[i].classList.add("active");
        }
      }
    };
    window.addEventListener("scroll", reveal);
    reveal(); 
    return () => window.removeEventListener("scroll", reveal);
  }, [hotSaleProducts, discountProducts, news]); 

  // --- TIMER LOGIC ---
  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft((t) => (t > 0 ? t - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (seconds: number) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return `${h.toString().padStart(2, "0")}:${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
  };

  // --- RENDER ---
  return (
    <div className="body-container">
      {/* 1. HERO SECTION */}
      <section className="hero-section">
        <div className="hero-content">
          <div className="hero-text">
            <h1>Công Nghệ Tương Lai<br />Trong Tầm Tay Bạn</h1>
            <p>Trải nghiệm mua sắm đẳng cấp với các sản phẩm chính hãng Apple & Samsung. Giảm giá sốc đến 50% duy nhất hôm nay.</p>
            <Link to="/all-products" className="hero-btn">Khám Phá Ngay</Link>
          </div>
          <div className="hero-image-container">
            <img 
              src="/public/banner.png" 
              alt="PrimeShop Hero" 
              onError={(e) => { e.currentTarget.src = "https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-max-blue-titan-1-750x500.jpg"; }}
            />
          </div>
        </div>
      </section>

      {/* 2. TRUST BADGES */}
      <section className="features-section reveal">
        <div className="feature-card">
          <FaShippingFast />
          <h3>Giao hàng 2H</h3>
          <p>Nội thành TP.HCM & HN</p>
        </div>
        <div className="feature-card">
          <FaShieldAlt />
          <h3>Bảo hành VIP</h3>
          <p>1 đổi 1 trong 30 ngày</p>
        </div>
        <div className="feature-card">
          <FaUndo />
          <h3>Hoàn tiền 100%</h3>
          <p>Nếu phát hiện hàng giả</p>
        </div>
        <div className="feature-card">
          <FaHeadset />
          <h3>Support 24/7</h3>
          <p>Luôn sẵn sàng hỗ trợ</p>
        </div>
      </section>

      {/* 2.5 BRAND MARQUEE CAROUSEL (STYLE LIKE IMAGE) */}
      {/* <section className="brand-carousel reveal">
        <div className="brand-header-box">
          <h2 className="brand-title">
            Những Nhà Phân Phối <span className="brand-highlight">Uy Tín</span>
          </h2>
        </div>
        
        <div className="brand-track">
          {brandLogos.map((brand, index) => (
            <div className="brand-item" key={`orig-${index}`}>
              <img 
                src={`/logo/${brand}.png`} 
                alt={brand} 
                title={`Thương hiệu ${brand}`}
                onError={(e) => {
                  e.currentTarget.src = `https://logo.clearbit.com/${brand}.com`;
                  e.currentTarget.onerror = null; 
                }}
              />
            </div>
          ))}

          {brandLogos.map((brand, index) => (
            <div className="brand-item" key={`dup-${index}`}>
              <img 
                src={`/logo/${brand}.png`} 
                alt={brand} 
                onError={(e) => {
                   e.currentTarget.src = `https://logo.clearbit.com/${brand}.com`;
                   e.currentTarget.onerror = null;
                }}
              />
            </div>
          ))}
        </div>
      </section> */}

      {/* 3. FLASH SALE */}
      <section className="product-category reveal">
        <div className="flash-sale-header">
          <div className="flash-title">
            <FaBolt className="flash-icon" /> KHUYẾN MÃI ONLINE
          </div>
          <div className="timer-box">
            <span>KẾT THÚC SAU:</span>
            <span>{formatTime(timeLeft)}</span>
          </div>
        </div>

        <div className="category-tabs">
          {categories.map((c) => (
            <button
              key={c}
              className={selectedCategory === c ? "active" : ""}
              onClick={() => setSelectedCategory(c)}
            >
              {c}
            </button>
          ))}
        </div>

        <div className="product-list">
          {discountProducts.length > 0 ? (
            discountProducts
              .filter((p) => selectedCategory === "Tất cả" || p.category === selectedCategory)
              .slice(0, 8)
              .map((p) => <ProductCard key={p.slug} {...p} />)
          ) : (
            <div className="no-product">Đang nạp năng lượng cho deal mới... ⚡</div>
          )}
        </div>

        <div style={{textAlign: 'center'}}>
          <Link to="/all-products" className="view-all-btn">
             Xem Tất Cả Deal <FaArrowRight style={{marginLeft: '8px'}}/>
          </Link>
        </div>
      </section>

      {/* 4. HOT SALE */}
      <section className="hot-sale-container reveal">
        <h2 className="hot-sale-title">
           <FaFire style={{color: '#F97316', marginRight: '10px'}}/>
           SẢN PHẨM BÁN CHẠY
        </h2>
        <div className="hot-sale-products">
          {hotSaleProducts.slice(0, 8).map((p) => (
            <ProductCard key={p.slug} {...p} />
          ))}
        </div>
        <Link to="/all-products" className="view-all-btn">
          Vào Cửa Hàng <FaArrowRight style={{marginLeft: '8px'}}/>
        </Link>
      </section>

      {/* 5. TECH NEWS HIGHLIGHT */}
      <section className="tech-news reveal">
        <div className="tech-news-content">
          <h2>📰 Tiêu Điểm Công Nghệ</h2>
          <p>
            Thế giới công nghệ thay đổi từng giờ. Cập nhật ngay những xu hướng AI, 
            Smartphone và Laptop mới nhất để không bị bỏ lại phía sau.
          </p>
          <Link to="/news" className="hero-btn" style={{background: '#1E3A8A', color: 'white', padding: '12px 30px'}}>
            Đọc Ngay
          </Link>
        </div>
        <div className="news-image-wrapper">
           <img 
             src="/images/news/news4/SS-gap4.png" 
             alt="Tech News" 
             onError={(e) => { e.currentTarget.src = "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&q=80&w=800"; }}
           />
        </div>
      </section>

      {/* 6. GENERAL NEWS GRID */}
      <section className="general-tech-news reveal">
        <h2 className="general-news-title">🌐 Tin Mới Nhất</h2>
        <div className="news-list">
          {news.slice(0, 3).map((n) => (
            <Link key={n.id} to={`/news/${n.id}`} className="news-item">
              <div style={{overflow: 'hidden', height: '220px'}}>
                <img src={n.imageUrl || "https://via.placeholder.com/300x200"} alt={n.title} />
              </div>
              <div className="news-info">
                <h3>{n.title}</h3>
                <p>{n.excerpt}</p>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
};

export default HomePage;