# Backend
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
For unit-tests a h2 in-memory database is used.

## Resources
Spring Boot
Maven
