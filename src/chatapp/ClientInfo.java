package chatapp;

public class ClientInfo {
    private String username;
    private boolean inRoom;

    /**
     * Stores basic information about the client.
     */
    public ClientInfo() {
        username = null; // The client might be connected but not have a username.
        inRoom = false; // The client might be connected but not in a room.
    }

    public String getUsername() { return username; }
    public boolean isInRoom() { return inRoom; }
    public void setUsername(String s) { username = s; }
    public void setRoomState(boolean b) { inRoom = b; }
}
