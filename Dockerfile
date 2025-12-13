
FROM eclipse-temurin:17-jdk-jammy AS build


WORKDIR /app


COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline


COPY src src


RUN ./mvnw clean install -DskipTests


FROM eclipse-temurin:17-jre-jammy


ENV JAR_FILE=quan_ly_kho-0.0.1-SNAPSHOT.jar
ENV PORT=8080


COPY --from=build /app/target/${JAR_FILE} /usr/app/

EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "/usr/app/quan_ly_kho-0.0.1-SNAPSHOT.jar"]