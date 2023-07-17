FROM gradle:7.4.0-jdk17

WORKDIR /app

COPY /app .

CMD ./gradlew bootRun
