# Skill Management System

Hệ thống Quản lý Kỹ năng xây dựng trên nền tảng **Spring Boot 3.4** + **Vue 3** + **PostgreSQL 17**.

## Tech Stack

### Backend
- **Java 21 LTS** — Records, Pattern Matching, Virtual Threads, Sealed Classes
- **Spring Boot 3.4.2** — Spring Security 6, Spring Data JPA, Bean Validation
- **PostgreSQL 17** — Relational database
- **Flyway** — Database migration management
- **jjwt 0.12.6** — JWT authentication
- **Maven** — Build tool

### Frontend
- **Vue 3.5** — Composition API, `<script setup>`
- **TypeScript** — Type safety
- **Vite** — Build tool & dev server
- **Pinia** — State management
- **Element Plus** — UI component library
- **Axios** — HTTP client

## Project Structure

```
skill-management/
├── backend/                    # Spring Boot API
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/
│       ├── main/java/com/furelise/skillmanagement/
│       │   ├── config/         # Security, CORS, App config
│       │   ├── controller/     # REST controllers
│       │   ├── dto/            # Java Records (request/response)
│       │   ├── model/          # JPA entities
│       │   ├── repository/     # Spring Data repositories
│       │   └── service/        # Business logic
│       └── main/resources/
│           ├── application.yml
│           └── db/migration/   # Flyway SQL migrations
│
├── frontend/                   # Vue 3 SPA
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── views/              # Page components
│       ├── stores/             # Pinia stores
│       ├── services/           # API client
│       ├── types/              # TypeScript types
│       └── router/             # Vue Router
│
├── docker-compose.yml          # PostgreSQL 17
└── README.md
```

## Quick Start

### Prerequisites
- Java 21 LTS
- Node.js 18+
- Docker (for PostgreSQL) or PostgreSQL 17 installed locally

### 1. Start Database

```bash
docker compose up -d
```

### 2. Start Backend

```bash
cd backend
./mvnw spring-boot:run
# On Windows:
# set JAVA_HOME=C:\Program Files\Java\jdk-21
# mvnw.cmd spring-boot:run
```
Backend runs at http://localhost:8080

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```
Frontend runs at http://localhost:5173 (with API proxy to backend)

## API Endpoints

| Method | Endpoint | Access | Description |
|:---|:---|:---|:---|
| POST | `/api/v1/auth/register` | Public | Register new user |
| POST | `/api/v1/auth/authenticate` | Public | Login |
| GET | `/api/v1/user/current-user` | Authenticated | Get current user profile |
| GET | `/api/v1/admin/get-users` | Admin only | List all users |
| DELETE | `/api/v1/admin/delete-user/{email}` | Admin only | Delete user |
| GET | `/health` | Public | Health check |

## Environment Variables

| Variable | Default | Description |
|:---|:---|:---|
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | Dev key | JWT signing key (Base64) |
| `CORS_ORIGINS` | `http://localhost:5173` | Allowed CORS origins |
