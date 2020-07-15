# DOCKER-VERSION 1.12
FROM anapsix/alpine-java:jdk8
MAINTAINER Marco Spasiano <marco.spasiano@cnr.it>

COPY target/*.war /opt/selezioni-agid.war

EXPOSE 8080

# https://spring.io/guides/gs/spring-boot-docker/#_containerize_it
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/selezioni-agid.war"]
