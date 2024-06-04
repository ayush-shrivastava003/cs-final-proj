package chatapp;

import processing.core.PGraphics;
import java.util.ArrayList;
import static processing.core.PConstants.*;

public class RoomScreen implements Screen {
    private static String message = "Type your message here...";
    private static boolean typing = false;
//    private static String errorMsg = "";
    public static ArrayList<String> messages = new ArrayList<>();
    public static String username = "";

    public void drawFrame(PGraphics g) {
        // Background & title
        g.background(255);
        g.fill(0);
        g.textAlign(CENTER);
        g.textSize(48);
        g.text("Chat Room", (Client.getWidth() / 2.f), (Client.getHeight() / 10.0f));

        // Add the messages in with a huge block of test
        g.textAlign(LEFT);
        g.textSize(18);
        g.text(String.join("\n", messages), 0.05f * Client.getWidth(), 0.15f * Client.getHeight());

        // Input to type messages
        g.fill(225);
        g.rect(0.05f * Client.getWidth(), 0.9f * Client.getHeight(), 0.9f * Client.getWidth(), 0.05f * Client.getHeight());
        g.fill(0);
        g.text(message, 0.06f * Client.getWidth(), 0.93f * Client.getHeight());
    }

    public void keyPressed(char key, int keyCode) {
        System.out.println(key + " was the key");
        if (!typing) return;
        if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`1234567890-=~!@#$%^&*()_+[]\\{}|;':\",./<>? ".indexOf(key) != -1) {
            message += key;
        } else if (key == '\b' && !message.isEmpty()) {
            message = message.substring(0, message.length() - 1);
        } else if (key == '\n' && !message.isBlank()) {
            Client.sendMessage("message:" + message);
            messages.add(username + " -> " + message);
            message = "";
        }
    }

    public void mousePressed(int mouseButton) {
        int mouseX = Client.getWindow().mouseX;
        int mouseY = Client.getWindow().mouseY;

        float inputXStart = 0.05f * Client.getWidth();
        float inputXEnd = 0.95f * Client.getWidth();
        float inputYStart = 0.9f * Client.getHeight();
        float inputYEnd = 0.95f * Client.getHeight();

        typing = (inputXStart < mouseX) && (mouseX < inputXEnd) && (inputYStart < mouseY) && (mouseY < inputYEnd);
    }

    public boolean handleMessage(String res) {
        String[] split = res.split(":");

        switch (split[0]) {
            case "message": { // New message from other clients
                // this is a very ugly way of getting the complete message that the client sent,
                // including all colon-separated strings that would have been lost if we used split[1]
                String temp = res.substring(res.indexOf(":") + 1);
                String rest = temp.substring(temp.indexOf(":") + 1);
                messages.add(split[1] + " -> " + rest);
                return true;
            }

//            case "msg_fail": { // Attempt to send a message failed for whatever reason
//                errorMsg = split[1];
//                return true;
//            }

            case "enter": {
                messages.add(split[1] + " has entered the chat!");
                return true;
            }

            case "leave": {
                messages.add(split[1] + " has left the chat.");
                return true;
            }

            default: {
                return false;
            }
        }
    }
}
