# Patient Management Microservices

A modern, scalable patient management system built using microservices architecture.

## Services Overview

- **API Gateway**: Entry point for all client requests, handles routing and JWT validation
- **Auth Service**: Manages user authentication and authorization
- **Patient Service**: Core service for patient data management
- **Analytics Service**: Processes patient-related events and generates analytics
- **Billing Service**: Handles billing operations using gRPC

## Technology Stack

- **Framework**: Spring Boot
- **Security**: JWT Authentication
- **Database**: PostgreSQL
- **Message Broker**: Apache Kafka
- **Service Communication**: REST APIs and gRPC
- **Infrastructure**: Docker, LocalStack
- **API Documentation**: HTTP request files included

## Project Structure

```
patient-management/
├── api-gateway/           # API Gateway service
├── auth-service/          # Authentication service
├── patient-service/       # Patient management service
├── analytics-service/     # Analytics processing service
├── billing-service/       # Billing operations service
├── infrastructure/        # Infrastructure configuration
└── api-requests/         # API documentation and example requests
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven

### Running Locally

1. Start the infrastructure services:
   ```bash
   cd infrastructure
   ./localstack-deploy.sh
   ```

2. Build all services:
   ```bash
   mvn clean install
   ```

3. Run each service:
   ```bash
   # Run these commands in separate terminals
   cd api-gateway && mvn spring-boot:run
   cd auth-service && mvn spring-boot:run
   cd patient-service && mvn spring-boot:run
   cd analytics-service && mvn spring-boot:run
   cd billing-service && mvn spring-boot:run
   ```

### API Testing

Example API requests are provided in the `api-requests` directory. These can be executed using any HTTP client that supports `.http` files.

## Service Ports

- API Gateway: 8080
- Auth Service: 8081
- Patient Service: 8082
- Analytics Service: 8083
- Billing Service: 8084

## Features

- Secure authentication using JWT
- Patient CRUD operations
- Real-time analytics processing
- Billing integration
- API gateway with request routing and validation
- Event-driven architecture using Kafka
- gRPC implementation for billing service

## Environment Variables

Each service requires specific environment variables to be set:

### API Gateway
```
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=your-jwt-secret
```

### Auth Service
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/auth_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
JWT_SECRET=your-jwt-secret
```

### Patient Service
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/patient_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Analytics Service
```
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/analytics_db
```

### Billing Service
```
GRPC_SERVER_PORT=9090
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/billing_db
```

## API Documentation

### Auth Service Endpoints
- POST `/api/auth/login` - User login
- POST `/api/auth/validate` - Validate JWT token

### Patient Service Endpoints
- GET `/api/patients` - List all patients
- GET `/api/patients/{id}` - Get patient by ID
- POST `/api/patients` - Create new patient
- PUT `/api/patients/{id}` - Update patient
- DELETE `/api/patients/{id}` - Delete patient

## Monitoring and Observability

The system includes:
- Prometheus metrics endpoint at `/actuator/prometheus`
- Health check endpoints at `/actuator/health`
- Distributed tracing using Spring Cloud Sleuth
- Centralized logging using ELK Stack

## Deployment

### Docker Deployment
```bash
# Build Docker images
docker-compose build

# Start all services
docker-compose up -d

# Stop all services
docker-compose down
```

### Kubernetes Deployment
Kubernetes manifests are available in the `k8s/` directory:
```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods
```

## Performance and Scaling

- Each service can be scaled independently
- Kafka enables async processing for better performance
- Connection pooling configured for database operations
- API Gateway implements rate limiting

## Troubleshooting

Common issues and solutions:
1. **Service Discovery Issues**
   - Verify Eureka Server is running
   - Check service registration status

2. **Database Connectivity**
   - Verify PostgreSQL is running
   - Check connection strings
   - Validate database credentials

3. **Kafka Connection Issues**
   - Ensure Kafka broker is running
   - Verify topic creation
   - Check consumer group configurations

## Security Measures

- JWT-based authentication
- Role-based access control
- API request validation
- Rate limiting
- SSL/TLS encryption
- Database encryption at rest

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
