// Main entry point for the React frontend
import "./App.css";
import React, { useState } from "react";
import Home from "./components/Home";
import Navbar from "./components/Navbar";
import Cart from "./components/Cart";
import AddProduct from "./components/AddProduct";
import Product from "./components/Product";
import Favourites from "./components/Favourites";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AppProvider } from "./Context/Context"; // Global state for products and cart
import UpdateProduct from "./components/UpdateProduct";
import Dashboard from "./components/Dashboard"; // Import Dashboard
import "bootstrap/dist/css/bootstrap.min.css"; // Global styles
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { ToastProvider } from "./Context/ToastContext"; // UI notifications

function App() {
  // State to track which product category the user has selected in the Navbar
  const [selectedCategory, setSelectedCategory] = useState("");

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    console.log("Category changed to:", category);
  };

  return (
    <AppProvider>
      <ToastProvider>
        <BrowserRouter>
          {/* Navigation bar remains visible on all pages */}
          <Navbar onSelectCategory={handleCategorySelect} />

          {/* Define the page routing for the application */}
          <Routes>
            <Route
              path="/"
              element={
                <Home selectedCategory={selectedCategory} />
              }
            />
            <Route path="/add_product" element={<AddProduct />} />
            <Route path="/product" element={<Product />} />
            <Route path="product/:id" element={<Product />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/favourites" element={<Favourites />} />
            <Route path="/product/update/:id" element={<UpdateProduct />} />
            <Route path="/dashboard" element={<Dashboard />} />
          </Routes>
        </BrowserRouter>
      </ToastProvider>
    </AppProvider>
  );
}

export default App;

