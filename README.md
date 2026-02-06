# ChoreHub

ChoreHub is a backend service for managing household chores, designed to integrate with Home Assistant (and potentially other systems). It provides a RESTful API for creating, scheduling, and tracking recurring and one-time chores, with support for user assignments and historical records.

## Features

- **Recurring Chores**: Support for fixed-interval chores (e.g., every first of the month) and flexible-interval chores (e.g., every 4 months since last completion).
- **One-Time Chores**: Ability to create and track non-recurring tasks.
- **User Management**: Associate chores with specific users or household members.
- **History Tracking**: Maintain a history of completed chores for auditing and analysis.
- **Home Assistant Integration**: Designed to run as a Home Assistant addon.
- **Database Support**: Uses MariaDB for persistent storage.

## Prerequisites

- Java 25 (as specified in the build configuration)
- Gradle (wrapper included)
- MariaDB database

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd chorehub
   ```

2. Configure the database:
   - Set up a MariaDB instance
   - Update `src/main/resources/application.yaml` with your database connection details:
     ```yaml
     spring:
       datasource:
         url: jdbc:mariadb://localhost:3306/chorehub
         username: your-username
         password: your-password
     ```

3. Build the application:
   ```bash
   ./gradlew build
   ```

4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## Usage

Once running, the API will be available at `http://localhost:8080`.

### API Endpoints

Currently, the API is in development. The following endpoints are available:

- `GET /` - Health check endpoint returning "ChoreHub is running."

Future endpoints will include:
- Chore management (CRUD operations)
- User management
- History retrieval

### Home Assistant Addon
 Add authentication and authorization
To run as a Home Assistant addon:
## Using the GHCR image (Home Assistant / Raspberry Pi 4 aarch64)

The GitHub Actions workflow builds the application and pushes a pre-built container image to GitHub Container Registry (GHCR).

Pull and run the image locally (replace `<OWNER>` with your GitHub user or org):

```bash
docker pull ghcr.io/<OWNER>/chorehub:latest
docker run --rm -p 8080:8080 ghcr.io/<OWNER>/chorehub:latest
```

On a Raspberry Pi 4 running Home Assistant OS (aarch64) the workflow builds an `aarch64` image, so HAOS will be able to pull and run the image without building on-device.

### Minimal Home Assistant Add-on manifest (example)

If you want to expose the pre-built image as an add-on, a minimal `manifest.json` could look like this (adjust fields as needed and replace `<OWNER>`):

```json
{
   "name": "ChoreHub",
   "version": "1.0",
   "slug": "chorehub",
   "arch": ["aarch64"],
   "startup": "services",
   "boot": "auto",
   "map": ["config:rw"],
   "options": {},
   "schema": {},
   "image": "ghcr.io/<OWNER>/chorehub:latest"
}
```

Notes:
- Replace `<OWNER>` with your GitHub username or organization.
- The `image` key points to the pre-built image on GHCR so Home Assistant Supervisor does not have to build the addon locally.
- If you need a 32-bit build (raspberrypi), the workflow must be extended to build `linux/arm/v7` as well.

1. Create an addon configuration in your Home Assistant setup.
2. Build the application as a JAR:
   ```bash
   ./gradlew bootJar
   ```
3. Deploy the JAR file and configure the addon to run it with the appropriate environment variables for database connection.

## Development

### Project Structure

- `src/main/java/de/caransgar/chorehub/` - Main application code
  - `ChorehubApplication.java` - Spring Boot application entry point
  - `controller/` - REST controllers
  - `services/` - Business logic services
- `src/main/resources/` - Configuration files
- `src/test/` - Unit tests

### Building and Testing

- Build: `./gradlew build`
- Test: `./gradlew test`
- Run: `./gradlew bootRun`

## Local Development with Podman

### Building the Image

Build the Docker image with a version tag:

```bash
cd chorehub
podman build -t chorehub:dev -t chorehub:latest .
```

### Running the Container

Run the container with host network access (required for connecting to MariaDB on your host):

```bash
podman run --rm --network host chorehub:dev

Options:

   --rm: Automatically removes the container when stopped

   --network host: Shares the host's network stack (allows access to localhost:3306)
```

### Database Requirements

The application requires a MariaDB instance running on your host machine. Ensure MariaDB is accessible at localhost:3306 with the credentials configured in src/main/resources/application.yaml.
Versioning

   chorehub:dev: Use for rapid development and testing

   chorehub:1.0.0: Use specific versions for releases (should match version in config.yaml)

   chorehub:latest: Always points to the most recent build

To tag an existing image:

```bash
podman tag <IMAGE_ID> chorehub:1.0.0
```


### Database

The application uses Spring Data JPA for database operations. Entity classes and repositories will be added as features are implemented.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the terms specified in the LICENSE file.

## Roadmap

- Implement chore entities and CRUD operations
- Add user management
- Implement scheduling logic for recurring chores
- Add history tracking
- Complete Home Assistant integration
- Add authentication and authorization</content>
<parameter name="filePath">/home/ansgar/projects/chorehub/README.md