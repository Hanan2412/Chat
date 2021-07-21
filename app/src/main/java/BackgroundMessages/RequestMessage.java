package BackgroundMessages;


import Consts.Requests;

public class RequestMessage {

    private final Requests request;
    public RequestMessage(Requests request)
    {
        this.request = request;
    }

    public Requests getRequest() {
        return request;
    }
}
