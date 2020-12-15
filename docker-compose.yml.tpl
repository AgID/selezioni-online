jconon:
  image: docker.si.cnr.it/##{CONTAINER_ID}##
  mem_limit: 1024m
  read_only: false
  env_file:
    - ./agid.env
  environment:
  - LANG=it_IT.UTF-8
  - REPOSITORY_BASE_URL=http://as1dock.si.cnr.it:8080/alfresco/
  volumes:
  - ./cacerts:/cacerts
  - ./webapp_logs:/logs
  - /tmp
  command: java -Xmx256m -Xss512k -Djavax.net.ssl.trustStore=/cacerts -Djavax.net.ssl.trustStorePassword=changeit -Dspring.profiles.active=prod,agid -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8787 -Djava.security.egd=file:/dev/./urandom -jar /opt/selezioni-agid.war
  labels:
  - SERVICE_NAME=##{SERVICE_NAME}##
  - PUBLIC_NAME=democnr.agid.gov.it
