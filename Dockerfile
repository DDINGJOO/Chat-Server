# =============================================================================
# Chat Server Dockerfile
# Multi-Architecture Support: amd64, arm64
# =============================================================================

# Build stage (optional - if building from source)
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy gradle files first for caching
COPY ChatDDing-service/gradle/ gradle/
COPY ChatDDing-service/gradlew .
COPY ChatDDing-service/build.gradle .
COPY ChatDDing-service/settings.gradle .

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY ChatDDing-service/src/ src/

# Build application
RUN ./gradlew bootJar -x test --no-daemon

# =============================================================================
# Runtime stage
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="TeamBind"
LABEL description="Chat Server - Multi-Architecture Image (amd64/arm64)"

WORKDIR /app

# Install required packages
RUN apk add --no-cache \
    curl \
    tzdata \
    && cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && echo "Asia/Seoul" > /etc/timezone

# Create non-root user
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

# Copy jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod"

# Run application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
