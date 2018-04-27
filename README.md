# Java NIO simple HTTP server

## Overview

A very simple HTTP server that is able to receive GET requests and serve local files.
Only HTTP/1.1 is supported.

This project is an educational project.

Built for Java 7+ without any external dependencies.
Works in a non-blocking fashion and uses Java NIO and NIO.2 (combines reactor and 
proactor patterns).

## Build

To build the server run:

```
gradlew build
```

The built `.jar` file will be located under `build/libs/`.

## Run

To start the server with default configuration run:

```
java -jar http-nio-server-1.0.0.jar
```

You also can override default configuration by specifying the path as 
a command line argument:

```
java -jar http-nio-server-1.0.0.jar "/home/settings.properties"
```

## Server configuration

By default tries to read configuration from `settings.properties` file located in the 
folder where server executable was run.
Falls back to default configuration if the file was not found.

Configuration file must have properties format and must have the following settings:

* `port` (default: 8080) - server port
* `www_root` (default: <empty\>) - path to the folder containing files that 
the server will be serving; empty or missing value means the folder where
server executable was run
* `session_timeout_secs` (default: 30) - maximum time in seconds that 
any connection will be served; connection will be closed when the timeout 
is reached
* `max_connections` (default: 10000) - maximum number of simultaneous 
connections that will be served

## License

Copyright 2018 puzpuzpuz

Licensed under MIT License.
