import React, { createContext, useState, useContext, useCallback } from 'react';

const ToastContext = createContext();

export const useToast = () => useContext(ToastContext);

export const ToastProvider = ({ children }) => {
    const [toasts, setToasts] = useState([]);

    const addToast = useCallback((message, type = 'info') => {
        const id = Date.now();
        setToasts((prevToasts) => [...prevToasts, { id, message, type }]);

        // Auto remove after 3 seconds
        setTimeout(() => {
            removeToast(id);
        }, 3000);
    }, []);

    const removeToast = useCallback((id) => {
        setToasts((prevToasts) => prevToasts.filter((toast) => toast.id !== id));
    }, []);

    return (
        <ToastContext.Provider value={{ addToast, removeToast, toasts }}>
            {children}
            <div className="toast-container position-fixed bottom-0 end-0 p-3" style={{ zIndex: 1100 }}>
                {toasts.map((toast) => (
                    <div
                        key={toast.id}
                        className={`toast show align-items-center text-white bg-${toast.type === 'success' ? 'success' : toast.type === 'error' ? 'danger' : 'primary'} border-0`}
                        role="alert"
                        aria-live="assertive"
                        aria-atomic="true"
                        style={{ marginBottom: '0.5rem' }}
                    >
                        <div className="d-flex">
                            <div className="toast-body">
                                {toast.message}
                            </div>
                            <button
                                type="button"
                                className="btn-close btn-close-white me-2 m-auto"
                                data-bs-dismiss="toast"
                                aria-label="Close"
                                onClick={() => removeToast(toast.id)}
                            ></button>
                        </div>
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    );
};

export default ToastContext;
