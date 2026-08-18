import React, { useEffect } from "react";
import "../assets/css/about.css";
import { Link } from "react-router-dom";
import { FaRocket, FaHandshake, FaLightbulb, FaUserTie } from "react-icons/fa";

const PageAbout: React.FC = () => {
  
  // Logic Scroll Reveal
  useEffect(() => {
    const reveal = () => {
      const reveals = document.querySelectorAll(".about-reveal");
      for (let i = 0; i < reveals.length; i++) {
        const windowHeight = window.innerHeight;
        const elementTop = reveals[i].getBoundingClientRect().top;
        const elementVisible = 100;
        if (elementTop < windowHeight - elementVisible) {
          reveals[i].classList.add("active");
        }
      }
    };
    window.addEventListener("scroll", reveal);
    reveal(); 
    return () => window.removeEventListener("scroll", reveal);
  }, []);

  return (
    <div className="about-body">
      
      {/* 1. HERO BANNER */}
      <section className="about-hero">
        <div className="about-hero-content">
          <h1>Hành Trình Kiến Tạo Công Nghệ</h1>
          <p>
            Từ một cửa hàng nhỏ năm 2015, PrimeShop đã vươn mình trở thành 
            biểu tượng uy tín hàng đầu trong lĩnh vực bán lẻ thiết bị công nghệ tại Việt Nam.
          </p>
        </div>
      </section>

      {/* 2. STATS SECTION */}
      <section className="about-stats-section">
        <div className="stats-grid">
          <div className="stat-item">
            <h3>10+</h3>
            <p>Năm Kinh Nghiệm</p>
          </div>
          <div className="stat-item">
            <h3>50+</h3>
            <p>Cửa Hàng Toàn Quốc</p>
          </div>
          <div className="stat-item">
            <h3>1M+</h3>
            <p>Khách Hàng Tin Dùng</p>
          </div>
          <div className="stat-item">
            <h3>100%</h3>
            <p>Sản Phẩm Chính Hãng</p>
          </div>
        </div>
      </section>

      {/* 3. STORY SECTION */}
      <section className="about-story-section">
        <div className="about-story-container about-reveal">
          <div className="about-story-text">
            <h2>Khởi Nguồn Đam Mê</h2>
            <p>
              Thành lập vào năm 2015, PrimeShop bắt đầu với một niềm tin đơn giản: 
              "Công nghệ phải dành cho mọi người". Chúng tôi nhận thấy thị trường lúc bấy giờ 
              đầy rẫy hàng giả và giá cả không minh bạch.
            </p>
            <p>
              Đội ngũ sáng lập đã quyết tâm xây dựng một điểm đến nơi mà <strong>Uy Tín</strong> 
              được đặt lên hàng đầu.
            </p>
          </div>
          <div className="about-story-image">
            <img 
              src="https://images.unsplash.com/photo-1556761175-5973dc0f32e7?auto=format&fit=crop&q=80&w=800" 
              alt="PrimeShop Founder" 
            />
          </div>
        </div>
      </section>

      {/* 🆕 TIMELINE SECTION (HORIZONTAL ZIG-ZAG) */}
      <section className="timeline-section about-reveal">
        <h2 className="timeline-title">Lộ Trình Phát Triển</h2>
        <div className="timeline">
          
          {/* Item 1 */}
          <div className="timeline-item">
            <div className="timeline-dot"></div>
            <div className="timeline-content">
              <span className="timeline-year">Tháng 3 2025</span>
              <h3>Khởi Đầu</h3>
              <p>Thành lập đội ngũ phát triển và tiến hành xây dựng nền tảng công nghệ đầu tiên.</p>
            </div>
          </div>

          {/* Item 2 */}
          <div className="timeline-item">
            <div className="timeline-dot"></div>
            <div className="timeline-content">
              <span className="timeline-year">Tháng 5 2025</span>
              <h3>Mở Rộng</h3>
              <p>Trình bày sản phẩm đầu tiên, đạt mức độ hoàn thiện cơ bản cho nền tảng công nghệ.</p>
            </div>
          </div>

          {/* Item 3 */}
          <div className="timeline-item">
            <div className="timeline-dot"></div>
            <div className="timeline-content">
              <span className="timeline-year">Tháng 7 2025</span>
              <h3>Chuyển Đổi Số</h3>
              <p>Ra mắt Website TMĐT, phục vụ khách hàng online toàn quốc.</p>
            </div>
          </div>

          {/* Item 4 */}
          <div className="timeline-item">
            <div className="timeline-dot"></div>
            <div className="timeline-content">
              <span className="timeline-year">Tháng 12 2025</span>
              <h3>Vươn Tầm</h3>
              <p>Hưởng ứng kỷ nguyên vươn mình, mở rộng quy mô dự án</p>
            </div>
          </div>

        </div>
      </section>

      {/* 4. VALUES SECTION */}
      <section className="about-values-section about-reveal">
        <h2 className="values-title">Giá Trị Cốt Lõi</h2>
        <div className="values-grid">
          <div className="about-value-card">
            <div className="value-icon">💎</div>
            <h3>Chất Lượng</h3>
            <p>Chỉ phân phối sản phẩm chính hãng, nguồn gốc rõ ràng. Nói không với hàng kém chất lượng.</p>
          </div>
          <div className="about-value-card">
            <div className="value-icon">❤️</div>
            <h3>Tận Tâm</h3>
            <p>Khách hàng là trung tâm. Dịch vụ hậu mãi và tư vấn luôn được ưu tiên hàng đầu.</p>
          </div>
          <div className="about-value-card">
            <div className="value-icon">🚀</div>
            <h3>Tốc Độ</h3>
            <p>Giao hàng thần tốc, quy trình bảo hành nhanh gọn, tiết kiệm thời gian cho khách hàng.</p>
          </div>
        </div>
      </section>

      {/* 5. TEAM SECTION */}
      <section className="about-team-section about-reveal">
        <h2 className="team-title">Gặp gỡ đội ngũ phát triển PrimeShop</h2>
        <div className="team-grid">
          <div className="about-team-member">
            <div className="member-avatar">
              <img src="/public/images/pht.jpg" alt="CEO" />
            </div>
            <h4>Lê Hồng Phát</h4>
            <span>Leader + Fullstack developer</span>
          </div>
          <div className="about-team-member">
            <div className="member-avatar">
              <img src="/public/images/tphat.jpg" alt="CTO" />
            </div>
            <h4>Võ Tấn Phát</h4>
            <span>FullStack developer</span>
          </div>
          <div className="about-team-member">
            <div className="member-avatar">
              <img src="/public/images/thoai.jpg" alt="CFO" />
            </div>
            <h4>Trần Thanh Hoài</h4>
            <span>Fullstack developer</span>
          </div>
        </div>
        
        <div style={{marginTop: '3rem'}}>
           <Link to="/contact" className="about-cta-btn">
             Liên hệ hợp tác ngay
           </Link>
        </div>
      </section>

    </div>
  );
};

export default PageAbout;