package chatapp;

import processing.core.PGraphics;
import static processing.core.PConstants.*;

public class MainScreen implements Screen {
    private static String username = ""; // Save progress on the username
    private static boolean editing = false; // Whether the username should be changeable
    private static String errorMsg = ""; // Appears if the server responds with a bas response

    /**
     * Use the PGraphics instance from the Client to render the fram for this screen.
     * This screen will draw the title, an input box for the username, and a button
     * to enter the chat room.
     *
     * @param g PGraphics instance passed from the Client
     */
    public void drawFrame(PGraphics g) {
        // Background & title
        g.background(255);
        g.fill(0);
        g.textAlign(CENTER);
        g.textSize(48);
        g.text("Chat App", (Client.getWidth() / 2.f), (Client.getHeight() / 10.0f));

        // Username input box
        g.textSize(24);
        g.text("Enter a username", (Client.getWidth() / 2.f), (Client.getHeight() / 5.f));
        g.fill(225);
        g.rect(0.2f * Client.getWidth() , 0.22f * Client.getHeight(), 0.6f * Client.getWidth(), 0.05f * Client.getHeight());
        g.fill(0);
        g.text(username, Client.getWidth() / 2.f, 0.25f * Client.getHeight());

        // Submit button & error message text
        g.fill(225);
        g.rect(0.4f * Client.getWidth(), 0.3f * Client.getHeight(), 0.2f * Client.getWidth(), 0.05f * Client.getHeight());
        g.fill(0);
        g.textSize(20);
        g.text("Go!", Client.getWidth() / 2.f, 0.33f * Client.getHeight());
        g.text(errorMsg, Client.getWidth() / 2.f, 0.37f * Client.getHeight());
    }

    /**
     * When the mouse is clicked, we need to check if that is within
     * the bounds of the input box or the submit button.
     *
     * If the user clicked in the input box, we need to set the editing mode to true.
     * If the user clicked the submit button, we need to check if the username is valid.
     *
     * Otherwise, set editing mode to false..
     *
     * @param mouseButton Passed from the Client
     */
    @Override
    public void mousePressed(int mouseButton) {
        int mouseX = Client.getWindow().mouseX;
        int mouseY = Client.getWindow().mouseY;

        // Bounds of the input box
        float userBoxXStart = 0.2f * Client.getWidth();
        float userBoxXEnd = 0.8f * Client.getWidth();
        float userBoxYStart = 0.22f * Client.getHeight();
        float userBoxYEnd = 0.27f * Client.getHeight();

        // Bounds of the submit button
        float goBoxXStart = 0.4f * Client.getWidth();
        float goBoxXEnd = 0.6f * Client.getWidth();
        float goBoxYStart = 0.3f * Client.getHeight();
        float goBoxYEnd = 0.35f * Client.getHeight();

        if ((mouseX > userBoxXStart) && (mouseX < userBoxXEnd) && (mouseY > userBoxYStart) && (mouseY < userBoxYEnd)) {
            // If the user clicked in the input box
            editing = true;
            System.out.println(editing);

        } else if ((mouseX > goBoxXStart) && (mouseX < goBoxXEnd) && (mouseY > goBoxYStart) && (mouseY < goBoxYEnd)) {
            // If the user clicked in the submit box
            errorMsg = "";
            if (username.isBlank()) {
                errorMsg = "You must enter a username.";
            } else {
                Client.sendMessage("join:" + username);
//                Client.changeScreen(new RoomScreen())
            }
        } else {
            // If the user clicked anywhere else on the screen
            editing = false;
            System.out.println(editing);
        }
    }

    /**
     * When a key is clicked, we need to check if we're even editing the username.
     * If we are, then we need to check if it's a valid character that you can use.
     * If we aren't, do nothing.
     * If the user hit backspace, we need to delete a character.
     *
     * @param key The character the user typed
     * @param keyCode The keycode corresponding to that character
     */
    public void keyPressed(char key, int keyCode) {
        if (!editing) return;
        if (Character.isAlphabetic(key) || Character.isDigit(key) || key == ' ') {
            username += key;
        } else if (key == '\b' && !username.isEmpty()) {
            username = username.substring(0, username.length() - 1);
        }
    }

    /**
     * Handle the messages coming from the server.
     * It's either to validate the username or to switch screens.
     * Any other response from the server isn't expected and should be logged.
     *
     * @param res The response from the server
     * @return true if the response was properly handled, false if not
     */
    public boolean handleMessage(String res) {
        String[] split = res.split(":");

        switch (split[0]) {
            case "join_success": {
                Client.changeScreen(new RoomScreen());
                RoomScreen.messages.add(username + " has entered the chat!");
                RoomScreen.username = username;
                return true;
            }

            case "join_fail": {
                errorMsg = split[1];
                return true;
            }

            default: {
                return false;
            }
        }
    }
}
