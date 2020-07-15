# Selezioni online - Progetto AGID

## Requisiti

Per l'avvio in locale occorre una istanza di [Alfresco Community Edition](https://www.alfresco.com/thank-you/thank-you-downloading-alfresco-community-edition) attiva sulla porta 9080, la versione minima è la 5.0.1 Community.  

Successivamente applicare i seguenti amps:
- https://repo.maven.apache.org/maven2/it/cnr/si/alfresco/groups-extension/2.22/groups-extension-2.22.amp
- https://repo.maven.apache.org/maven2/it/cnr/si/alfresco/zip-content/2.22/zip-content-2.22.amp
- https://repo.maven.apache.org/maven2/it/cnr/si/alfresco/cnr-extension-content-model/2.22/cnr-extension-content-model-2.22.amp

**Per una corretta inizializzazione delle risorse installare Alfresco localizzato in Inglese**

### Docker Alfresco
In alternativa si può rendere disponibile Alfresco tramite [docker-compose](docker-compose/docker-compose.yml)   
```bash
git clone https://github.com/consiglionazionaledellericerche/cool-jconon-agid.git
cd cool-jconon-agid/docker-compose
docker-compose up -d
```

## Compilazione e Primo Avvio

```bash
git clone https://github.com/consiglionazionaledellericerche/cool-jconon-agid.git
cd cool-jconon-agid
mvn clean install -Pprod
java -jar target/selezioni-agid.war --spring.profiles.active=prod,agid
```

## Avvio locale

```bash
git clone https://github.com/consiglionazionaledellericerche/cool-jconon-agid.git
cd cool-jconon-agid
mvn clean spring-boot:run -Pprod -Dspring.profiles.active=prod,agid
```

L'applicazionre sarà attiva alla seguente URL: <http://localhost:8080>

