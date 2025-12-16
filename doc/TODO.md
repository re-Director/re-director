# TODO

## High Prio

- new homepage!
  - Rework
      - FAQ
      - Getting started
      - wording on page sucks
  - theme
      - https://getdoks.org/
      - https://www.docsy.dev/
- path, query parameter forwarding

## Medium Prio

- write Doc on how to behind reverse proxies: caddy, nginx
  - nginx users should use nginx proxy manager (NPM)
- simplify css
    - utility classes for hr in table
- metrics, eg. last called
  - as timeseries?

## Low Prio

- table ordering
  - fixed to target name for now
  - dynamically
- if redirect inactive or no match, redirect temporarily to own domain / default page, currently it looks like that this is then hosted under the target domain
  - needs current domain name
- pagination
- table
    - odd coloring
    - show header next to values in mobile view
    - move styling into css file
- deletion confirmation modal
- password protection
  - default user - for selfhosters - configured in parameters or else use default - feature may be deactivated
- Caching - add options clear caches
- jspecify instead of jakarta validation