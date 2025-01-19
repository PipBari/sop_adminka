# Этап сборки
FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем проект
COPY src ./src
RUN mvn clean package -DskipTests

# Этап запуска
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Копируем скомпилированный JAR из этапа сборки
COPY --from=builder /app/target/domain-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт приложения
EXPOSE 9090

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
