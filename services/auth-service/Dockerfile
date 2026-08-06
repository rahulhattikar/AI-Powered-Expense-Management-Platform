
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

COPY --from=builder /app/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

ENV TZ=Asia/Kolkata
ENV JAVA_OPTS="-Duser.timezone=Asia/Kolkata"

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]