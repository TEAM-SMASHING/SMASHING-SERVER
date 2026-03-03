FROM eclipse-temurin:21
COPY build/libs/*.jar smashing-app.jar
ENTRYPOINT ["java", "-jar", "/smashing-app.jar"]
