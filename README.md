# re:Director

**Manage Redirects Like a Pro**

A powerful, yet simple to use, self-hosted redirect service.

## Overview

re:Director enables you to redirect any domain simply by specifying where it should direct to. Make URLs shorter and easier to memorize.

![re:Director Main Page](doc/main_page.png)

## Key Features

- **Manage Redirects**: Create redirects for the domains you want to redirect to some target
- **Filter Redirects**: Filter you redirects by source, target or status
- **Pause Redirects**: Option to temporarily pause redirects and resume them at a later time
- and more upcoming!

## Getting Started

### Java

Clone this repo, make sure to have Java 21 installed and run the following command:

```bash
./mvnw spring-boot:run
```

### Docker

An image of the application can be pulled from [Docker Hub](https://hub.docker.com/repository/docker/jensknipper/re-director).

You can run it using the following command:
```bash
docker run \
-p 80:80 \
-v ./re-director-data:/data \
jensknipper/re-director:latest
```
make sure to create the folder for the volume first!

### Docker Compose

You can also run this application using [Docker Compose](https://docs.docker.com/compose/)
Simply save the following code into a `docker-compose.yml` file and run `docker-compose up`.

```yaml
services:
  re-director:
    image: jensknipper/re-director:latest
    ports:
      - "80:80"
    volumes:
      - ./re-director-data:/data
```
make sure to create the folder for the volume first!

### Docker Compose behind Traefik reverse proxy

You might have quite a few applications running behind a Traefik reverse proxy. The configuration should then look like this.

```yaml
services:
  traefik:
    image: traefik:v3.4
    command:
      - "--providers.docker"
      - "--entrypoints.web.address=:80"
    ports:
      - "80:80"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
 
  re-director:
    image: jensknipper/re-director:latest
    expose:
      - 80
    volumes:
      - ./re-director-data:/data
    labels:
      - "traefik.http.routers.re-director.rule=Host(`re-director.localhost`) || HostRegexp(`.+`)"
      - "traefik.http.routers.re-director.entrypoints=web"
      - "traefik.http.routers.re-director.priority=1"
```
make sure to create the folder for the volume first!
