package chatapp;

import processing.core.PApplet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client extends PApplet {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;
    private static final int HEADER_SIZE = 10;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private static SocketChannel channel;
    private static Screen screen;
    private static Client window;

    public static void main(String[] args) {
        try {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to " + SERVER_HOST + ":" + SERVER_PORT);
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
                        channel.read(buff);
                        String msg = new String(buff.array());
                        System.out.println(msg);
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
                System.out.println(e);
            }
        }));
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
