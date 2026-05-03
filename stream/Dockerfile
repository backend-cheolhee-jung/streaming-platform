FROM eclipse-temurin:21-jre
LABEL authors="cherhy"

ARG JAR_FILE=build/libs/stream.jar
COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul
ENV PROFILE=dev

EXPOSE 10102

CMD ["java", "-jar", "/app.jar", "-port=10102"]