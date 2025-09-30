FROM maven:3.9.3-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY Main.java ./src/main/java/com/exemplo/Main.java
RUN mvn clean package -DskipTests

FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=build /app/target/gerador-mensagens-1.0.jar ./gerador-mensagens.jar
EXPOSE 8000

COPY database_face/images/validation/*/*.jpg ./database_face/
COPY database_futebol/database_futebol/logos/*/*.png ./database_futebol/
CMD ["java", "-jar", "gerador-mensagens.jar"]
