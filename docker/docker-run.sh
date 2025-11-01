docker run \
  --name re-director \
  -p 80:80 \
  -v re-director-data:/data \
  jensknipper/re-director:0.0.4