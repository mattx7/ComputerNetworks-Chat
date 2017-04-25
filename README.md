# Rechnernetze-Chat
HAW - Rechnernetze - Chat

## Getting started

The JAR can be build with  the Gradle-Task "fatJar". This will create ja ChatApp.jar in the build/libs directory.

> ./gradlew fatJar

### Client

Runs with:
> java -jar ChatApp.jar Client [username] [port] [serverAddress]

Defaults:
- portNumber is 1500
- address is "localhost"
- username is "Anonymous"

### Server

Runs with:
> java -jar ChatApp.jar Server [port]

If the port is not specified, 1500 is used

## Specification

We are using an object-stream, which transfers a message-object or a simple text message. The message-object will be send from the client to the server and the server will response with a simple text. The advantage of the own message-object is that it can hold commands. 

### Data Model

![data model](https://github.com/mattx7/Rechnernetze-Chat/blob/master/pics/data_model.png)