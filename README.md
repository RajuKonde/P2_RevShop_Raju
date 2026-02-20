# P2_RevShop_Raju
Full-stack e-commerce web application using Spring Boot, Thymeleaf, Oracle DB with buyer and seller modules.
# 🛒 RevShop - Full Stack E-Commerce Application

RevShop is a full-stack monolithic e-commerce web application built using **Spring Boot**, **Thymeleaf**, and **Oracle Database**.  
The platform supports both buyers and sellers with real-world e-commerce functionality including product management, cart operations, order processing, and review systems.

---

## 🎯 Project Objective

To design and implement a scalable e-commerce platform using enterprise-level layered architecture, normalized relational database design, and modular Spring Boot development.

---

## 🚀 Features

### 👤 Buyer Features
- User registration & authentication
- Browse and search products
- Sorting by price, brand, discount
- Add to cart & checkout
- Order history tracking
- Reviews and ratings
- Wishlist management
- Profile management

### 🏪 Seller Features
- Product CRUD operations
- Inventory management
- Order tracking
- Discount and pricing control
- Seller rating system

---

## 🛠 Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate

### Frontend
- Thymeleaf
- Bootstrap

### Database
- Oracle Database

### Tools
- Maven
- Log4j
- JUnit
- Git & GitHub

---

## 🗄 Database Design

The system uses a fully normalized relational schema consisting of:

- USER
- USER_PROFILE
- SELLER
- CATEGORY
- PRODUCT
- CART
- CART_ITEM
- ORDERS
- ORDER_ITEM
- REVIEW
- WISHLIST
- NOTIFICATION
- PASSWORD_RESET

Designed using enterprise layered architecture and Oracle relational modeling.

---

## 📊 ER Diagram

![ER Diagram](docs/RevShop_ER_Diagram.png)

---

## 🏗 Architecture

The application follows a layered architecture pattern:

Controller → Service → Repository → Database

This ensures separation of concerns, scalability, and maintainability.

---

## 📌 Project Status

✅ Database design completed  
✅ ER diagram completed  
🚧 Backend development in progress  

---

## 👨‍💻 Author

**Raju Konde**  
Java Full Stack Developer
