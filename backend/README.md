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

## Node Roles and configuring profiles

The application runs with default functionality stored in the *.shared-package.
To extend the functionality of the application and let the app run with different roles or configure for prod/dev:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=backup_manager,dev
```
Profiles: backup_manager,cluster_manager,backup_node,dev,prod

Dev and Prod profiles can be used in the future to configure the nodes independently of the node roles to configure e.g. datasources, etc.

Docker/Docker Compose:
SPRING_PROFILES_ACTIVE=cluster_manager

IntelliJ:

Spring Application Profile -> Active profiles -> cluster_manager,dev ...

For running a multi-node setup locally you can configure each role for every runtime on start-command.
For this to work, you need unique ports:
- IntelliJ: Modify Options -> Program arguments -> --APPLICATION_PORT=...
- Maven: mvn spring-boot:run -Dspring-boot.run.profiles=backup_manager,dev -Dspring-boot.run.arguments="--APPLICATION_PORT=8089"
- Docker/Docker Compose: Change service port mapping to "XXXX:8080" with XXXX being a unique Port. Changing the port of the app won't make much a difference since the port exposed by the container matters.

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
