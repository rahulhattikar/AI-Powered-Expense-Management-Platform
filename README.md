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
