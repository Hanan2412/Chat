package Backend;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.lifecycle.Observer;


import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import NormalObjects.Conversation;
import NormalObjects.Message;
import Retrofit.Server;


@SuppressWarnings("Convert2Lambda")
public class WorkScheduler extends Worker {


    private ChatDataBase db;
    private ChatDao dao;
    private final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    public WorkScheduler(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context,params);
        db = ChatDataBase.getInstance(context);
        dao = db.chatDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Server server = Server.getInstance();
        Observer<List<Conversation>> observer = new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                server.backupConversations(conversations,currentUser);
                dao.getAllConversations().removeObserver(this);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                dao.getAllConversations().observeForever(observer);
            }
        });
        Observer<List<Message>>messageObserver = new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                server.backupMessages(messages,currentUser);
                dao.getAllMessages().removeObserver(this);
            }
        };
        Handler handler1 = new Handler(Looper.getMainLooper());
        handler1.post(new Runnable() {
            @Override
            public void run() {
                dao.getAllMessages().observeForever(messageObserver);
            }
        });
        return Result.success();
    }
}
