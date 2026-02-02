# Backup Cluster Manager (AMOS WS 2025/26)
<p align="center">
<img width="300" height="300" alt="Plugin_icon_-_8v3" src="https://github.com/user-attachments/assets/c5b7d888-a133-47b7-a265-55a2d50889c2" />
</p>

## Project Description

This project is an MVP developed for our industry partner SEP. It implements an alternative approach for managing distributed nodes in a backup cluster environment.

The system consists of:
- A **Frontend** that offers a UI for editing and viewing data from nodes, users, and their permissions.
- A **Backend** that simulates backup nodes and handles the interaction logic between nodes.

For comprehensive documentation, please visit our **[Project Wiki](https://github.com/amosproj/amos2025ws02-backup-cluster-manager/wiki)**.

## Components Overview

The project is divided into two main components. Please refer to their respective README files for detailed documentation and instructions on how to run them individually:

### [Frontend](frontend/README.md)
The frontend is built with **Angular**. It communicates with the backend to get, edit, and delete data such as users, permissions, backups, and cluster information.

### [Backend](backend/README.md)
The backend is a **Java Spring Boot** application. It manages the business logic, simulates node interactions, and persists data.

## Configuration

### Database
The project uses a PostgreSQL database, which is configured via `docker-compose.yml` in the root directory.

### Backend Configuration
Backend configuration can be found in `backend/src/main/resources/application.yml`.

### Frontend Configuration
Frontend environment configurations are located in `frontend/src/environments/`.

## Build and Run

### Prerequisites
- **Docker Desktop**

### Run Full Stack
To start the entire application (Frontend, Backend, and PostgreSQL), run the following command from the root directory:
```bash
docker compose up --build
```
This will start:
- Frontend at `http://localhost:4200`
- Backend (Cluster Manager) at `http://localhost:8080`
- PostgreSQL Database at `http://localhost:5432`

### Run with Node Simulation
To start the application along with simulated nodes (defined in the `test` profile), use:
```bash
docker compose --profile test up --build
```
This includes the components above plus the configured backup nodes.

## Metrics, Prometheus & Grafana

This project includes integrated monitoring with **Prometheus** and **Grafana** to track system performance metrics from the cluster manager and backup nodes.

### Overview

- **Prometheus**: Scrapes and stores metrics from Spring Boot applications (Micrometer)
- **Grafana**: Visualizes metrics in dashboards with automatic provisioning
- **Metrics**: CPU, memory, heap usage, HTTP requests, garbage collection, and more

### Running with Metrics

To start the stack with Prometheus and Grafana along with simulated nodes for more comprehensive metrics:

```bash
docker compose --profile test up --build
```

### Accessing Prometheus

Visit `http://localhost:9090` to access Prometheus:
- **Targets**: Shows all monitored instances
- **Graph**: Query and visualize metrics in real-time
- **Status**: View Prometheus configuration and alerts

### Accessing Grafana

Visit `http://localhost:3000` to access Grafana:
- Default login: **admin / admin** (change on first login)
- **Data Source**: Prometheus is pre-configured at `http://prometheus:9090`
- **Dashboard**: "Cluster Overview" dashboard is auto-loaded

## Stress Testing

---
### Requirement for running any stress test:
1. Navigate to the docker-compose.yml file located in the root directory of the project.
2. Replace the environment variable  
   `- SPRING_PROFILES_ACTIVE=cluster_manager`  
   inside the cluster-manager service with  
   `- SPRING_PROFILES_ACTIVE=cluster_manager,test-runner`

### Running the stress test:
1. Make sure you have followed the Requirement above for running any stress test.
2. Select a test from the `/stress-test` folder you want to run
    - Each test has its own folder with a descriptive folder name, starting with `test-`
    - Each test folder contains its own README.md file with specific instructions.
3. After selecting the test, run  
   `docker compose --profile [folder-name] up --build`  
   where `[folder-name]` is the name of the test folder you selected in step 2.
4. After running the test, you can find the results in the `/stress-test/[folder-name]/results` folder.
5. If you want to see the HTML report, run `node generate-results-page.js` which will update the `results.html`.

#### Credits
`k6-reporter` is used to create HTML reports of each test, you can access the code from [Github](https://github.com/benc-uk/k6-reporter)