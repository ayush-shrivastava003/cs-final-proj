package chatapp;

// import packages for non-blocking I/O
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
    private static final int HEADER_SIZE = 4;
    private static final HashMap<SocketChannel, ClientInfo> clients = new HashMap<>();

    public static void main(String[] args) {
        try {
            // set up the selector & server to manage/accept connecting sockets
            Selector selector = Selector.open();
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(PORT));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Setup complete! Listening for connections...");

            while (true) {
                if (selector.select() == 0) continue; // no client channels ready to read/write

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey k = iterator.next();

                    if (!k.isValid()) continue;

                    if (k.isAcceptable()) { // server socket is able to accept new client that connected
                        SocketChannel client = ((ServerSocketChannel) k.channel()).accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        clients.put(client, new ClientInfo());
                        System.out.println("New client connected: " + client.getRemoteAddress());
                    }

                    if (k.isReadable()) { // client socket wrote data into the stream that must be read
                        System.out.println("readable key");
                        SocketChannel client = (SocketChannel) k.channel();

                        ByteBuffer headerBuff = ByteBuffer.allocate(HEADER_SIZE);
                        try {
                            int initRead = client.read(headerBuff);

                            if (initRead == -1) {
                                System.out.println("Client " + client.getRemoteAddress() + " has disconnected");
                                ClientInfo user = clients.get(client);
                                if (user.isInRoom()) {
                                    for (SocketChannel c : clients.keySet()) {
                                        if (c != client && clients.get(c).isInRoom()) sendMessage(c, "leave:" + user.getUsername());
                                    }
                                }
                                clients.remove(client);
                                client.close();
                            }

                            if (headerBuff.position() == HEADER_SIZE) {
                                headerBuff.flip();
                                int msgSize = headerBuff.getInt();
                                ByteBuffer buff = ByteBuffer.allocate(msgSize);

                                while (buff.hasRemaining()) {
                                    System.out.println("has remaining");
                                    int bytesRead = client.read(buff);
                                    if (bytesRead == -1) break;
                                }

                                buff.flip();
                                byte[] messageBytes = new byte[buff.remaining()];
                                buff.get(messageBytes);
                                String msg = new String(messageBytes);

                                System.out.println(msg + " from " + client.getRemoteAddress());
                                parseMessage(msg, client);
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

    public static void parseMessage(String msg, SocketChannel client) {
        String[] split = msg.split(":");

        switch (split[0]) {
            case "join": {
                String username = split[1];

                for (ClientInfo c : clients.values()) {
                    if (c != clients.get(client) && c.getUsername() != null && c.getUsername().equals(username)) {
                        sendMessage(client, "join_fail:Username in use");
                        return;
                    }
                }

                ClientInfo info = clients.get(client);
                info.setRoomState(true);
                info.setUsername(split[1]);

                for (SocketChannel c : clients.keySet()) {
                    if (c != client && clients.get(c).isInRoom()) sendMessage(c, "enter:" + split[1]);
                }

                sendMessage(client, "join_success");
                return;
            }

            case "message": {
                for (SocketChannel c : clients.keySet()) {
                    ClientInfo info = clients.get(c);
                    String username = clients.get(client).getUsername();
                    String rest = msg.substring(msg.indexOf(":") + 1);
                    if (c != client && info.isInRoom()) sendMessage(c, "message:" + username + ":" + rest);
                }
            }
        }
    }

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
