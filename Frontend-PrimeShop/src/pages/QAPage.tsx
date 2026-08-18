import React, { useState, useEffect } from "react";
import "../assets/css/qa.css";
// Import Icons cho sinh động
import { 
  FaSearch, FaChevronDown, FaPhoneAlt, FaCommentDots, 
  FaShippingFast, FaCreditCard, FaShieldAlt, FaUserCircle, FaThList 
} from "react-icons/fa";

// Định nghĩa kiểu dữ liệu cho câu hỏi
interface QAItem {
  id: number;
  category: string; // 'transport', 'payment', 'warranty', 'account'
  question: string;
  answer: string;
}

const QAPage = () => {
  const [questions, setQuestions] = useState<QAItem[]>([]);
  const [filteredQuestions, setFilteredQuestions] = useState<QAItem[]>([]);
  const [loading, setLoading] = useState(true);
  
  // State quản lý UI
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("all");

  // Dữ liệu Mock (Sau này thay bằng API)
  useEffect(() => {
    const mockQuestions: QAItem[] = [
      {
        id: 1,
        category: "transport",
        question: "PrimeShop có giao hàng toàn quốc không?",
        answer: "Dạ có ạ! PrimeShop hỗ trợ giao hàng tới 63 tỉnh thành trên toàn quốc thông qua các đối tác uy tín như Giao Hàng Nhanh, Viettel Post, Ninja Van.",
      },
      {
        id: 2,
        category: "transport",
        question: "Thời gian giao hàng là bao lâu?",
        answer: "Nội thành TP.HCM & Hà Nội: 1-2 ngày. Các tỉnh thành khác: 3-5 ngày làm việc (không tính Chủ Nhật và ngày lễ).",
      },
      {
        id: 3,
        category: "payment",
        question: "Tôi có thể thanh toán khi nhận hàng (COD) không?",
        answer: "Được ạ. PrimeShop hỗ trợ thanh toán tiền mặt khi nhận hàng (COD) cho đơn hàng dưới 10 triệu đồng. Với đơn hàng giá trị cao hơn, vui lòng đặt cọc hoặc thanh toán online.",
      },
      {
        id: 4,
        category: "payment",
        question: "Hỗ trợ những phương thức thanh toán nào?",
        answer: "Chúng tôi chấp nhận: Tiền mặt (COD), Chuyển khoản ngân hàng, Thẻ ATM/Visa/Mastercard, Ví MoMo, VNPAY, và Trả góp qua thẻ tín dụng.",
      },
      {
        id: 5,
        category: "warranty",
        question: "Chính sách đổi trả trong bao lâu?",
        answer: "Bạn được quyền 1 đổi 1 trong vòng 30 ngày đầu nếu sản phẩm có lỗi từ nhà sản xuất. Sản phẩm phải còn nguyên hộp, tem nhãn và không bị trầy xước.",
      },
      {
        id: 6,
        category: "warranty",
        question: "Làm sao để kích hoạt bảo hành điện tử?",
        answer: "Tất cả sản phẩm tại PrimeShop đều được kích hoạt bảo hành điện tử tự động qua số điện thoại mua hàng ngay khi đơn hàng hoàn tất.",
      },
      {
        id: 7,
        category: "account",
        question: "Tôi quên mật khẩu thì phải làm sao?",
        answer: "Bạn hãy bấm vào nút 'Quên mật khẩu' ở trang Đăng nhập, sau đó nhập email đã đăng ký. Hệ thống sẽ gửi link đặt lại mật khẩu cho bạn.",
      },
    ];
    setQuestions(mockQuestions);
    setFilteredQuestions(mockQuestions);
    setLoading(false);
  }, []);

  // Logic Lọc (Filter)
  useEffect(() => {
    const result = questions.filter((item) => {
      const matchCategory = selectedCategory === "all" || item.category === selectedCategory;
      const matchSearch = item.question.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          item.answer.toLowerCase().includes(searchTerm.toLowerCase());
      return matchCategory && matchSearch;
    });
    setFilteredQuestions(result);
    setExpandedId(null); // Reset accordion khi lọc
  }, [searchTerm, selectedCategory, questions]);

  const toggleExpand = (id: number) => {
    setExpandedId(expandedId === id ? null : id);
  };

  // Danh sách danh mục để render Tabs
  const categories = [
    { id: "all", label: "Tất cả", icon: <FaThList /> },
    { id: "transport", label: "Vận chuyển", icon: <FaShippingFast /> },
    { id: "payment", label: "Thanh toán", icon: <FaCreditCard /> },
    { id: "warranty", label: "Bảo hành", icon: <FaShieldAlt /> },
    { id: "account", label: "Tài khoản", icon: <FaUserCircle /> },
  ];

  if (loading) return <div className="qa-page-wrapper" style={{padding:'50px', textAlign:'center'}}>Đang tải dữ liệu...</div>;

  return (
    <div className="qa-page-wrapper">
      
      {/* 1. HERO SEARCH */}
      <section className="qa-hero-section">
        <h1 className="qa-hero-title">Xin chào, chúng tôi có thể giúp gì?</h1>
        <p className="qa-hero-subtitle">Tìm kiếm câu trả lời nhanh chóng cho các vấn đề của bạn</p>
        
        <div className="qa-search-container">
          <FaSearch className="qa-search-icon" />
          <input 
            type="text" 
            className="qa-search-box" 
            placeholder="Nhập từ khóa (ví dụ: giao hàng, hoàn tiền...)"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </section>

      {/* 2. CATEGORY TABS */}
      <section className="qa-categories">
        {categories.map((cat) => (
          <div 
            key={cat.id} 
            className={`category-tab ${selectedCategory === cat.id ? "active" : ""}`}
            onClick={() => setSelectedCategory(cat.id)}
          >
            <span className="category-icon">{cat.icon}</span>
            {cat.label}
          </div>
        ))}
      </section>

      {/* 3. ACCORDION LIST */}
      <section className="qa-content-container">
        <div className="qa-list">
          {filteredQuestions.length > 0 ? (
            filteredQuestions.map((item) => (
              <div 
                key={item.id} 
                className={`qa-item ${expandedId === item.id ? "active" : ""}`}
              >
                <div className="qa-question" onClick={() => toggleExpand(item.id)}>
                  {item.question}
                  <div className="toggle-icon">
                    <FaChevronDown />
                  </div>
                </div>
                <div className="qa-answer">
                  <p>{item.answer}</p>
                </div>
              </div>
            ))
          ) : (
            <div className="no-result">
              <p>🤔 Không tìm thấy kết quả nào cho từ khóa "{searchTerm}"</p>
            </div>
          )}
        </div>

        {/* 4. CTA SECTION */}
        <div className="qa-cta-section">
          <h2 className="qa-cta-title">Vẫn chưa tìm thấy câu trả lời?</h2>
          <p className="qa-cta-desc">Đừng lo lắng, đội ngũ hỗ trợ của chúng tôi luôn sẵn sàng lắng nghe bạn.</p>
          <div className="qa-cta-buttons">
            <a href="tel:19009099" className="cta-btn cta-hotline">
              <FaPhoneAlt /> Gọi Hotline 1900 9099
            </a>
            <a href="https://zalo.me/primeshop" target="_blank" rel="noreferrer" className="cta-btn cta-chat">
              <FaCommentDots /> Chat ngay với tư vấn viên
            </a>
          </div>
        </div>
      </section>

    </div>
  );
};

export default QAPage;