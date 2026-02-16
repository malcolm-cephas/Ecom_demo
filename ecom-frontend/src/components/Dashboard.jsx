import React, { useEffect, useState } from "react";
import axios from "../axios";
import { Link } from "react-router-dom";
import { useToast } from "../Context/ToastContext";

const Dashboard = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const { addToast } = useToast();
    const [editingStockIds, setEditingStockIds] = useState({});

    useEffect(() => {
        fetchProducts();
    }, []);

    const fetchProducts = async () => {
        try {
            const response = await axios.get("/products");
            setProducts(response.data);
            // Initialize editing state
            const initialStock = {};
            response.data.forEach(p => {
                initialStock[p.id] = p.stockQuantity;
            });
            setEditingStockIds(initialStock);
        } catch (error) {
            console.error("Error fetching products:", error);
            addToast("Failed to load products", "error");
        } finally {
            setLoading(false);
        }
    };

    const handleStockChange = (id, value) => {
        setEditingStockIds(prev => ({ ...prev, [id]: value }));
    };

    const updateStock = async (id) => {
        const newStock = editingStockIds[id];
        try {
            await axios.patch(`/product/${id}/stock`, Number(newStock), {
                headers: { "Content-Type": "application/json" }
            });
            addToast("Stock updated successfully", "success");
            fetchProducts(); // Refresh list to get updated availability status
        } catch (error) {
            console.error("Error updating stock:", error);
            addToast("Failed to update stock", "error");
        }
    };

    const deleteProduct = async (id) => {
        if (window.confirm("Are you sure you want to delete this product?")) {
            try {
                await axios.delete(`/product/${id}`);
                addToast("Product deleted successfully", "success");
                setProducts(prev => prev.filter(p => p.id !== id));
            } catch (error) {
                console.error("Error deleting product:", error);
                addToast("Failed to delete product", "error");
            }
        }
    };

    return (
        <div className="container" style={{ marginTop: "100px", marginBottom: "50px" }}>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2>Inventory Dashboard</h2>
                <Link to="/add_product" className="btn btn-primary">
                    <i className="bi bi-plus-lg me-2"></i>Add New Product
                </Link>
            </div>

            {loading ? (
                <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            ) : (
                <div className="table-responsive shadow-sm rounded">
                    <table className="table table-hover align-middle">
                        <thead className="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Image</th>
                                <th>Name</th>
                                <th>Brand</th>
                                <th>Price</th>
                                <th>Category</th>
                                <th>Stock</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {products.map((product) => (
                                <tr key={product.id}>
                                    <td>{product.id}</td>
                                    <td>
                                        <img
                                            src={`http://localhost:8080/api/product/${product.id}/image`}
                                            alt={product.name}
                                            style={{ width: "50px", height: "50px", objectFit: "cover", borderRadius: "4px" }}
                                            onError={(e) => { e.target.src = "https://placehold.co/50"; }}
                                        />
                                    </td>
                                    <td>
                                        <Link to={`/product/${product.id}`} className="text-decoration-none fw-bold text-dark">
                                            {product.name}
                                        </Link>
                                    </td>
                                    <td>{product.brand}</td>
                                    <td>Rs {product.price}</td>
                                    <td>{product.category}</td>
                                    <td style={{ minWidth: "150px" }}>
                                        <div className="input-group input-group-sm">
                                            <input
                                                type="number"
                                                className="form-control"
                                                value={editingStockIds[product.id] || 0}
                                                onChange={(e) => handleStockChange(product.id, e.target.value)}
                                                min="0"
                                            />
                                            <button
                                                className="btn btn-outline-success"
                                                type="button"
                                                onClick={() => updateStock(product.id)}
                                                disabled={editingStockIds[product.id] == product.stockQuantity}
                                            >
                                                <i className="bi bi-check-lg"></i>
                                            </button>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="btn-group btn-group-sm">

                                            <Link to={`/product/${product.id}`} className="btn btn-outline-primary">
                                                <i className="bi bi-eye"></i>
                                            </Link>
                                            <button
                                                className="btn btn-outline-danger"
                                                onClick={() => deleteProduct(product.id)}
                                            >
                                                <i className="bi bi-trash"></i>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default Dashboard;
