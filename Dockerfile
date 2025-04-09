FROM openjdk:23-ea-17-jdk
WORKDIR /app
COPY ./target/notifications-microservice-0.0.1-SNAPSHOT.jar .
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "notifications-microservice-0.0.1-SNAPSHOT.jar"]