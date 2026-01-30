import React, { useContext } from "react";
import AppContext from "../Context/Context";
import { Link } from "react-router-dom";
import { useToast } from "../Context/ToastContext";

const Favourites = () => {
    const { data, addToCart, toggleFavorite } = useContext(AppContext);
    const { addToast } = useToast();

    const favoriteProducts = data.filter((product) => product.favorite);

    const handleToggleFavorite = async (productId, e) => {
        e.preventDefault();
        e.stopPropagation();
        await toggleFavorite(productId);
        addToast("Removed from Favourites", "info");
    };

    return (
        <div className="container" style={{ marginTop: "100px", marginBottom: "100px" }}>
            <h2 className="text-center mb-4">My Favourites</h2>
            {favoriteProducts.length === 0 ? (
                <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "50vh" }}>
                    <h4 className="text-center">No favourite products yet</h4>
                </div>
            ) : (
                <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
                    {favoriteProducts.map((product) => {
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
    );
};

export default Favourites;
