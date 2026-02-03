# Full Stack E-Commerce Application

A complete E-Commerce solution featuring a robust **Spring Boot** backend and a dynamic **React** frontend. This project demonstrates a full-stack architecture with RESTful APIs, database persistence, and a responsive user interface.

## 🚀 Key Features

### Backend (Spring Boot)
*   **Product Management**: comprehensive CRUD operations for products.
*   **Search & Pagination**: Advanced search capability with pagination and sorting.
*   **Data Seeding**: Automatic initialization of database records using `DataSeeder`.
*   **Image Handling**: Support for storing and serving product images (BLOB storage).
*   **API Documentation**: Integrated **Swagger/OpenAPI** for easy endpoint testing.

### Frontend (React)
*   **Responsive Design**: Built with **Bootstrap 5** and custom CSS for specific aesthetics.
*   **Product Catalog**: Grid view of products with images and details.
*   **Search Functionality**: Real-time search integration with the backend.
*   **Shopping Cart**: (If implemented) State management for cart operations.
*   **Clean Architecture**: Component-based structure using Vite for fast tooling.

---

## 🛠️ Technology Stack

| Component | Technology |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.3.2, Spring Data JPA, Hibernate, MySQL 8 |
| **Frontend** | React 18, Vite, Bootstrap 5, Axios, React Router 6, Sass |
| **Database** | MySQL |
| **Tools** | Maven, Lombok, Swagger UI |

---

## ⚙️ Prerequisites

*   [Java JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
*   [Node.js](https://nodejs.org/) (v18 or higher)
*   [MySQL Server](https://dev.mysql.com/downloads/mysql/)
*   [Maven](https://maven.apache.org/)

---

## 🏃‍♂️ Setup & Installation

Follow these steps to get the application running locally.

### 1. Database Setup
1.  Open your MySQL Client (Workbench or Command Line).
2.  Create the database:
    ```sql
    CREATE DATABASE springbootdb;
    ```
3.  (Optional) The application is configured to auto-update the schema.

### 2. Backend Setup
1.  Navigate to the `ecom-proj` directory.
2.  Configure Database Credentials:
    Open `src/main/resources/application.properties` and update:
    ```properties
    spring.datasource.username=your_db_username
    spring.datasource.password=your_db_password
    ```
3.  **Data Seeding Note**:
    The `DataSeeder.java` class attempts to load initial product images from a local directory.
    *   Find the `loadImages()` method in `DataSeeder.java`.
    *   Update the `imageDirectoryPath` variable to point to your local image folder if you wish to seed images.
4.  Run the application:
    ```bash
    mvn spring-boot:run
    ```
    The backend will start on `http://localhost:8080`.

### 3. Frontend Setup
1.  Navigate to the frontend directory (e.g., `ecom-frontend-1`):
    ```bash
    cd ../ecom-frontend-1
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm run dev
    ```
    The frontend will typically run on `http://localhost:5173`.

---

## 📚 API Documentation

You can explore the backend APIs using the built-in Swagger UI:
*   **URL**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 📂 Project Structure

```
├── ecom-proj/                # Backend Application
│   ├── src/main/java/        # Spring Boot Controllers, Services, Models
│   ├── src/main/resources/   # Config (application.properties, data.sql)
│   └── pom.xml               # Maven Dependencies
│
├── ecom-frontend-1/          # Frontend Application
│   ├── src/                  # React Components (Pages, Context, Assets)
│   ├── package.json          # Node Dependencies
│   └── vite.config.js        # Vite Configuration
```

## 🤝 Contributing

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/NewFeature`).
3.  Commit your changes.
4.  Push to the branch.
5.  Open a Pull Request.

## 📝 License

This project is open-source and available under the [MIT License](LICENSE).
