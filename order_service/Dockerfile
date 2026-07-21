FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/public_key.pem /app/keys/public_key.pem
ENV APP_KEYS_PUBLIC_KEY_PATH=/app/keys/public_key.pem
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]