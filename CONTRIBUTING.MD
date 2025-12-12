# Contributing

## Setup

You need the following software installed to get started:
- Git
- Java JDK 25
- Docker
- Docker Compose
- a Browser of your choice
- an IDE of your choice (preferably IntelliJ)

Do the following steps to make sure you are ready to go:
- Clone this repo using the following command:
  `git clone git@github.com:re-Director/re-director.git`
- Change into the repos directory and make sure everything the tests are running and everything compiles: `./mvnw verify`
- Start the application: `./mvnw spring-boot:run`
- Make sure it runs on `http://localhost:8080`
- Have fun!

## Hot Swap

Run the `ReDirectorApplication` run config, start developing and hit `Ctrl + F9` to Hot Swap.

## Release

Whenever there is a new release, new Docker images will be built and published to Docker Hub.
Regular commits etc. will not trigger the images to be built.  
By default `amd64` and `arm64` images will be built.

Before creating a release, make sure that:
- all tests pass
- you update the version in the `pom.xml` to the next version
- update all the mentioned versions in the documentation
- you are able to run the docker containers - build them locally to do that
    - run `./mvnw jib:dockerBuild` to create the container locally
    - use the example files in the `docker` directory
    - go to http://localhost and check if the application is running
- create the new release [here](https://github.com/re-Director/re-director/releases)