package com.example.woofmeow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Adapters.ImageAdapter;

@SuppressWarnings({"Convert2Lambda", "AnonymousHasLambdaAlternative"})
public class BackgroundImageActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> backgroundImage;
    private final String backgroundImageKey = "backgroundImage";
    private  List<Bitmap>bitmapList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_preference);
        GridView gridView = findViewById(R.id.grid_view);
        ImageAdapter adapter = new ImageAdapter();
        int[] backgrounds = {
                R.drawable.abstract_sun,
                R.drawable.flowers,
                R.drawable.flowers_in_blue,
                R.drawable.kiwi,
                R.drawable.night_sky,
                R.drawable.puffs,
                R.drawable.tower_in_the_woods,
        };
        bitmapList = new ArrayList<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<List<Bitmap>>bitmapCallable = new Callable<List<Bitmap>>() {
            @Override
            public List<Bitmap> call() {
                List<Bitmap>bitmapList = new ArrayList<>();
                for (int background : backgrounds) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), background);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 450, 540, false);
                    bitmapList.add(bitmap);
                }
                return bitmapList;
            }
        };
        Thread thread = new Thread(){
            @Override
            public void run() {
                Future<List<Bitmap>>bitmapListFuture = executorService.submit(bitmapCallable);
                while (!bitmapListFuture.isDone())
                {
                    /*
                     * waiting for the picture to load
                     * since get method blocks and makes the app freeze until the image is loaded
                     * noticeable with multiple images
                     * */
                }
                try{
                    bitmapList = bitmapListFuture.get();
                    Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setBackgroundsList(bitmapList);
                        }
                    });
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    executorService.shutdown();
                }
            }
        };
        thread.setName("loadBackgroundsThread");
        thread.start();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelected(position);
                if (position >= bitmapList.size())
                {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("image/*")
                        .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    backgroundImage.launch(intent);
                }
                else
                {
                    SharedPreferences sharedPreferences = getSharedPreferences("background", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(backgroundImageKey + " normal", backgrounds[position]);
                    editor.apply();
                }

                view.setSelected(true);
//                adapter.setSelectedPosition(position);
                Toast.makeText(BackgroundImageActivity.this, "new background selected", Toast.LENGTH_SHORT).show();
            }
        });
        backgroundImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData()!=null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            SharedPreferences sharedPreferences = getSharedPreferences("background", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(backgroundImageKey, result.getData().getData().toString());
                            adapter.setGalleryImage(result.getData().getData());
                            editor.remove(backgroundImageKey + " normal");
                            editor.apply();
                        }
                    }
                }
            }
        });
    }
}
