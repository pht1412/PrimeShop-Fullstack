import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { useCartStore } from "../../store/cartStore";
import { CartItem } from "../../types/cart";
import { mockProducts } from "../../mocks/mockData";
import styles from "./styles/ProductsDetails.module.css";


// Dữ liệu khuyến mãi mẫu (vì mockProducts không có trường này)
const mockPromotions = [
  "🎁 Giảm ngay 500.000đ khi thanh toán qua Momo.",
  "🚀 Miễn phí giao hàng toàn quốc.",
  "💳 Trả góp 0% qua thẻ tín dụng.",
  "📦 Đổi trả miễn phí trong 7 ngày.",
  "🎧 Tặng tai nghe khi mua trong hôm nay.",
];

const ProductsDetailsPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCartStore();
  

  // Tìm sản phẩm từ mockProducts
  const productData = mockProducts.find((product) => product.id === id);


  // State quản lý
  const [selectedStorage, setSelectedStorage] = useState<string>(
    productData?.storageOptions[0] || ""
  );
  const [quantity, setQuantity] = useState<number>(1);
  const [currentImageIndex, setCurrentImageIndex] = useState<number>(0);

  if (!productData) {
    return <p className={styles.error}>Không tìm thấy sản phẩm.</p>;
  }

  // Giả lập hình ảnh (vì mockProducts chỉ có 1 ảnh, mình thêm ảnh mẫu)
  const productImages = [
    productData.image,
    "https://via.placeholder.com/300?text=Product+Image+2",
    "https://via.placeholder.com/300?text=Product+Image+3",
  ];

  // Giá thay đổi theo dung lượng
  const priceOptions: { [key: string]: number } = {};
  productData.storageOptions.forEach((option, index) => {
    const multiplier = 1 + index * 0.2; // Giả lập: 128GB giá gốc, 256GB +20%, 512GB +40%
    priceOptions[option] = Math.round(productData.price * multiplier);
  });
  const currentPrice = priceOptions[selectedStorage];

  // Thông số kỹ thuật (giả lập từ mockProducts)
  const specifications = {
    screen: `${productData.screenType} ${productData.screenSize}`,
    brand: productData.brand,
    storage: productData.storageOptions.join(" / "),
    category: productData.category,
  };

  // Xử lý thêm vào giỏ hàng
  const handleAddToCart = () => {
    const product: CartItem = {
      productId: productData.id,
      name: `${productData.name} (${selectedStorage})`,
      price: currentPrice,
      image: productImages[currentImageIndex],
      quantity,
    };
    addToCart(product);
    toast.success(`Đã thêm ${quantity} ${productData.name} vào giỏ hàng!`, {
      position: "top-right",
      autoClose: 2000,
    });
  };

  // Xử lý mua ngay
  const handleBuyNow = () => {
    handleAddToCart();
    navigate("/cart");
  };

  // Xử lý chuyển đổi hình ảnh
  const handleImageChange = (index: number) => {
    setCurrentImageIndex(index);
  };

  return (
    <div className={styles.productPage}>
      {/* Thông báo Toastify */}
      <ToastContainer />

      {/* SECTION 1: Tổng quan sản phẩm */}
      <section className={styles.productOverview}>
        {/* Bên trái: Hình ảnh sản phẩm */}
        <div className={styles.productImages}>
          <div className={styles.mainImage}>
            <img
              src={productImages[currentImageIndex]}
              alt={productData.name}
            />
          </div>
          <div className={styles.thumbnailImages}>
            {productImages.map((image, index) => (
              <button
                key={image}
                className={`${styles.thumbnailButton} ${
                  currentImageIndex === index ? styles.thumbnailActive : ""
                }`}
                onClick={() => handleImageChange(index)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    handleImageChange(index);
                  }
                }}
                aria-label={`Select thumbnail ${index + 1}`}
              >
                <img
                  src={image}
                  alt={`Thumbnail ${index + 1}`}
                  className={styles.thumbnail}
                />
              </button>
            ))}
          </div>
        </div>

        {/* Ở giữa: Thông tin chính */}
        <div className={styles.productInfo}>
          <h1>{productData.name}</h1>
          <p className={styles.price}>{currentPrice.toLocaleString("vi-VN")}đ</p>
          <div className={styles.priceInfo}>
            <span className={styles.originalPrice}>
              {productData.originalPrice.toLocaleString("vi-VN")}đ
            </span>
            <span className={styles.discount}>-{productData.discount}%</span>
          </div>
          <p className={styles.rating}>
            ⭐ {productData.rating} • Đã bán{" "}
            {productData.sold >= 1000
              ? `${(productData.sold / 1000).toFixed(1)}K`
              : productData.sold}
          </p>

          {/* Chọn dung lượng */}
          <p className={styles.label}>Dung lượng:</p>
          <div className={styles.storageOptions}>
            {productData.storageOptions.map((storage) => (
              <button
                key={storage}
                className={`${styles.storageButton} ${
                  selectedStorage === storage ? styles.storageButtonActive : ""
                }`}
                onClick={() => setSelectedStorage(storage)}
              >
                {storage}
              </button>
            ))}
          </div>

          {/* Số lượng */}
          <p className={styles.label}>Số lượng:</p>
          <div className={styles.quantity}>
            <button
              onClick={() => setQuantity((q) => Math.max(1, q - 1))}
              disabled={quantity <= 1}
            >
              -
            </button>
            <span>{quantity}</span>
            <button onClick={() => setQuantity((q) => q + 1)}>+</button>
          </div>

          {/* Nút hành động */}
          <div className={styles.actions}>
            <button className={styles.buyNow} onClick={handleBuyNow}>
              Mua ngay
            </button>
            <button className={styles.addToCart} onClick={handleAddToCart}>
              Thêm vào giỏ hàng
            </button>
          </div>
        </div>

        {/* Bên phải: Khuyến mãi */}
        <div className={styles.promotions}>
          <h3>🎉 Khuyến mãi khi mua hàng</h3>
          <ul>
            {mockPromotions.map((promo) => (
              <li key={promo}>{promo}</li>
            ))}
          </ul>
        </div>
      </section>

      {/* SECTION 2: Chi tiết sản phẩm */}
      <section className={styles.productDetails}>
        {/* Bên trái: Thông tin sản phẩm */}
        <div className={styles.description}>
          <h2>Thông tin sản phẩm</h2>
          <p>{productData.description}</p>
        </div>

        {/* Bên phải: Thông số kỹ thuật */}
        <div className={styles.specifications}>
          <h2>Thông số kỹ thuật</h2>
          <ul>
            {Object.entries(specifications).map(([key, value]) => (
              <li key={key}>
                <strong>{key.charAt(0).toUpperCase() + key.slice(1)}:</strong>{" "}
                {value}
              </li>
            ))}
          </ul>
        </div>
      </section>
    </div>
  );
};

export default ProductsDetailsPage;