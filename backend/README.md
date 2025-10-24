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

## Build & Run
```bash
mvn clean install
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
## Resources
Spring Boot
Maven
