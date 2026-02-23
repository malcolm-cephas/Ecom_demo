import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import API from '../axios';
import { useAuth } from '../Context/AuthContext';
import { useToast } from '../Context/ToastContext';

const Register = () => {
    const [user, setUser] = useState({ username: '', email: '', password: '' });
    const { login } = useAuth();
    const { addToast } = useToast();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setUser({ ...user, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await API.post('/auth/register', user);
            login(response.data);
            addToast('Registration Successful!', 'success');
            navigate('/');
        } catch (error) {
            addToast(error.response?.data?.message || 'Registration Failed', 'error');
        }
    };

    return (
        <div className="container mt-5" style={{ maxWidth: '400px', paddingTop: '100px' }}>
            <div className="card shadow p-4">
                <h2 className="text-center mb-4">Register</h2>
                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="form-label">Username</label>
                        <input
                            type="text"
                            name="username"
                            className="form-control"
                            value={user.username}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label">Email</label>
                        <input
                            type="email"
                            name="email"
                            className="form-control"
                            value={user.email}
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
                            value={user.password}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <button type="submit" className="btn btn-success w-100">Register</button>
                </form>
                <p className="mt-3 text-center">
                    Already have an account? <Link to="/login">Login here</Link>
                </p>
            </div>
        </div>
    );
};

export default Register;
