# 📚 StudyHive

StudyHive is a collaborative study platform designed to help students and professionals create, share, and discuss study notes in real-time.  
It combines a lightweight room-based collaboration system with chat, file sharing, and a modern microservice backend architecture.

---

## ✨ Features

- 🔑 **User Authentication** – Secure signup/signin with JWT and role-based authorization.  
- 🏠 **Study Rooms** – Create and join open or private study rooms.  
- 📝 **Collaborative Notes** – Add, edit, and share notes with members of a room.  
- 💬 **Messaging Service** – Real-time chat powered by **WebSockets**.  
- 📂 **File Sharing** – Upload and share documents, images, and media via **Firebase Storage**.  
- 🖧 **Microservices** – Modular backend with separate services for users, rooms, notes, and messaging.  
- 📊 **Database** – Managed with JPA/Hibernate, optimized queries, and transaction safety.  
- 🛡 **Error Handling** – Consistent API error responses with custom exception handling.

---

## 🛠 Tech Stack

**Backend**
- Java + Spring Boot  
- Spring Security (JWT Auth)  
- Hibernate / JPA  
- WebSockets for real-time messaging   

**Infrastructure**
- PostgreSQL (primary DB)  
- Firebase Storage (file uploads)  
- Docker (for containerization, **planned**)  

---

## 📐 Architecture

[[Architecture Diagram](https://dbdiagram.io/api/file/68a03ede1d75ee360ad3aec2)](https://dbdiagram.io/d/68a03ede1d75ee360ad3aec2)
---

## 🚀 Getting Started

### 1. Clone the repositories

```bash
git clone https://github.com/whiteNight39/StudyHive.git
````

### 2. Backend Setup

```bash
cd StudyHive-BE
./mvnw clean install
./mvnw spring-boot:run
```

### 4. Environment Variables

Create a `.env` file in both frontend and backend directories with:

```
# Backend
DB_URL=jdbc:postgresql://localhost:5432/studyhive
DB_USERNAME=your_user
DB_PASSWORD=your_password

SECRET_KEY=your_secret_key

SPRING_MAIL_USERNAME=your_email
SPRING_MAIL_PASSWORD=your_password
```

---

## 📌 Roadmap

* [ ] Deploy backend & frontend to cloud
* [ ] Add notifications system
* [ ] Enable video/audio room support
* [ ] Dockerize services for production

---

## 🤝 Contributing

Contributions are welcome!
Open an issue or submit a pull request for feature requests, bug fixes, or documentation improvements.
