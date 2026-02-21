# Production-grade micro-investment engine — port 5477
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ARG JAR_FILE=target/micro-investment-engine-1.0.0.jar
COPY ${JAR_FILE} app.jar

EXPOSE 5477
ENV SERVER_PORT=5477
ENTRYPOINT ["java", "-jar", "app.jar"]
