# Simple Runtime Dockerfile for Estoque Central Backend
# Uses pre-built JAR from local build
# Context: . (root of project)

FROM eclipse-temurin:21-jre-alpine AS runtime

# Build argument for version tagging
ARG VERSION=0.0.1-SNAPSHOT
LABEL version="${VERSION}"
LABEL maintainer="Estoque Central Team"
LABEL description="Spring Boot Backend for Estoque Central ERP"

# Install wget for health checks
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy pre-built JAR from local target directory
COPY --chown=spring:spring backend/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check configuration
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization flags for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
