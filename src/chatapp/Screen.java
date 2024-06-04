package chatapp;

import processing.core.PGraphics;

public interface Screen {
    void drawFrame(PGraphics g);
    default void mousePressed(int mouseButton) {}
    default void keyPressed(char key, int keyCode) {}
    default boolean handleMessage(String res) { return false; }
}
