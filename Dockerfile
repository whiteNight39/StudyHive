# ---- Build stage ----
FROM gradle:8.9-jdk21 AS builder
WORKDIR /app

# Copy everything and build fat JAR
COPY . .
RUN gradle bootJar --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose Render’s default port (Render sets PORT env var automatically)
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]