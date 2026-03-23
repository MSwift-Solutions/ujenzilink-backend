# ====== Stage 1: Build the application ======
# Use Maven with Eclipse Temurin JDK 22 to compile and package the app
FROM maven:3.9.9-eclipse-temurin-22 AS build

# Set the working directory inside the build container
WORKDIR /app

# Copy only the pom.xml first to take advantage of Docker layer caching.
# Dependencies will only be re-downloaded when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the full source code
COPY src ./src

# Build the fat JAR, skipping tests to speed up the image build
RUN mvn clean package -DskipTests

# ====== Stage 2: Run the application ======
# Use a slim Eclipse Temurin JDK 22 runtime image (jammy = Ubuntu 22.04 LTS)
FROM eclipse-temurin:22-jdk-jammy

# Create a non-root user for security best practice (no specific UID required for Render)
RUN useradd -m appuser

# Switch to the non-root user
USER appuser

# Set the working directory inside the container
WORKDIR /home/appuser/app

# Copy the packaged JAR from the build stage, assigning ownership to the non-root user
COPY --from=build --chown=appuser /app/target/ujenzilink-backend-0.0.1-SNAPSHOT.jar app.jar

# Render injects a PORT environment variable at runtime.
# Render's default is 10000; override in the Render Dashboard if needed.
ENV PORT=10000

# Expose the port — Render reads this to route traffic correctly
EXPOSE $PORT

# Use shell form so the $PORT environment variable is expanded at runtime.
# Bind explicitly to 0.0.0.0 so Render's load balancer can forward traffic to this container.
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT} --server.address=0.0.0.0"]