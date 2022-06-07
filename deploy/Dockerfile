FROM openjdk:18.0.1.1-slim

COPY build/sso-oauth2-server-*.jar /sso-oauth2-server.jar

ENTRYPOINT ["java", "-jar", "/sso-oauth2-server.jar"]
