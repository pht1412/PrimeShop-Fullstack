FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn clean install

CMD ["java", "-jar", "target/primeshop-0.0.1-SNAPSHOT.jar"]
