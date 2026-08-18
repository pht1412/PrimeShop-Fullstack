import { Product } from '../types/product';
import { CartItem } from '../types/cart';
import { Order } from '../types/order';
import { News } from '../types/news';

// Mock data cho sản phẩm (iPhone 16 và iPhone 16 Pro Max)
export const mockProducts: Product[] = [
  {
    id: "1",
    name: "realme C75 8GB/128GB",
    price: 5390000,
    originalPrice: 5990000,
    discount: 5,
    image: "https://via.placeholder.com/300",
    description: "Điện thoại realme C75 với hiệu năng mạnh mẽ.",
    category: "Điện thoại",
    brand: "realme",
    screenType: "FullHD+",
    screenSize: "6.72\"",
    storageOptions: ["128GB", "256GB", "512GB"],
    rating: 4.9,
    sold: 22000,
  },
  {
    id: "2",
    name: "Samsung Galaxy S23",
    price: 18000000,
    originalPrice: 19990000,
    discount: 10,
    image: "https://via.placeholder.com/300",
    description: "Điện thoại Samsung Galaxy S23 với camera vượt trội.",
    category: "Điện thoại",
    brand: "Samsung",
    screenType: "AMOLED",
    screenSize: "6.1\"",
    storageOptions: ["128GB", "256GB"],
    rating: 4.7,
    sold: 15000,
  },
  {
    id: "3",
    name: "MacBook Pro M2",
    price: 35000000,
    originalPrice: 37990000,
    discount: 8,
    image: "https://via.placeholder.com/300",
    description: "MacBook Pro M2 với hiệu năng vượt trội.",
    category: "Laptop",
    brand: "Apple",
    screenType: "Retina",
    screenSize: "14\"",
    storageOptions: ["512GB", "1TB"],
    rating: 4.8,
    sold: 5000,
  },
];

// Mock data cho giỏ hàng
export const mockCart: CartItem[] = [
  {
    productId: '1',
    name: 'iPhone 16',
    price: 22990000,
    quantity: 1,
    image: '/images/products/iphone-16.jpg',
  },
  {
    productId: '2',
    name: 'iPhone 16 Pro Max',
    price: 34990000,
    quantity: 2,
    image: '/images/products/iphone-16-promax.jpg',
  },
];

// Mock data cho đơn hàng
export const mockOrders: Order[] = [
  {
    id: 'order1',
    userId: 'user1',
    items: [
      { productId: '1', quantity: 1, price: 22990000 },
      { productId: '2', quantity: 1, price: 34990000 },
    ],
    total: 57980000, // Tổng: 22.99tr + 34.99tr
    status: 'Đang giao',
    createdAt: '2025-04-25T10:00:00Z',
  },
  {
    id: 'order2',
    userId: 'user1',
    items: [{ productId: '2', quantity: 1, price: 34990000 }],
    total: 34990000,
    status: 'Đã giao',
    createdAt: '2025-04-20T15:30:00Z',
  },
];

// Mock data cho tin tức
export const mockNews: News[] = [
  {
    id: 'news1',
    title: 'Ra mắt iPhone 16 Series: Có gì mới?',
    excerpt: 'Apple vừa ra mắt iPhone 16 và 16 Pro Max với nhiều cải tiến vượt trội.',
    content: 'Apple đã chính thức ra mắt iPhone 16 Series tại sự kiện tháng 9/2024. Dòng iPhone 16 mang đến chip A18 mạnh mẽ, camera 48MP cải tiến, và thiết kế titan cao cấp (Pro Max). Ngoài ra, iPhone 16 hỗ trợ Apple Intelligence, mang đến trải nghiệm AI đỉnh cao...',
    image: '/images/news/iphone-16-launch.jpg',
    createdAt: '2025-04-15T09:00:00Z',
  },
  {
    id: 'news2',
    title: 'Ưu đãi khủng: Giảm ngay 10% khi mua iPhone 16',
    excerpt: 'Chương trình ưu đãi đặc biệt dành cho khách hàng mua iPhone 16 tại PrimeShop.',
    content: 'Từ nay đến hết 30/4/2025, PrimeShop giảm ngay 10% cho tất cả khách hàng mua iPhone 16 và 16 Pro Max. Ngoài ra, khách hàng còn được tặng kèm sạc nhanh 20W và miễn phí giao hàng toàn quốc...',
    image: '/images/news/iphone-16-sale.jpg',
    createdAt: '2025-04-10T14:00:00Z',
  },
];