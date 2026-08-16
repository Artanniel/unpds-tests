FROM eclipse-temurin:25-jdk
VOLUME /tmp
COPY target/unipds-tests-0.0.1-SNAPSHOT.jar app.jar
COPY start_app.sh start_app.sh
RUN chmod +x start_app.sh
ENTRYPOINT ["java","-jar","/app.jar"]
