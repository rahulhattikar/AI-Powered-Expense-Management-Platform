# AI-Powered-Expense-Management-Platform
Buiding AI powered Expense Management App, that tracks daily expenses of a user.


Production-grade backend project built to learn:

- Spring Boot
- Microservices
- Docker
- Kubernetes
- Kafka
- Redis
- AWS
- CI/CD
- AI Integration

Status:
Phase 1 - Monolith Development


## Tech Stack
- Java 21
- Spring Boot 4.0.6
- Spring Security + JWT
- PostgreSQL 16
- Docker
- Maven

## Features Completed
- ✅ User Authentication (JWT)
- ✅ Expense Management
- ✅ Budget Management
- ✅ Reporting & Analytics

## API Endpoints
### Auth
- POST /api/v1/auth/register
- POST /api/v1/auth/login

### Expenses
- POST   /api/v1/expenses
- GET    /api/v1/expenses
- GET    /api/v1/expenses/{id}
- PUT    /api/v1/expenses/{id}
- DELETE /api/v1/expenses/{id}

### Budgets
- POST   /api/v1/budgets
- GET    /api/v1/budgets
- GET    /api/v1/budgets/{id}
- PUT    /api/v1/budgets/{id}
- DELETE /api/v1/budgets/{id}
- GET    /api/v1/budgets/status

### Reports
- GET /api/v1/reports/monthly-summary
- GET /api/v1/reports/category-summary
- GET /api/v1/reports/top-spending-categories
- GET /api/v1/reports/budget-remaining
- GET /api/v1/reports/monthly-trend

## Setup
1. Clone the repo
2. Start PostgreSQL via Docker: `docker-compose up -d`
3. Run Spring Boot application
4. Access API at `http://localhost:8080`

## Running with Docker

This project is fully containerized — no local Java, Maven, PostgreSQL, or Redis installation required.

### Prerequisites
- Docker Desktop installed and running

### Run the full stack
```bash
git clone https://github.com/rahulhattikar/AI-Powered-Expense-Management-Platform.git
cd AI-Powered-Expense-Management-Platform/ExpenseTracker
docker-compose up --build
```

The application will be available at `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Stopping
```bash
docker-compose down          # stops containers, keeps data
docker-compose down -v       # stops containers AND wipes all data
```
