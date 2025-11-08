# Backend
[![CI Build](https://github.com/amosproj/amos2025ws02-backup-cluster-manager/actions/workflows/ci-build-backend.yml/badge.svg)](https://github.com/amosproj/amos2025ws02-backup-cluster-manager/actions)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/amosproj/amos2025ws02-backup-cluster-manager/actions)
[![Branches](.github/badges/branches.svg)](https://github.com/amosproj/amos2025ws02-backup-cluster-manager/actions)

## Example Project Structure
```bash
backend/
├── src/
│   ├── main/
│   │   ├── java/com/bcm/
│   │   │   ├── BackendApplication.java
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── dto/
│   │   │   │   └── ApiResponse.java
│   │   │   ├── clusters/
│   │   │   │   ├── ClusterController.java
│   │   │   │   └── ClusterService.java
│   │   │   └── nodes/
│   │   │       └── NodeService.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/java/com/bcm/
│       └── BackendApplicationTests.java
├── pom.xml
└── README.md
```

## Prerequisites
Java 21+
Maven 3.8+
Docker Desktop (for local development)

## Build & Run
```bash
mvn clean install
```

To run the application locally you need to start a local PostgreSQL DB in the root directory.
This requires docker (desktop) to be installed and running.

```bash
docker-compose up 
```

```bash
mvn spring-boot:run
```
Server runs at http://localhost:8080/

## API Endpoints
curl http://localhost:8080/example

## Testing

```bash
mvn test
```

Run tests with coverage report:
```bash
mvn clean test
open target/site/jacoco/index.html
```

For unit-tests a h2 in-memory database is used.

## Resources
Spring Boot
Maven

## Building the Docker Image and running local docker-compose setup

multi-stage Docker build for Spring Boot bcm application

To build and run locally:

```bash
# Build the image
docker build -t backend:latest .

# Run the container
docker run -p 8080:8080 backend:latest
```

The back- and frontend images need to be built to start successfully.
To start the cluster setup in the project root directory:

```bash
cd ..

docker-compose up
```
