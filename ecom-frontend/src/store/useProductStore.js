import { create } from 'zustand';
import axios from '../axios';

const useProductStore = create((set, get) => ({
    products: [],
    loading: false,
    error: null,
    totalPages: 0,
    currentPage: 1,

    fetchProducts: async (page = 0, size = 9, category = "") => {
        set({ loading: true });
        try {
            let url = `/products/page?page=${page}&size=${size}`;
            if (category) {
                url = `/products/search/page?keyword=${category}&page=${page}&size=${size}`;
            }

            const response = await axios.get(url);
            set({
                products: response.data.content,
                totalPages: response.data.totalPages,
                currentPage: page + 1,
                loading: false
            });
        } catch (error) {
            console.error("Error fetching products:", error);
            set({ error: error.message, loading: false });
        }
    },

    toggleFavorite: async (productId) => {
        try {
            const response = await axios.put(`/product/${productId}/favorite`);
            const { products } = get();

            // Update the products in state
            const updatedProducts = products.map(p =>
                p.id === productId ? { ...p, favorite: response.data.favorite } : p
            );

            set({ products: updatedProducts });
            return response.data;
        } catch (error) {
            console.error("Error toggling favorite:", error);
            throw error;
        }
    }
}));

export default useProductStore;
