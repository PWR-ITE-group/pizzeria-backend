## 📝 Updated Application Run Manual

### Prerequisites
1.  Download and install [**Docker Desktop**](https://www.docker.com/products/docker-desktop/)
2.  Open **Docker Desktop** and ensure it's running.
3.  Ensure you have the necessary environment configuration file:
    * The **`.env`** file containing database secrets (e.g., `POSTGRES_USER`, `POSTGRES_PASSWORD`) will be sent to you separately.
    * A **`.env.sample`** file should exist in the repository to show the required variables.

---

## Launching the Application (3 Ways)

### 1. Using an IDE (IntelliJ)
This method launches and manages all services (`db`, `flyway`, `app`) directly within your IDE.

1.  Open the project and navigate to **`docker-compose.yml`**.
2.  Launch all services using the **green arrows** next to the `services:` line.
3.  On the bottom left of the IDE, click the arrow in the hexagon (**Services**) or the **Docker** tab.
4.  Verify that the containers (`pizzeria_db`, `pizzeria_backend`) are running and healthy.

### 2. Manually (Recommended for initial setup)
This uses the terminal to start all services, including building the `app` image.

1.  Open your terminal in the root directory of the project (where `docker-compose.yml` is located).
2.  Run the command to **build images** and start containers:
    ```bash
    docker compose up --build
    ```
    *(The `--build` flag ensures that the `app` service is compiled from the latest local code.)*

### 3. Launch DB in Docker and App Locally (For faster development)
This is ideal for quick code changes, as it avoids rebuilding the entire Spring Boot image.

> **⚠️ Possible only after running all services at least once** (via Method 1 or 2) so that the **DB schema synchronization (Flyway)** and initial data setup are complete.

1.  Open the terminal or your IDE's Docker interface.
2.  Start only the **`db`** container:
    ```bash
    docker compose up -d db
    ```
    *or* use the IDE interface to start only the `db` service.
3.  Launch the Spring Boot application locally by running the **main class `PizzeriaBackendApplication`** from your IDE.

---

## 🛠️ Rebuilding/Updating Containers

If you make changes to the application code, the database structure (Flyway scripts), or the container configurations (`Dockerfile`, `docker-compose.yml`), you must **rebuild and restart** the corresponding container.

### 1. Rebuilding the Backend Application (`app`)

When you change the **Java code**:

* **Using Method 2 (Manual):**
    ```bash
    docker compose up -d --build app
    ```
  *(This command rebuilds the `app` image, replaces the old container, and starts the new one.)*

* **Using the IDE:**
    1.  Locate the `pizzeria_backend` container in the Services/Docker tab.
    2.  **Right-click** on the service and select **"Rebuild and Restart"** (or similar option).

### 2. Rebuilding the Database (`db`) or Flyway (`flyway`)

When you change the **Flyway migration scripts** (in `./db/migration`):

* You only need to rebuild and restart the `flyway` service to apply the new schema changes. The `flyway` service will automatically wait for the `db` to be healthy.
    ```bash
    docker compose up -d --build flyway
    ```
  *(The `--build` here is technically only needed if the Flyway image itself was customized, but it's good practice. It forces the `flyway` container to run the migration command again.)*

**Note on Database Changes:** If you need to wipe and restart the database schema (e.g., for major testing), you must remove the persistent volume:

1.  **Stop all services:**
    ```bash
    docker compose down
    ```
2. **Rerun:**
    ```bash
    docker compose up --build
    ```