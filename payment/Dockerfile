FROM eclipse-temurin:21-jre
LABEL authors="cherhy"
ARG JAR_FILE=build/libs/payment.jar
COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE dev

EXPOSE 10101

ENTRYPOINT ["java","-jar","/app.jar"]