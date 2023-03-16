FROM openjdk:17-jdk-alpine3.14

RUN mkdir /app
COPY build/libs/dog.catfood.app-all.jar /app/app.jar
WORKDIR /app
EXPOSE 8080:8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]
