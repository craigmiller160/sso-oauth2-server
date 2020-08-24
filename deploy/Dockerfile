FROM openjdk:11.0.2-jre-slim

COPY build/sso-oauth2-server-*.jar /sso-oauth2-server.jar

ENTRYPOINT ["java", "-jar", "/sso-oauth2-server.jar"]
