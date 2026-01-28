[![Tests](https://github.com/re-Director/re-director/actions/workflows/run-tests.yml/badge.svg)](https://github.com/re-Director/re-director/actions/workflows/run-tests.yml)
[![Release](https://github.com/re-Director/re-director/actions/workflows/publish.yml/badge.svg)](https://github.com/re-Director/re-director/actions/workflows/publish.yml)

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
- **Different HTTP Status Codes**: Chose between different redirects like: Moved Permanently (301), Found (302), Temporary Redirect (307) and Permanent Redirect (308)
- **Dark Mode**: perfect for working in dark environments, respects you system default settings
- **Responsive Design**: easily manage your redirects, even with your phone on the go
- and more upcoming!

## Getting Started

### Java

Clone this repo, make sure to have Java 25 installed and run the following command:

```bash
./mvnw spring-boot:run
```

### Docker

An image of the application can be pulled from [Docker Hub](https://hub.docker.com/r/jensknipper/re-director).

You can run it using the following command:
```bash
docker run \
  --name re-director \
  -p 80:80 \
  -v re-director-data:/data \
  jensknipper/re-director:0.0.6
```

### Docker Compose

You can also run this application using [Docker Compose](https://docs.docker.com/compose/)
Simply save the following code into a `docker-compose.yml` file and run `docker-compose up`.

```yaml
services:
  re-director:
    image: jensknipper/re-director:0.0.6
    ports:
      - "80:80"
    volumes:
      - re-director-data:/data

volumes:
  re-director-data:
```

### Docker Compose behind reverse proxy

There is some documentation on how to run re:Director behind a reverse proxy on the [website](https://re-director.github.io/docs/installation/reverse-proxy/).

## Configuration

Documentation on how to configure re:Director can be found on the [website](https://re-director.github.io/docs/configuration/health-checks/).