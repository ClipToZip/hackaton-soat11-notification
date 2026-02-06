# ====== STAGE 1: build ======
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

# Skip tests entirely (including compilation) for faster, deterministic container builds
RUN mvn -q -Dmaven.test.skip=true package

# ====== STAGE 2: runtime ======
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
