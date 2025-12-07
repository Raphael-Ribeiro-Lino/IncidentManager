# --- Estágio 1: Build (Construção) ---
# Usa imagem oficial do Maven com Java 17 para compilar
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia todos os arquivos do projeto para o container
COPY . .

# Executa o Maven para gerar o .jar
# -DskipTests é CRUCIAL para não falhar tentando conectar no banco durante o build
RUN mvn clean package -DskipTests

# --- Estágio 2: Run (Execução) ---
# Usa uma imagem leve do Java 17 apenas para rodar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Pega o .jar gerado no estágio anterior e renomeia para app.jar
COPY --from=build /app/target/*.jar app.jar

# Informa ao Render que a aplicação usa a porta 8080
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]