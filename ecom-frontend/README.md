# E-Commerce Frontend (React)

This is the user interface for the E-Commerce platform, built with **React** and **Vite**. It provides a responsive shopping experience and an integrated AI chat assistant.

## 🚀 Features

*   **Product Gallery**: Browse products with dynamic images served directly from the backend.
*   **Search & Filter**: Find products using a real-time search bar and category filters.
*   **AI Chat Integration**: An integrated chat widget that allows users to talk to an intelligent store assistant.
*   **Cart & Favorites**: Full shopping functionality including managing a shopping cart and a "Favorites" list.
*   **Clean UI**: Responsive design built with Bootstrap 5 and customized CSS.

---

## 🛠️ Technology Stack

| Component | Technology |
| :--- | :--- |
| **Framework** | React 18, Vite |
| **Styling** | Bootstrap 5, CSS |
| **HTTP Client** | Axios |
| **Routing** | React Router DOM |

---

## ⚙️ Prerequisites

*   [Node.js](https://nodejs.org/) (v18 or higher)
*   The **Backend** (`ecom-proj`) running on port `8080`.
*   The **AI Service** (`ecom-ai`) running on port `8081` (for chat features).

---

## 🏃‍♂️ Setup & Run

1.  **Install Dependencies**:
    ```bash
    npm install
    ```

2.  **Start Development Server**:
    ```bash
    npm run dev
    ```

3.  **Access the App**:
    Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 🔌 API Connections

This frontend connects to two backend services:
*   **Core API**: `http://localhost:8080` (Products, Images)
*   **AI API**: `http://localhost:8081` (Chat, Intelligent Search)

Make sure both services are running for full functionality.

---

## 📂 Project Structure

```text
ecom-frontend/
├── src/
│   ├── components/       # Reusable UI components (Navbar, ProductCard)
│   ├── App.jsx           # Main application layout
│   └── main.jsx          # Entry point
├── public/               # Static assets
└── package.json          # NPM dependencies
```
