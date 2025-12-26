FROM eclipse-temurin:21

# Spring Boot Application JAR
COPY build/libs/*SNAPSHOT.jar smashing-app.jar

# Run Application
ENTRYPOINT ["java", "-jar", "/smashing-app.jar"]