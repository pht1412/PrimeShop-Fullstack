import axios from 'axios';

const api = axios.create({
    baseURL: "https://localhost:8080/api",
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;
// import axios from 'axios';

// // 1. Lấy đường dẫn từ biến môi trường (File .env)
// // Nếu không tìm thấy biến môi trường thì fallback về localhost (để phòng hờ lỗi quên tạo file .env)
// const BASE_URL = import.meta.env.VITE_API_BASE_URL || "https://backend-primeshop.onrender.com";

// const api = axios.create({
//     // 2. Nối thêm đuôi /api vào Base URL
//     baseURL: `${BASE_URL}/api`,
    
//     // (Optional) Timeout 10s để tránh user đợi mãi nếu mạng lag
//     timeout: 10000, 
//     headers: {
//         'Content-Type': 'application/json',
//     },
// });

// api.interceptors.request.use(
//     (config) => {
//         const token = localStorage.getItem("token");
//         if (token) {
//             config.headers.Authorization = `Bearer ${token}`;
//         }
//         return config;
//     },
//     (error) => {
//         return Promise.reject(error);
//     }
// );

// export default api;