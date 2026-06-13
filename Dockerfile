# ============================================================
# Stage 1 : Builder — JDK 21
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper & pom files
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY stockmaster-shared/pom.xml stockmaster-shared/
COPY stockmaster-auth/pom.xml stockmaster-auth/
COPY stockmaster-groupe/pom.xml stockmaster-groupe/
COPY stockmaster-utilisateur/pom.xml stockmaster-utilisateur/
COPY stockmaster-catalogue/pom.xml stockmaster-catalogue/
COPY stockmaster-tiers/pom.xml stockmaster-tiers/
COPY stockmaster-achat/pom.xml stockmaster-achat/
COPY stockmaster-stock/pom.xml stockmaster-stock/
COPY stockmaster-vente/pom.xml stockmaster-vente/
COPY stockmaster-notification/pom.xml stockmaster-notification/
COPY stockmaster-reporting/pom.xml stockmaster-reporting/

# Download dependencies (layer caching)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -q || true

# Copy source code
COPY stockmaster-shared/src stockmaster-shared/src

# Build layered JAR
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests -q && \
    java -Djarmode=layertools -jar stockmaster-shared/target/*.jar extract --destination extracted

# ============================================================
# Stage 2 : Runtime — JRE 21 Alpine (non-root)
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -S stockmaster && adduser -S stockmaster -G stockmaster

WORKDIR /app

# Copy layers from builder
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

# Switch to non-root user
USER stockmaster

# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=30s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
