FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN mvn clean package

# Create a new stage for the final image
FROM mcr.microsoft.com/openjdk/jdk:17-distroless

# Set the working directory in the final image
WORKDIR /app

# Copy the JAR file from the build stage to the final image
COPY --from=build /workspace/app/target/money-transfer-service.jar .

# Specify the command to run your application with Dapr
# ENTRYPOINT ["dapr", "run", "--app-id", "money-transfer-service", "--", "java", "-jar", "money-transfer-service.jar", "com.example.daprworkflowjavamoneytransfer.DaprWorkflowJavaMoneyTransferApplication"]
ENTRYPOINT ["java", "-jar", "money-transfer-service.jar", "com.example.daprworkflowjavamoneytransfer.DaprWorkflowJavaMoneyTransferApplication"]