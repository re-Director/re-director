# TODO

## High Prio

- metrics, eg. last called
    - as timeseries?
      - QuestDb, ChronoDB, InfluxDB embedded
    - other options: store only last 30 days (configurable) with cleanup job
- variable redirects, add different redirects (subdomains etc.) to hosts
  - maybe look at npm or other SaaS solutions
  - disallow path in host - add path based redirects to source-target redirect
  - CSV import
- better debug logging
- normalize / url endings
- write Documentation
    - how to create redirects after installation
    - how to use testing redirects feature
    - how to host behind reverse proxies: caddy, nginx
    - how to configure authentication
    - how to configure base url

## Medium Prio

- password protection
    - rate limit login - bucket4j
    - password change
- automated checks with nullaway
- broken links checker
    - periodically check target for 4xx or 5xx status code

## Low Prio

- table
    - odd coloring
    - show header next to values in mobile view
    - move styling into css file
- simplify css
    - utility classes for hr in table
- hardened images with distroless images and jlink