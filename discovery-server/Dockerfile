FROM eclipse-temurin:21-jre-alpine
ADD ./target/*.jar app.jar
RUN apk add --no-cache curl
ENTRYPOINT ["java", "-jar", "app.jar"]