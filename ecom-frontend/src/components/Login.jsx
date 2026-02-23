import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import API from '../axios';
import { useAuth } from '../Context/AuthContext';
import { useToast } from '../Context/ToastContext';

const Login = () => {
    const [credentials, setCredentials] = useState({ username: '', password: '' });
    const { login } = useAuth();
    const { addToast } = useToast();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setCredentials({ ...credentials, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await API.post('/auth/login', credentials);
            login(response.data);
            addToast('Login Successful!', 'success');
            navigate('/');
        } catch (error) {
            addToast(error.response?.data?.message || 'Login Failed', 'error');
        }
    };

    return (
        <div className="container mt-5" style={{ maxWidth: '400px', paddingTop: '100px' }}>
            <div className="card shadow p-4">
                <h2 className="text-center mb-4">Login</h2>
                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="form-label">Username</label>
                        <input
                            type="text"
                            name="username"
                            className="form-control"
                            value={credentials.username}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label">Password</label>
                        <input
                            type="password"
                            name="password"
                            className="form-control"
                            value={credentials.password}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <button type="submit" className="btn btn-primary w-100">Login</button>
                </form>
                <p className="mt-3 text-center">
                    Don't have an account? <Link to="/register">Register here</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;
