# Use Java 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy project
COPY . .

# Build app
RUN ./mvnw clean package -DskipTests

# Run app
CMD ["java", "-jar", "target/*.jar"]