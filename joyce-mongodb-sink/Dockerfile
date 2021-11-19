FROM adoptopenjdk:11-jre-hotspot as builder
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ./application.jar
RUN java -Djarmode=layertools -jar /app/application.jar extract

FROM adoptopenjdk:11-jre-hotspot
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
RUN true
COPY --from=builder /app/spring-boot-loader/ ./
RUN true
COPY --from=builder /app/snapshot-dependencies/ ./
RUN true
COPY --from=builder /app/application/ ./
RUN true
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]