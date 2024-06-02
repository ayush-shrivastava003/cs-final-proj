package chatapp;

import processing.core.PGraphics;
import static processing.core.PConstants.*;

public class MainScreen implements Screen {
    private static String username = "";
    private static boolean editing = false;
    private static String errorMsg = "";

    public void drawFrame(PGraphics g) {
        g.background(255, 255, 255);
        g.fill(0);
        g.textAlign(CENTER);
        g.textSize(48);
        g.text("Chat App", (Client.getWidth() / 2.f), (Client.getHeight() / 10.0f));

        g.textSize(24);
        g.text("Enter a username", (Client.getWidth() / 2.f), (Client.getHeight() / 5.f));

        g.fill(225);
        g.rect(0.2f * Client.getWidth() , 0.22f * Client.getHeight(), 0.6f * Client.getWidth(), 0.05f * Client.getHeight());

        g.rect(0.4f * Client.getWidth(), 0.3f * Client.getHeight(), 0.2f * Client.getWidth(), 0.05f * Client.getHeight());
        g.fill(0);
        g.textSize(20);
        g.text("Go!", Client.getWidth() / 2.f, 0.33f * Client.getHeight());
        g.text(username, Client.getWidth() / 2.f, 0.25f * Client.getHeight());
        g.text(errorMsg, Client.getWidth() / 2.f, 0.37f * Client.getHeight());
    }

    @Override
    public void mousePressed(int mouseButton) {
        int mouseX = Client.getWindow().mouseX;
        int mouseY = Client.getWindow().mouseY;

        float userBoxXStart = 0.2f * Client.getWidth();
        float userBoxXEnd = 0.8f * Client.getWidth();
        float userBoxYStart = 0.22f * Client.getHeight();
        float userBoxYEnd = 0.27f * Client.getHeight();

        float goBoxXStart = 0.4f * Client.getWidth();
        float goBoxXEnd = 0.6f * Client.getWidth();
        float goBoxYStart = 0.3f * Client.getHeight();
        float goBoxYEnd = 0.35f * Client.getHeight();

        if ((mouseX > userBoxXStart) && (mouseX < userBoxXEnd) && (mouseY > userBoxYStart) && (mouseY < userBoxYEnd)) {
            editing = true;
            System.out.println(editing);
        } else if ((mouseX > goBoxXStart) && (mouseX < goBoxXEnd) && (mouseY > goBoxYStart) && (mouseY < goBoxYEnd)) {
            errorMsg = "";
            if (username.isEmpty()) {
                errorMsg = "You must enter a username.";
            } else {
                Client.sendMessage("join:" + username);
            }
        } else {
            editing = false;
            System.out.println(editing);
        }
    }

    public void keyPressed(char key, int keyCode) {
        if (!editing) return;
        if (Character.isAlphabetic(key) || Character.isDigit(key) || key == ' ') {
            username += key;
        } else if (key == '\b' && !username.isEmpty()) {
            username = username.substring(0, username.length() - 1);
        }
    }
}
