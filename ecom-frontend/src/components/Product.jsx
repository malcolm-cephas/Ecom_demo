import { useNavigate, useParams } from "react-router-dom";
import { useContext, useEffect, useState } from "react";
import AppContext from "../Context/Context";
import axios from "../axios";
import { useToast } from "../Context/ToastContext";

const Product = () => {
  const { id } = useParams();
  const { addToCart, removeFromCart, toggleFavorite, refreshData } = useContext(AppContext);
  const { addToast } = useToast();
  const [product, setProduct] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await axios.get(
          `/product/${id}`
        );
        setProduct(response.data);
      } catch (error) {
        console.error("Error fetching product:", error);
      }
    };

    fetchProduct();
  }, [id]);

  const imageUrl = `http://localhost:8080/api/product/${id}/image`;

  const deleteProduct = async () => {
    try {
      await axios.delete(`/product/${id}`);
      removeFromCart(id);
      addToast("Product deleted successfully", "success");
      refreshData();
      navigate("/");
    } catch (error) {
      console.error("Error deleting product:", error);
      addToast("Failed to delete product", "error");
    }
  };

  const handleEditClick = () => {
    navigate(`/product/update/${id}`);
  };

  const handlAddToCart = () => {
    if (product.stockQuantity > 0) {
      addToCart(product);
      addToast("Product added to cart", "success");
    } else {
      addToast("Product is out of stock", "error");
    }
  };

  const handleToggleFavorite = async () => {
    const updatedProduct = await toggleFavorite(product.id);
    if (updatedProduct) {
      setProduct({ ...product, favorite: updatedProduct.favorite });
    }
  };
  if (!product) {
    return (
      <h2 className="text-center" style={{ padding: "10rem" }}>
        Loading...
      </h2>
    );
  }
  return (
    <>
      <div className="containers">
        <img
          className="left-column-img"
          src={imageUrl}
          alt={product.imageName}
          onError={(e) => {
            e.target.onerror = null;
            e.target.src = "https://placehold.co/600x400";
          }}
        />

        <div className="right-column">
          <div className="product-description">
            <span>{product.category}</span>
            <h1>{product.name}</h1>
            <h5>{product.brand}</h5>
            <p>{product.description}</p>
          </div>

          <div className="product-price">
            <span>{"₹" + product.price}</span>
            <button
              className={`cart-btn ${!product.available ? "disabled-btn" : ""
                }`}
              onClick={handlAddToCart}
              disabled={!product.available}
            >
              {product.available ? "Add to cart" : "Out of Stock"}
            </button>
            <h6>
              Stock Available :{" "}
              <i style={{ color: "green", fontWeight: "bold" }}>
                {product.stockQuantity}
              </i>
              <button
                className="ms-3 border-0 bg-transparent"
                onClick={handleToggleFavorite}
                style={{ cursor: "pointer" }}
              >
                <i
                  className={`bi ${product.favorite ? "bi-heart-fill text-danger" : "bi-heart text-danger"}`}
                  style={{ fontSize: "1.5rem" }}
                ></i>
              </button>
            </h6>
            <p className="release-date">
              <h6>Product listed on:</h6>
              <i> {product.releaseDate.replace(/-/g, " ")}</i>
            </p>
          </div>
          <div className="update-button ">
            <button
              className="btn btn-primary"
              type="button"
              onClick={handleEditClick}
            >
              Update
            </button>
            {/* <UpdateProduct product={product} onUpdate={handleUpdate} /> */}
            <button
              className="btn btn-primary"
              type="button"
              onClick={deleteProduct}
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default Product;
