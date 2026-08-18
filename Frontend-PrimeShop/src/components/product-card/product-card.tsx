import React, { useState } from "react";
import { Link } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import styles from "./styles/ProductCard.module.css";
import { components } from "../../types/api-types";
import { Review } from "../../types/review";
// import { ProductCardType } from "../../types/product";

export type Product = components["schemas"]["ProductResponse"]
export type ProductSpecs = components["schemas"]["ProductSpecResponse"]

// Định nghĩa type cho props
interface ProductCardProps {
  // id: string;
  name: string;
  price: number;
  originalPrice: number;
  discount: number;
  discountPrice: number;
  image: string;
  brand: string;
  // screenType: string;
  // screenSize: string;
  // storageOptions: string[];
  reviews: Review[];
  sold: number;
}

interface Spec {
  name: String,
  value: String
}

const ProductCard: React.FC<Product> = ({
  // id,
  name,
  slug,
  brand,
  price,
  discountPercent,
  discountPrice,
  imageUrl,
  stock,
  category,
  specs,
  isDiscounted,
  rating,
  sold,
  // originalPrice,
  // discount,
  // screenType,
  // screenSize,
  // storageOptions,
  // rating,
  // sold,
}) => {
  // const [selectedStorage, setSelectedStorage] = useState<string>(storageOptions[0]);

  return (
    <div className={styles.productCard}>
      <Link to={`/product-detail/${slug}`}>
      {/* Thông báo Toastify */}
      <ToastContainer />

      {/* Hình ảnh sản phẩm */}
      <div className={styles.imageWrapper}>
        <img src={imageUrl} alt={name} className={styles.productImage} />
      </div>

      {/* Thông tin sản phẩm */}
      <div className={styles.productInfo}>
        <p className={styles.productBrand}>{brand}</p>
        <Link to={`/product-detail/${slug}`} className={styles.productName}>
          <h3>{name}</h3>
        </Link>
        {/* <p className={styles.screenInfo}>
          {screenType} {screenSize}
        </p> */}

        {/* Tùy chọn bộ nhớ */}
        {/* <div className={styles.storageOptions}>
          {storageOptions.map((option) => (
            <button
              key={option}
              className={`${styles.storageButton} ${
                selectedStorage === option ? styles.storageButtonActive : ""
              }`}
              onClick={() => setSelectedStorage(option)}
            >
              {option}
            </button>
          ))}
        </div> */}

        {/* Giá và giảm giá */}
        <div className={styles.priceInfo}>
          {isDiscounted ? (
            <>
              <span className={styles.currentPrice}>
                {(discountPrice ?? 0).toLocaleString("vi-VN")} ₫
              </span>
              <div className={styles.discountInfo}>
                <span className={styles.originalPrice}>
                  {price?.toLocaleString("vi-VN")}₫
                </span>
                <span className={styles.discount}>-{discountPercent}%</span>
              </div>
            </>
          ) : (
            <span className={styles.currentPrice}>
              {price?.toLocaleString("vi-VN")} ₫
            </span>
          )}
        </div>

        {/* Đánh giá và số lượng đã bán */}
        <div className={styles.ratingInfo}>
          <span className={styles.rating}>
          ⭐ Đánh giá: Xem chi tiết
          <br />
          💸 Đã bán: {sold && sold >= 1000 ? `${(sold / 1000).toFixed(1)}K` : sold}
          </span>
        </div>
      </div>
      </Link>
    </div>
  );
};

export default ProductCard;