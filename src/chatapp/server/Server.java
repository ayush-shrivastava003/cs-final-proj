package chatapp.server;

// Import packages for non-blocking I/O
import chatapp.ClientInfo;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.HashMap;

public class Server {
    private static final int PORT = 3000;
    private static final int HEADER_SIZE = 4; // See client for info
    private static final HashMap<SocketChannel, ClientInfo> clients = new HashMap<>(); // Store info about connected clients

    public static void main(String[] args) {
        try {
            // Set up the selector & server to manage/accept connecting sockets
            Selector selector = Selector.open();
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(PORT));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Setup complete! Listening for connections...");

            while (true) { // Main loop in which we listen for connections
                if (selector.select() == 0) continue; // No client channels ready to read/write

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey k = iterator.next();

                    if (!k.isValid()) continue;

                    if (k.isAcceptable()) { // Server socket is able to accept new client that connected
                        SocketChannel client = ((ServerSocketChannel) k.channel()).accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        clients.put(client, new ClientInfo()); // Add client to list, but we have no info
                        System.out.println("New client connected: " + client.getRemoteAddress());
                    }

                    if (k.isReadable()) { // Client socket wrote data into the stream that must be read
                        SocketChannel client = (SocketChannel) k.channel();

                        ByteBuffer headerBuff = ByteBuffer.allocate(HEADER_SIZE); // Prep a buffer to read the msg size
                        try {
                            int initRead = client.read(headerBuff);

                            if (initRead == -1) {
                                System.out.println("Client " + client.getRemoteAddress() + " has disconnected");
                                ClientInfo user = clients.get(client);
                                if (user.isInRoom()) { // If the user disconnected while in a room, notify the other clients
                                    for (SocketChannel c : clients.keySet()) {
                                        if (c != client && clients.get(c).isInRoom()) sendMessage(c, "leave:" + user.getUsername());
                                    }
                                }
                                clients.remove(client); // Stop tracking info about this client
                                client.close();
                            }

                            if (headerBuff.position() == HEADER_SIZE) {
                                headerBuff.flip(); // Prep buffer to reading mode by setting the position to 0
                                int msgSize = headerBuff.getInt();
                                ByteBuffer buff = ByteBuffer.allocate(msgSize); // Make a new buffer for the exact size of the msg

                                while (buff.hasRemaining()) { // Read the info from the client. This might take multiple loops.
                                    int bytesRead = client.read(buff);
                                    if (bytesRead == -1) break;
                                }

                                buff.flip(); // Prep buffer to reading mode by setting the position to 0
                                byte[] messageBytes = new byte[buff.remaining()];
                                buff.get(messageBytes); // Stores the bytes read from a buffer into an array
                                String msg = new String(messageBytes); // Convert the bytes we read into a string

                                System.out.println(msg + " from " + client.getRemoteAddress());
                                parseMessage(msg, client); // Actually procexss the message we received
                            }
                        } catch (Exception e) {
                            System.out.println("uh oh");
                            e.printStackTrace();
                        }
                    }

                    iterator.remove();
                }
            }

        } catch (IOException e) { // initializing the selector could throw this
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Process the message we recieved from the client and respond appropriately.
     * @param msg The message we received from the stream
     * @param client The channel that we received the message from in case we need to respond
     */
    public static void parseMessage(String msg, SocketChannel client) {
        String[] split = msg.split(":"); // Messages are split with colons, see README for info

        switch (split[0]) {
            case "join": { // User wants to join the chat room
                String username = split[1];

                for (ClientInfo c : clients.values()) { // Check all clients to see if the username is in use
                    if (c != clients.get(client) && c.getUsername() != null && c.getUsername().equals(username)) {
                        sendMessage(client, "join_fail:Username in use");
                        return;
                    }
                }

                // Update the info about the client since they're now validated
                ClientInfo info = clients.get(client);
                info.setRoomState(true);
                info.setUsername(split[1]);

                for (SocketChannel c : clients.keySet()) { // Let everyone know about the new client
                    if (c != client && clients.get(c).isInRoom()) sendMessage(c, "enter:" + split[1]);
                }

                sendMessage(client, "join_success"); // Confirm that the join was successful
                return;
            }

            case "message": { // Client messaged the group
                for (SocketChannel c : clients.keySet()) {
                    ClientInfo info = clients.get(c);
                    String username = clients.get(client).getUsername();
                    String rest = msg.substring(msg.indexOf(":") + 1);

                    // Relay the message to all clients that are in the room
                    if (c != client && info.isInRoom()) sendMessage(c, "message:" + username + ":" + rest);
                }
            }
        }
    }

    /**
     * Write the given message into the given client's stream.
     * @param client The client the message should be sent to
     * @param msg The message to send (check README for message formatting)
     */
    public static void sendMessage(SocketChannel client, String msg) {
        try {
            byte[] bytes = msg.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + bytes.length);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            client.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
