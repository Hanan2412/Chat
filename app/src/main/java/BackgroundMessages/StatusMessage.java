package BackgroundMessages;

//sends the status of the user - online or offline
public class StatusMessage {
    private String userStatus;

    public StatusMessage(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}
