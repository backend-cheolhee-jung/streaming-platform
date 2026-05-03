FROM eclipse-temurin:21-jre
LABEL authors="cherhy"
ARG JAR_FILE=build/libs/gateway.jar
COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE docker

EXPOSE 10100

ENTRYPOINT ["java","-jar","/app.jar"]