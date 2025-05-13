FROM eclipse-temurin:21-jre
WORKDIR /app

COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=production

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
