# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A demo-level personal blog project for learning Spring Boot, MyBatis-Plus, Redis. This project is designed as a learning resource with detailed comments explaining design decisions.

**Core Features**: Article management, Comments, View statistics
**Auth**: Simple session-based login with fixed admin account

## Build Commands

Uses Maven wrapper. On Windows, use `mvnw.cmd` instead of `./mvnw`.

```bash
# Build
./mvnw clean install

# Run application
./mvnw spring-boot:run

# Run all tests
./mvnw test
```

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- MyBatis-Plus 3.5.9
- MySQL
- Redis
- Lombok

## Database Setup

1. Create MySQL database: `myblog`
2. Run schema: `src/main/resources/schema.sql`

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/articles | Article list (paginated) | No |
| GET | /api/articles/{id} | Article detail | No |
| POST | /api/articles | Create article | Yes |
| PUT | /api/articles/{id} | Update article | Yes |
| DELETE | /api/articles/{id} | Delete article | Yes |
| GET | /api/articles/{id}/comments | Comments for article | No |
| POST | /api/articles/{id}/comments | Add comment | No |
| POST | /api/auth/login | Login | No |
| POST | /api/auth/logout | Logout | No |
| GET | /api/auth/check | Check login status | No |

**Admin credentials**: admin / admin123 (configurable in application.yaml)

## Architecture

```
Controller → Service → Mapper (MyBatis-Plus)
                ↓
              Redis (view count cache)
```

Key design decisions are documented as comments in each class. Look for "思考:" (Thinking:) sections for learning notes.
