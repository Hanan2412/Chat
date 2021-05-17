package Try;

public class TryData {

    private String user;
    private String body;
    private String title;
    private String sent;
    private boolean b = true;
    private int v = 1;
    public TryData(String user, String body, String title, String sent) {
        this.user = user;
        this.body = body;
        this.title = title;
        this.sent = sent;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }
}
