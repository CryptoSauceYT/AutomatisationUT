# Stage 1: Build (compilation du projet Java)
FROM eclipse-temurin:11-jdk AS build

WORKDIR /build

# Copier les fichiers Maven
COPY pom.xml .
COPY settings.xml .

# Copier le code source
COPY src ./src

# Installer Maven et compiler le projet
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests

# Stage 2: Runtime (image finale)
FROM eclipse-temurin:11-jre

WORKDIR /app

# Copier le JAR depuis le stage de build
COPY --from=build /build/target/trading-bot-1.0.0.jar trading-bot.jar

# Créer le dossier de logs
RUN mkdir -p /var/logs/trading-bot/ && chmod 777 /var/logs/trading-bot/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./trading-bot.jar"]
