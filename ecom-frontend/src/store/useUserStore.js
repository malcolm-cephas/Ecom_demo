import { create } from 'zustand';

const useUserStore = create((set) => ({
    user: null,
    isAuthenticated: false,
    token: null,
    loading: true,

    setAuth: (token, idTokenData) => set({
        token: token,
        user: idTokenData,
        isAuthenticated: !!token,
        loading: false
    }),

    clearAuth: () => set({
        token: null,
        user: null,
        isAuthenticated: false,
        loading: false
    }),

    setLoading: (loading) => set({ loading })
}));

export default useUserStore;
