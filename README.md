# Rechnernetze-Chat
HAW - Rechnernetze - Chat

## Client

Runs with:
> java -jar ChatApp.jar Client [username] [port] [serverAddress]

If an error occurs the program simply stops

Defaults:
- portNumber is 1500
- address is "localhost"
- username is "Anonymous"

> java -jar ChatApp.jar Client

is equivalent to 

> java -jar ChatApp.jar Client Anonymous 1500 localhost 

## Server

Runs with:
> java -jar ChatApp.jar Server [port]

If the port is not specified, 1500 is used
