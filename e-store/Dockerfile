# Этап 1: Сборка
FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /build

# Получаем имя модуля на этапе сборки
ARG MODULE_NAME
ENV MODULE=${MODULE_NAME}

# Копируем исходники
COPY . .

# Собираем только нужный модуль (и его зависимости, например Library). Это намного быстрее!
RUN mvn clean package -pl ${MODULE} -am -DskipTests

# Находим правильный Spring Boot jar (игнорируя -plain.jar) и копируем его с понятным именем
RUN ls -1 ${MODULE}/target/*.jar | grep -v "plain" | xargs -I {} cp {} /build/app-ready.jar

# Этап 2: Запуск
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем правильный jar из первого этапа
COPY --from=builder /build/app-ready.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]