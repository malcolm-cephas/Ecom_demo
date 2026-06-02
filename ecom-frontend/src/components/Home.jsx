import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import useCartStore from "../store/useCartStore";
import { useAuth } from "../Context/AuthContext";
import useProductStore from "../store/useProductStore";
import { Heart, ShoppingCart, Loader2 } from "lucide-react";

const Home = ({ selectedCategory }) => {
  const { addToCart } = useCartStore();
  const { isAuthenticated } = useAuth();
  const {
    products,
    loading,
    fetchProducts,
    toggleFavorite,
    totalPages,
    currentPage
  } = useProductStore();

  const productsPerPage = 9;

  useEffect(() => {
    fetchProducts(0, productsPerPage, selectedCategory);
  }, [selectedCategory, fetchProducts]);

  const handlePageChange = (pageNumber) => {
    fetchProducts(pageNumber - 1, productsPerPage, selectedCategory);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleToggleFavorite = async (productId, e) => {
    e.preventDefault();
    e.stopPropagation();

    if (!isAuthenticated) {
      window.location.href = '/login';
      return;
    }

    try {
      await toggleFavorite(productId);
    } catch (error) {
      console.error("Failed to toggle favorite:", error);
    }
  };

  return (
    <>
      <div className="container" style={{ marginTop: "100px", marginBottom: "100px" }}>
        {loading ? (
          <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "50vh" }}>
            <Loader2 className="animate-spin text-primary" size={48} />
          </div>
        ) : products.length === 0 ? (
          <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "50vh" }}>
            <h2 className="text-center">No Products Available</h2>
          </div>
        ) : (
          <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
            {products.map((product) => {
              const { id, brand, name, price, available } = product;
              const imageUrl = `http://localhost:8080/api/product/${id}/image`;

              return (
                <div className="col" key={id}>
                  <div
                    className="card h-100 shadow-sm border-0 transition-hover"
                    style={{
                      backgroundColor: available ? "var(--card-bg-clr)" : "#f8f9fa",
                      overflow: "hidden",
                      borderRadius: "15px"
                    }}
                  >
                    <Link
                      to={`/product/${id}`}
                      style={{ textDecoration: "none", color: "inherit", height: "100%", display: "flex", flexDirection: "column" }}
                    >
                      <div style={{ position: "relative", height: "220px", overflow: "hidden" }}>
                        <img
                          src={imageUrl}
                          alt={name}
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = "https://placehold.co/600x400?text=No+Image";
                          }}
                          style={{
                            width: "100%",
                            height: "100%",
                            objectFit: "cover",
                          }}
                        />
                        <div className="position-absolute top-0 end-0 p-2" style={{ zIndex: 10 }}>
                          <button
                            className="bg-white rounded-circle p-2 shadow-sm border-0 d-flex align-items-center justify-content-center"
                            style={{ width: "38px", height: "38px", cursor: "pointer" }}
                            onClick={(e) => handleToggleFavorite(id, e)}
                          >
                            <Heart
                              size={20}
                              fill={product.favorite ? "#dc3545" : "none"}
                              className={product.favorite ? "text-danger" : "text-muted"}
                            />
                          </button>
                        </div>
                      </div>

                      <div className="card-body d-flex flex-column justify-content-between">
                        <div>
                          <p className="text-muted small mb-1 uppercase tracking-wider">{brand}</p>
                          <h5 className="card-title fw-bold text-truncate mb-2" style={{ fontSize: "1.1rem" }}>
                            {name}
                          </h5>
                        </div>
                        <div className="mt-3">
                          <h5 className="fw-bold mb-3 text-primary">{"₹" + price.toLocaleString()}</h5>
                          <button
                            className={`btn ${available ? 'btn-primary' : 'btn-secondary'} w-100 py-2 d-flex align-items-center justify-content-center gap-2`}
                            onClick={async (e) => {
                              e.preventDefault();
                              if (!isAuthenticated) {
                                window.location.href = '/login';
                                return;
                              }
                              await addToCart(id, 1);
                            }}
                            disabled={!available}
                          >
                            {available ? (
                              <>
                                <ShoppingCart size={18} />
                                Add to Cart
                              </>
                            ) : "Out of Stock"}
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
          backgroundColor: "rgba(255, 255, 255, 0.9)",
          backdropFilter: "blur(10px)",
          borderTop: "1px solid rgba(0,0,0,0.05)",
          padding: "0.75rem",
          display: "flex",
          justifyContent: "center",
          zIndex: 1000,
        }}
      >
        {totalPages > 1 && (
          <nav className="d-flex align-items-center gap-2">
            <button
              className="btn btn-outline-primary btn-sm px-3"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
            >
              Prev
            </button>
            <div className="d-flex gap-1 mx-2">
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                <button
                  key={page}
                  className={`btn btn-sm ${currentPage === page ? 'btn-primary' : 'btn-light'}`}
                  onClick={() => handlePageChange(page)}
                  style={{ width: "32px", height: "32px", padding: 0 }}
                >
                  {page}
                </button>
              ))}
            </div>
            <button
              className="btn btn-outline-primary btn-sm px-3"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
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

