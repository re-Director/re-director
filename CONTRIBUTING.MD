# Contributing

## Setup

You need the following software installed to get started:
- Git
- Java JDK 25
  - easiest to install with SDKMAN
- Docker
- Docker Compose
- a Browser of your choice
- an IDE of your choice 
  - preferably IntelliJ

Do the following steps to make sure you are ready to go:
- Clone this repo using the following command:
  `git clone git@github.com:re-Director/re-director.git`
- Change into the repos directory and make sure everything the tests are running and everything compiles: `./mvnw verify`
- Start the application: `./mvnw spring-boot:run`
- Make sure it runs on `http://localhost:8080`
- Have fun!

## Formatting

To ensure a uniform formatting spotless is used.
Make sure to run `./mvnw spotless:apply` before commiting and pushing.

## Hot Swap

Run the `ReDirectorApplication` run config, start developing and hit `Ctrl + F9` to Hot Swap.

## Release

Whenever there is a new release, new Docker images will be built and published to Docker Hub.
Regular commits etc. will not trigger the images to be built.  
By default `amd64` and `arm64` images will be built.

Before creating a release, make sure that:
- all tests pass, run: `./mvnw clean verify`
- you update the version in the `pom.xml` to the next version
- update all the mentioned versions in the documentation and docker examples
- you are able to run the docker containers - build them locally to do that
    - run `./mvnw jib:dockerBuild` to create the container locally
      - you might have to temporarily set the `architecture` variable to `arm64`
    - use the example files in the `docker` directory
      - you might have to temporarily  add e.g. `-amd64` or `-arm64` to the image name to make it work
    - go to http://localhost and check if the application is running
- create the new release [here](https://github.com/re-Director/re-director/releases)