import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import axios from "../axios";
import { CATEGORIES } from "../constants";
import { useAuth } from "../Context/AuthContext";
import useCartStore from "../store/useCartStore";
import useUserStore from "../store/useUserStore";
import {
  ShoppingCart,
  Moon,
  Sun,
  Search,
  User as UserIcon,
  LogOut,
  PlusCircle,
  Menu
} from "lucide-react";

const Navbar = ({ onSelectCategory, onSearch }) => {
  const { cart } = useCartStore();
  const { user, logout, login, isAuthenticated, loading: authLoading } = useAuth();

  const getInitialTheme = () => {
    const storedTheme = localStorage.getItem("theme");
    return storedTheme ? storedTheme : "light-theme";
  };

  const [selectedCategory, setSelectedCategory] = useState("");
  const [theme, setTheme] = useState(getInitialTheme());
  const [input, setInput] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [noResults, setNoResults] = useState(false);
  const [showSearchResults, setShowSearchResults] = useState(false)

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      if (input.length >= 1) {
        setShowSearchResults(true);
        searchProducts(input);
      } else {
        setShowSearchResults(false);
        setSearchResults([]);
        setNoResults(false);
      }
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [input]);

  const searchProducts = async (value) => {
    try {
      const response = await axios.get(`/products/search?keyword=${value}`);
      setSearchResults(response.data);
      setNoResults(response.data.length === 0);
    } catch (error) {
      console.error("Error searching:", error);
    }
  };

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    onSelectCategory(category);
  };

  const toggleTheme = () => {
    const newTheme = theme === "dark-theme" ? "light-theme" : "dark-theme";
    setTheme(newTheme);
    localStorage.setItem("theme", newTheme);
  };

  useEffect(() => {
    document.body.className = theme;
  }, [theme]);

  const cartCount = cart?.items?.length || 0;

  return (
    <header>
      <nav className="navbar navbar-expand-lg fixed-top shadow-sm px-3" style={{
        backgroundColor: "rgba(255, 255, 255, 0.8)",
        backdropFilter: "blur(10px)",
        borderBottom: "1px solid rgba(0,0,0,0.05)"
      }}>
        <div className="container-fluid">
          <Link className="navbar-brand fw-bold text-primary" to="/" style={{ fontSize: "1.5rem" }}>
            Malcolm
          </Link>

          <button
            className="navbar-toggler border-0"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarSupportedContent"
          >
            <Menu size={24} />
          </button>

          <div className="collapse navbar-collapse" id="navbarSupportedContent">
            <ul className="navbar-nav me-auto mb-2 mb-lg-0 align-items-center">
              <li className="nav-item">
                <Link className="nav-link fw-medium" to="/">Home</Link>
              </li>

              <li className="nav-item dropdown">
                <a className="nav-link dropdown-toggle fw-medium" href="#" role="button" data-bs-toggle="dropdown">
                  Categories
                </a>
                <ul className="dropdown-menu border-0 shadow-sm">
                  <li>
                    <button className="dropdown-item" onClick={() => handleCategorySelect("")}>
                      All Products
                    </button>
                  </li>
                  <li className="dropdown-divider"></li>
                  {CATEGORIES.map((category) => (
                    <li key={category}>
                      <button className="dropdown-item" onClick={() => handleCategorySelect(category)}>
                        {category}
                      </button>
                    </li>
                  ))}
                </ul>
              </li>

              {authLoading ? (
                <li className="nav-item px-2">
                  <div className="spinner-border spinner-border-sm text-primary" role="status"></div>
                </li>
              ) : isAuthenticated ? (
                <>
                  <li className="nav-item">
                    <Link className="nav-link d-flex align-items-center gap-1" to="/add_product">
                      <PlusCircle size={18} />
                      <span>Add Product</span>
                    </Link>
                  </li>
                  <li className="nav-item dropdown ms-lg-3">
                    <a className="nav-link dropdown-toggle d-flex align-items-center gap-2" href="#" role="button" data-bs-toggle="dropdown">
                      <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style={{ width: "32px", height: "32px" }}>
                        <UserIcon size={18} />
                      </div>
                      <span className="fw-medium d-none d-xl-inline">
                        {user?.sub || user?.name || 'User'}
                      </span>
                    </a>
                    <ul className="dropdown-menu dropdown-menu-end border-0 shadow-sm">
                      <li><Link className="dropdown-item" to="/favourites">Favourites</Link></li>
                      <li><hr className="dropdown-divider" /></li>
                      <li>
                        <button className="dropdown-item text-danger d-flex align-items-center gap-2" onClick={logout}>
                          <LogOut size={16} />
                          Logout
                        </button>
                      </li>
                    </ul>
                  </li>
                </>
              ) : (
                <li className="nav-item ms-lg-3">
                  <button className="btn btn-primary btn-sm px-4 rounded-pill" onClick={() => login()}>Login</button>
                </li>
              )}
            </ul>

            <div className="d-flex align-items-center gap-3">
              <div className="position-relative d-none d-md-block" style={{ width: "250px" }}>
                <Search className="position-absolute top-50 start-0 translate-middle-y ms-2 text-muted" size={18} />
                <input
                  className="form-control form-control-sm ps-5 rounded-pill border-0 bg-light"
                  type="search"
                  placeholder="Search products..."
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                />
                {showSearchResults && (
                  <ul className="search-results-list position-absolute w-100 mt-2 shadow-sm border-0" style={{ zIndex: 1000 }}>
                    {searchResults.length > 0 ? (
                      searchResults.map((result) => (
                        <li key={result.id} className="search-results-item list-group-item-action border-0">
                          <Link to={`/product/${result.id}`} className="text-decoration-none text-dark d-block">
                            {result.name}
                          </Link>
                        </li>
                      ))
                    ) : noResults && (
                      <li className="search-results-item border-0 text-muted small">No products found</li>
                    )}
                  </ul>
                )}
              </div>

              <button className="btn btn-link text-dark p-2" onClick={toggleTheme}>
                {theme === "dark-theme" ? <Sun size={20} /> : <Moon size={20} />}
              </button>

              <Link to="/cart" className="btn btn-link text-dark p-2 position-relative">
                <ShoppingCart size={22} />
                {cartCount > 0 && (
                  <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style={{ fontSize: "0.6rem" }}>
                    {cartCount}
                  </span>
                )}
              </Link>
            </div>
          </div>
        </div>
      </nav>
    </header>
  );
};

export default Navbar;
