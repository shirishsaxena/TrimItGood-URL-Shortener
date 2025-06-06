# ---- Builder Stage ----
FROM gradle:8.7.0-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

RUN gradle --no-daemon build || true  # allow it to fail if src isn't copied yet (for cache)

COPY . .

# Run full build (no tests for speed during container build)
RUN gradle clean build -x test --no-daemon

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jdk AS runner

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 4005

ENTRYPOINT ["java", "-jar", "app.jar"]
