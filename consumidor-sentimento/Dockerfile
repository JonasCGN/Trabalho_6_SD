FROM openjdk:11-jdk-slim

WORKDIR /app

# Instalar Maven para build
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copiar arquivos do projeto
COPY pom.xml .
COPY src ./src

# Compilar o projeto
RUN mvn clean package -DskipTests

# Executar a aplicação
CMD ["java", "-jar", "target/consumidor-sentimento-1.0-SNAPSHOT.jar"]