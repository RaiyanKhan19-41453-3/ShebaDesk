# Build stage: no local Java/Maven needed, everything happens inside Docker
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy wrapper + pom first for better layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Pre-fetch dependencies (cached unless pom.xml changes)
RUN ./mvnw -q dependency:go-offline

# Copy sources and build
COPY src src
RUN ./mvnw -q package -DskipTests

# Runtime stage: minimal JRE only
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -r -u 10001 appuser

COPY --from=build /app/target/shebadesk-*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser

EXPOSE 4000
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:4000/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
