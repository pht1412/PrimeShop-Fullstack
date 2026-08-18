import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../../api/api";
import "../../pages/news/news.css"; // Nhớ import đúng css mới
import { News } from "../../types/news";
import { FaCalendarAlt, FaArrowRight, FaClock } from "react-icons/fa";

const NewsPage: React.FC = () => {
  const [newsList, setNewsList] = useState<News[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  // Tăng pageSize lên 7 (1 bài Hero + 6 bài Grid) cho đẹp đội hình
  const pageSize = 7;

  useEffect(() => {
    let isMounted = true;
    const fetchNews = async () => {
      setLoading(true);
      try {
        const response = await api.get("/news", {
          params: { page: currentPage, size: pageSize, sort: "publishedAt,desc" },
        });
        if (isMounted) {
          setNewsList(response.data.content);
          setTotalPages(response.data.totalPages);
          setCurrentPage(response.data.number);
          setLoading(false);
        }
      } catch (error) {
        console.error("Lỗi tải tin:", error);
        if (isMounted) {
          setError("Không thể tải tin tức. Vui lòng thử lại sau.");
          setLoading(false);
        }
      }
    };
    fetchNews();
    return () => { isMounted = false; };
  }, [currentPage]);

  // Loading Skeleton đơn giản
  if (loading) {
    return (
      <div className="news-page-container" style={{display:'flex', justifyContent:'center', marginTop:'100px'}}>
        <div className="loader">Đang tải tạp chí công nghệ...</div>
      </div>
    );
  }

  if (error) return <div className="news-page-container">{error}</div>;

  // Tách bài đầu tiên làm Hero, các bài còn lại làm Grid
  const heroNews = newsList.length > 0 ? newsList[0] : null;
  const gridNews = newsList.length > 1 ? newsList.slice(1) : [];

  return (
    <div className="news-page-container">
      {/* Header */}
      <header className="news-header">
        <h1 className="news-title-page">Tạp Chí Công Nghệ</h1>
        <p className="news-subtitle">Cập nhật xu hướng, đánh giá sản phẩm và mẹo hay mỗi ngày</p>
      </header>

      {/* 1. HERO SECTION (Bài mới nhất) */}
      {heroNews && (
        <section className="news-hero-section">
          <Link to={`/news/${heroNews.id}`} className="news-hero-card">
            <img 
              src={heroNews.imageUrl || "https://via.placeholder.com/1200x600?text=PrimeShop+News"} 
              alt={heroNews.title} 
              onError={(e) => e.currentTarget.src = "https://via.placeholder.com/1200x600?text=No+Image"}
            />
            <div className="news-hero-overlay">
              <span className="hero-tag">Mới Nhất</span>
              <h2 className="news-hero-title">{heroNews.title}</h2>
              <div className="news-hero-meta">
                <span><FaCalendarAlt style={{marginRight: '5px'}}/> {new Date(heroNews.publishedAt).toLocaleDateString("vi-VN")}</span>
                <span><FaClock style={{marginRight: '5px'}}/> 5 phút đọc</span>
              </div>
            </div>
          </Link>
        </section>
      )}

      {/* 2. GRID LIST (Các bài còn lại) */}
      <section className="news-grid">
        {gridNews.map((news) => (
          <Link key={news.id} to={`/news/${news.id}`} className="news-card">
            <div className="news-card-image">
              <img 
                src={news.imageUrl || "https://via.placeholder.com/400x300"} 
                alt={news.title}
                onError={(e) => e.currentTarget.src = "https://via.placeholder.com/400x300?text=PrimeShop"}
              />
            </div>
            <div className="news-card-content">
              <h3 className="news-card-title">{news.title}</h3>
              <p className="news-card-excerpt">{news.excerpt}</p>
              <div className="news-card-footer">
                <span>{new Date(news.publishedAt).toLocaleDateString("vi-VN")}</span>
                <span className="read-more">Đọc tiếp <FaArrowRight size={12}/></span>
              </div>
            </div>
          </Link>
        ))}
      </section>

      {/* 3. PAGINATION */}
      <div className="pagination">
        <button onClick={() => setCurrentPage(p => Math.max(0, p - 1))} disabled={currentPage === 0}>&lt;</button>
        {Array.from({ length: totalPages }, (_, i) => (
          <button key={i} onClick={() => setCurrentPage(i)} className={currentPage === i ? "active" : ""}>
            {i + 1}
          </button>
        ))}
        <button onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))} disabled={currentPage === totalPages - 1}>&gt;</button>
      </div>
    </div>
  );
};

export default NewsPage;