import { create } from 'zustand';
import API from '../axios';
import { toast } from 'react-hot-toast';

const useCartStore = create((set, get) => ({
    cart: { items: [] },
    loading: false,

    fetchCart: async () => {
        set({ loading: true });
        try {
            const response = await API.get('/cart');
            set({ cart: response.data || { items: [] }, loading: false });
        } catch (error) {
            console.error('Failed to fetch cart:', error);
            set({ loading: false });
        }
    },

    addToCart: async (productId, quantity) => {
        try {
            const response = await API.post('/cart/add', null, {
                params: { productId, quantity }
            });
            set({ cart: response.data });
            toast.success('Added to cart!');
            return true;
        } catch (error) {
            const msg = error.response?.data?.message || 'Failed to add to cart';
            toast.error(msg);
            return false;
        }
    },

    removeFromCart: async (productId) => {
        try {
            const response = await API.delete(`/cart/remove/${productId}`);
            set({ cart: response.data });
            toast.success('Removed from cart');
        } catch (error) {
            toast.error('Failed to remove item');
        }
    },

    updateQuantity: async (productId, quantity) => {
        try {
            const response = await API.put(`/cart/update/${productId}`, null, {
                params: { quantity }
            });
            set({ cart: response.data });
        } catch (error) {
            toast.error('Failed to update quantity');
        }
    },

    clearCart: async () => {
        try {
            await API.delete('/cart/clear');
            set({ cart: { items: [] } });
            toast.success('Cart cleared');
        } catch (error) {
            toast.error('Failed to clear cart');
        }
    },

    setCart: (cart) => set({ cart }),
}));

export default useCartStore;
