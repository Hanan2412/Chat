package Messages;

import Consts.MessageType;

public class ContentMessage extends TextMessage{

    private String latitude;
    private String longitude;
    private String locationAddress;
    private String contactName;
    private String phoneNumber;


    public ContentMessage(String messageID, String conversationID, String senderToken, int messageType) {
        super(messageID, conversationID, senderToken,messageType);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
