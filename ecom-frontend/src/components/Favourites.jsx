import React, { useEffect } from "react";
import { Link } from "react-router-dom";
import { useToast } from "../Context/ToastContext";
import useProductStore from "../store/useProductStore";
import useCartStore from "../store/useCartStore";
import { useAuth } from "../Context/AuthContext";
import { Heart, ShoppingCart, ShoppingBag } from "lucide-react";

const Favourites = () => {
    const { products, toggleFavorite, fetchProducts } = useProductStore();
    const { addToCart } = useCartStore();
    const { isAuthenticated } = useAuth();
    const { addToast } = useToast();

    useEffect(() => {
        // Fetch all products (or enough for the demo) to identify favorites
        fetchProducts(0, 100);
    }, [fetchProducts]);

    const favoriteProducts = products.filter((product) => product.favorite);

    const handleToggleFavorite = async (productId, e) => {
        e.preventDefault();
        e.stopPropagation();
        try {
            await toggleFavorite(productId);
            addToast("Removed from Favourites", "info");
        } catch (error) {
            addToast("Failed to update favourites", "error");
        }
    };

    return (
        <div className="container" style={{ marginTop: "120px", marginBottom: "100px" }}>
            <div className="d-flex align-items-center gap-3 mb-5 justify-content-center">
                <Heart className="text-danger" size={32} fill="#dc3545" />
                <h1 className="fw-bold mb-0">My Favorites</h1>
            </div>

            {favoriteProducts.length === 0 ? (
                <div className="text-center py-5 shadow-sm rounded-4 bg-white">
                    <ShoppingBag size={80} className="text-muted mb-4 opacity-25" />
                    <h3 className="text-muted fw-bold">No favorites yet</h3>
                    <p className="text-muted mb-4">You haven't added any products to your wishlist.</p>
                    <Link to="/" className="btn btn-primary rounded-pill px-5 py-2 fw-medium">
                        Start Shopping
                    </Link>
                </div>
            ) : (
                <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
                    {favoriteProducts.map((product) => {
                        const { id, brand, name, price, available } = product;
                        const imageUrl = `http://localhost:8080/api/product/${id}/image`;

                        return (
                            <div className="col" key={id}>
                                <div className="card h-100 shadow-sm border-0 rounded-4 transition-hover overflow-hidden bg-white">
                                    <Link to={`/product/${id}`} className="text-decoration-none text-dark h-100 d-flex flex-column">
                                        <div className="position-relative" style={{ height: "240px" }}>
                                            <img
                                                src={imageUrl}
                                                alt={name}
                                                className="w-100 h-100"
                                                style={{ objectFit: "cover" }}
                                                onError={(e) => { e.target.src = "https://placehold.co/600x400?text=No+Image"; }}
                                            />
                                            <div className="position-absolute top-0 end-0 p-3">
                                                <button
                                                    className="btn btn-white shadow-sm rounded-circle p-2 d-flex align-items-center justify-content-center"
                                                    style={{ width: "40px", height: "40px" }}
                                                    onClick={(e) => handleToggleFavorite(id, e)}
                                                >
                                                    <Heart size={20} fill="#dc3545" className="text-danger" />
                                                </button>
                                            </div>
                                        </div>

                                        <div className="card-body p-4 d-flex flex-column justify-content-between">
                                            <div>
                                                <p className="text-muted small mb-1 uppercase tracking-wider">{brand}</p>
                                                <h5 className="fw-bold mb-3">{name}</h5>
                                            </div>
                                            <div>
                                                <h4 className="fw-bold text-primary mb-3">₹ {price.toLocaleString()}</h4>
                                                <button
                                                    className="btn btn-primary w-100 py-2 d-flex align-items-center justify-content-center gap-2 rounded-pill shadow-sm"
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
    );
};

export default Favourites;
