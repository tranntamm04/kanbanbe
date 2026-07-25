# Kanban Backend

Backend API cho ứng dụng Kanban, xây dựng bằng Spring Boot 3.5 và MySQL.

## Stack

- Java 17
- Spring Boot 3.5.13
- Spring Web, Security, Validation
- Spring Data JPA
- MySQL
- JWT
- Mail
- WebSocket
- Maven

## Chức năng chính

- Đăng ký, đăng nhập, JWT authentication
- Quên mật khẩu và reset mật khẩu bằng OTP email
- Workspace, member, invite workspace
- Project, column, task, comment
- Notification và activity log
- WebSocket cho realtime event/notification

## Chạy local

```bash
./mvnw spring-boot:run
```

Trên Windows:

```bash
mvn spring-boot:run
```

Backend mặc định chạy ở `http://localhost:8080`.

## Test

```bash
./mvnw test
```

Trên Windows:

```bash
mvn test
```