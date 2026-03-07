import { useContext, useEffect } from 'react';
import { AuthContext } from 'react-oauth2-code-pkce';
import useUserStore from '../store/useUserStore';
import useCartStore from '../store/useCartStore';

export const useAuth = () => {
    const { token, idToken, login, logOut, idTokenData, error, loading: oauthLoading } = useContext(AuthContext);
    const { setAuth, clearAuth, user: storeUser, isAuthenticated: storeAuth, token: storeToken } = useUserStore();
    const { fetchCart, setCart } = useCartStore();

    useEffect(() => {
        if (token) {
            setAuth(token, idTokenData);
            fetchCart(); // Fetch personalized cart on login
        } else if (!oauthLoading && !token) {
            clearAuth();
            setCart({ items: [] });
        }
    }, [token, idTokenData, oauthLoading, setAuth, clearAuth, fetchCart, setCart]);

    const handleLogout = () => {
        // Explicitly clear our local zustand states first
        clearAuth();
        setCart({ items: [] });

        // Explicitly clear ROCP storage to avoid re-auth on redirect/reload
        // The library should do this, but doing it manually ensures it's done before redirect
        Object.keys(sessionStorage).forEach(key => {
            if (key.includes('ROCP_')) sessionStorage.removeItem(key);
        });
        Object.keys(localStorage).forEach(key => {
            if (key.includes('ROCP_')) localStorage.removeItem(key);
        });

        // Log out from library (this will clear memory and redirect to logoutEndpoint)
        logOut();
    };

    return {
        user: storeUser || idTokenData,
        login: login,
        logout: handleLogout,
        isAuthenticated: storeAuth || (!!token || !!idToken),
        loading: oauthLoading,
        token: storeToken || token,
        error: error
    };
};
