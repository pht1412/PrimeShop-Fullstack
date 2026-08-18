import React from "react";

interface Props {
  images?: string[];
  altPrefix?: string;
  maxShow?: number;
}

const ProductImagesPreview: React.FC<Props> = ({
  images = [],
  altPrefix = "Ảnh",
  maxShow = 3,
}) => {
  return (
    <div className="flex gap-2">
      {(images || []).slice(0, maxShow).map((imgUrl, index) => (
        <div
          key={index}
          className="w-16 h-16 relative rounded-lg overflow-hidden bg-gray-200 group"
        >
          {/* Skeleton */}
          <div className="absolute inset-0 bg-gray-300 animate-pulse" />

          <img
            src={imgUrl || "https://via.placeholder.com/150"}
            alt={`${altPrefix} ${index + 1}`}
            loading="lazy"
            onError={(e) => {
              (e.currentTarget as HTMLImageElement).src =
                "https://via.placeholder.com/150";
            }}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110 relative z-10"
            onLoad={(e) => {
              const prev = e.currentTarget.previousElementSibling as
                | HTMLElement
                | null;
              if (prev) prev.style.display = "none";
            }}
          />
        </div>
      ))}

      {(!images || images.length === 0) && (
        <span className="text-gray-400 text-sm italic">Không có ảnh</span>
      )}
    </div>
  );
};

export default ProductImagesPreview;
