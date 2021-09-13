package com.example.woofmeow;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import Fragments.SignFragment;
import NormalObjects.Network2;
import NormalObjects.NetworkChange;

@SuppressWarnings("Convert2Lambda")
public class FirstPageActivity extends AppCompatActivity implements SignFragment.Sign {

    private String SignUp = "signUp";
    private String SignIn = "signIn";
    private FirebaseAuth auth;
    private Button signUpBtn, signInBtn;
    private LinearLayout showCaseLayout;
    private TextView orTv;
    private boolean connectedToInternet = false;
    private Network2 network2;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_page);
        auth = FirebaseAuth.getInstance();
        CheckForConnection();

        //Animation animation = AnimationUtils.loadAnimation(this,R.anim.rotate_move);
        Animation inLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left_slow);
        Animation outRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        //Animation inRight = AnimationUtils.makeInAnimation(this,false);
        Animation outLeft = AnimationUtils.makeOutAnimation(this, false);

        Animation inRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);


        signUpBtn = findViewById(R.id.SignUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectedToInternet)
                    StartSignFragment(SignUp);
                else
                    Toast.makeText(FirstPageActivity.this, "In order to signUp, you require an active internet connection. No such connection was detected", Toast.LENGTH_SHORT).show();
            }
        });

        signInBtn = findViewById(R.id.SignInBtn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectedToInternet)
                    StartSignFragment(SignIn);
                else
                    Toast.makeText(FirstPageActivity.this, "In order to signIn, you require an active internet connection. No such connection was detected", Toast.LENGTH_SHORT).show();
            }
        });

        signInBtn.startAnimation(inRight);
        signUpBtn.startAnimation(inLeft);

        showCaseLayout = findViewById(R.id.showCaseLayout);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        showCaseLayout.setAnimation(slideUp);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        orTv = findViewById(R.id.orTv);
        orTv.startAnimation(fadeIn);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FirstPageActivity.this);
        boolean start = preferences.getBoolean("onRestart", true);
        if (start)
            if (user != null) {
                startActivity(new Intent(FirstPageActivity.this, MainActivity.class));
                finish();
            }
    }

    private void CheckForConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI).addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN);
        NetworkRequest request = builder.build();
        network2 = Network2.getInstance();
        network2.setListener(new NetworkChange() {
            @Override
            public void onNetwork() {
                connectedToInternet = true;
            }

            @Override
            public void onNoNetwork() {
                connectedToInternet = false;
            }

            @Override
            public void onNetworkLost() {
                connectedToInternet = false;
            }

            @Override
            public void onChangedNetworkType() {
                connectedToInternet = true;
            }
        });
        if (connectivityManager != null)
            connectivityManager.registerNetworkCallback(request, network2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager !=  null && network2 != null)
            connectivityManager.unregisterNetworkCallback(network2);
    }

    @Override
    public void onSign(String sign, String email, String password) {
        if (sign.equals(SignUp)) {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(FirstPageActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Animation outRight = AnimationUtils.loadAnimation(FirstPageActivity.this, R.anim.slide_out_right);
                                Animation slideDown = AnimationUtils.loadAnimation(FirstPageActivity.this, R.anim.slide_down);
                                Animation slideUp = AnimationUtils.loadAnimation(FirstPageActivity.this, R.anim.slide_up);
                                Animation fadeOut = AnimationUtils.loadAnimation(FirstPageActivity.this, android.R.anim.fade_out);
                                outRight.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        String currentUserUID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                                        Intent intent = new Intent(FirstPageActivity.this, UserCreationActivity.class);
                                        intent.putExtra("UID",currentUserUID);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                orTv.startAnimation(fadeOut);
                                showCaseLayout.setAnimation(slideUp);
                                signUpBtn.startAnimation(outRight);
                            } else {
                                if (password.length() < 6)
                                    Toast.makeText(FirstPageActivity.this, "password must be 6 characters or more", Toast.LENGTH_SHORT).show();
                                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                                    Toast.makeText(FirstPageActivity.this, "please provide a valid email address", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(FirstPageActivity.this, "Creating account has failed, try again later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if (sign.equals(SignIn)) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Animation outLeft = AnimationUtils.loadAnimation(FirstPageActivity.this, R.anim.slide_out_left);
                                Animation slideDown = AnimationUtils.loadAnimation(FirstPageActivity.this, R.anim.slide_down);
                                Animation fadeOut = AnimationUtils.loadAnimation(FirstPageActivity.this, android.R.anim.fade_out);
                                Animation outRight = AnimationUtils.loadAnimation(FirstPageActivity.this, R.anim.slide_out_right);
                                outLeft.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        startActivity(new Intent(FirstPageActivity.this, MainActivity.class));
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                orTv.startAnimation(fadeOut);
                                showCaseLayout.setAnimation(slideDown);
                                signInBtn.startAnimation(outLeft);
                                signUpBtn.startAnimation(outRight);
                                SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("currentUser", Objects.requireNonNull(auth.getCurrentUser()).getUid());
                                editor.apply();
                            } else
                                Toast.makeText(FirstPageActivity.this, "SignIn has failed, verify that the email or password is correct or try again later", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onCancel() {

    }

    private void StartSignFragment(String sign) {
        SignFragment signFragment = new SignFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Sign", sign);
        signFragment.setArguments(bundle);
        signFragment.show(getSupportFragmentManager(), sign);
    }
}
