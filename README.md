# Rechnernetze-Chat

This application is a small chat that runs in the shell. It consits of two separate parts, a client and a server. It is to consider that the server has to run bevor the chat.

## Getting started

The JAR can be build with the Gradle-Task "fatJar". This will create a ChatApp.jar in the build/libs directory.

> ./gradlew fatJar

### Server

Runs with:
> java -jar ChatApp.jar Server [port]

If the port is not specified, 1500 is used

### Client

Runs with:
> java -jar ChatApp.jar Client [username] [port] [serverAddress]

Defaults:
- portNumber is 1500
- address is "localhost"
- username is "Anonymous"

## Specification

### For users

When a user connects to the server he will be allocated to the room waiting-hall. The waiting-hall is the only default room from the server. All user can create new rooms with the command "CREATE [NameOFRoom]" or look if there are existing rooms with the command "AVAILABLE". A switch to another room is possible with the command "SWITCH [NameOFRoom]". (NOTE: You can't go back to the waiting-hall) 
Other available commands are HELP, WHOISIN to see all users in the current room or LOGOUT to disconnect from the server.

### For developers

We are using a client-server-architecture that uses a TCP-connection between client and server. Whereby we dont have a typical question-answer protocol because the client can receive messages from the server without sending a request.

The **client-application** is composed of a client-entity that specifies the client and a server-listener that informs the client about incoming messages. Furthermore exists a message-object for the transfer.

The **server-application** has a server-entity similar to the client-entity and chat-rooms that holds the connected clients. Every connected client gets his own thread, which will be hold in the room-objects. When a client trys to connect to the server the server-entity accepts the connection and gives the socket to the default room "Waiting-Hall". Everytime a client switches to another room his socket, he is connected with on the server side, will be moved to the new room.

All **messages** are intern handled as a message-objet that consits of the name of the sender, a timestamp, a text and a type. Possible are the types WHO_IS_IN, MESSAGE, LOGOUT, CREATE_ROOM, SWITCH_ROOM, AVAILABLE_ROOMS or HELP. Every message-type stands for an other command the client can use. The default is the type "message" for the transfer of a text which the user entered. For the transfer will the message-oject converted to JSON.
The advantage of the own message-object is that it can hold commands and separate informations. Furthermore is it more scalable in further development. 
On the server side the sent message will be assumed from the allocated thread for the client, which executes a method from the room that allocates the message to all client-thread the room is holding. All client-threads will send these message to their connected clients.  

### Data Model

![data model](https://github.com/mattx7/Rechnernetze-Chat/blob/master/pics/data_model.png)
