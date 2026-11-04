# Stage 1: Build file JAR bằng Maven & Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy POM và mã nguồn
COPY pom.xml .
COPY src ./src

# Build ứng dụng và bỏ qua Unit Test (JUnit 5/Mockito) để tối ưu thời gian đóng gói
RUN mvn clean package -DskipTests

# Stage 2: Runtime Container siêu nhẹ dùng JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file .jar đã build từ Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port ứng dụng
EXPOSE 8080

# Chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]