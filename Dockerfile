# Etapa 1: compila el proyecto y genera backend.jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa 2: imagen final, solo con Java y el jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/backend.jar backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "backend.jar"]