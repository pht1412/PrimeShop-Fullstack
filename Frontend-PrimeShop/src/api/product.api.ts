import api from "./api";
import { Product } from "../types/product";

export const getAllProducts = async (): Promise<Product[]> => {
    const response = await api.get<Product[]>("/product");
    return response.data;
};

export const fetchProducts = async (params?: any) => {
    const queryString = new URLSearchParams(params).toString();
    const response = await api.get(`/product/search?${queryString}`);
    return response.data;
};

export const fetchCategories = async () => {
    const res = await api.get("/categories");
    return res.data;
};

export const fetchBrands = async () => {
    const res = await api.get("/brands");
    return res.data;
};

export const getProducts = async () => {
    const res = await api.get("/product/all-products", {
        params: {
            category: category !== 
        }
    })
}

