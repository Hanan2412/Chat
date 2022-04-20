package Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;


@SuppressWarnings("Convert2Lambda")
public abstract class Uploads {

    private final String UPLOAD_INFO = "UploadInfo";
    private final String UPLOAD_ERROR = "UploadError";
    public interface onResult{
        void onPathReady(String path);
        void onStartedUpload();
        void onProgress(int progress);
        void onError(String errorDescription);
    }

    private onResult result;
    public void setOnResultListener(onResult result){this.result = result;}

    public void uploadImageBitmap(Bitmap imageBitmap) {
        String firebasePath = System.currentTimeMillis() + "image";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        Bitmap bitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 450, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        String childPath = "feed/";
        final StorageReference pictureReference = storageReference.child(childPath + firebasePath);
        StorageTask<UploadTask.TaskSnapshot> uploadTask;
        uploadTask = pictureReference.putStream(new ByteArrayInputStream(out.toByteArray()));
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                long bytesTransferred = snapshot.getBytesTransferred();
                long totalBytes = snapshot.getTotalByteCount();
                int progress = (int) ((bytesTransferred / totalBytes) * 100);
                result.onProgress(progress);
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    result.onStartedUpload();
                    Log.i(UPLOAD_INFO, "started upload of image bitmap");
                }
                else {
                    if (task.getException() != null) {
                        Log.e(UPLOAD_ERROR, "Error while uploading image bitmap");
                        result.onError("Failed to upload image, try again later");
                        throw task.getException();
                    }
                }

                return pictureReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    if (uri != null) {
                        String sUri = uri.toString();
                        result.onPathReady(sUri);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(UPLOAD_ERROR,"uploading error");
                e.printStackTrace();
                result.onError("Failed to upload image, try again later");
            }
        });
    }

    public void uploadImage(String photoPath) {
        String firebasePath = System.currentTimeMillis() + "image";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference pictureReference = storageReference.child("feed/" + firebasePath);
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 450, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        StorageTask<UploadTask.TaskSnapshot> uploadTask;
        uploadTask = pictureReference.putStream(new ByteArrayInputStream(out.toByteArray()));
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                long bytesTransferred = snapshot.getBytesTransferred();
                long totalBytes = snapshot.getTotalByteCount();
                int progress = (int) ((bytesTransferred / totalBytes) * 100);
                result.onProgress(progress);
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                {
                    result.onStartedUpload();
                    Log.i(UPLOAD_INFO, "started upload of image bitmap");
                }
                else {
                    if (task.getException() != null){
                        Log.e(UPLOAD_ERROR,"Error while uploading image bitmap");
                        result.onError("Failed to upload image, try again later");
                        throw task.getException();
                    }
                }

                return pictureReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    if (uri != null) {
                        String sUri = uri.toString();
                        result.onPathReady(sUri);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(UPLOAD_ERROR,"uploading error");
                e.printStackTrace();
                result.onError("Failed to upload image, try again later");
            }
        });
    }

    public void uploadProfileImage(Bitmap imageBitmap)
    {
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final String path = "users/" + uid;
            String firebasePath = System.currentTimeMillis() + "image";
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            Bitmap bitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 450, false);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            String childPath = "feed/";
            final StorageReference pictureReference = storageReference.child(childPath + firebasePath);
            StorageTask<UploadTask.TaskSnapshot> uploadTask;
            uploadTask = pictureReference.putStream(new ByteArrayInputStream(out.toByteArray()));
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (task.isSuccessful()) {
                        Log.i(UPLOAD_INFO, "started upload of image bitmap");
                    }
                    else {
                        if (task.getException() != null) {
                            Log.e(UPLOAD_ERROR, "Error while uploading image bitmap");
                            throw task.getException();
                        }
                    }
                    return pictureReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        if (uri != null) {
                            String sUri = uri.toString();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference(path);
                            HashMap<String, Object> pictureMap = new HashMap<>();
                            pictureMap.put("pictureLink", sUri);
                            reference.updateChildren(pictureMap);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(UPLOAD_ERROR,"uploading error");
                    e.printStackTrace();
                }
            });
        }
    }

    public void uploadFile(String filePath)
    {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String firebasePath = System.currentTimeMillis() + "recording";
        StorageReference fileReference = storageReference.child("feed/" + firebasePath);
        StorageTask<UploadTask.TaskSnapshot> uploadTask;
        uploadTask = fileReference.putFile(Uri.parse(filePath));
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                {
                    result.onStartedUpload();
                    Log.i(UPLOAD_INFO, "started upload of image bitmap");
                }
                else {
                    if (task.getException() != null) {
                        Log.e(UPLOAD_ERROR,"Error while uploading image bitmap");
                        result.onError("Failed to upload image, try again later");
                        throw task.getException();
                    }
                }

                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    if (uri != null) {
                        String sUri = uri.toString();
                        result.onPathReady(sUri);
                    }
                }
            }
        });
    }
}
