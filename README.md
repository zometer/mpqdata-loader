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
		-Dspring.profiles.active=local,download-archive,load-database \
		-Dspring.datasource.url=$DB_URL \
		-Dspring.datasource.username=$DB_USERNAME \
		-Dspring.datasource.password=$DB_PASSWORD \
		-jar \
		target/mpqdata-loader-0.0.1-SNAPSHOT.jar
````

### Docker
````bash
# Run the application.
$ docker run -it \
		-e SPRING_PROFILES_ACTIVE=local,download-archive,load-database \
		-e SPRING_DATASOURCE_URL=$DB_URL \
		-e SPRING_DATAUSER_USERNAME=$DB_USERNAME \
		-e SPRING_DATAUSER_PASSWORD=$DB_PASSWORD \
		mpqdata-loader:latest
````

### Helm / Kubernetes


### CLI Utilities

This application has a few arguments that provide a way to hash a password
or generate a new random password (clear text and hash). When run with the
CLI arguments, the application will exit immediately after the utility is
done executing.


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

## Issues

## TODOs


## Additional Information

1. https://zenhax.com/viewtopic.php?t=12756

