package DataBase;

import android.provider.BaseColumns;

public final class DataBaseContract {
    private DataBaseContract(){}

    public static class Entry implements BaseColumns{
        public static final String CONVERSATIONS_TABLE = "conversations";
        public static final String CONVERSATIONS_ID_COLUMN_NAME = "conversationID";
        public static final String CONVERSATIONS_MUTE_COLUMN_NAME = "muted";
        public static final String CONVERSATIONS_BLOCK_COLUMN_NAME = "blocked";
        public static final String CONVERSATION_LAST_MESSAGE_COLUMN_NAME = "lastMessage";
        public static final String CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME = "lastMessageTime";
        public static final String CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME = "lastMessageType";
        public static final String CONVERSATION_LAST_MESSAGE_ID = "messageID";
        public static final String CONVERSATION_RECIPIENT = "recipient";
        public static final String CONVERSATION_RECIPIENT_NAME = "recipientName";
        public static final String CONVERSATION_RECIPIENT_IMAGE_PATH = "imagePath";
        public static final String USER_UID = "UID";

    }

    public static class Messages implements BaseColumns{

        public static final String MESSAGES_TABLE = "messages";
        public static final String MESSAGE_ID = "messageID";
        public static final String MESSAGE_SENDER_COLUMN_NAME = "sender";
        public static final String MESSAGE_RECIPIENT_COLUMN_NAME = "recipient";
        public static final String MESSAGE_TIME_SENT_COLUMN_NAME = "time_sent";
        public static final String MESSAGE_TIME_DELIVERED_COLUMN_NAME = "time_delivered";
        public static final String MESSAGE_CONTENT_COLUMN_NAME = "content";
        public static final String MESSAGE_TYPE_COLUMN_NAME = "type";
        public static final String MESSAGE_STATUS_COLUMN_NAME = "status";
        public static final String MESSAGE_IMAGE_PATH = "image_path";
        public static final String MESSAGE_LATITUDE = "latitude";
        public static final String MESSAGE_LONGITUDE = "longitude";
        public static final String MESSAGE_ADDRESS = "address";
        public static final String MESSAGE_LINK = "link";
        public static final String MESSAGE_LINK_TITLE = "link_title";
        public static final String MESSAGE_LINK_CONTENT = "link_content";
    }

    public static class User implements BaseColumns{

        public static final String USER_TABLE = "user_table";
        public static final String USER_UID = "UID";
        public static final String USER_NAME = "Name";
        public static final String USER_LAST_NAME = "lastName";
        public static final String USER_TIME_CREATED = "time_created";
        public static final String USER_PICTURE_LINK = "picture_link";
        //public static final String USER_BLOCKED_USERS = "blocked_users";
    }

    public static class  BlockedUsers implements BaseColumns{
        public static final String BLOCKED_USERS_TABLE = "blocked_users";
        public static final String USER_UID = "UID";
    }
}
