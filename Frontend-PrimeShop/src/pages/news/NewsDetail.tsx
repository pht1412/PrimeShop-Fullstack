import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import DOMPurify from "dompurify";
import "../../pages/news/newsDetail.css";
import { News } from "../../types/news";
import api from "../../api/api";
import { FaCalendarAlt, FaUser } from "react-icons/fa";

const NewsDetail2: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [news, setNews] = useState<News | null>(null);
  const [relatedNews, setRelatedNews] = useState<News[]>([]);
  const [loading, setLoading] = useState(true);
  const [htmlContent, setHtmlContent] = useState<string | null>(null);
  const [scrollProgress, setScrollProgress] = useState(0);
  const [toc, setToc] = useState<string[]>([]); // Table of Contents

  // 1. Fetch News Detail & Related News
  useEffect(() => {
    let isMounted = true;
    const fetchData = async () => {
      setLoading(true);
      try {
        // Fetch Current News
        const newsRes = await api.get(`/news/${id}`);
        if (isMounted) {
          setNews(newsRes.data);
          
          // Fetch Content HTML from textUrl
          if (newsRes.data.textUrl) {
            try {
              const htmlRes = await fetch(newsRes.data.textUrl);
              if (htmlRes.ok) {
                const rawHtml = await htmlRes.text();
                // Sanitize
                const cleanHtml = DOMPurify.sanitize(rawHtml, {
                  USE_PROFILES: { html: true },
                  FORBID_TAGS: ['script', 'iframe']
                });
                setHtmlContent(cleanHtml);
                
                // Extract H2 headers for TOC (Simple Regex)
                const headers = cleanHtml.match(/<h2.*?>(.*?)<\/h2>/g);
                if (headers) {
                  const titles = headers.map(h => h.replace(/<[^>]+>/g, ''));
                  setToc(titles);
                }
              }
            } catch (e) { console.error("Error loading HTML content"); }
          }
        }

        // Fetch Related News (Latest 3 excluding current)
        const relatedRes = await api.get('/news', { params: { size: 4, sort: "publishedAt,desc" } });
        if (isMounted) {
          // Filter out current news ID
          const filtered = relatedRes.data.content.filter((n: News) => String(n.id) !== id).slice(0, 3);
          setRelatedNews(filtered);
        }

        setLoading(false);
      } catch (err) {
        console.error(err);
        setLoading(false);
      }
    };

    fetchData();
    window.scrollTo(0, 0); // Scroll to top on change
    return () => { isMounted = false; };
  }, [id]);

  // 2. Scroll Progress Logic
  useEffect(() => {
    const handleScroll = () => {
      const totalHeight = document.body.scrollHeight - window.innerHeight;
      const progress = (window.scrollY / totalHeight) * 100;
      setScrollProgress(progress);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  if (loading) return <div style={{padding:'100px', textAlign:'center'}}>Đang tải bài viết...</div>;
  if (!news) return <div style={{padding:'100px', textAlign:'center'}}>Không tìm thấy bài viết</div>;

  return (
    <div className="news-detail-wrapper">
      {/* 🟢 Reading Progress Bar */}
      <div className="reading-progress-container">
        <div className="reading-progress-bar" style={{ width: `${scrollProgress}%` }}></div>
      </div>

      {/* 🔵 Hero Header */}
      <header className="news-detail-header">
        <span className="news-detail-category">Tin Công Nghệ</span>
        <h1 className="news-detail-title">{news.title}</h1>
        <div className="news-detail-meta">
          <span><FaCalendarAlt style={{marginRight:5}}/> {new Date(news.publishedAt).toLocaleDateString("vi-VN")}</span>
          <span><FaUser style={{marginRight:5}}/> Admin PrimeShop</span>
        </div>
      </header>

      <div className="news-content-container">
        {/* 🟠 Main Content Column */}
        <div className="news-main-col">
          <img 
            src={news.imageUrl || "https://via.placeholder.com/800x400"} 
            alt={news.title} 
            className="news-featured-image"
            onError={(e) => e.currentTarget.src = "https://via.placeholder.com/800x400"}
          />
          
          <div className="news-body-content">
            <p className="news-excerpt" style={{fontWeight: 'bold', fontSize: '1.2rem'}}>
              {news.excerpt}
            </p>
            {htmlContent ? (
              <div dangerouslySetInnerHTML={{ __html: htmlContent }} />
            ) : (
              <p>Nội dung chi tiết đang được cập nhật...</p>
            )}
          </div>
        </div>

        {/* 🟣 Sidebar Column (Sticky) */}
        <aside className="news-sidebar-col">
          <div className="sticky-sidebar">
            {/* Table of Contents */}
            {toc.length > 0 && (
              <div className="toc-box">
                <div className="toc-title">Mục lục bài viết</div>
                <ul className="toc-list">
                  {toc.map((header, index) => (
                    <li key={index} onClick={() => alert("Tính năng cuộn đang phát triển!")}>
                      {index + 1}. {header}
                    </li>
                  ))}
                </ul>
              </div>
            )}
            
            {/* Mini Promo Banner (Mockup) */}
            <div style={{background: '#EFF6FF', padding: '20px', borderRadius: '12px', textAlign: 'center'}}>
              <h4 style={{color:'#1E3A8A', marginBottom:'10px'}}>PrimeShop Sale</h4>
              <p style={{fontSize:'0.9rem', marginBottom:'15px'}}>Săn deal công nghệ giá sốc ngay hôm nay!</p>
              <Link to="/all-products" style={{display:'inline-block', background:'#2563EB', color:'white', padding:'8px 16px', borderRadius:'6px', textDecoration:'none', fontSize:'0.9rem'}}>Mua Ngay</Link>
            </div>
          </div>
        </aside>
      </div>

      {/* 🟡 Related Posts Section */}
      <section className="related-posts-section">
        <h2 className="related-title">Bài Viết Liên Quan</h2>
        <div className="related-grid">
          {relatedNews.map((item) => (
            <Link key={item.id} to={`/news/${item.id}`} className="related-card">
              <img src={item.imageUrl || "https://via.placeholder.com/300x200"} alt={item.title} />
              <div className="related-card-body">
                <h4 className="related-card-title">{item.title}</h4>
                <span className="related-card-date">{new Date(item.publishedAt).toLocaleDateString("vi-VN")}</span>
              </div>
            </Link>
          ))}
        </div>
        <div style={{textAlign:'center', marginTop:'3rem'}}>
           <Link to="/news" className="back-button">Xem tất cả tin tức</Link>
        </div>
      </section>
    </div>
  );
};

export default NewsDetail2;