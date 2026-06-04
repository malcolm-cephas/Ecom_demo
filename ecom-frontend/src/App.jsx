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
import UpdateProduct from "./components/UpdateProduct";
import Dashboard from "./components/Dashboard"; // Import Dashboard
import "bootstrap/dist/css/bootstrap.min.css"; // Global styles
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { ToastProvider } from "./Context/ToastContext"; // UI notifications
import ChatBox from "./components/ChatBox"; // Import ChatBox
import Login from "./components/Login";
import Register from "./components/Register";
import ProtectedRoute from "./components/ProtectedRoute";
import AgentUI from "./components/AgentUI"; // Import AgentUI

function App() {
  // State to track which product category the user has selected in the Navbar
  // This state is passed down to the Home component to filter products
  const [selectedCategory, setSelectedCategory] = useState("");

  /**
   * Handler for when a category is selected in the Navbar.
   * Updates the state to trigger a re-render of the product list.
   * @param {string} category - The selected category name
   */
  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    console.log("Category changed to:", category);
  };

  return (
    <>
      <ToastProvider>
        <BrowserRouter>
          {/* Navigation bar remains visible on all pages */}
          {/* It receives the handler to update the selected category */}
          <Navbar onSelectCategory={handleCategorySelect} />

          {/* Define the page routing for the application */}
          <Routes>
            <Route
              path="/"
              element={
                /* Home page receives the selected category to filter products */
                <Home selectedCategory={selectedCategory} />
              }
            />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/add_product"
              element={
                <ProtectedRoute>
                  <AddProduct />
                </ProtectedRoute>
              }
            />
            <Route path="/product" element={<Product />} />
            <Route path="product/:id" element={<Product />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/favourites" element={<Favourites />} />
            <Route path="/product/update/:id" element={<UpdateProduct />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/agent" element={<AgentUI />} />
          </Routes>

          {/* AI ChatBox: Floating chat interface available globally */}
          <ChatBox />
        </BrowserRouter>
      </ToastProvider>
    </>
  );
}

export default App;

