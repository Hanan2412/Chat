package Backend;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

import NormalObjects.User;
import Retrofit.Server;

public class UserVM extends AndroidViewModel {
    private final String currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private Repository repository;
    private LiveData<User>currentUser;
    public UserVM(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        currentUser = repository.getUserByID(currentUserUID);
    }

    public void updateToken(String uid,String token)
    {
        repository.updateUserToken(uid,token);
    }

    public void insertUser(User user)
    {
        repository.insertNewUser(user);
    }

    public void updateUser(User user)
    {
        repository.updateUser(user);
        repository.updateUserInServer(user);
    }

    public LiveData<Boolean> checkIfUserExists(User user)
    {
        return repository.isUserExists(user);
    }

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

//    public void updateFBData(String path,String data)
//    {
//        repository.updateFBData(path, data);
//    }
//
//    public void updateFBData(String path, HashMap<String,Object> map)
//    {
//        repository.updateFBData(path, map);
//    }
//
//    public void updateFBData(String path,boolean data)
//    {
//        repository.updateFBData(path, data);
//    }
//    public void deleteFBData(String path)
//    {
//        repository.deleteFBData(path);
//    }
    public void downloadUser(String uid)
    {
        repository.downloadUser(uid);
    }

    public void createNewUser(String name, String lastName, String nick, Bitmap userImage)
    {
        repository.createNewUser(name, lastName, nick, userImage);
    }

    public void createNewUser(User user)
    {
        repository.createNewUser(user);
    }

    public void searchForUsers(String query)
    {
        //repository.searchForUsers(query);
        repository.searchUsers(query);
    }

//    public void setOnUserDownloadListener(Server3.onUserDownloaded listener)
//    {
//        repository.setOnUserDownloadListener(listener);
//    }

//    public void setOnUsersFoundListener(Server3.onUserFound listener)
//    {
//        repository.setOnUsersFoundListener(listener);
//    }
    //////////////////////////////////////////////////////////////////////////////




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
}
