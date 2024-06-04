package chatapp;

import processing.core.PApplet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client extends PApplet {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;
    private static final int HEADER_SIZE = 4;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private static SocketChannel channel;
    private static Screen screen;
    private static Client window;
    private static ConcurrentLinkedQueue<String> queue;

    public static void main(String[] args) {
        try {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to " + SERVER_HOST + ":" + SERVER_PORT);
            queue = new ConcurrentLinkedQueue<>();
            startReception();
            PApplet.main("chatapp.Client", args);
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
    }

    public static int getWidth() { return WIDTH; }
    public static int getHeight() { return HEIGHT; }
    public static Client getWindow() {return window; }

    public static void startReception() {
        new Thread(() -> {
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
                        headerBuff.clear();

                        while (buff.hasRemaining()) {
                            int bytesRead = channel.read(buff);
                            if (bytesRead == -1) {
                                channel.close();
                                return;
                            }
                        }

                        buff.flip();
                        byte[] messageBytes = new byte[buff.remaining()];
                        buff.get(messageBytes);
                        String msg = new String(messageBytes);
//                        String msg = new String(buff.array());
                        System.out.println("Received " + msg + " from the server");
                        queue.offer(msg);
                    }
                }
            } catch (Exception e) {
                System.out.println("uh oh" + e);
                System.exit(0);
            }
        }).start();
    }

    public void createExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public static void sendMessage(String content) {
        try {
            byte[] messageBytes = content.getBytes();
            int messageLength = messageBytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + messageLength);
            buffer.putInt(messageLength);
            buffer.put(messageBytes);
            buffer.flip();

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            System.out.println("sent: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleMessage(String res) {
        if (!screen.handleMessage(res)) {
            System.out.println("The current screen couldn't handle this message: " + res);
        }
    }

    public void process() {
        while (!queue.isEmpty()) {
            handleMessage(queue.poll());
        }
    }

    public static void changeScreen(Screen s) {
        screen = s;
    }

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);

        int displayDensity = displayDensity();
        pixelDensity(displayDensity);
        System.out.println("Display Density: " + displayDensity);
    }

    @Override
    public void setup() {
        screen = new MainScreen();
        window = this;
        createExitHandler();
        windowTitle("Chat App");
    }

    @Override
    public void draw() {
        process();
        screen.drawFrame(g);
    }

    @Override
    public void mousePressed() {
        screen.mousePressed(mouseButton);
    }

    @Override
    public void keyPressed() {
        screen.keyPressed(key, keyCode);
    }
}
