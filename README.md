# Spring Boot Baseline Application

This is a baseline Spring Boot application. Use as boilerplate to quickly build new ideas.

## Technologies Used

*   **Java:** 25
*   **Spring Boot:** 4.0.0
*   **Build Tool:** Gradle
*   **Database:** PostgreSQL (managed by Docker Compose)
*   **Database Migrations:** Liquibase
*   **API Documentation:** Springdoc OpenAPI
*   **Testing:** JUnit 5, Testcontainers

## Prerequisites

Ensure you have the following installed:

*   **Java Development Kit (JDK):** Version 25
*   **Docker:** For running the PostgreSQL database via Docker Compose.
*   **Gradle:** Although `./gradlew` wrapper is included, familiarity is helpful.

## Getting Started

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/your-username/spring-boot-baseline.git
    cd spring-boot-baseline
    ```

2.  **Build the Project:**
    ```bash
    ./gradlew clean build
    ```
    This will compile the code, run tests, and assemble the application.

## Running the Application

The application uses Docker Compose to manage its PostgreSQL database. When you run `bootRun`, Spring Boot will automatically start the services defined in `compose.yaml`.

```bash
./gradlew bootRun
```

Once started, the application will be available at `http://localhost:8080`.

# Liquibase Hibernate 7 Incompatibility Fix (Spring Boot 4)

## The Problem
Running `liquibase diffChangeLog` fails in a Spring Boot 4 (Java 25) project.
*   **Error 1:** `java.lang.NoSuchMethodError: '...MetadataBuildingOptions.getIdentifierGeneratorFactory()'`
*   **Error 2:** `java.lang.NoClassDefFoundError: picocli/CommandLine$IVersionProvider`

## The Root Cause
1.  **API Mismatch:** Spring Boot 4 uses **Hibernate 7**, but the latest Liquibase extension (`liquibase-hibernate6`) is only compatible with **Hibernate 6**. Hibernate 7 removed internal APIs that the extension relies on.
2.  **Gradle Auto-Upgrade:** Even when specifying Hibernate 6 dependencies for Liquibase, the Spring Boot Dependency Management plugin forces them to upgrade to Hibernate 7 to match the project's Spring Boot version.
3.  **Missing CLI Parser:** One more issue to solve was isolating the configuration removes transitive dependencies, causing `picocli` (required by Liquibase) to be missing.

## The Solution
We must create an "Isolation Bubble" for the Liquibase task:
1.  **Isolate Configuration:** Set `extendsFrom = []` for `liquibaseRuntime` to prevent inheriting the main app's Hibernate 7 jars.
2.  **Force Downgrade:** Use `resolutionStrategy { force ... }` to override Spring Boot's BOM and strictly enforce Hibernate 6.6.x.
3.  **Manual Dependencies:** Manually re-add `picocli` and a Spring Boot 3.x starter (which uses Hibernate 6) to the runtime configuration.
4.  **Package Scanning:** Change `referenceUrl` to scan packages (`hibernate:com.example...`) instead of the Spring context (`hibernate:spring:...`) to avoid loading the Spring Boot 4 app context in the Hibernate 6 environment.

These workarounds should be removed once `liquibase-hibernate7` gets released. 

## Working `build.gradle` Configuration

```groovy
configurations {
    liquibaseRuntime {
        // 1. ISOLATION: Do not inherit dependencies from the main app (keeps Hibernate 7 out)
        extendsFrom = [] 
        
        // 2. FORCE VERSION: Prevent Spring Boot 4 BOM from upgrading Hibernate back to 7.x
        resolutionStrategy {
            force 'org.hibernate.orm:hibernate-core:6.6.15.Final'
        }
    }
}

dependencies {
    // ... Main App Dependencies (Spring Boot 4 / Hibernate 7) ...

    // --- LIQUIBASE RUNTIME (Isolated Hibernate 6 Environment) ---
    liquibaseRuntime 'org.liquibase:liquibase-core:5.0.1'
    liquibaseRuntime 'org.liquibase.ext:liquibase-hibernate6:5.0.1'
    liquibaseRuntime 'info.picocli:picocli:4.7.6' // Required for CLI parsing
    liquibaseRuntime 'org.postgresql:postgresql'
    liquibaseRuntime sourceSets.main.output // To read your entities

    // Use Spring Boot 3.4.1 dependencies to safely pull in Hibernate 6
    liquibaseRuntime('org.springframework.boot:spring-boot-starter-data-jpa:3.4.1') {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    }
}

liquibase {
    activities {
        main {
            // ... database credentials ...
            
            // NOTE: Use package scanning ('hibernate:com...') instead of Spring context ('hibernate:spring:...')
            referenceUrl 'hibernate:com.samueln.spring_boot_baseline?dialect=org.hibernate.dialect.PostgreSQLDialect&hibernate.physical_naming_strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy'
        }
    }
}
```

```bash
./gradlew diffChangelog -PchangeLogFile=src/main/resources/db/changelog/changes/$(date +%Y%m%d%H%M%S)_new_change.yaml
```

## Liquibase Workflows

This codebase uses Liquibase for managing database schema changes. There are two primary workflows for managing migrations:

### 1. Manual Migration (Safety First)
This is the standard, safest approach for sensitive or complex data migrations.
*   **Process:** You manually write the YAML/SQL changelog files describing the schema changes.
*   **Pros:** precise control, self-documenting, no surprises.
*   **Cons:** Slower, prone to human error (typos).

### 2. Automated Diff (Developer Velocity)
This approach leverages the `liquibase-gradle-plugin` to automatically generate migrations by comparing Java Entities to the current database state.
*   **Process:** 
    1. Update your Java Entities (e.g., add a field to `UserEntity`).
    2. Run the diff command.
    3. Liquibase generates the YAML for you.
    4. You **review** the generated YAML and commit it.
*   **Pros:** Extremely fast, reduces syntax errors, keeps DB and Java in sync.
*   **Cons:** Generated names or constraints might need minor tweaking; requires a running local DB.

`liquibase-gradle-plugin` gives developers the best of both worlds: the speed of `ddl-auto` (via diff generation) with the safety and version control of physical migration files.

### How to Generate a Migration (Diff)

Prerequisite: Ensure local database is running (usually via `./gradlew bootRun` or just `docker compose up`).

1.  **Make changes** to Java entities.
2.  **Run the Diff command:**
    ```bash
    ./gradlew diffChangelog -PchangeLogFile=src/main/resources/db/changelog/changes/$(date +%Y%m%d%H%M%S)_new_change.yaml
    ```
    *(Note: Name the file whatever you want. The above command uses a timestamp.)*

3.  **Review the generated file** in `src/main/resources/db/changelog/changes/`.
4.  **Register it** in `src/main/resources/db/changelog/db.changelog-master.yaml` (Liquibase does not auto-register new files in the master).
5.  **Restart** the application to apply the change.

### How Migrations Are Applied

*   **Automatic on Startup:** Liquibase migrations are automatically applied when the Spring Boot application starts (`./gradlew bootRun`). The application checks the database for pending changesets and applies them in order.

## Running Tests

All unit and integration tests (including those that use Testcontainers for a PostgreSQL database) can be executed using:

```bash
./gradlew test
```

## Generating OpenAPI Specification

API documentation is generated using the `springdoc-openapi-gradle-plugin`. This plugin starts the application in the background, fetches the OpenAPI JSON, and then shuts down the application.

To generate the `openapi.json` file:

```bash
./gradlew generateOpenApiDocs
```

The generated file will be located at `build/api-spec/openapi.json`. This file is useful for generating API clients for frontend applications or for sharing your API contract.

## API Endpoints

*   **Swagger UI:** Access the interactive API documentation at `http://localhost:8080/swagger-ui.html`
*   **API Documentation (JSON):** The raw OpenAPI JSON can be found at `http://localhost:8080/v3/api-docs`
*   **User API:** `http://localhost:8080/api/users` (GET request to retrieve all users)
