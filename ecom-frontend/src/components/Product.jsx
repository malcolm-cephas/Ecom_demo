import { useNavigate, useParams, Link } from "react-router-dom";
import { useEffect, useState } from "react";
import useCartStore from "../store/useCartStore";
import useUserStore from "../store/useUserStore";
import useProductStore from "../store/useProductStore";
import axios from "../axios";
import {
  Heart,
  ShoppingCart,
  Edit,
  Trash2,
  Calendar,
  ArrowLeft,
  Package,
  CheckCircle2,
  XCircle
} from "lucide-react";
import { toast } from "react-hot-toast";

const Product = () => {
  const { id } = useParams();
  const { addToCart, removeFromCart } = useCartStore();
  const { isAuthenticated } = useUserStore();
  const { toggleFavorite } = useProductStore();
  const [product, setProduct] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await axios.get(`/product/${id}`);
        setProduct(response.data);
      } catch (error) {
        console.error("Error fetching product:", error);
        toast.error("Failed to load product details");
      }
    };

    fetchProduct();
  }, [id]);

  const imageUrl = `http://localhost:8080/api/product/${id}/image`;

  const deleteProduct = async () => {
    if (!window.confirm("Are you sure you want to delete this product?")) return;

    try {
      await axios.delete(`/product/${id}`);
      removeFromCart(id);
      toast.success("Product deleted successfully");
      navigate("/");
    } catch (error) {
      console.error("Error deleting product:", error);
      toast.error("Failed to delete product");
    }
  };

  const handlAddToCart = async () => {
    if (!isAuthenticated) {
      toast.error("Please login to add items to cart");
      navigate('/login');
      return;
    }

    if (product.stockQuantity > 0) {
      await addToCart(id, 1);
    } else {
      toast.error("Product is out of stock");
    }
  };

  const handleToggleFavorite = async () => {
    if (!isAuthenticated) {
      toast.error("Please login to add favorites");
      navigate('/login');
      return;
    }

    try {
      const response = await toggleFavorite(id);
      setProduct({ ...product, favorite: response.favorite });
      toast.success(response.favorite ? "Added to favorites!" : "Removed from favorites!");
    } catch (error) {
      toast.error("Failed to update favorites");
    }
  };

  if (!product) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "80vh" }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ marginTop: "120px", marginBottom: "100px" }}>
      <button
        className="btn btn-link text-decoration-none text-muted mb-4 d-flex align-items-center gap-2 p-0"
        onClick={() => navigate(-1)}
      >
        <ArrowLeft size={18} />
        Back to Products
      </button>

      <div className="row g-5">
        <div className="col-md-6">
          <div className="card border-0 shadow-sm overflow-hidden" style={{ borderRadius: "24px" }}>
            <img
              src={imageUrl}
              alt={product.name}
              className="img-fluid"
              style={{ minHeight: "400px", objectFit: "cover", width: "100%" }}
              onError={(e) => {
                e.target.onerror = null;
                e.target.src = "https://placehold.co/600x600?text=Product+Image";
              }}
            />
          </div>
        </div>

        <div className="col-md-6">
          <div className="product-info ps-md-4">
            <div className="d-flex align-items-center gap-2 mb-2 text-primary fw-bold small uppercase tracking-wider">
              <Package size={16} />
              {product.category}
            </div>

            <h1 className="display-5 fw-bold mb-1">{product.name}</h1>
            <p className="text-muted fs-5 mb-4">{product.brand}</p>

            <div className="d-flex align-items-center gap-3 mb-4">
              <h2 className="fw-bold text-primary mb-0">₹ {product.price.toLocaleString()}</h2>
              {product.available ? (
                <span className="badge bg-success-subtle text-success border border-success-subtle rounded-pill px-3 py-2 d-flex align-items-center gap-1">
                  <CheckCircle2 size={14} />
                  In Stock
                </span>
              ) : (
                <span className="badge bg-danger-subtle text-danger border border-danger-subtle rounded-pill px-3 py-2 d-flex align-items-center gap-1">
                  <XCircle size={14} />
                  Out of Stock
                </span>
              )}
            </div>

            <div className="p-4 bg-light rounded-4 mb-4">
              <h6 className="fw-bold mb-2">Description</h6>
              <p className="text-muted mb-0" style={{ lineHeight: "1.6" }}>
                {product.description}
              </p>
            </div>

            <div className="d-flex align-items-center gap-3 mb-4">
              <div className="flex-grow-1">
                <button
                  className={`btn ${product.available ? 'btn-primary' : 'btn-secondary'} w-100 py-3 rounded-pill fw-bold shadow-sm d-flex align-items-center justify-content-center gap-2`}
                  onClick={handlAddToCart}
                  disabled={!product.available}
                >
                  <ShoppingCart size={20} />
                  {product.available ? "Add to Cart" : "Out of Stock"}
                </button>
              </div>
              <button
                className="btn btn-outline-light border shadow-sm p-3 rounded-circle d-flex align-items-center justify-content-center"
                style={{ width: "56px", height: "56px" }}
                onClick={handleToggleFavorite}
              >
                <Heart size={24} fill={product.favorite ? "#dc3545" : "none"} className={product.favorite ? "text-danger" : "text-muted"} />
              </button>
            </div>

            <div className="d-flex align-items-center gap-4 text-muted small mb-5">
              <div className="d-flex align-items-center gap-1">
                <Calendar size={14} />
                <span>Listed on: {product.releaseDate}</span>
              </div>
              <div className="d-flex align-items-center gap-1">
                <Package size={14} />
                <span>Stock: {product.stockQuantity} items</span>
              </div>
            </div>

            <div className="d-flex gap-2">
              <button
                className="btn btn-outline-primary rounded-pill px-4 d-flex align-items-center gap-2"
                onClick={() => navigate(`/product/update/${id}`)}
              >
                <Edit size={16} />
                Edit Product
              </button>
              <button
                className="btn btn-outline-danger rounded-pill px-4 d-flex align-items-center gap-2"
                onClick={deleteProduct}
              >
                <Trash2 size={16} />
                Delete
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Product;
