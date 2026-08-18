import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { useCartStore } from "../../store/cartStore";
import styles from "./styles/ProductsDetails.module.css";
import api from "../../api/api";
import { Product } from "../../components/product-card/product-card";
import { Review } from "../../types/review";
import { Button, Card, CardContent, TextField } from "@mui/material";
import StarRatings from 'react-star-ratings';
import Swal from "sweetalert2";
// Import thêm icon cho Policy Box
import { FaShoppingCart, FaCreditCard, FaCommentDots, FaStore, FaStar, FaCircle, FaShieldAlt, FaTruck, FaUndo, FaCheckCircle } from "react-icons/fa";

// --- INTERFACE GIỮ NGUYÊN ---
interface Seller {
  id: number;
  userId: number;
  shopName: string;
  avatar?: string;
  identityCard?: string;
}

interface ProductDetail extends Product {
  seller: Seller;
  description?: string;
  specs?: { name: string; value: string }[];
}

const ProductDetailPage: React.FC = () => {
  const { slug } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCartStore();
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [productImages, setProductImages] = useState<string[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [rating, setRating] = useState<number>(0);
  const [reviewContent, setReviewContent] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(true);
  const [currentImageIndex, setCurrentImageIndex] = useState<number>(0);

  // --- LOGIC GIỮ NGUYÊN ---
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [productRes, imagesRes, reviewsRes] = await Promise.all([
          api.get(`/product/product-detail/${slug}`),
          api.get(`/product/images/${slug}`).catch(() => ({ data: [] })),
          api.get('/review', { params: { productSlug: slug } }).catch(() => ({ data: [] }))
        ]);

        setProduct(productRes.data);
        setProductImages(imagesRes.data);
        setReviews(reviewsRes.data);
        
        if (!productRes.data.seller.userId) {
             console.warn("⚠️ CẢNH BÁO: API chưa trả về userId của Seller!");
        }

      } catch (err) {
        console.error("Lỗi tải dữ liệu chi tiết:", err);
        navigate("/not-found");
      } finally {
        setLoading(false);
      }
    };
    if (slug) fetchData();
  }, [slug, navigate]);

  const handleAddToCart = async () => {
    const token = localStorage.getItem('token');
    if (!token) return navigate('/login');

    try {
      await api.post("/cart/add", { productSlug: product?.slug, quantity: 1 });
      addToCart({ ...product, quantity: 1 } as any); 
      toast.success(`Đã thêm vào giỏ hàng!`);
    } catch (err) {
      toast.error("Lỗi khi thêm vào giỏ hàng.");
    }
  };

  const handleBuyNow = () => {
    const token = localStorage.getItem('token');
    if (!token) return navigate('/login');
    handleAddToCart();
    navigate("/cart");
  };

  const handleChatWithShop = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
        toast.info("Vui lòng đăng nhập để chat với Shop!");
        return navigate('/login');
    }

    if (!product?.seller.userId) {
        toast.error("Lỗi hệ thống: Không xác định được chủ shop.");
        return;
    }
    
    try {
        toast.info("Đang kết nối...");
        const res = await api.post('/chat/create', null, { 
            params: { sellerId: product.seller.userId } 
        });
        const chatEvent = new CustomEvent('PRIMESHOP_OPEN_CHAT', { detail: res.data });
        window.dispatchEvent(chatEvent);
    } catch (error) {
        console.error("Lỗi tạo hội thoại:", error);
        toast.error("Không thể kết nối với Shop lúc này.");
    }
};

  const handleSubmitReview = async () => {
    if (!rating) return toast.warning("Vui lòng chọn số sao đánh giá!");
    try {
      await api.post("/review", { productSlug: product?.slug, rating, content: reviewContent });
      Swal.fire("Thành công!", "Cảm ơn đánh giá của bạn.", "success").then(() => window.location.reload());
    } catch (err) {
      toast.error("Không thể gửi đánh giá.");
    }
  };

  if (loading || !product) {
    return <div className={styles.productPage}><div style={{textAlign:'center', marginTop: 100}}>Đang tải...</div></div>;
  }

  const displayImage = productImages[currentImageIndex] || product.imageUrl || "https://via.placeholder.com/400";
  const sellerAvatar = product.seller.identityCard || "https://cdn-icons-png.flaticon.com/512/1041/1041846.png"; 

  return (
    <div className={styles.productPage}>
      <ToastContainer position="top-right" autoClose={2000} />
      
      {/* 1. BREADCRUMB (Mới) */}
      <div className={styles.breadcrumb}>
        <span onClick={() => navigate('/')}>Trang chủ</span> / 
        <span onClick={() => navigate('/all-products')}> Sản phẩm</span> / 
        <span className={styles.activeBreadcrumb}> {product.name}</span>
      </div>

      <section className={styles.productOverview}>
        <div className={styles.productImages}>
          <div className={styles.mainImage}>
            <img src={displayImage} alt={product.name} />
          </div>
          <div className={styles.thumbnailImages}>
            {productImages.slice(0, 5).map((img, index) => (
              <button
                key={index}
                className={`${styles.thumbnailButton} ${currentImageIndex === index ? styles.thumbnailActive : ""}`}
                onClick={() => setCurrentImageIndex(index)}
              >
                <img src={img} alt="thumb" className={styles.thumbnail} />
              </button>
            ))}
          </div>
        </div>

        <div className={styles.productInfo}>
          <h1>{product.name}</h1>
          
          <div className={styles.metaInfo}>
            <div className={styles.rating}>
              <span className={styles.starIcon}><FaStar /></span>
              <span>{reviews.length > 0 ? (reviews.reduce((a, b) => a + b.rating, 0) / reviews.length).toFixed(1) : "5.0"}</span>
              <span style={{color: '#d1d5db'}}>|</span>
              <span>{reviews.length} Đánh giá</span>
            </div>
            <div className={styles.soldCount}>
              Đã bán {product.sold >= 1000 ? `${(product.sold/1000).toFixed(1)}k` : product.sold}
            </div>
          </div>

          <div className={styles.priceWrapper}>
            {product.isDiscounted ? (
              <>
                <span className={styles.originalPrice}>{product.price.toLocaleString("vi-VN")}₫</span>
                <span className={styles.currentPrice}>{product.discountPrice?.toLocaleString("vi-VN")}₫</span>
                <span className={styles.discountBadge}>GIẢM {product.discountPercent}%</span>
              </>
            ) : (
              <span className={styles.currentPrice}>{product.price.toLocaleString("vi-VN")}₫</span>
            )}
          </div>

          <div className={styles.sellerSection}>
            <div className={styles.sellerContainer}>
                <div className={styles.sellerProfile}>
                    <img src={sellerAvatar} alt={product.seller.shopName} className={styles.sellerAvatar} />
                    <div className={styles.sellerInfo}>
                        <h3>{product.seller.shopName}</h3>
                        <span className={styles.sellerStatus}>
                            <FaCircle size={8} /> Online
                        </span>
                    </div>
                </div>
                <div className={styles.sellerActions}>
                    <button className={styles.btnChat} onClick={handleChatWithShop}>
                        <FaCommentDots /> Chat Ngay
                    </button>
                    <button className={styles.btnViewShop} onClick={() => navigate(`/shop/${product.seller.id}`)}>
                        <FaStore /> Xem Shop
                    </button>
                </div>
            </div>
          </div>

          <div className={styles.actions}>
            <button className={styles.addToCart} onClick={handleAddToCart}>
              <FaShoppingCart /> Thêm vào giỏ
            </button>
            <button className={styles.buyNow} onClick={handleBuyNow}>
              <FaCreditCard /> Mua ngay
            </button>
          </div>

          {/* 2. POLICY BOX (Mới - Trust Signals) */}
          <div className={styles.policyBox}>
              <div className={styles.policyItem}><FaCheckCircle className={styles.policyIcon}/> Hàng chính hãng 100%</div>
              <div className={styles.policyItem}><FaTruck className={styles.policyIcon}/> Miễn phí vận chuyển</div>
              <div className={styles.policyItem}><FaShieldAlt className={styles.policyIcon}/> Bảo hành 12 tháng</div>
              <div className={styles.policyItem}><FaUndo className={styles.policyIcon}/> Đổi trả trong 7 ngày</div>
          </div>

        </div>
      </section>

      <section className={styles.contentGrid}>
         <div className={styles.sectionBox}>
            <h2 className={styles.sectionTitle}>Mô tả sản phẩm</h2>
            <div className={styles.descriptionText}>
                {product.description || "Đang cập nhật mô tả..."}
            </div>
         </div>

         <div className={styles.sectionBox}>
            <h2 className={styles.sectionTitle}>Thông số kỹ thuật</h2>
            {/* 3. Specs List (CSS sẽ xử lý Zebra Striping) */}
            <ul className={styles.specsList}>
                {product.specs?.length > 0 ? product.specs.map((spec, idx) => (
                    <li key={idx}>
                        <strong>{spec.name}</strong>
                        <span>{spec.value}</span>
                    </li>
                )) : (
                    <li><em>Chưa có thông số chi tiết</em></li>
                )}
            </ul>
         </div>
      </section>

      <section className={styles.sectionBox}>
         <h2 className={styles.sectionTitle}>Đánh giá ({reviews.length})</h2>
         
         <div className={styles.reviewFormBox}>
            <h4 style={{marginTop: 0, marginBottom: '1rem'}}>Viết đánh giá của bạn</h4>
            <div style={{marginBottom: '1rem'}}>
                <StarRatings
                    rating={rating}
                    starRatedColor="#f59e0b"
                    starHoverColor="#f59e0b"
                    changeRating={setRating}
                    numberOfStars={5}
                    starDimension="24px"
                    starSpacing="2px"
                />
            </div>
            <TextField
                fullWidth
                multiline
                rows={3}
                variant="outlined"
                placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..."
                value={reviewContent}
                onChange={(e) => setReviewContent(e.target.value)}
                style={{background: 'white'}}
            />
            <Button 
                variant="contained" 
                color="primary" 
                onClick={handleSubmitReview}
                style={{marginTop: '1rem', textTransform: 'none', fontWeight: 600}}
            >
                Gửi đánh giá
            </Button>
         </div>

         <div>
            {reviews.length > 0 ? reviews.map(review => (
                <div key={review.id} className={styles.reviewCard}>
                    <div className={styles.reviewHeader}>
                        <img 
                            src="https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png" 
                            alt="user" 
                            className={styles.reviewAvatar} 
                        />
                        <div>
                            <span className={styles.reviewName}>{review.username || "Người dùng ẩn danh"}</span>
                            <div style={{fontSize: 12, color: '#f59e0b'}}>
                                {Array(review.rating).fill("★").join("")}
                            </div>
                        </div>
                    </div>
                    <p className={styles.reviewContent}>{review.content}</p>
                </div>
            )) : (
                <p style={{color: '#6b7280', fontStyle: 'italic'}}>Chưa có đánh giá nào. Hãy là người đầu tiên!</p>
            )}
         </div>
      </section>
    </div>
  );
};

export default ProductDetailPage;