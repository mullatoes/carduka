# =========================
# Stage 1: Build the app
# =========================
FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# =========================
# Stage 2: Run the app
# =========================
FROM eclipse-temurin:25-jre

LABEL maintainer="Carduka Team" version="1.0.0"

ENV APP_HOME=/app
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

WORKDIR ${APP_HOME}

RUN groupadd -r carduka && useradd -r -g carduka carduka

COPY --from=build /build/target/*.jar application.jar

RUN chown -R carduka:carduka ${APP_HOME}

USER carduka

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar application.jar"]