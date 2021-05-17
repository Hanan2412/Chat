package com.example.woofmeow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import NormalObjects.Server;

public class UserCreationActivity extends AppCompatActivity {

    private Bitmap imageBitmap;
    private int WRITE_PERMISSION = 2;
    private int CAMERA_REQUEST = 3;
    private int GALLERY_REQUEST = 4;
    private  String photoPath;
    private Uri imageUri;
    private ImageView userImage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_creation_layout);
        final TextInputEditText firstName = findViewById(R.id.userName);
        final TextInputEditText lastName = findViewById(R.id.lastName);
        final TextInputEditText nickname = findViewById(R.id.nickname);
        userImage = findViewById(R.id.userPhoto);
        Button cameraBtn = findViewById(R.id.openCameraBtn);
        Button galleryBtn = findViewById(R.id.openGalleryBtn);
        Button continueBtn = findViewById(R.id.nextBtn);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = "",nick="",last="";
                if(firstName.getText()!=null)
                    name = firstName.getText().toString();
                if(lastName.getText()!=null)
                    last = lastName.getText().toString();
                if(nickname.getText()!=null)
                    nick = nickname.getText().toString();

                if(!name.equals("")&&!last.equals("")) {
                    createNewUser(name, last, nick,imageBitmap);
                    startActivity(new Intent(UserCreationActivity.this,MainActivity.class));
                }

            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCamera();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void createNewUser(String name, String lastName, String nickname, Bitmap userImage)
    {
        Server.createNewUser(name,lastName,nickname,userImage,UserCreationActivity.this);
    }

    private void requestCamera()
    {
        if(AskPermission())
            takePicture();
    }
    private boolean AskPermission()
    {
        int hasWritePermission = UserCreationActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(hasWritePermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION);
            return false;
        }
        else return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(UserCreationActivity.this, "permission is required to use the camera", Toast.LENGTH_SHORT).show();
            else
                takePicture();
        }
    }

    private void takePicture()
    {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = UserCreationActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName,".jpg",storageDir);
            photoPath = image.getAbsolutePath();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePictureIntent.resolveActivity(UserCreationActivity.this.getPackageManager())!=null)
            {
                File photoFile;
                photoFile = image;
                Uri photoURI = FileProvider.getUriForFile(UserCreationActivity.this,
                        "com.example.woofmeow.provider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);

                imageUri = photoURI;
                startActivityForResult(takePictureIntent,CAMERA_REQUEST);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent,"Select Picture to Upload"),GALLERY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            Drawable drawable = Drawable.createFromPath(photoPath);
            if(drawable!=null)
            {
                userImage.setImageDrawable(drawable);
                BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
                imageBitmap = bitmapDrawable.getBitmap();
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap,500,450,false);
            }
        }
        else if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
        {
            if(data!=null)
            {
                Uri uri = data.getData();
                if(uri!=null) {
                    userImage.setImageURI(uri);
                    imageBitmap = getImageBitmap(uri);
                    userImage.setImageBitmap(imageBitmap);
                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap,500,450,false);
                }

            }
        }
    }

    private Bitmap getImageBitmap(Uri uri)
    {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > 27) {
                ImageDecoder.Source source = ImageDecoder.createSource(UserCreationActivity.this.getContentResolver(), uri);
                image = ImageDecoder.decodeBitmap(source);
            }
            else
            {
                image = MediaStore.Images.Media.getBitmap(UserCreationActivity.this.getContentResolver(),uri);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }
}
