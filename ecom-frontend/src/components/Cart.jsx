import React, { useState } from "react";
import { Link } from "react-router-dom";
import useCartStore from "../store/useCartStore";
import axios from "../axios";
import CheckoutPopup from "./CheckoutPopup";
import {
  Trash2,
  Plus,
  Minus,
  Heart,
  ShoppingBag,
  ArrowRight
} from "lucide-react";
import { toast } from "react-hot-toast";

const Cart = () => {
  const { cart, removeFromCart, clearCart, updateQuantity } = useCartStore();
  const [showModal, setShowModal] = useState(false);

  // Cart items are stored in cart.items
  const items = cart?.items || [];

  const totalPrice = items.reduce(
    (acc, item) => acc + (item.product?.price || 0) * item.quantity,
    0
  );

  const handleIncreaseQuantity = (item) => {
    if (item.quantity < item.product.stockQuantity) {
      updateQuantity(item.product.id, item.quantity + 1);
    } else {
      toast.error("Cannot add more than available stock");
    }
  };

  const handleDecreaseQuantity = (item) => {
    if (item.quantity > 1) {
      updateQuantity(item.product.id, item.quantity - 1);
    }
  };

  const handleCheckout = async () => {
    try {
      for (const item of items) {
        const updatedStockQuantity = item.product.stockQuantity - item.quantity;
        await axios.patch(`/product/${item.product.id}/stock`, Number(updatedStockQuantity), {
          headers: { "Content-Type": "application/json" }
        });
      }
      toast.success("Checkout successful!");
      clearCart();
      setShowModal(false);
    } catch (error) {
      console.log("error during checkout", error);
      toast.error("Checkout failed");
    }
  };

  return (
    <div className="container" style={{ marginTop: "120px", marginBottom: "100px" }}>
      <div className="row justify-content-center">
        <div className="col-lg-8">
          <div className="card border-0 shadow-sm overflow-hidden" style={{ borderRadius: "20px" }}>
            <div className="card-header bg-white border-bottom p-4 d-flex align-items-center gap-3">
              <ShoppingBag className="text-primary" size={24} />
              <h4 className="mb-0 fw-bold">Shopping Bag</h4>
              <span className="badge bg-light text-primary rounded-pill ms-auto">
                {items.length} {items.length === 1 ? 'item' : 'items'}
              </span>
              {items.length > 0 && (
                <button
                  className="btn btn-sm btn-outline-danger border-0 ms-2 fw-medium"
                  onClick={() => {
                    if (window.confirm("Clear all items?")) clearCart();
                  }}
                >
                  Clear All
                </button>
              )}
            </div>

            <div className="card-body p-0">
              {items.length === 0 ? (
                <div className="text-center py-5">
                  <ShoppingBag size={64} className="text-muted mb-3 opacity-25" />
                  <h4 className="text-muted">Your cart is empty</h4>
                  <p className="text-muted small">Looks like you haven't added anything to your bag yet.</p>
                  <Link to="/" className="btn btn-primary mt-3 rounded-pill px-4">Continue Shopping</Link>
                </div>
              ) : (
                <div className="list-group list-group-flush">
                  {items.map((item) => (
                    <div key={item.id} className="list-group-item p-4 border-0 border-bottom">
                      <div className="d-flex align-items-center gap-4">
                        <div style={{ width: "100px", height: "100px", flexShrink: 0 }}>
                          <img
                            src={item.product?.imageUrl || `http://localhost:8080/api/product/${item.product?.id}/image`}
                            alt={item.product?.name}
                            className="w-100 h-100 rounded shadow-sm"
                            style={{ objectFit: "cover" }}
                            onError={(e) => { e.target.src = "https://placehold.co/100?text=No+Image"; }}
                          />
                        </div>

                        <div className="flex-grow-1">
                          <div className="d-flex justify-content-between align-items-start mb-2">
                            <div>
                              <p className="text-muted small mb-0 uppercase tracking-wider">{item.product?.brand}</p>
                              <h5 className="fw-bold mb-0">{item.product?.name}</h5>
                            </div>
                            <h5 className="fw-bold text-primary mb-0">₹ {((item.product?.price || 0) * item.quantity).toLocaleString()}</h5>
                          </div>

                          <div className="d-flex align-items-center gap-4 mt-3">
                            <div className="d-flex align-items-center bg-light rounded-pill p-1">
                              <button
                                className="btn btn-sm btn-white rounded-circle shadow-sm p-1"
                                onClick={() => handleDecreaseQuantity(item)}
                              >
                                <Minus size={14} />
                              </button>
                              <span className="px-3 fw-bold small">{item.quantity}</span>
                              <button
                                className="btn btn-sm btn-white rounded-circle shadow-sm p-1"
                                onClick={() => handleIncreaseQuantity(item)}
                              >
                                <Plus size={14} />
                              </button>
                            </div>

                            <div className="ms-auto d-flex gap-2">
                              <button className="btn btn-sm btn-outline-light border-0 text-muted p-2 rounded-circle">
                                <Heart size={18} />
                              </button>
                              <button
                                className="btn btn-sm btn-danger-subtle border-0 p-2 rounded-circle shadow-sm"
                                title="Remove item"
                                onClick={() => removeFromCart(item.product.id)}
                                style={{
                                  backgroundColor: "rgba(220, 53, 69, 0.1)",
                                  color: "#dc3545",
                                  transition: "all 0.2s ease"
                                }}
                              >
                                <Trash2 size={18} />
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {items.length > 0 && (
              <div className="card-footer bg-light border-0 p-4">
                <div className="d-flex justify-content-between align-items-center mb-4">
                  <h5 className="mb-0 text-muted">Subtotal</h5>
                  <h3 className="mb-0 fw-bold text-primary">₹ {totalPrice.toLocaleString()}</h3>
                </div>
                <button
                  className="btn btn-primary w-100 py-3 rounded-pill fw-bold shadow-sm d-flex align-items-center justify-content-center gap-2"
                  onClick={() => setShowModal(true)}
                >
                  Proceed to Checkout
                  <ArrowRight size={20} />
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      <CheckoutPopup
        show={showModal}
        handleClose={() => setShowModal(false)}
        cartItems={items}
        totalPrice={totalPrice}
        handleCheckout={handleCheckout}
      />
    </div>
  );
};

export default Cart;
