import React, { useEffect } from 'react';
import { useAuth } from '../Context/AuthContext';

const Login = () => {
    const { login, error } = useAuth();

    useEffect(() => {
        if (!error) {
            // Automatically redirect to the secure Spring Authorization Server
            login();
        }
    }, [login, error]);

    if (error) {
        return (
            <div className="container mt-5 text-center" style={{ paddingTop: '100px' }}>
                <div className="alert alert-danger" role="alert">
                    <h4 className="alert-heading">Something went wrong during Login</h4>
                    <p>{error.message || "An unknown error occurred during authentication."}</p>
                    <hr />
                    <p className="mb-0">Please check the browser console (F12) for more details.</p>
                </div>
                <button className="btn btn-primary mt-3" onClick={() => window.location.href = '/'}>Go back to Home</button>
            </div>
        );
    }

    return (
        <div className="container mt-5 text-center" style={{ paddingTop: '100px' }}>
            <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
            </div>
            <h2 className="mt-3">Redirecting to Secure Login...</h2>
        </div>
    );
};

export default Login;
