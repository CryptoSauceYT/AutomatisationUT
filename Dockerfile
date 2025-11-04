FROM eclipse-temurin:11-jdk-slim

WORKDIR /app

COPY target/trading-bot-1.0.0.jar trading-bot.jar

RUN mkdir -p /var/logs/trading-bot/ && chmod 777 /var/logs/trading-bot/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./trading-bot.jar"]
