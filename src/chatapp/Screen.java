package chatapp;

import processing.core.PGraphics;

/**
 * Both screens implement this interface.
 * Screens handle rendering and responding to events and server messages.
 * Screens are the real backbone of the UI.
 */
public interface Screen {
    void drawFrame(PGraphics g);
    default void mousePressed(int mouseButton) {}
    default void keyPressed(char key, int keyCode) {}
    default boolean handleMessage(String res) { return false; }
}