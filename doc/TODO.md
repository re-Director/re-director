# TODO

## High Prio

- spring: accept trailing slash
- deletion confirmation modal
- table ordering
    - fixed to insertion order for now
      - make it by hostname
    - dynamically
- pagination
- password protection
    - default user - for selfhosters - configured in parameters or else use default - feature may be deactivated
- metrics, eg. last called
    - as timeseries?
      - QuestDb, ChronoDB, InfluxDB embedded
- variable redirects, add different redirects (subdomains etc.) to hosts
  - maybe look at npm or other SaaS solutions
  - preserve path option
- add test for database migrations not breaking the app

## Medium Prio

- write Doc on how to behind reverse proxies: caddy, nginx
- jspecify (+nullaway?) instead of jakarta validation
- redirect tester
    - like https://domain-forward.com/url-redirect-tester-tool/
    - or https://redirect.pizza/tester?url=http://blog.jensknipper.de
- broken links checker
    - periodically check target for 4xx or 5xx status code

## Low Prio

- if redirect inactive or no match, redirect temporarily to own domain / default page, currently it looks like that this is then hosted under the target domain
  - needs current domain name
- table
    - odd coloring
    - show header next to values in mobile view
    - move styling into css file
- simplify css
    - utility classes for hr in table