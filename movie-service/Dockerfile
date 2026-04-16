# ============================================================
# Stage 1 — Build
# Build context: project root  →  docker build -f movie-service/Dockerfile .
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# ── 1. Copy ALL pom.xml files before any source ─────────────────────────────
COPY pom.xml                       ./pom.xml
COPY api-gateway/pom.xml           api-gateway/pom.xml
COPY user-service/pom.xml          user-service/pom.xml
COPY movie-service/pom.xml         movie-service/pom.xml
COPY booking-service/pom.xml       booking-service/pom.xml
COPY notification-service/pom.xml  notification-service/pom.xml

# ── 2. Resolve & cache dependencies (re-runs only when pom.xml changes) ──────
RUN mvn -B -q dependency:go-offline \
        -pl movie-service -am \
        --no-transfer-progress

# ── 3. Copy only this module's source ────────────────────────────────────────
COPY movie-service/src movie-service/src

# ── 4. Build the fat JAR ─────────────────────────────────────────────────────
RUN mvn -B package \
        -pl movie-service -am \
        -DskipTests \
        --no-transfer-progress

# ============================================================
# Stage 2 — Runtime
# ============================================================
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system --gid 1001 appgroup \
 && useradd  --system --uid 1001 --gid appgroup --no-create-home appuser \
 && apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/movie-service/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8082

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
