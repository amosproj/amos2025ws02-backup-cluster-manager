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
- PostgreSQL Database

### Run with Node Simulation
To start the application along with simulated nodes (defined in the `test` profile), use:
```bash
docker compose --profile test up --build
```
This includes the components above plus the configured backup nodes.
