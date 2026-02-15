# Dockerfile for Render deployment (backend-only, no frontend)
# Context: . (root of project)
# Build: docker build -f docker/backend-render.Dockerfile -t estoque-central-api .

# =============================================================================
# Stage 1: Build
# =============================================================================
FROM eclipse-temurin:21-jdk AS builder

ARG MAVEN_VERSION=3.9.6
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven && \
    rm apache-maven-${MAVEN_VERSION}-bin.tar.gz

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

WORKDIR /app

# Copy backend pom.xml for dependency caching
COPY backend/pom.xml backend/pom.xml

# Create dummy frontend structure so maven-resources-plugin doesn't fail
RUN mkdir -p frontend/dist/frontend/browser && \
    echo '{}' > frontend/package.json && \
    echo '{}' > frontend/package-lock.json

# Download dependencies (cached layer)
RUN cd backend && mvn dependency:go-offline -B

# Copy backend source
COPY backend/src backend/src

# Build backend-only (skip frontend build)
RUN cd backend && mvn clean package -DskipTests -Dskip.frontend=true -B

# =============================================================================
# Stage 2: Runtime
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk add --no-cache wget

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder --chown=spring:spring /app/backend/target/*.jar app.jar

USER spring:spring

# Render sets PORT env var (default 10000)
EXPOSE ${PORT:-10000}

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-10000}/actuator/health || exit 1

# JVM optimized for Render free tier (512MB RAM)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Xss256k -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
