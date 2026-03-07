import React from 'react';
import { Modal, Button } from 'react-bootstrap';

const CheckoutPopup = ({ show, handleClose, cartItems, totalPrice, handleCheckout }) => {
  return (
    <div className="checkoutPopup">

      <Modal show={show} onHide={handleClose}>
        <Modal.Header closeButton>
          <Modal.Title>Checkout</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className="checkout-items">
            {cartItems.map((item) => (
              <div key={item.id} className="checkout-item" style={{ display: 'flex', marginBottom: '10px' }}>
                <img
                  src={item.product?.imageUrl || `http://localhost:8080/api/product/${item.product?.id}/image`}
                  alt={item.product?.name}
                  style={{ width: '100px', marginRight: '10px', objectFit: 'cover', borderRadius: '8px' }}
                />
                <div>
                  <b><p className="mb-1">{item.product?.name}</p></b>
                  <p className="mb-1 text-muted small">Quantity: {item.quantity}</p>
                  <p className="mb-0 fw-bold text-primary">Price: ₹ {((item.product?.price || 0) * item.quantity).toLocaleString()}</p>
                </div>
              </div>
            ))}
            <div className="total" >
              <h5>Total: ₹ {totalPrice}</h5>
            </div>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Close
          </Button>
          <Button variant="primary" onClick={handleCheckout}>
            Confirm Purchase
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default CheckoutPopup;
