# Chat App

For my AP Computer Science final project, I built a simple chat app with the Processing library and sockets.

## Installation and execution

1. Clone the repository with `git clone https://github.com/ayush-shrivastava003/github.io`
2. Pre-built files are located in `out/artifacts` and can be executed with `java -jar client/final-proj.jar` and `java -jar server/final-proj.jar`

If you are running the server on a different machine, you can set the `SERVER_HOST` variable to that machine's IP address in `Client.java`. However, you'll need to rebuild it to a `.jar` file for it to work. I built the files with IntelliJ IDEA Community Edition.

## Documentation for client-server messages

Sent by the client:

* `join:{username}` - Attempt to join the chat room
  * Payload
    * `username` - the username the client is joining with
  * Responses
    * `join_success` - status OK, signals client to switch screens
    * `join_fail:{reason}` - join failed
      * `reason` - reason for failure (usually bc username is taken)
* `message:{content}` - Send a message to the chat
  * Payload
    * `content` - Message the user wants to send
  * Responses
    * No response from the server, but the server will relay this message to the other clients in the room

Sent by the server:

* `enter:{username}` - Signal to other clients that a new user has entered the chat
  * Payload
    * `username` - the username of the newly joined client
* `leave:{username}` - Signal to the other clients that a user has left the chat
  * Payload
    * `username` - the username of the client that left
* `message:{username}:{content}` - Relay to other clients a new message
  * Payload
    * `username` - The user that send the message
    * `content` - The content of the message they sent