package Backend;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

import NormalObjects.User;
import Retrofit.Server;

public class UserVM extends AndroidViewModel {
    private final String currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private Repository repository;
    private LiveData<User>currentUser;
    private final String USER_VM = "USER_VN";
    public UserVM(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        currentUser = repository.getUserByID(currentUserUID);
    }

    /**
     * updates the token of a specific user
     * @param uid the uid of the user to update
     * @param token the token to update
     */
    public void updateToken(String uid,String token)
    {
        repository.updateUserToken(uid,token);
    }

    /**
     * inserts new user into local database. if the user exists already,
     * it will be updated instead
     * @param user the user to insert
     */
    public void saveUser(User user)
    {
        LiveData<User>users = repository.getUserByID(user.getUserUID());
        Observer<User> observer = new Observer<User>() {
            @Override
            public void onChanged(User user1) {
                if (user1 == null)
                {
                    users.removeObserver(this);
                    repository.insertNewUser(user);
                }
                else
                {
                    repository.updateUser(user);
                }
            }
        };
        users.observeForever(observer);
    }

    /**
     * updates user in local database and on the server
     * @param user the user to update
     */
    public void updateUser(User user)
    {
        Log.d(USER_VM, "update user");
        repository.updateUser(user);
        repository.updateUserInServer(user);
    }

    public void updateUserLocal(User user)
    {
        repository.updateUser(user);
    }

    public void updateUserRemote(User user)
    {
        repository.updateUserInServer(user);
    }

    public LiveData<Boolean> isUserExists(User user)
    {
        return repository.isUserExists(user);
    }

    /**
     * blocks user
     * @param userID the uid of the user to block
     * @return boolean if the user was blocked
     */
    public LiveData<Boolean> blockUser(String userID)
    {
        repository.blockUser(userID);
        return repository.isUserBlocked(userID);
    }

    public LiveData<Boolean> isUserBlocked(String uid)
    {
        return repository.isUserBlocked(uid);
    }

    public void muteUser(String uid)
    {
        repository.muteUser(uid);
    }

    public void unBlockUser(String uid)
    {
        repository.unBlockUser(uid);
    }

    public void unMuteUser(String uid)
    {
        repository.unMuteUser(uid);
    }

    public LiveData<Boolean> isUserMuted(String uid)
    {
        return repository.isUserMuted(uid);
    }

    public LiveData<List<User>>getAllMutedOrBlockedUsers(boolean blocked)
    {
        return repository.getAllMutedOrBlockedUsers(blocked);
    }

    public void reset()
    {
        repository.clearAll();
    }

    public LiveData<User>getCurrentUser(){return currentUser;}

    public LiveData<User>loadUserByID(String userUID)
    {
        return repository.getUserByID(userUID);
    }

    public void downloadUser(String uid)
    {
        repository.downloadUser(uid);
    }

    public void createNewUser(User user, Bitmap userImage, Context context)
    {
        repository.createNewUser(user, userImage, context);
    }

    public void createNewUser(User user)
    {
        repository.createNewUser(user);
    }

    public void searchForUsers(String query)
    {
        repository.searchUsers(query);
    }

    public void setOnUserImageDownloadListener(Server.onFileDownload listener)
    {
        repository.setOnFileDownloadListener(listener);
    }


    public void downloadImage(String iid)
    {
        repository.downloadImage(iid);
    }

    public void setOnUserDownloadedListener(Server.onUserDownload listener)
    {
        repository.setOnUserDownloadedListener(listener);
    }

    public void setOnUserFoundListener(Server.onUsersFound listener)
    {
        repository.setOnUserFoundListener(listener);
    }

    public void getUserToken(String uid)
    {
        repository.getUserToken(uid);
    }

    public void setOnTokenDownloadedListener(Server.onTokenDownloaded listener)
    {
        repository.setOnTokenDownloadListener(listener);
    }

    public void setOnFileUploadListener(Server.onFileUpload listener)
    {
        repository.setOnFileUploadListener(listener);
    }

    public void updateUserImage(User user,Bitmap userImage, Context context)
    {
        repository.updateUserImage(user,userImage,context);
    }

    public void deleteUser(User user)
    {
        repository.deleteUser(user);
    }

    public void updateBlockUser(String userID)
    {
        repository.updateUserBlock(userID);
    }
}
