import api from "./api";
import { components } from "../types/api-types";

export type Category = components["schemas"]["CategoryResponse"];

export const getAllCategories = async (): Promise<Category[]> => {
    const response = await api.get<Category[]>("/category");
    return response.data;
};
