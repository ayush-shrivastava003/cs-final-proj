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

public class Server {
    private static final int PORT = 3000;
    private static final int HEADER_SIZE = 4;
//    private HashMap<SocketChannel, ClientInfo> clients = new HashMap<SocketChannel, ClientInfo>();

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
                                client.close();
                            }

                            if (headerBuff.position() == HEADER_SIZE) {
                                headerBuff.flip();
                                int msgSize = headerBuff.getInt();
                                ByteBuffer buff = ByteBuffer.allocate(msgSize);

                                while (buff.hasRemaining()) {
                                    System.out.println("has remaining");
                                    int bytesRead = client.read(buff);
                                    if (bytesRead == -1 || bytesRead == 0) break;
                                }

                                buff.flip();
                                byte[] messageBytes = new byte[buff.remaining()];
                                buff.get(messageBytes);
                                String msg = new String(messageBytes);

                                System.out.println(msg + " from " + client.getRemoteAddress());
                            } else {
                                System.out.println("how did you even get here?");
                            }
                        } catch (Exception e) {
                            System.out.println("uh oh" + e.toString());
                        }
                    }

                    iterator.remove();
                }
            }

        } catch (IOException e) { // initializing the selector could throw this
            System.out.println(e.toString());
            System.exit(1);
        }
    }
}
