# mpqdata-loader

## Overview

A spring-boot command line application to download the latest config
archive and load updated character information to the database.

## Usage

### Maven

````bash
# Build the project
$ ./mvnw build
````

### Java

````bash
# Run the application.
# Be sure to set the profile on the command line, otherwise you won't have a data source.
$ java -Dspring.profiles.active=local -jar target/mpqdata-loader-0.0.1-SNAPSHOT.jar
````


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

1. APPSEC - Database that contains role mapping, "secure objects" (dynamic roles), and external account credentials.

### Environment Variables

## Issues

## TODOs


## Additional Information

1. https://zenhax.com/viewtopic.php?t=12756

