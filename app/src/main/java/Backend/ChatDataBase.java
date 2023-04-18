package Backend;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import NormalObjects.Conversation;
import NormalObjects.Group;
import NormalObjects.Message;
import NormalObjects.MessageHistory;
import NormalObjects.MessageViews;
import NormalObjects.User;

@Database(entities = {Conversation.class, User.class, Message.class, Group.class, MessageHistory.class, MessageViews.class},version = 25)
public abstract class ChatDataBase extends RoomDatabase {

    private static ChatDataBase instance;
    public abstract ChatDao chatDao();

    public static synchronized ChatDataBase getInstance(Context context)
    {
        if (instance == null)
        {
            instance = Room.databaseBuilder(context.getApplicationContext(),ChatDataBase.class,"ChatDataBase")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
