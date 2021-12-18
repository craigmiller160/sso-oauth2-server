FROM openjdk:15.0.2-slim

COPY build/sso-oauth2-server-*.jar /sso-oauth2-server.jar

ENTRYPOINT ["java", "-jar", "/sso-oauth2-server.jar"]
