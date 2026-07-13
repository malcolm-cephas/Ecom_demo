# E-Commerce Core Backend (REST API)

The foundational **Spring Boot** service that powers the E-Commerce platform. It handles the core business logic, database interactions, and product management.

## 🎯 Primary Functions

1.  **Product Management**: REST APIs for creating, updating, deleting, and retrieving products.
2.  **Image Storage**: Handles uploading and serving product images (stored as BLOBs in the database).
3.  **Automated Seeding**: Includes a `DataSeeder` that automatically populates the DB from `data.sql` and synchronizes image files from a local directory on startup.
4.  **Search & Pagination**: Implementation of search logic and paginated results for the frontend.
5.  **Database Master**: Manages the schema and connections to the MySQL database `springbootdb`.
6.  **Request Logging**: All incoming HTTP requests are logged with `[BACKEND-REQUEST]` prefix for debugging and monitoring.
7.  **Data Provider for MCP**: Serves as the authoritative source for all products and categories used by the AI assistant.

> **Note**: This service is the "Source of Truth" for the entire ecosystem. The AI orchestration layers (`ecom-agent`, `ecom-ai`) rely strictly on this API to access product data.

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

#### Option 1: Automated (Recommended)
From the project root, run:
```bash
run_all.bat
```
This starts all services including the backend.

#### Option 2: Manual
```bash
mvnw spring-boot:run
```
Server starts on: `http://localhost:8080`

---

## 📊 Logging

Logs are output to both:
- **Console/Terminal**: Real-time request monitoring
- **Log File**: `../Logs/spring_backend.log`

All HTTP requests are logged with the format:
```
[BACKEND-REQUEST] GET /api/products
[BACKEND-REQUEST] POST /api/products
```

---

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
