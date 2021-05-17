package View;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.woofmeow.R;

import Controller.Controller;
import Controller.ControllerInterface;

public class View extends AppCompatActivity implements ViewInterface{

    ControllerInterface controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_layout);

        controller = new Controller(this);

        Button btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                controller.onPassThrough("on","off");
            }
        });
        Button btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                controller.onPassThrough("off","on");
            }
        });
        TextView txt = findViewById(R.id.txt);
    }

    @Override
    public void onUserAction(String b1,String b2) {
        Toast.makeText(this, "onUserAction", Toast.LENGTH_SHORT).show();
    }
}
