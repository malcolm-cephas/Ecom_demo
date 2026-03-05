import { useContext } from 'react';
import { AuthContext } from 'react-oauth2-code-pkce';

// Now we simply export the OAuth2 PKCE hook wrapped to match the old interface slightly
export const useAuth = () => {
    const { token, login, logOut, idTokenData, error } = useContext(AuthContext);

    if (error) {
        console.error("OAuth2 Error:", error);
    }

    if (token) {
        console.log("OAuth2 Token acquired successfully!");
    }

    return {
        user: idTokenData,
        login: login, // triggers the redirect to Auth Server
        logout: logOut,
        isAuthenticated: () => !!token,
        loading: false, // react-oauth2 handles its own loading state implicitly usually
        token: token
    };
};

