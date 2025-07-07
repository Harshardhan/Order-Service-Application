# ===================== Stage 1: Build the application =====================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy entire project early so Maven can see all modules
COPY . .


# Now go offline successfully (all modules exist now)
RUN ./mvnw dependency:go-offline -B

RUN mvn clean package -pl OrderServiceApplication -am -DskipTests

# ===================== Stage 2: Run the application =====================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl
RUN mkdir -p logs

COPY --from=build /app/OrderServiceApplication/target/OrderServiceApplication-0.0.1-SNAPSHOT.jar OrderServiceApplication.jar

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java", "-jar", "OrderServiceApplication.jar"]
