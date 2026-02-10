# E-Commerce Core Backend (REST API)

The foundational **Spring Boot** service that powers the E-Commerce platform. It handles the core business logic, database interactions, and product management.

## 🎯 Primary Functions

1.  **Product Management**: REST APIs for creating, updating, deleting, and retrieving products.
2.  **Image Storage**: Handles uploading and serving product images (stored as BLOBs in the database).
3.  **Automated Seeding**: Includes a `DataSeeder` that automatically populates the DB from `data.sql` and synchronizes image files from a local directory on startup.
4.  **Search & Pagination**: Implementation of search logic and paginated results for the frontend.
5.  **Database Master**: Manages the schema and connections to the MySQL database `springbootdb`.

> **Note**: This service focuses purely on *business logic*. AI and Chat capabilities are delegated to the `ecom-ai` service.

---

## 🛠️ Technology Stack

*   **Java 21**
*   **Spring Boot 3.3.2** (Web, Data JPA)
*   **MySQL 8**
*   **Lombok**
*   **Swagger/OpenAPI**

---

## 🏃‍♂️ Setup & Run

### 1. Database
Ensure **MySQL** is running and a database named `springbootdb` exists.

### 2. Configuration
Check `src/main/resources/application.properties`:
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/springbootdb
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Start Application
```bash
mvn spring-boot:run
```
Server starts on: `http://localhost:8080`

## 📚 API Documentation
*   Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 📂 Project Structure

```text
ecom-proj/
├── src/main/java/com/malcolm/ecomproj/
│   ├── controller/       # REST Controllers (ProductController)
│   ├── model/            # JPA Entities (Product, Category)
│   ├── repo/             # JpaRepository Interfaces
│   └── service/          # Business Logic
└── pom.xml               # Maven Dependencies
```
