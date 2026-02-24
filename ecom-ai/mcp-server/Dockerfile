# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Environment variable to map port dynamically for various cloud providers
ENV SERVER_PORT=7860
EXPOSE 7860
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${SERVER_PORT}"]
