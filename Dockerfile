# ----------------------------------------------------------
# 🧱 STAGE 1: Build the Spring Boot application
# ----------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies (skip prefetch, rely on normal build)
RUN mvn -B dependency:resolve dependency:resolve-plugins

# Copy source code into container
COPY src ./src

# Package the Spring Boot app (skip tests for faster build)
RUN mvn clean package -DskipTests

# ----------------------------------------------------------
# 🚀 STAGE 2: Run the built JAR
# ----------------------------------------------------------
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy only the packaged JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the service port (⚠️ change this per service)
EXPOSE 8081

# Optional Java optimizations (adjust memory as needed)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Start the Spring Boot service
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
