package DataBase;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


public class DataBase extends SQLiteOpenHelper {

    private static final String SQL_CREATE_CONVERSATIONS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DataBaseContract.Entry.CONVERSATIONS_TABLE + " (" +
                    DataBaseContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Entry.CONVERSATIONS_MUTE_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Entry.CONVERSATIONS_BLOCK_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_ID + " TEXT," +
                    DataBaseContract.Entry.CONVERSATION_RECIPIENT + " TEXT," +
                    DataBaseContract.Entry.CONVERSATION_RECIPIENT_NAME + " TEXT," +
                    DataBaseContract.Entry.CONVERSATION_RECIPIENT_IMAGE_PATH + " TEXT," +
                    DataBaseContract.Entry.USER_UID + " TEXT)";

    private static final String SQL_DELETE_CONVERSATIONS_TABLE = "DROP TABLE IF EXISTS " + DataBaseContract.Entry.CONVERSATIONS_TABLE;

    private static final String SQL_CREATE_MESSAGES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DataBaseContract.Messages.MESSAGES_TABLE + " (" +
                    DataBaseContract.Messages._ID + " INTEGER PRIMARY KEY," +
                    DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_ID + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_IMAGE_PATH + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_LONGITUDE + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_LATITUDE + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_ADDRESS + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_LINK + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_LINK_TITLE + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_LINK_CONTENT + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_RECORDING_PATH + " TEXT," +
                    DataBaseContract.Messages.MESSAGE_STAR + " TEXT)";


    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DataBaseContract.User.USER_TABLE + " (" +
                    DataBaseContract.User._ID + " INTEGER PRIMARY KEY," +
                    DataBaseContract.User.USER_UID + " TEXT," +
                    DataBaseContract.User.USER_NAME + " TEXT," +
                    DataBaseContract.User.USER_LAST_NAME + " TEXT," +
                    DataBaseContract.User.USER_TIME_CREATED + " TEXT," +
                    DataBaseContract.User.USER_PICTURE_LINK + " TEXT)";// +
                   // DataBaseContract.User.USER_BLOCKED_USERS + " TEXT)";

    private static final String SQL_CREATE_BLOCKED_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DataBaseContract.BlockedUsers.BLOCKED_USERS_TABLE + " (" +
                    DataBaseContract.BlockedUsers._ID + " INTEGER PRIMARY KEY," +
                    DataBaseContract.BlockedUsers.USER_UID + " TEXT)";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CHAT_DATABASE.db";

    public DataBase(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }

    public DataBase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DataBase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public DataBase(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONVERSATIONS_TABLE);
        db.execSQL(SQL_CREATE_MESSAGES_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_BLOCKED_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(SQL_DELETE_CONVERSATIONS_TABLE);
        //db.execSQL(SQL_CREATE_CONVERSATIONS_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL(SQL_DELETE_CONVERSATIONS_TABLE);
       // onUpgrade(db,oldVersion,newVersion);
    }

}
