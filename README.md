# Shorty

Shorty is a simple url-shortener service written with:

 - Play 2.2.1 (scala) with Guice for dependency injection.
 - MongoDB with the ReactiveMongo driver.
 - Specs2 with Mockito for unit tests.

It supports:

 - Creating short URLs.
 - Redirecting a short URL to the original URL.
 - Viewing basic click stats on a given short URL (how many times it's been clicked).

It's released under the [MIT](http://en.wikipedia.org/wiki/MIT_License) license.

## Usage

Shorty has both an HTML interface as well as a JSON-based REST API.

### HTML interface

(These URLs all assume the server is running on `http://localhost`)

 - To shorten a URL, visit `http://localhost/` and enter your "long URL".
 - To use the shortened URL, visit `http://localhost/:hash` (where :hash is the "short" code). This will increment the click stats for the url.
 - To view click stats for a shortened URL, visit `http://localhost/:hash/stats`.

## REST API

All API responses have two custom HTTP headers:
 - `X-Shorty-NodeId` - Indicates which application server node handled the request.
 - `X-Shorty-ResponseTime` - Indicates how long the request took to generate.

### `POST /v1/urls`

Shorten a URL. If the same `long_url` is shortened more than once, the same short url will be generated.

**Request Payload**

    {
      "long_url": "http://www.something.com/some/really/long/url"
    }

**Response**

`200` If the URL was successfully shortened with a body of:

    {
      "short_url": "http://localhost/A1b43",
      "hash": "A1b43",
      "long_url": "http://www.something.com/some/really/long/url",
      "created": "2014-01-01T12:00:00Z"
    }

`400` If the `long_url` parameter is missing:

    {
      "error": "MISSING_URL"
    }

`400` If the `long_url` parameter is not a valid URL with a body of:

    {
      "error": "INVALID_URL"
    }

`400` If the `long_url` parameter is longer than 2048 characters with a body of:

    {
      "error": "URL_TOO_LONG"
    }

### `GET /v1/urls/:hash`

Expand a short URL (where `:hash` is the "short code" in the short URL). This does *not* increase
the click count.

**Response**

`200` If the short URL exists with a body of:

    {
      "short_url": "http://localhost/A1b43",
      "hash": "A1b43",
      "long_url": "http://www.something.com/some/really/long/url",
      "created": "2014-01-01T12:00:00Z"
    }

`404` If a short URL doesn't exist with the specified hash with a body of:

    {
      "error": "NOT_FOUND"
    }

### `GET /v1/links/:hash/stats`

Get click stats for the specified short URL (where `:hash` is the "short code" in the short URL).

**Response**

`200` If the short URL exists with a body of:

    {
      "clicks": 12345
    }

`404` If a short URL doesn't exist with the specified hash with a body of:

    {
      "error": "NOT_FOUND"
    }

## Building + Running

### Install JDK

You'll need a Java 1.7 JDK installed on your system.

### Install Play

If you're using a Mac and Homebrew, just run:

    $ brew install play

Otherwise, go to the [Play Framework Download Page](http://www.playframework.com/download) and follow the instructions for installing Play 2.2.1

### Install MongoDB

If you're using a Mac and Homebrew, just run:

    $ brew install mongodb

Otherwise, go to the [MongoDB Website](http://www.mongodb.org/downloads) and follow the instructions for installing it. This has been tested with the latest version of MongoDB at this time (2.4.9)

### Configuration

Some modifications may be necessary to `conf/application.conf` depending on your mongo installation:

    # Base server name to use in generated URLs
    application.shortDomain=http://localhost:8080
    # Location of your mongo server
    mongodb.servers = ["localhost:27017"]

You will also need to run a setup script on mongo to set up some collections/indexes. This script is in the `scripts` folder -called `create-db.js`. Note that running this script will drop the existing database if it already exists. Just run:

    $ mongo shorty scripts/create-db.js

### Running locally

You can run the application locally in "development mode" by running:

    $ play "run 8080"

Or "production mode" by running:

    $ play "start 8080"

from this directory (you can substitute something other than `8080` for a port).

### Running tests

To run the tests, run:

    $ play test

Note: Some of the tests require a mongo connection. They put data in a `shorty-test` database. This database is cleared before each test that requires mongo is run.

### Packaging for deployment

For more information about deploying the application, take a look at [DEPLOYMENT.md](DEPLOYMENT.md)
