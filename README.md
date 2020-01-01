# oyun-ws

Handle incoming web websocket traffic for oyunkeyf.net.

    oyun <-> redis <-> oyun-ws <-> websocket <-> client

Start:

    sbt
    ~reStart

    sbt -Dconfig.file=/path/to/my.conf

Custom config file example

    include "application"
    http.port = 8080
    netty.useEpoll = true
    mongo.uri = ""
    redis.uri = ""
