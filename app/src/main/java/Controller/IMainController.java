package Controller;

import android.content.Context;

public interface IMainController {

    void onDownloadConversations(Context context);
    void onFindUsersQuery(String query,Context context);
}
