package chatapp;

import processing.core.PApplet; // The graphics library this project uses

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client extends PApplet {

    private static final String SERVER_HOST = "localhost"; // This can be changed if the server is running on another machine
    private static final int SERVER_PORT = 3000;
    private static final int HEADER_SIZE = 4; // The number of bytes the socket should read to get the length of the message (integers are at most 4 bytes long)
    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private static SocketChannel channel; // The socket channel representing the client's connection to the server
    private static Screen screen; // The screen that we are rendering
    private static Client window; // Refers to itself so Screens can access mouseX and mouseY
    private static ConcurrentLinkedQueue<String> queue; // A queue of messages that are received from the reception thread

    public static void main(String[] args) {
        try {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to " + SERVER_HOST + ":" + SERVER_PORT);
            queue = new ConcurrentLinkedQueue<>();
            startReception(); // Open the thread to receive messages while rendering the screen
            PApplet.main("chatapp.Client", args); // Call the parent class's main function to start the GUI
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
    }

    public static int getWidth() { return WIDTH; }
    public static int getHeight() { return HEIGHT; }
    public static Client getWindow() {return window; }

    /**
     * Opens a new thread on which the socket listens for messages from the server.
     * Resulting messages are offered to the queue, which the main thread can access
     * and process. Essentially, the queue allows for communication between threads
     * so we can read messages while rendering the UI.
     * */
    public static void startReception() {
        new Thread(() -> {
            ByteBuffer headerBuff = ByteBuffer.allocate(HEADER_SIZE); // prepare a buffer for the header
            try {
                while (true) {
                    int initRead = channel.read(headerBuff);

                    if (initRead == -1) { // Server disconnected
                        channel.close();
                    }

                    if (headerBuff.position() == HEADER_SIZE) { // Check if we actually read 4 bytes from the stream
                        headerBuff.flip(); // Reset the position to 0 so we can read from the buffer
                        int msgSize = headerBuff.getInt();
                        ByteBuffer buff = ByteBuffer.allocate(msgSize); // Prep a buffer based on the message size we got from the header
                        headerBuff.clear();

                        while (buff.hasRemaining()) { // The full message might not come in one read, so we need to loop until the buffer has lost space.
                            int bytesRead = channel.read(buff);
                            if (bytesRead == -1) { // Disconnected during communication
                                channel.close();
                                return;
                            }
                        }

                        buff.flip(); // Switch to reading mode
                        byte[] messageBytes = new byte[buff.remaining()];
                        buff.get(messageBytes); // Store the bytes from the buffer into a byte array, which we can convert to a String
                        String msg = new String(messageBytes);
                        System.out.println("Received " + msg + " from the server");
                        queue.offer(msg); // Offer to the queue
                    }
                }
            } catch (Exception e) {
                System.out.println("uh oh" + e);
                System.exit(0);
            }
        }).start();
    }

    /**
     * Gracefully closes the channel and joins the thread when quitting.
     */
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

    /**
     * Convert the desired message into bytes and write it to the stream.
     * @param content String of the complete messsage to be sent (see README for formatting)
     */
    public static void sendMessage(String content) {
        try {
            byte[] messageBytes = content.getBytes();
            int messageLength = messageBytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + messageLength);
            buffer.putInt(messageLength); // Write the length of the message in bytes (header)
            buffer.put(messageBytes); // Write the actual message in bytes
            buffer.flip(); // Bring the position back to 0

            while (buffer.hasRemaining()) { // Might need to loop writing multiple times if the msg is long
                channel.write(buffer);
            }

            System.out.println("sent: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send the message to the current screen and allow it to respond accordingly.
     * If the screen can't/won't respond, then something went wrong.
     * @param res The message received from the server
     */
    public static void handleMessage(String res) {
        if (!screen.handleMessage(res)) {
            System.out.println("The current screen couldn't handle this message: " + res);
        }
    }

    /**
     * Attempt to handle all the messages accumulated in the queue.
     * This could be multiple messages, hence why we need to loop.
     */
    public void process() {
        while (!queue.isEmpty()) {
            handleMessage(queue.poll());
        }
    }

    /**
     * Allows the current screen to switch to a new screen for the client ot render.
     * @param s Screen instance that should handle the rendering
     */
    public static void changeScreen(Screen s) {
        screen = s;
    }

    /**
     * From the PApplet class. Establishes the main settings.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);

        int displayDensity = displayDensity(); // Make it look sharper on Mac
        pixelDensity(displayDensity);
        System.out.println("Display Density: " + displayDensity);
    }

    /**
     * From the PApplet class. Another method to set things up before drawing frames.
     */
    @Override
    public void setup() {
        screen = new MainScreen();
        window = this;
        createExitHandler();
        windowTitle("Chat App");
    }

    /**
     * From the PApplet class. This is called every frame.
     */
    @Override
    public void draw() {
        process(); // Handle any messages prior to drawing the frame.
        screen.drawFrame(g); // Pass the PGraphics instance we get through PApplet to the current Screen
    }

    /**
     * From the PApplet class. We pass the data to the current
     * screen so it can respond accordingly.
     */
    @Override
    public void mousePressed() {
        screen.mousePressed(mouseButton);
    }

    /**
     * From the PApplet class. We pass the data to the current
     * screen so it can respond accordingly.
     */
    @Override
    public void keyPressed() {
        screen.keyPressed(key, keyCode);
    }
}
