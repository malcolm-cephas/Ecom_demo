import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import axios from "../axios";
import AppContext from "../Context/Context";
import { useToast } from "../Context/ToastContext";

const Home = ({ selectedCategory }) => {
  const { isError, addToCart, toggleFavorite } = useContext(AppContext);
  const { addToast } = useToast();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const productsPerPage = 9;

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      try {
        let url = `/products/page?page=${currentPage - 1}&size=${productsPerPage}`;
        // If there's a selected category, we might need a specific category endpoint or handle it via search
        // For now, let's assume the basic pagination. 
        // Note: The backend has search/page but not specifically category/page yet.
        // We'll use the search endpoint for categories if one is selected.
        if (selectedCategory) {
          url = `/products/search/page?keyword=${selectedCategory}&page=${currentPage - 1}&size=${productsPerPage}`;
        }

        const response = await axios.get(url);
        // Page object from Spring returns content in 'content' field
        setProducts(response.data.content);
        setTotalPages(response.data.totalPages);
      } catch (error) {
        console.error("Error fetching products:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [selectedCategory, currentPage]);

  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleToggleFavorite = async (productId, e) => {
    e.preventDefault();
    e.stopPropagation();
    const updatedProduct = await toggleFavorite(productId);
    if (updatedProduct) {
      setProducts(prevProducts => prevProducts.map(p =>
        p.id === productId ? { ...p, favorite: updatedProduct.favorite } : p
      ));
    }
  };

  if (isError) {
    return (
      <h2 className="text-center" style={{ padding: "10rem" }}>
        Something went wrong...
      </h2>
    );
  }

  return (
    <>
      <div className="container" style={{ marginTop: "100px", marginBottom: "100px" }}>
        {loading ? (
          <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
            {[...Array(6)].map((_, i) => (
              <div className="col" key={i}>
                <div className="card h-100 border-0 shadow-sm" aria-hidden="true">
                  <div className="placeholder-glow">
                    <div className="placeholder col-12" style={{ height: "200px" }}></div>
                  </div>
                  <div className="card-body">
                    <h5 className="card-title placeholder-glow">
                      <span className="placeholder col-6"></span>
                    </h5>
                    <p className="card-text placeholder-glow">
                      <span className="placeholder col-7"></span>
                      <span className="placeholder col-4"></span>
                      <span className="placeholder col-4"></span>
                    </p>
                    <a href="#" tabIndex="-1" className="btn btn-primary disabled placeholder col-6"></a>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : products.length === 0 ? (
          <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "50vh" }}>
            <h2 className="text-center">No Products Available</h2>
          </div>
        ) : (
          <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
            {products.map((product) => {
              const { id, brand, name, price, available } = product;
              // Direct URL to backend image endpoint
              const imageUrl = `http://localhost:8080/api/product/${id}/image`;

              return (
                <div className="col" key={id}>
                  <div
                    className="card h-100 shadow-sm border-0 transition-hover"
                    style={{
                      backgroundColor: available ? "var(--card-bg-clr)" : "#ccc",
                      overflow: "hidden"
                    }}
                  >
                    <Link
                      to={`/product/${id}`}
                      style={{ textDecoration: "none", color: "inherit", height: "100%", display: "flex", flexDirection: "column" }}
                    >
                      <div style={{ position: "relative", height: "200px", overflow: "hidden" }}>
                        <img
                          src={imageUrl}
                          alt={name}
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = "https://placehold.co/600x400";
                          }}
                          style={{
                            width: "100%",
                            height: "100%",
                            objectFit: "cover",
                          }}
                        />
                        <div className="position-absolute top-0 end-0 p-2" style={{ zIndex: 10 }}>
                          <button
                            className="bg-white rounded-circle p-2 shadow-sm border-0"
                            style={{ width: "35px", height: "35px", display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}
                            onClick={(e) => handleToggleFavorite(id, e)}
                          >
                            <i className={`bi ${product.favorite ? "bi-heart-fill text-danger" : "bi-heart text-danger"}`}></i>
                          </button>
                        </div>
                      </div>
                      <div className="card-body d-flex flex-column justify-content-between">
                        <div>
                          <p className="text-muted small mb-1">{brand}</p>
                          <h5 className="card-title fw-bold text-truncate mb-2">
                            {name.toUpperCase()}
                          </h5>
                        </div>
                        <div className="mt-3">
                          <h5 className="fw-bold mb-3">{"Rs " + price}</h5>
                          <button
                            className="btn btn-primary w-100 py-2 shadow-sm"
                            onClick={(e) => {
                              e.preventDefault();
                              addToCart(product);
                              addToast("Product added to cart", "success");
                            }}
                            disabled={!available}
                          >
                            {available ? "Add to Cart" : "Out of Stock"}
                          </button>
                        </div>
                      </div>
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
      <footer
        style={{
          position: "fixed",
          bottom: 0,
          left: 0,
          right: 0,
          backgroundColor: "#f8f9fa",
          borderTop: "1px solid #ddd",
          padding: "1rem",
          display: "flex",
          justifyContent: "center",
          zIndex: 1000,
        }}
      >
        {totalPages > 1 && (
          <nav
            style={{
              display: "flex",
              justifyContent: "center",
              gap: "0.5rem",
              flexWrap: "wrap",
            }}
          >
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
              style={{
                padding: "0.5rem 1rem",
                cursor: currentPage === 1 ? "default" : "pointer",
                opacity: currentPage === 1 ? 0.5 : 1,
              }}
            >
              Previous
            </button>
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
              <button
                key={page}
                onClick={() => handlePageChange(page)}
                style={{
                  padding: "0.5rem 1rem",
                  backgroundColor: currentPage === page ? "#007bff" : "#fff",
                  color: currentPage === page ? "#fff" : "#000",
                  border: "1px solid #007bff",
                  cursor: "pointer",
                }}
              >
                {page}
              </button>
            ))}
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
              style={{
                padding: "0.5rem 1rem",
                cursor: currentPage === totalPages ? "default" : "pointer",
                opacity: currentPage === totalPages ? 0.5 : 1,
              }}
            >
              Next
            </button>
          </nav>
        )}
      </footer>
    </>
  );
};

export default Home;
