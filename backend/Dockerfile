# syntax=docker/dockerfile:1.7

# Runs on whatever machine invokes the build, never under emulation: a jar is the same
# bytes on every platform, so only the runtime stage below needs to be linux/amd64.
# Building this stage for the target instead would put Maven under QEMU and turn a
# one-minute build into a ten-minute one.
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /build

# Dependencies resolve from the POM alone, so this layer survives every source change.
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src/ ./src/
RUN mvn -B -ntp -DskipTests package


FROM eclipse-temurin:25-jre-alpine AS runtime

RUN addgroup -S app && adduser -S -G app app

# The Spring Boot plugin leaves the unpackaged jar as .jar.original, so this matches the
# repackaged one and nothing else.
COPY --from=build /build/target/backend-*.jar /app/app.jar

USER app:app

EXPOSE 8080
STOPSIGNAL SIGTERM

# The JVM reads the container's memory limit, so the heap follows mem_limit in
# compose.prod.yaml rather than being pinned here.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"

# Actuator's own health endpoint. It is never proxied publicly - nginx forwards /api/
# only - which is what keeps it permitAll in SecurityConfig without exposing anything.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=5 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
