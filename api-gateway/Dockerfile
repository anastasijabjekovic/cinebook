# ============================================================
# Stage 1 — Build
# Build context: project root  →  docker build -f api-gateway/Dockerfile .
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# ── 1. Copy ALL pom.xml files before any source ─────────────────────────────
# Changes to pom.xml invalidate the dependency layer; source changes do not.
COPY pom.xml                       ./pom.xml
COPY api-gateway/pom.xml           api-gateway/pom.xml
COPY user-service/pom.xml          user-service/pom.xml
COPY movie-service/pom.xml         movie-service/pom.xml
COPY booking-service/pom.xml       booking-service/pom.xml
COPY notification-service/pom.xml  notification-service/pom.xml

# ── 2. Resolve & cache dependencies (re-runs only when pom.xml changes) ──────
RUN mvn -B -q dependency:go-offline \
        -pl api-gateway -am \
        --no-transfer-progress

# ── 3. Copy only this module's source ────────────────────────────────────────
COPY api-gateway/src api-gateway/src

# ── 4. Build the fat JAR (skip tests — run them in CI before building image) ─
RUN mvn -B package \
        -pl api-gateway -am \
        -DskipTests \
        --no-transfer-progress

# ============================================================
# Stage 2 — Runtime
# Minimal JRE image; no build tools, no source code
# ============================================================
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

# Non-root user for security hardening
RUN groupadd --system --gid 1001 appgroup \
 && useradd  --system --uid 1001 --gid appgroup --no-create-home appuser \
 && apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# Copy only the built artifact from the build stage
COPY --from=build /workspace/api-gateway/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

# Informational; actual port binding is done at runtime
EXPOSE 8080

# JVM flags optimised for containers:
#   UseContainerSupport  — honours cgroup memory limits instead of host RAM
#   MaxRAMPercentage     — use 75 % of the container's memory for the heap
#   security.egd         — faster SecureRandom startup (common in containers)
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
