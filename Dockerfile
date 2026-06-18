# === Build stage ===
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Cache dependencies first for faster rebuilds
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Build the app
COPY src src
RUN ./mvnw clean package -DskipTests -B

# === Runtime stage ===
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy the built JAR (whichever name Maven produced)
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]