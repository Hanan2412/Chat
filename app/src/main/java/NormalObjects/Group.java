package NormalObjects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class Group {

    @PrimaryKey(autoGenerate = true)
    private int logId;

    private String conversationID;
    private String uid;

    public Group(String conversationID, String uid) {
        this.conversationID = conversationID;
        this.uid = uid;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getLogId() {
        return logId;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @NonNull
    @Override
    public String toString() {
        return "Group: conversationID:" + conversationID + ", UID:" + uid;
    }
}
