package DataBase;

import android.provider.BaseColumns;

public final class DataBaseContract {
    private DataBaseContract(){}

    public static class Conversations implements BaseColumns{
        public static final String CONVERSATIONS_TABLE = "conversations";
        public static final String CONVERSATION_ID = "conversationID";
        public static final String MUTED = "muted";
        public static final String BLOCKED = "blocked";
        public static final String LAST_MESSAGE = "lastMessage";
        public static final String LAST_MESSAGE_TIME = "lastMessageTime";
        public static final String LAST_MESSAGE_TYPE = "lastMessageType";
        public static final String LAST_MESSAGE_ID = "messageID";
        public static final String RECIPIENT = "recipient";
        public static final String RECIPIENT_NAME = "recipientName";
        public static final String GROUP_NAME = "groupName";//group name replaces recipient name .
        // if its an individual talk I.E only with one recipient then group name should be recipient name
        public static final String IMAGE_PATH = "imagePath";
        public static final String USER_UID = "UID";
        public static final String CONVERSATION_INDEX = "index";
    }

    public static class Messages implements BaseColumns{

        public static final String MESSAGES_TABLE = "messages";
        public static final String MESSAGE_ID = "messageID";
        public static final String SENDER = "sender";
        public static final String RECIPIENT = "recipient";
        public static final String TIME_SENT = "time_sent";
        public static final String TIME_DELIVERED = "time_delivered";
        public static final String CONTENT = "content";
        public static final String TYPE = "type";
        public static final String STATUS = "status";
        public static final String MESSAGE_IMAGE_PATH = "image_path";
        public static final String MESSAGE_LATITUDE = "latitude";
        public static final String MESSAGE_LONGITUDE = "longitude";
        public static final String MESSAGE_ADDRESS = "address";
        public static final String MESSAGE_LINK = "link";
        public static final String MESSAGE_LINK_TITLE = "link_title";
        public static final String MESSAGE_LINK_CONTENT = "link_content";
        public static final String MESSAGE_RECORDING_PATH = "recording_path";
        public static final String MESSAGE_STAR = "star";
        public static final String MESSAGE_RECIPIENT_NAME = "recipient_name";
        public static final String MESSAGE_FILE_PATH = "file_path";
        public static final String MESSAGE_SENDER_NAME = "sender_name";
        public static final String MESSAGE_READ_TIME = "read_time";
        public static final String QUOTE = "quote";
        public static final String QUOTE_ID = "quote_id";
    }

    public static class User implements BaseColumns{

        public static final String USER_TABLE = "user_table";
        public static final String USER_UID = "UID";
        public static final String USER_NAME = "Name";
        public static final String USER_LAST_NAME = "lastName";
        public static final String USER_TIME_CREATED = "time_created";
        public static final String USER_PICTURE_LINK = "picture_link";
        public static final String USER_PHONE_NUMBER = "phone_number";
        public static final String USER_LAST_STATUS = "status";
        public static final String TOKEN = "token";
        public static final String BLOCKED = "block";
    }

    public static class Group implements BaseColumns
    {
        public static final String GroupTable = "group_table";
    }
}
