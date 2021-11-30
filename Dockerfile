FROM openjdk:17.0.1-jdk-slim
WORKDIR /opt/metrics-app
ENTRYPOINT ["java", "-jar", "metrics-app.jar"]
COPY target/metrics-app.jar ./metrics-app.jar