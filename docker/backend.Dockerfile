# Multi-Stage Dockerfile for Estoque Central Backend
# Context: . (root of project)
# Build command: docker build -f docker/backend.Dockerfile -t estoque-central:latest .

# =============================================================================
# Stage 1: Build
# =============================================================================
FROM eclipse-temurin:21-jdk AS builder

# Install Maven 3.9+
ARG MAVEN_VERSION=3.9.6
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven && \
    rm apache-maven-${MAVEN_VERSION}-bin.tar.gz

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Set working directory
WORKDIR /app

# Copy dependency files first for layer caching optimization
# Dependencies change less frequently than source code
COPY backend/pom.xml backend/pom.xml
COPY frontend/package.json frontend/package.json
COPY frontend/package-lock.json frontend/package-lock.json

# Download dependencies (cached if pom.xml and package.json haven't changed)
RUN cd backend && mvn dependency:go-offline -B

# Copy source code
COPY backend/src backend/src
COPY frontend frontend

# Build application (Maven will trigger frontend-maven-plugin to build Angular)
# -DskipTests to speed up build (tests run in CI/CD)
RUN cd backend && mvn clean package -DskipTests -B

# Verify JAR was created
RUN ls -lh /app/backend/target/*.jar

# =============================================================================
# Stage 2: Runtime
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Build argument for version tagging
ARG VERSION=0.0.1-SNAPSHOT
LABEL version="${VERSION}"
LABEL maintainer="Estoque Central Team"
LABEL description="Spring Boot Backend for Estoque Central ERP"

# Install wget for health checks (Alpine uses wget instead of curl)
RUN apk add --no-cache wget

# Create non-root user for security (principle of least privilege)
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder --chown=spring:spring /app/backend/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check configuration
# Checks /actuator/health endpoint every 30s
# Gives 40s for application to start before first check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization flags for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
