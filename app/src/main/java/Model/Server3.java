package Model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import NormalObjects.User;

@SuppressWarnings("unchecked")
public class Server3 extends Uploads{

    private static final String USERS_PATH = "users/";
    private static Server3 server3 = null;

    public interface onUserDownloaded
    {
        void downloadedUser(User user);
    }

    public interface onUserFound{
        void foundUser(User user);
    }

    private onUserDownloaded userDownloaded;
    private onUserFound userFound;

    private Server3() {

    }

    public void setUserDownloadListener(onUserDownloaded listener)
    {
        userDownloaded = listener;
    }

    public void setUserFoundListener(onUserFound listener)
    {
        userFound = listener;
    }

    public static Server3 getInstance() {
        if (server3 == null)
            server3 = new Server3();
        return server3;
    }

    @Override
    public void uploadImageBitmap(Bitmap imageBitmap) {
        super.uploadImageBitmap(imageBitmap);
    }

    @Override
    public void uploadImage(String photoPath) {
        super.uploadImage(photoPath);
    }

    @Override
    public void uploadFile(String filePath) {
        super.uploadFile(filePath);
    }

    @Override
    public void setOnResultListener(onResult result) {
        super.setOnResultListener(result);
    }


    public void updateData(String path, String data) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.setValue(data);
    }

    public void updateData(String path, HashMap<String, Object> map) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.updateChildren(map);
    }


    public void updateData(String path, boolean data) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.setValue(data);
    }

    public void deleteData(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.removeValue();
    }


    public void downloadUser(String uid)
    {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(USERS_PATH + uid);
            ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    HashMap<String, Object> userHash = (HashMap<String, Object>) snapshot.getValue();
                    User user = new User();
                    if (userHash != null) {
                        user.setName((String) userHash.get("name"));
                        user.setLastName((String) userHash.get("lastName"));
                        user.setNickName((String) userHash.get("nickname"));
                        user.setPictureLink((String) userHash.get("pictureLink"));
                        user.setTimeCreated((String) userHash.get("timeCreated"));
                        user.setStatus((String) userHash.get("status"));
                        user.setLastTimeLogIn((String) userHash.get("lastTimeLogIn"));
                        user.setPhoneNumber((String) userHash.get("phoneNumber"));
                        user.setUserUID(uid);
                        if (userDownloaded!=null)
                            userDownloaded.downloadedUser(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            };
            userReference.addValueEventListener(userListener);
        }
    }


    public void createNewUser(String name, String lastName, String nick, Bitmap userImage) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference reference = database.getReference("/users/" + currentUserUID);
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("lastName", lastName);
            userMap.put("nickname", nick);
            String time = System.currentTimeMillis() + "";
            userMap.put("lastTimeLogIn", time);
            userMap.put("timeCreated", time);
            reference.updateChildren(userMap);
            if (userImage != null) {
                //uploading user image to firebase
                uploadProfileImage(userImage);
            }
        }
    }


    public void searchForUsers(String query)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users/");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HashMap<String, Object> userQuery = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (userQuery != null) {
                        String name = (String) userQuery.get("name");
                        if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                            User user = new User();
                            user.setPictureLink((String) userQuery.get("pictureLink"));
                            user.setName(name);
                            user.setLastName((String) userQuery.get("lastName"));
                            if (dataSnapshot.getKey()!=null)
                                user.setUserUID(dataSnapshot.getKey());
                            if (userFound!=null)
                                userFound.foundUser(user);
                            reference.removeEventListener(this);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });
    }
}
