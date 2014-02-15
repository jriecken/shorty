# Deployment

You'll need:

 - Mongo installed (locally to the app server or externally)
 - Java 1.7 JDK installed on the app server
 - (optional) Nginx installed

## Packaging

As mentioned in the README, you can package shorty for deployment by running:

    $ play universal:package-zip-tarball

This creates a `shorty-X.X.tgz` tarball in the `target/universal` directory.

## Setting up the database

The first time that you deploy shorty, you'll need to set up the `shorty` mongo database.

There is a `scripts/create-db.js` script that will do this for you. To run it:

    $ mongo shorty create-db.js

## Running the application server

Extract the `shorty-X.X.tgz` file to a location on your server.

You can then run the application with: (replace url/domain/nodeId/pidfile name as appropriate)

    $ ./bin/shorty -Dmongo.url=mongodb://localhost:27017/shorty \
        -Dapplication.shortDomain=http://shorty.example.com \
        -Dapplication.nodeId=1 \
        -Dpidfile=/var/run/shorty.pid

To stop the application:

    $ kill `cat /var/run/shorty.pid`

For more startup options, see the [Play Production Configuration](http://www.playframework.com/documentation/2.2.x/ProductionConfiguration) page.

## Runing the application behind Nginx

It is recommended that you run the application behind a front-end HTTP server. This way you can run several load-balanced instances of the application if you want to (or run multiple separate apps)

Example nginx config load balacing 3 instances (running on `8080`, `8081`, `8082`):

    server {
      listen 80;
      server_name shorty.example.com;

      access_log /var/log/nginx/access-shorty.log;
      error_log /var/log/nginx/error-shorty.log error;

      gzip on;
      gzip_proxied any;
      gzip_types text/plain text/javascript text/css application/javascript application/json application/xml;
      gzip_vary on;

      charset UTF-8;

      upstream shorty-backend {
        server localhost:8080;
        server localhost:8081;
        server localhost:8082;
      }

      location / {
        proxy_buffering  off;
        proxy_set_header  X-Real-IP $remote_addr;
        proxy_set_header  X-Scheme $scheme;
        proxy_set_header  X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header  Host $http_host;
        proxy_pass http://shorty-backend;
      }
    }
