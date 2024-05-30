package chatapp;

import processing.core.PApplet;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client extends PApplet {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;
    private static final int HEADER_SIZE = 10;

    public static void main(String[] args) {
        try {
            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to " + SERVER_HOST + ":" + SERVER_PORT);

            ByteBuffer headerBuff = ByteBuffer.allocate(HEADER_SIZE);
            try {
                while (true) {
                    int initRead = channel.read(headerBuff);

                    if (initRead == -1) {
                        channel.close();
                    }

                    if (headerBuff.position() == HEADER_SIZE) {
                        headerBuff.flip();
                        int msgSize = headerBuff.getInt();
                        ByteBuffer buff = ByteBuffer.allocate(msgSize);
                        channel.read(buff);
                        String msg = new String(buff.array());
                        System.out.println(msg);
                    }
                }
            } catch (Exception e) {
                System.out.println("uh oh" + e.toString());
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("error: " + e.toString());
        }
    }


}
