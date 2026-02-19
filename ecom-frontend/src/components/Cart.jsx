import React, { useContext, useState, useEffect } from "react";
import AppContext from "../Context/Context";
import axios from "../axios";
import CheckoutPopup from "./CheckoutPopup";
import { Button } from 'react-bootstrap';
import { useToast } from "../Context/ToastContext";

const Cart = () => {
  const { cart, removeFromCart, clearCart, updateQuantity } = useContext(AppContext);
  const { addToast } = useToast();
  const [totalPrice, setTotalPrice] = useState(0);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const total = cart.reduce(
      (acc, item) => acc + item.price * item.quantity,
      0
    );
    setTotalPrice(total);
  }, [cart]);

  const handleIncreaseQuantity = (item) => {
    if (item.quantity < item.stockQuantity) {
      updateQuantity(item.id, item.quantity + 1);
    } else {
      addToast("Cannot add more than available stock", "error");
    }
  };

  const handleDecreaseQuantity = (item) => {
    if (item.quantity > 1) {
      updateQuantity(item.id, item.quantity - 1);
    }
  };

  const handleRemoveFromCart = (itemId) => {
    removeFromCart(itemId);
  };

  const handleCheckout = async () => {
    try {
      for (const item of cart) {
        const updatedStockQuantity = item.stockQuantity - item.quantity;
        // Use the new PATCH endpoint for updating stock
        await axios.patch(`/product/${item.id}/stock`, Number(updatedStockQuantity), {
          headers: { "Content-Type": "application/json" }
        });
      }
      addToast("Checkout successful!", "success");
      clearCart();
      setShowModal(false);
    } catch (error) {
      console.log("error during checkout", error);
      addToast("Checkout failed", "error");
    }
  };

  return (
    <div className="cart-container">
      <div className="shopping-cart">
        <div className="title">Shopping Bag</div>
        {cart.length === 0 ? (
          <div className="empty" style={{ textAlign: "left", padding: "2rem" }}>
            <h4>Your cart is empty</h4>
          </div>
        ) : (
          <>
            {cart.map((item) => (
              <li key={item.id} className="cart-item">
                <div
                  className="item"
                  style={{ display: "flex", alignContent: "center" }}
                >
                  <div className="buttons">
                    <div className="buttons-liked">
                      <i className="bi bi-heart"></i>
                    </div>
                  </div>
                  <div>
                    <img
                      src={`http://localhost:8080/api/product/${item.id}/image`}
                      alt={item.name}
                      className="cart-item-image"
                      onError={(e) => { e.target.src = "https://placehold.co/100"; }}
                    />
                  </div>
                  <div className="description">
                    <span>{item.brand}</span>
                    <span>{item.name}</span>
                  </div>

                  <div className="quantity">
                    <button
                      className="plus-btn"
                      type="button"
                      onClick={() => handleIncreaseQuantity(item)}
                    >
                      <i className="bi bi-plus-square-fill"></i>
                    </button>
                    <input
                      type="button"
                      value={item.quantity}
                      readOnly
                    />
                    <button
                      className="minus-btn"
                      type="button"
                      onClick={() => handleDecreaseQuantity(item)}
                    >
                      <i className="bi bi-dash-square-fill"></i>
                    </button>
                  </div>

                  <div className="total-price " style={{ textAlign: "center" }}>
                    ₹ {item.price * item.quantity}
                  </div>
                  <button
                    className="remove-btn"
                    onClick={() => handleRemoveFromCart(item.id)}
                  >
                    <i className="bi bi-trash3-fill"></i>
                  </button>
                </div>
              </li>
            ))}
            <div className="total">Total: ₹ {totalPrice}</div>
            <Button
              className="btn btn-primary"
              style={{ width: "100%" }}
              onClick={() => setShowModal(true)}
            >
              Checkout
            </Button>
          </>
        )}
      </div>
      <CheckoutPopup
        show={showModal}
        handleClose={() => setShowModal(false)}
        cartItems={cart}
        totalPrice={totalPrice}
        handleCheckout={handleCheckout}
      />
    </div>
  );
};

export default Cart;
