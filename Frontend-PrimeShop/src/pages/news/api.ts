import axios, { AxiosInstance } from 'axios';

const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'https://backend-primeshop.onrender.com/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('Response Error:', {
      status: error.response?.status,
      data: error.response?.data,
      message: error.message,
    });
    return Promise.reject(error);
  }
);

const get = async <T>(endpoint: string, params?: any): Promise<T> => {
  try {
    const response = await api.get(endpoint, { params });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const post = async <T>(endpoint: string, data: any): Promise<T> => {
  const response = await api.post(endpoint, data);
  return response.data;
};

const put = async <T>(endpoint: string, data: any): Promise<T> => {
  const response = await api.put(endpoint, data);
  return response.data;
};

const del = async <T>(endpoint: string): Promise<T> => {
  const response = await api.delete(endpoint);
  return response.data;
};

export { get, post, put, del };
export default api;