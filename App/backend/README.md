# Olive Bank — Backend

The backend service for **Olive Bank**, a banking application built with Java and Spring Boot. It provides REST APIs for user authentication, account management, deposits, withdrawals, fund transfers, transaction history, and OTP-based verification.

## Technology Stack

- **Java:** 17
- **Framework:** Spring Boot 3.3.1
- **Build Tool:** Maven
- **Database:** MySQL
- **Authentication:** JWT-based authentication
- **Email:** Spring Mail
- **Containerization:** Docker

## Features

- User registration and authentication
- JWT-based access control
- Bank account management
- Account balance retrieval
- Cash deposits and withdrawals
- Transfers between accounts
- Transaction history
- PIN management
- OTP verification
- Password reset functionality
- Email notifications for login and verification events

> This project is intended for development and demonstration unless separately reviewed and hardened for production banking use.

## Project Structure

```text
backend/
├── src/
│   └── main/
│       ├── java/com/webapp/bankingportal/
│       │   ├── config/
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── entity/
│       │   ├── exception/
│       │   ├── repository/
│       │   ├── security/
│       │   ├── service/
│       │   └── util/
│       └── resources/
│           └── application.properties
├── mvnw
├── mvnw.cmd
├── pom.xml
├── Dockerfile
└── README.md
```

*Directory names may vary slightly depending on the current source tree.*

## Prerequisites

Install the following before running the application:

- Java Development Kit (JDK) 17
- MySQL Server
- Git (if cloning the repository)
- Docker (optional, for containerized deployment)

Verify Java and MySQL installations:

```bash
java -version
mysql --version
```

## Database Configuration

1. Start your MySQL server.
2. Create a database for the application if it does not already exist:

```sql
CREATE DATABASE bankingportal;
```

3. Create a dedicated MySQL user and grant access to the application database.
4. Configure the database connection in `src/main/resources/application.properties`.

Example configuration:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/bankingportal}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Use the database URL, username, and password appropriate to your environment. The example above assumes the application reads its database credentials from environment variables.

**Important:** Ensure the application's actual `application.properties` matches the configuration used here. Do not commit database passwords or other secrets to version control.

## Environment Variables

Configure the following variables as required by your application's settings:

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC URL for the MySQL database |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret used to sign authentication tokens |
| `MAIL_USERNAME` | Email account used to send notifications |
| `MAIL_PASSWORD` | Email provider password or app password |

Variable names must match the placeholders configured in `application.properties`. Configure any additional required application properties before running the service.

Never publish real credentials, signing secrets, email app passwords, or production connection strings.

## Run Locally

### 1. Configure Java

On macOS with Homebrew Java 17:

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

### 2. Build the application

From the backend directory:

```bash
./mvnw clean package -DskipTests
```

The packaged JAR should be generated under `target/`.

### 3. Start the backend

```bash
./mvnw spring-boot:run
```

Alternatively, run the packaged JAR:

```bash
java -jar target/*.jar
```

By default, the application is configured to use port `8081`.

Local base URL:

```text
http://localhost:8081
```

API base path:

```text
http://localhost:8081/api
```

The application also requires a reachable MySQL database and any configured email service.

## API Overview

The backend exposes REST endpoints for the application's banking features. Exact endpoint paths, HTTP methods, request fields, and response formats are defined in the controller classes.

| Feature | Description |
|---|---|
| User management | Registration, login, and logout |
| Authentication | JWT token validation and access control |
| Accounts | Account details and balance information |
| Deposits | Add funds to an account |
| Withdrawals | Withdraw funds from an account |
| Transfers | Transfer funds between accounts |
| Transactions | Retrieve account transaction history |
| PIN | Create or update an account PIN |
| OTP | Generate and verify one-time passwords |
| Password reset | Support password recovery workflows |

For example, the fund transfer endpoint is:

```text
POST /api/account/transfer
```

Protected endpoints require the authentication token in the request's `Authorization` header:

```text
Authorization: Bearer <your-token>
```

Refer to the controller and security configuration classes for the authoritative endpoint list and access rules.

## Docker

The backend Dockerfile packages the built JAR into a container image.

Build the JAR first:

```bash
./mvnw clean package -DskipTests
```

Build the Docker image from the backend directory:

```bash
docker build --platform linux/arm64 -t securebank-frontend .

```

Run the container:

```bash
docker run -d \
  --name securebank-backend \
  -p 8081:8081 \
  olive-bank-backend
```

Replace the example credentials with your own environment-specific values. The database URL shown is a local macOS Docker example; other environments require the appropriate database hostname and network configuration.

Ensure the application actually maps these environment variable names before using this command. A container cannot connect to MySQL on `localhost` to reach the host machine's database.

## Security Considerations

- Store secrets in environment variables or a dedicated secrets manager.
- Never commit passwords, JWT signing keys, or email credentials.
- Use HTTPS for deployed environments.
- Validate and authorize every sensitive banking operation on the server.
- Use appropriate transaction boundaries and concurrency controls for monetary operations.
- Do not use demonstration account PINs or predictable account-number generation in production.
- Use a production-grade database configuration, backups, monitoring, and audit logging.
- Verify that authentication, authorization, and error handling are suitable for the intended deployment.

## Troubleshooting

**Database connection errors**
- Verify that MySQL is running.
- Check the JDBC URL, database name, username, and password.
- Confirm that the database user has the required privileges.

**Java version errors**
- Verify that Java 17 is active with `java -version`.
- Ensure `JAVA_HOME` points to the intended JDK.

**Port 8081 is already in use**
- Stop the other process using the port or configure a different server port.

**Email notifications fail**
- Verify the email provider settings and credentials.
- Check provider security requirements and application logs.
- Never expose email credentials in logs or screenshots.

**Docker database connection errors**
- Use a hostname reachable from inside the container.
- Verify the database accepts connections from the container's network.

## Development Build

Run the Maven tests with:

```bash
./mvnw test
```

Build without running tests:

```bash
./mvnw clean package -DskipTests
```

Skipping tests verifies compilation and packaging, but does not establish that the application behaves correctly.

## License

Add the applicable license and attribution information before distributing this project
