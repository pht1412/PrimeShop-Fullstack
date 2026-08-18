import React, { useEffect, useState } from "react";
import { IProductCardResponse } from "../../types/seller";
import {
  adminGetPendingProducts,
  adminApproveProduct,
  adminRejectProduct,
} from "../../api/seller.api";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

const AdminC2CManagementPage: React.FC = () => {
  const [products, setProducts] = useState<IProductCardResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [processingId, setProcessingId] = useState<number | null>(null);

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const response = await adminGetPendingProducts();
      setProducts(response.data || []);
    } catch (error) {
      toast.error("Không thể tải danh sách sản phẩm.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleApprove = async (sellerId: number, productId: number) => {
    if (!window.confirm(`Xác nhận DUYỆT sản phẩm #${productId}?`)) return;

    setProcessingId(productId);
    try {
      await adminApproveProduct(sellerId, productId);
      setProducts((prev) => prev.filter((p) => p.id !== productId));
      toast.success(`Đã duyệt sản phẩm #${productId}`);
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Lỗi duyệt sản phẩm.");
    } finally {
      setProcessingId(null);
    }
  };

  const handleReject = async (sellerId: number, productId: number) => {
    if (!window.confirm(`Xác nhận TỪ CHỐI sản phẩm #${productId}?`)) return;

    setProcessingId(productId);
    try {
      await adminRejectProduct(sellerId, productId);
      setProducts((prev) => prev.filter((p) => p.id !== productId));
      toast.info(`Đã từ chối sản phẩm #${productId}`);
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Lỗi từ chối sản phẩm.");
    } finally {
      setProcessingId(null);
    }
  };

  // New: Thumbnail component to ensure consistent sizing + skeleton + fallback
  const Thumbnail: React.FC<{
    src?: string | null;
    alt?: string;
    width?: number;
    height?: number;
  }> = ({ src, alt = "", width = 84, height = 60 }) => {
    const [loaded, setLoaded] = useState(false);
    const [errored, setErrored] = useState(false);
    const imgSrc = errored || !src ? "https://via.placeholder.com/400" : src!;

    return (
      <div
        className="rounded-md overflow-hidden bg-gray-100 border"
        style={{ width: `${width}px`, height: `${height}px` }}
      >
        <div className="w-full h-full relative">
          {!loaded && (
            <div
              className="absolute inset-0 bg-gray-200 animate-pulse z-20"
              aria-hidden
            />
          )}
          <img
            src={imgSrc}
            alt={alt}
            loading="lazy"
            onLoad={() => setLoaded(true)}
            onError={() => {
              setErrored(true);
              setLoaded(true);
            }}
            style={{
              width: "100%",
              height: "100%",
              objectFit: "cover",
              display: "block",
            }}
            className="relative z-10"
          />
        </div>
      </div>
    );
  };

  return (
    <div className="p-6 bg-gray-50 min-h-screen font-sans">
      <ToastContainer position="top-right" autoClose={2500} />

      {/* Header / Controls */}
      <div className="mb-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">
            Quản Lý Duyệt Sản Phẩm C2C
          </h1>
          <p className="text-sm text-gray-500">
            Xem và duyệt sản phẩm theo danh sách
          </p>
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <button
            onClick={fetchProducts}
            disabled={loading}
            className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-800 hover:bg-gray-100 shadow-sm flex items-center gap-2"
          >
            {loading ? <span className="animate-spin">⏳</span> : "🔄"} Làm mới
          </button>
        </div>
      </div>

      {/* Empty / Loading */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-24 text-gray-400">
          <div className="w-12 h-12 border-4 border-gray-200 border-t-blue-500 rounded-full animate-spin mb-4"></div>
          <p>Đang tải dữ liệu...</p>
        </div>
      ) : products.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 bg-white rounded-xl border border-gray-200 border-dashed">
          <div className="text-6xl mb-3 opacity-40">✔️</div>
          <p className="text-gray-600 font-medium">
            Không còn sản phẩm nào cần duyệt.
          </p>
          <p className="text-sm text-gray-400">Hệ thống đang rất sạch 🤍</p>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  #
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Ảnh
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Tên sản phẩm
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Shop
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Loại
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Giá
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Kho
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Trạng thái
                </th>
                <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Hành động
                </th>
              </tr>
            </thead>

            <tbody className="bg-white divide-y divide-gray-100">
              {products.map((product) => (
                <tr key={product.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm text-gray-600">{product.id}</td>

                  {/* Image cell: use Thumbnail component for consistent sizing */}
                  <td className="px-4 py-3">
                    <Thumbnail
                      src={product.imageUrl}
                      alt={product.name}
                      width={84}
                      height={60}
                    />
                  </td>

                  <td className="px-4 py-3 max-w-[220px]">
                    <div
                      className="text-sm font-medium text-gray-800 line-clamp-2"
                      title={product.name}
                    >
                      {product.name}
                    </div>
                    <div className="text-xs text-gray-400 mt-1">{/* optional short desc */}</div>
                  </td>

                  <td className="px-4 py-3 text-sm text-gray-700">
                    🏬 {product.shopName}
                  </td>

                  <td className="px-4 py-3 text-sm text-gray-700">
                    <span className="px-2 py-0.5 bg-gray-100 rounded text-xs text-gray-600">
                      {product.category}
                    </span>
                  </td>

                  <td className="px-4 py-3 text-sm text-gray-800 text-right font-semibold">
                    {formatCurrency(product.price)}
                  </td>

                  <td className="px-4 py-3 text-sm text-gray-700 text-right">
                    {product.stock}
                  </td>

                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`px-2 py-1 rounded text-xs font-medium ${
                        product.condition === "new"
                          ? "bg-green-50 text-green-700"
                          : "bg-yellow-50 text-yellow-800"
                      }`}
                    >
                      {product.condition === "new" ? "Mới" : "Cũ"}
                    </span>
                  </td>

                  <td className="px-4 py-3 text-center">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        onClick={() => handleReject(product.sellerId, product.id)}
                        disabled={processingId === product.id}
                        className="px-2 py-1 text-xs bg-white border border-red-200 text-red-600 rounded hover:bg-red-50 disabled:opacity-50"
                      >
                        ✕ Từ chối
                      </button>
                      <button
                        onClick={() => handleApprove(product.sellerId, product.id)}
                        disabled={processingId === product.id}
                        className="px-3 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-60"
                      >
                        {processingId === product.id ? (
                          <span className="inline-block w-3 h-3 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                        ) : (
                          "✓ Duyệt"
                        )}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default AdminC2CManagementPage;
