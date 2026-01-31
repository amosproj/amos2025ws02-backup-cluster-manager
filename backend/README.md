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
│   │   │   ├── Application.java
│   │   │   │
│   │   │   ├── shared/              # shared functionality across all node types
│   │   │   │   ├── config/          # security, CORS, web configuration
│   │   │   │   ├── controller/      # shared REST endpoints
│   │   │   │   ├── model/           # shared data models
│   │   │   │   ├── pagination/      # pagination helpers
│   │   │   │   ├── mapper/          # entity-DTO mappers
│   │   │   │   ├── repository/      # data access layer
│   │   │   │   ├── service/         # shared business logic
│   │   │   │   └── util/            # utility classes
│   │   │   │
│   │   │   └── cluster_manager/     # cluster manager specific code
│   │   │       ├── config/
│   │   │       ├── controller/      # REST endpoints
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── BCMCronJob.java    # cron job for heartbeat checks
│   │   │
│   │   └── resources/
│   │       ├── application.yml      # main configuration
│   │       └── db/
│   │           └── migration/       # Flyway database migrations
│   │
│   └── test/
│       ├── java/com/bcm/
│       │   ├── ApplicationTests.java
│       │   ├── cluster_manager/     # cluster manager tests
│       │   └── shared/              # shared component tests
│       └── resources/
│           └── application.yml      # test configuration
│
├── pom.xml                          # Maven configuration
├── Dockerfile                       # multi-stage Docker build
├── mvnw                             # Maven wrapper (Unix)
├── mvnw.cmd                         # Maven wrapper (Windows)
└── README.md
```

## Prerequisites
- Java 21+
- Maven 3.8+
- Docker Desktop (for local development)
- PostgreSQL 15+ (or via Docker)

## Tech Stack
- **Spring Boot 3.x** - Application framework
- **Spring WebFlux** - Reactive web framework
- **R2DBC** - Reactive database connectivity
- **PostgreSQL** - Primary database
- **Flyway** - Database migrations
- **MyBatis** - SQL mapping (for complex queries)
- **H2** - In-memory database for testing

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
mvn spring-boot:run -Dspring-boot.run.profiles=cluster_manager,dev
```
Profiles: cluster_manager,dev,prod,test

Dev and Prod profiles can be used in the future to configure the nodes independently of the node roles to configure e.g. datasources, etc.

Docker/Docker Compose:
SPRING_PROFILES_ACTIVE=cluster_manager

IntelliJ:

Spring Application Profile -> Active profiles -> cluster_manager,dev ...

For running a multi-node setup locally you can configure each role for every runtime on start-command.
For this to work, you need unique ports:
- IntelliJ: Modify Options -> Program arguments -> --APPLICATION_PORT=...
- Maven: mvn spring-boot:run -Dspring-boot.run.profiles=cluster_manager,dev -Dspring-boot.run.arguments="--APPLICATION_PORT=8089"
- Docker/Docker Compose: Change service port mapping to "XXXX:8080" with XXXX being a unique Port. Changing the port of the app won't make much a difference since the port exposed by the container matters.

### Database Configuration

The application uses **R2DBC** for reactive database access:

**Cluster Manager** uses two databases:
- **CM Database** (`bcm`): Cluster management data (users, groups, etc.)
- **BN Database** (`bcm_node0`): Backup node data

**Backup Node** uses one database:
- **BN Database** (`bcm_nodeX`): Node-specific backup data

**Reactive Data Access**:
- R2DBC repositories with reactive types (`Mono`, `Flux`)
- Non-blocking operations with Spring WebFlux
- 
### Caching

The application uses **Caffeine Cache** for performance optimization:

**Cached Data** (5 min TTL, 100 entries max):
- **Backup Pages**: Backup metadata and pagination
- **Client Pages**: Client information and pagination
- **Task Pages**: Backup task data and pagination

**Cache Invalidation**:
- Backup Nodes emit events on data changes
- Cluster Manager polls nodes every 5 seconds
- Events are acknowledged and caches cleared automatically

Configuration in `CacheConfig.java`, invalidation logic in `EventPollingService.java`.

### Database Migrations

Flyway manages database migrations in two locations:
- `db/migration/base/`: Shared migrations for all nodes
- `db/migration/cluster_manager/`: Cluster manager specific migrations (only applied when `cluster_manager` profile is active)

Migrations run automatically on application startup.

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

Start the test setup with multiple nodes (1 cluster manager, 2 backup nodes):
"--profile test" to enable the test setup in docker-compose.yml. 
This setup demonstrates features like multiple nodes and clustering with heartbeat checks, node table syncing and node registering.
Fallout and revival handling can be tested by stopping and starting containers via the Docker Desktop UI.
The creation of multiple databases inside the same postgres container only works with a clean volume.

```bash
docker compose --profile test up
```

To start the test setup with the current code base for the docker images:

```bash
docker compose --profile test up --build
```

## Run Backup Task (test)
- time-controlled mock backup
- duration in ms
- shouldSucceed: true/false
- id: backup task id
- port of the backup node where the task is executed

```bash
curl -X POST   http://localhost:8080/api/v1/bn/backups/{id}/execute
   -H "Content-Type: application/json"
      -H "Accept: application/json"
         -d '{"duration":20000,"shouldSucceed":true}'
```