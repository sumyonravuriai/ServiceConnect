# =============================================================================
# ServiceConnect Backend Dockerfile
# Multi-stage production build using Eclipse Adoptium JDK 21
# =============================================================================

# Build stage
FROM eclipse-adoptium:21-jdk AS builder
WORKDIR /app

# Copy Maven Wrapper and POM
COPY pom.xml .
COPY src ./src

# Build JAR package
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    mvn clean package -DskipTests && \
    rm -rf /var/lib/apt/lists/*

# Runtime stage
FROM eclipse-adoptium:21-jre
WORKDIR /app

# Non-root user for security (Ubuntu/Debian syntax)
RUN groupadd -r scgroup && useradd -r -g scgroup scuser
USER scuser

# Copy JAR from builder
COPY --from=builder /app/target/serviceconnect-backend-1.0.0.jar app.jar

# Expose Spring Boot HTTP port
EXPOSE 8080

# Environment variables with defaults
ENV DB_URL=jdbc:mysql://mysql:3306/serviceconnect_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
ENV DB_USERNAME=root
ENV DB_PASSWORD=root

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
