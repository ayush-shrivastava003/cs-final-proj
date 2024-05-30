package chatapp;

// import packages for non-blocking I/O
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class Server {
    private static final int PORT = 3000;
    private static final int HEADER_SIZE = 10;
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

                for (SelectionKey k : selector.selectedKeys()) {
                    if (k.isAcceptable()) { // server socket is able to accept new client that connected
                        SocketChannel client = ((ServerSocketChannel) k.channel()).accept();
                        client.configureBlocking(false);
                        System.out.println("New client connected: " + client.getRemoteAddress());
                    }

                    if (k.isReadable()) { // client socket wrote data into the stream that must be read
                        SocketChannel client = (SocketChannel) k.channel();

                        ByteBuffer headerBuff = ByteBuffer.allocate(HEADER_SIZE);
                        try {
                            int initRead = client.read(headerBuff);

                            if (initRead == -1) {
                                client.close();
                            }

                            if (headerBuff.position() == HEADER_SIZE) {
                                headerBuff.flip();
                                int msgSize = headerBuff.getInt();
                                ByteBuffer buff = ByteBuffer.allocate(msgSize);
                                client.read(buff);
                                String msg = new String(buff.array());
                                System.out.println(msg + " from " + client.getRemoteAddress());
                            }
                        } catch (Exception e) {
                            System.out.println("uh oh" + e.toString());
                            continue;
                        }
                    }
                }
            }

        } catch (IOException e) { // initializing the selector could throw this
            System.out.println(e.toString());
            System.exit(1);
        }
    }
}
