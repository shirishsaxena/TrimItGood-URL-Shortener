# TrimItGood – URL Shortening Service

> It ain't much, but it shortened the link.

TrimItGood is a minimalist URL shortening service written in **Kotlin + Spring Boot**. It lets you shrink long, ugly URLs into short, manageable links — with features like analytics, custom slugs, and expiry dates.

## Features

- Shorten long URLs to tiny slugs
- Custom aliases (e.g., `example.it/github`)
- Expiration support for links
- Click tracking and analytics
- Auto-cleanup of expired links

## Tech Stack

- Backend: Kotlin (JVM), Spring Boot
- Database: PostgreSQL, H2
- Dev Tools: Docker, Swagger UI, Testcontainers - Karate (optional)

## Getting Started

### Prerequisites

- Java 21+
- Kotlin
- Docker
- Gradle

### Run Locally

```bash
# Clone the repo
git clone https://github.com/shirishsaxena/TrimItGood.git
cd TrimItGood

# Run the app
./gradlew bootRun


Env Variables (dev, qa)
```env
--spring.profiles.active=dev
```

### Run using docker
```env

docker build -t url-shorty . 
docker run -d -p 8090:8090 url-shorty

```
### Project details by https://roadmap.sh/projects/url-shortening-service
