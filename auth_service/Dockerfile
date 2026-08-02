FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Keys are NOT baked into the image. They are JWT signing secrets that the app
# rotates at runtime (RsaKeyConfig.persistKeyPair writes new .pem files here), so
# /app/keys must be a mounted, writable, persistent volume — seeded with the
# initial keypair before first boot. See planCICD.md §5.1.
RUN mkdir -p /app/keys
ENV APP_KEYS_PRIVATE_KEY_PATH=/app/keys/private_key.pem
ENV APP_KEYS_PUBLIC_KEY_PATH=/app/keys/public_key.pem
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
