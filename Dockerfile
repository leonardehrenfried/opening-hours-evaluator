FROM openjdk:11-jre-slim
ENV APP_VERSION 1.0.0
RUN rm -rf /myapp && mkdir /myapp
WORKDIR /myapp
ADD build/libs/dropwizard-gradle-${APP_VERSION}-standalone.jar /myapp
ENTRYPOINT java -jar dropwizard-gradle-${APP_VERSION}-standalone.jar server
EXPOSE 8080 8081
