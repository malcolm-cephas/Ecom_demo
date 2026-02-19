import axios from "../axios";
import { useState, useEffect, createContext } from "react";

// Create the context with default values (for auto-completion/TS-like hints)
const AppContext = createContext({
  data: [],
  isError: "",
  cart: [],
  addToCart: (product) => { },
  removeFromCart: (productId) => { },
  updateQuantity: (productId, quantity) => { },
  toggleFavorite: (productId) => { },
  clearCart: () => { },
  refreshData: () => { },
});

/**
 * Global Provider Component.
 * Wraps the application to provide shared state (Products, Cart) to all components.
 */

export const AppProvider = ({ children }) => {
  const [data, setData] = useState([]);
  const [isError, setIsError] = useState("");
  const [cart, setCart] = useState([]);


  /**
   * Fetches the latest product list from the backend.
   * Called on mount and after updates (add/delete product).
   */
  const refreshData = async () => {
    try {
      const response = await axios.get("/products");
      setData(response.data);
    } catch (error) {
      setIsError(error.message);
    }
  };

  /**
   * Fetches the current user's cart.
   * Maps the backend's nested structure to a flatter frontend-friendly format.
   */
  const refreshCart = async () => {
    try {
      const response = await axios.get("/cart");
      // Map backend structure (cart.items -> item.product + quantity) to frontend structure
      const mappedCart = response.data.items.map(item => ({
        ...item.product,
        quantity: item.quantity
      }));
      setCart(mappedCart);
    } catch (error) {
      console.error("Error fetching cart:", error);
    }
  };

  const addToCart = async (product) => {
    try {
      await axios.post(`/cart/add?productId=${product.id}&quantity=1`);
      refreshCart();
    } catch (error) {
      console.error("Error adding to cart:", error);
    }
  };

  const removeFromCart = async (productId) => {
    try {
      await axios.delete(`/cart/remove/${productId}`);
      refreshCart();
    } catch (error) {
      console.error("Error removing from cart:", error);
    }
  };

  const updateQuantity = async (productId, quantity) => {
    try {
      await axios.put(`/cart/update/${productId}?quantity=${quantity}`);
      refreshCart();
    } catch (error) {
      console.error("Error updating cart quantity:", error);
    }
  };

  const clearCart = async () => {
    try {
      await axios.delete("/cart/clear");
      setCart([]);
    } catch (error) {
      console.error("Error clearing cart:", error);
    }
  };

  const toggleFavorite = async (productId) => {
    try {
      const response = await axios.put(`/product/${productId}/favorite`);
      setData((prevData) =>
        prevData.map((item) =>
          item.id === productId ? { ...item, favorite: response.data.favorite } : item
        )
      );
      return response.data;
    } catch (error) {
      console.error("Error toggling favorite:", error);
    }
  };

  useEffect(() => {
    refreshData();
    refreshCart();
  }, []);

  return (
    <AppContext.Provider
      value={{
        data,
        isError,
        cart,
        addToCart,
        removeFromCart,
        updateQuantity,
        refreshData,
        clearCart,
        toggleFavorite,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export default AppContext;