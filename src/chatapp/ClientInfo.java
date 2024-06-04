package chatapp;

public class ClientInfo {
    private String username;
    private boolean inRoom;

    public ClientInfo() {
        username = null;
        inRoom = false;
    }

    public String getUsername() { return username; }
    public boolean isInRoom() { return inRoom; }
    public void setUsername(String s) { username = s; }
    public void setRoomState(boolean b) { inRoom = b; }
}
