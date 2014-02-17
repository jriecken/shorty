# Deployment

 - [Software](#software)
 - [Packaging](#packaging)
 - [Setting up the database](#setting-up-the-database)
 - [Running the application server](#running-the-application-server)
 - [Running the application behind Nginx](#running-the-application-behind-nginx)

## Software

You'll need:

 - MongoDB installed (somewhere the app server can access)
 - Java 1.7 JDK installed on the app server
 - (optional) Nginx installed

## Packaging

As mentioned in the README, you can package shorty for deployment by running:

    $ play universal:package-zip-tarball

This creates a `shorty-1.0.tgz` tarball in the `target/universal` directory.

## Setting up the database

The first time that you deploy shorty, you'll need to set up the `shorty` Mongo database.

There is a `scripts/create-db.js` script that will do this for you. To run it:

    $ mongo shorty create-db.js

## Running the application server

Extract the `shorty-1.0.tgz` file to a location on your server.

You can then run the application with: (replace port/Mongo url/short domain/node id/pidfile as appropriate)

    $ nohup ./bin/shorty -mem 256 -J-server -Dhttp.port=8080 \
        -Dmongodb.url=mongodb://localhost:27017/shorty \
        -Dapplication.shortDomain=http://shorty.example.com \
        -Dapplication.nodeId=1 \
        -Dpidfile.path=/path/to/shorty-1.pid >&/dev/null &


To stop the application:

    $ kill `cat /path/to/shorty-1.pid`

For more startup options, see the [Play Production Configuration](http://www.playframework.com/documentation/2.2.x/ProductionConfiguration) page.

## Running the application behind Nginx

Shorty can easily run in a load-balanced environment with multiple application server instances.

If you run multiple instances, ensure that the `application.nodeId` configuration value is different on each node.

Example Nginx config for load balancing 3 instances (running on `8080`, `8081`, `8082`):

In `/etc/nginx/sites-available/shorty`

    upstream shorty-backend {
      server localhost:8080;
      server localhost:8081;
      server localhost:8082;
    }

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

      location / {
        proxy_pass http://shorty-backend;
        proxy_http_version 1.1;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Scheme $scheme;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Host $http_host;
      }
    }
