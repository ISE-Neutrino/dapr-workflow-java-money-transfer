FROM maven:3.8.4-openjdk-17-slim AS build

# FROM mcr.microsoft.com/openjdk/jdk:17-mariner as build

# Set the working directory in the container
WORKDIR /workspace/app

# Copy the project source code and pom.xml file to the container
COPY pom.xml .
COPY src src

# Build and Package the application
RUN mvn clean package

# A Spring Boot fat JAR naturally has “layers” because of the way that the JAR itself is packaged.
# If we unpack it first, it is already divided into external and internal dependencies.
# RUN mkdir -p build/libs/dependency && (cd build/libs/dependency; jar -xf ../*.jar)


# If the application dependencies do not change, the first layer (from BOOT-INF/lib) need not change,
# so the build is faster, and the startup of the container at runtime if also faster,
# as long as the base layers are already cached.
# FROM mcr.microsoft.com/openjdk/jdk:17-distroless
# VOLUME /tmp
# ARG DEPENDENCY=/workspace/app/build/libs/dependency
# COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
# COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
# COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
# ENTRYPOINT ["java","-cp","app:app/lib/*","com.example.publicapiservice.PublicApiServiceApplication"]


# Create a new stage for the final image
FROM mcr.microsoft.com/openjdk/jdk:17-distroless

# Set the working directory in the final image
WORKDIR /app

# Copy the JAR file from the build stage to the final image
COPY --from=build /workspace/app/target/public-api-service.jar .

# Specify the command to run your application
CMD ["dapr", "run", "--app-id", "public-api-service", "--", "java", "-jar", "public-api-service.jar"]