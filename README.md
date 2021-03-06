# mpqdata-loader

## Overview

A spring-boot command line application to download the latest config
archive and load updated character information to the database.

## Usage

### gradle

````bash
# Build the project
$ ./gradlew build
````

### Java

````bash
# Run the application.
# Be sure to set the profile on the command line, otherwise you won't have a data source.
$ java \
    -Dspring.profiles.active=download-archive,load-database \
    -Dspring.datasource.url=$DB_URL \
    -Dspring.datasource.username=$DB_USERNAME \
    -Dspring.datasource.password=$DB_PASSWORD \
    -jar \
    target/mpqdata-loader-0.0.1-SNAPSHOT.jar
````

### Docker

Images are hosted at https://hub.docker.com/r/zometer/mpqdata-loader. 

````bash
# Run the application.
$ docker run -it \
    -e SPRING_PROFILES_ACTIVE=download-archive,load-database \
    -e SPRING_DATASOURCE_URL=$DB_URL \
    -e SPRING_DATAUSER_USERNAME=$DB_USERNAME \
    -e SPRING_DATAUSER_PASSWORD=$DB_PASSWORD \
    zometer/mpqdata-loader:latest
````

### Helm / Kubernetes

Helm charts are hosted at https://zometer.github.io/helm-charts. 

```bash
# Add the helm repository
$ helm repo add zometer https://zometer.github.io/helm-charts

# Install the chart, which creates the cronjob
$ helm install mpqdata-loader-cron zometer/mpqdata-loader \
    -n mpqdata \
    --set config.db.url=$DB_URL,config.db.username=$DB_USERNAME,config.db.password=$DB_PASSWORD,config.cloudConfig.uri=$CLOUD_CONFIG_URL

# Create and start a standalone job for the initial load. 
$ kubectl create job -n mpqdata --from=cronjob/mpqdata-loader-cron mpqdata-loader-job

# Delete the standalone job when complete.  
$ kubectl delete job -n mpqdata mpqdata-loader-job
```

#### Example values.yaml

```yaml
config: 
  cloudConfig: 
    uri: http://mpqdata-config:8888
  db:
    url: jdbc:postgresql://localhost:5432/mpqdata
    username: db_user
    password: Super!Secure-Password-1231
  profiles: 
    active: 
      - download-archive
      - load-database

cron: 
  schedule: "0 0 * * *"
```

### Spring Profiles

| Name              | Notes |
|-------------------|-------|
| `default`         | This profile is loaded when none are specified and merged into all other profiles. When called with no other profiles, the version information output, with no other processing executed. |
| `download-archive` | Tells the job to scrape the app store page for the current version and download and expand the latest sar archive. |
| `load-database`    | Tells the job to read the data from the expanded archive data file and load that data into the database. | |


## Dependencies

### Java

1. JDK 17
1. Spring boot
1. Spring Cloud Config
1. Postgres Jdbc
1. Junit 5

### Databases

1. MPQDATA - Postgres database containing baseline MPQ character data.

### Environment Variables

| Name                     | Value                                                | Notes / Example      |
|--------------------------|------------------------------------------------------|----------------------|
| SPRING_PROFILES_ACTIVE   | Spring profiles to activate for a particular job run | `download-archive,load-database` |
| SPRING_DATASOURCE_URL    | URL for the mpqdata database. | `jdbc:postgresql://localhost:5432/mpqdata`      |
| SPRING_DATAUSER_USERNAME | Database username             | |
| SPRING_DATAUSER_PASSWORD | Database password             | |

## Issues

## TODOs


## Additional Information

1. https://zenhax.com/viewtopic.php?t=12756

