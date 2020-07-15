jconon:
  image: docker.si.cnr.it/##{CONTAINER_ID}##
  mem_limit: 1024m
  read_only: false
  environment:
  - LANG=it_IT.UTF-8
  - REPOSITORY_BASE_URL=http://as1dock.si.cnr.it:8080/alfresco/
  - SPID_ENABLE=true
  - SPID_IDP_TEST_POSTURL=http://spid-testenv2.test.si.cnr.it/sso
  - SPID_ASSERTIONCONSUMERSERVICEINDEX=2
  - SPID_ATTRIBUTECONSUMINGSERVICEINDEX=2
  - SPID_DESTINATION=http://cool-jconon-agid.test.si.cnr.it/jconon/spid/send-response
  volumes:
  - ./webapp_logs:/logs
  - /tmp
  command: java -Xmx256m -Xss512k -Dspring.profiles.active=prod,agid -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8787 -Djava.security.egd=file:/dev/./urandom -jar /opt/selezioni-agid.war
  labels:
  - SERVICE_NAME=##{SERVICE_NAME}##
