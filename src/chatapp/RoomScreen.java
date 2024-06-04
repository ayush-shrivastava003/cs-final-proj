package chatapp;

import processing.core.PGraphics;
import java.util.ArrayList;
import static processing.core.PConstants.*;

public class RoomScreen implements Screen {
    private static String message = "Type your message here..."; // The message the user is writing
    private static boolean typing = false; // Whether the user is typing
    public static ArrayList<String> messages = new ArrayList<>(); // A log of the messages
    public static String username = ""; // This user's name

    /**
     * Use the PGraphics instance from the Client to render the fram for this screen.
     * This screen will draw the title, the message log, and a box for writing messages
     *
     * @param g PGraphics instance passed from the Client
     */
    public void drawFrame(PGraphics g) {
        // Background & title
        g.background(255);
        g.fill(0);
        g.textAlign(CENTER);
        g.textSize(48);
        g.text("Chat Room", (Client.getWidth() / 2.f), (Client.getHeight() / 10.0f));

        // Add the messages in with a huge block of text
        g.textAlign(LEFT);
        g.textSize(18);
        g.text(String.join("\n", messages), 0.05f * Client.getWidth(), 0.15f * Client.getHeight());

        // Input to type messages
        g.fill(225);
        g.rect(0.05f * Client.getWidth(), 0.9f * Client.getHeight(), 0.9f * Client.getWidth(), 0.05f * Client.getHeight());
        g.fill(0);
        g.text(message, 0.06f * Client.getWidth(), 0.93f * Client.getHeight());
    }

    /**
     * When a key is clicked, we need to check if we're even typing a message.
     * If we are, then we need to check if it's a valid character that you can use (which is pretty much every character).
     * If we aren't, do nothing.
     * If the user hit backspace, we need to delete a character.
     * If the user hit enter, we need to send the message.
     *
     * @param key The character the user typed
     * @param keyCode The keycode corresponding to that character
     */
    public void keyPressed(char key, int keyCode) {
        System.out.println(key + " was the key");
        if (!typing) return;
        if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`1234567890-=~!@#$%^&*()_+[]\\{}|;':\",./<>? ".indexOf(key) != -1) {
            message += key;
        } else if (key == '\b' && !message.isEmpty()) { // Backspace
            message = message.substring(0, message.length() - 1);
        } else if (key == '\n' && !message.isBlank()) { // The user hit enter and the message isn't blank
            Client.sendMessage("message:" + message);
            messages.add(username + " -> " + message);
            message = "";
        }
    }

    /**
     * When the mouse is clicked, we need to check if that is within
     * the bounds of the input box.
     *
     * If the user clicked in the input box, we need to set the typing mode to true.
     *
     * Otherwise, set editing mode to false..
     *
     * @param mouseButton Passed from the Client
     */
    public void mousePressed(int mouseButton) {
        int mouseX = Client.getWindow().mouseX;
        int mouseY = Client.getWindow().mouseY;

        // Bounds of the input box
        float inputXStart = 0.05f * Client.getWidth();
        float inputXEnd = 0.95f * Client.getWidth();
        float inputYStart = 0.9f * Client.getHeight();
        float inputYEnd = 0.95f * Client.getHeight();

        // Set typing to true if the user clicked in the input box and false otherwise
        typing = (inputXStart < mouseX) && (mouseX < inputXEnd) && (inputYStart < mouseY) && (mouseY < inputYEnd);
    }

    /**
     * Handle the messages coming from the server.
     * It can be about:
     *  - a new message from another client
     *  - a new client entering the chat
     *  - a client leaving the chat
     *
     * Any other response from the server isn't expected and should be logged.
     *
     * @param res The response from the server
     * @return true if the response was properly handled, false if not
     */
    public boolean handleMessage(String res) {
        String[] split = res.split(":");

        switch (split[0]) {
            case "message": { // New message from other clients
                // this is a very ugly way of getting the complete message that the client sent,
                // including all colon-separated strings that would have been lost if we used split[2]
                String temp = res.substring(res.indexOf(":") + 1);
                String rest = temp.substring(temp.indexOf(":") + 1);
                messages.add(split[1] + " -> " + rest);
                return true;
            }

            case "enter": { // New client entered
                messages.add(split[1] + " has entered the chat!");
                return true;
            }

            case "leave": { // Existing client left
                messages.add(split[1] + " has left the chat.");
                return true;
            }

            default: {
                return false;
            }
        }
    }
}
