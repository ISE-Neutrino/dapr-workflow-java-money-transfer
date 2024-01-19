FROM maven:3.8.4-openjdk-17-slim AS build

# FROM mcr.microsoft.com/openjdk/jdk:17-mariner as build

# Set the working directory in the container
WORKDIR /workspace/app

# Copy the project source code and pom.xml file to the container
COPY pom.xml .
COPY src src

# Build and Package the application
RUN mvn clean package

# Create a new stage for the final image
FROM mcr.microsoft.com/openjdk/jdk:17-distroless

# Set the working directory in the final image
WORKDIR /app

# Copy the JAR file from the build stage to the final image
COPY --from=build /workspace/app/target/public-api-service.jar .

# Specify the command to run your application
ENTRYPOINT ["java", "-jar", "public-api-service.jar", "com.example.publicapiservice.PublicApiServiceApplication"]
