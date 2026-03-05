import React, { useEffect, useState, useContext } from "react";
import axios from "../axios";
import { CATEGORIES } from "../constants";
import AppContext from "../Context/Context";
import { useAuth } from "../Context/AuthContext";

const Navbar = ({ onSelectCategory, onSearch }) => {
  const { cart } = useContext(AppContext);
  const { user, logout } = useAuth();
  const getInitialTheme = () => {
    const storedTheme = localStorage.getItem("theme");
    return storedTheme ? storedTheme : "light-theme";
  };
  const [selectedCategory, setSelectedCategory] = useState("");
  const [theme, setTheme] = useState(getInitialTheme());
  const [input, setInput] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [noResults, setNoResults] = useState(false);
  const [searchFocused, setSearchFocused] = useState(false);
  const [showSearchResults, setShowSearchResults] = useState(false)

  // Debounce the search input to avoid making API calls on every keystroke.
  // Waits for 300ms of inactivity before firing the search request.
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      if (input.length >= 1) {
        setShowSearchResults(true);
        searchProducts(input);
      } else {
        // Clear results if input is empty
        setShowSearchResults(false);
        setSearchResults([]);
        setNoResults(false);
      }
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [input]);

  const searchProducts = async (value) => {
    try {
      const response = await axios.get(
        `/products/search?keyword=${value}`
      );
      setSearchResults(response.data);
      setNoResults(response.data.length === 0);
    } catch (error) {
      console.error("Error searching:", error);
    }
  };

  const handleChange = (value) => {
    setInput(value);
  };
  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    onSelectCategory(category); // Notify parent (App.jsx) to update the Home view
  };

  const toggleTheme = () => {
    const newTheme = theme === "dark-theme" ? "light-theme" : "dark-theme";
    setTheme(newTheme);
    localStorage.setItem("theme", newTheme); // Persist theme preference
  };

  useEffect(() => {
    document.body.className = theme;
  }, [theme]);

  const categories = CATEGORIES;
  return (
    <>
      <header>
        <nav className="navbar navbar-expand-lg fixed-top">
          <div className="container-fluid">
            <a className="navbar-brand" href="/">
              Malcolm
            </a>
            <button
              className="navbar-toggler"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarSupportedContent"
              aria-controls="navbarSupportedContent"
              aria-expanded="false"
              aria-label="Toggle navigation"
            >
              <span className="navbar-toggler-icon"></span>
            </button>
            <div
              className="collapse navbar-collapse"
              id="navbarSupportedContent"
            >
              <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                <li className="nav-item">
                  <a className="nav-link active" aria-current="page" href="/">
                    Home
                  </a>
                </li>
                <li className="nav-item">
                  <a className="nav-link" href="/favourites">
                    Favourites
                  </a>
                </li>

                <li className="nav-item dropdown">
                  <a
                    className="nav-link dropdown-toggle"
                    href="/"
                    role="button"
                    data-bs-toggle="dropdown"
                    aria-expanded="false"
                  >
                    Categories
                  </a>

                  <ul className="dropdown-menu">
                    <li>
                      <button
                        className="dropdown-item"
                        onClick={() => handleCategorySelect("")}
                      >
                        All Products
                      </button>
                    </li>
                    <li className="dropdown-divider"></li>
                    {categories.map((category) => (
                      <li key={category}>
                        <button
                          className="dropdown-item"
                          onClick={() => handleCategorySelect(category)}
                        >
                          {category}
                        </button>
                      </li>
                    ))}
                  </ul>
                </li>

                {user ? (
                  <>
                    <li className="nav-item">
                      <a className="nav-link" href="/add_product">
                        Add Product
                      </a>
                    </li>
                    <li className="nav-item dropdown">
                      <a className="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        Hi, {user.sub || user.preferred_username || 'User'}
                      </a>
                      <ul className="dropdown-menu">
                        <li><button className="dropdown-item" onClick={logout}>Logout</button></li>
                      </ul>
                    </li>
                  </>
                ) : (
                  <li className="nav-item">
                    <button className="nav-link btn btn-link" onClick={() => login()}>Login</button>
                  </li>
                )}

                <li className="nav-item"></li>
              </ul>
              <button className="theme-btn" onClick={() => toggleTheme()}>
                {theme === "dark-theme" ? (
                  <i className="bi bi-moon-fill"></i>
                ) : (
                  <i className="bi bi-sun-fill"></i>
                )}
              </button>
              <div className="d-flex align-items-center cart">
                <a href="/cart" className="nav-link text-dark position-relative">
                  <i
                    className="bi bi-cart me-2"
                    style={{ display: "flex", alignItems: "center" }}
                  >
                    Cart
                  </i>
                  {cart.length > 0 && (
                    <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                      {cart.length}
                    </span>
                  )}
                </a>
                {/* <form className="d-flex" role="search" onSubmit={handleSearch} id="searchForm"> */}
                <input
                  className="form-control me-2"
                  type="search"
                  placeholder="Search"
                  aria-label="Search"
                  value={input}
                  onChange={(e) => handleChange(e.target.value)}
                  onFocus={() => setSearchFocused(true)} // Set searchFocused to true when search bar is focused
                  onBlur={() => setSearchFocused(false)} // Set searchFocused to false when search bar loses focus
                />
                {showSearchResults && (
                  <ul className="list-group">
                    {searchResults.length > 0 ? (
                      searchResults.map((result) => (
                        <li key={result.id} className="list-group-item">
                          <a href={`/product/${result.id}`} className="search-result-link">
                            <span>{result.name}</span>
                          </a>
                        </li>
                      ))
                    ) : (
                      noResults && (
                        <p className="no-results-message">
                          No Prouduct with such Name
                        </p>
                      )
                    )}
                  </ul>
                )}
                {/* <button
                  className="btn btn-outline-success"
                  onClick={handleSearch}
                >
                  Search Products
                </button> */}
                {/* </form> */}
                <div />
              </div>
            </div>
          </div>
        </nav>
      </header >
    </>
  );
};

export default Navbar;
