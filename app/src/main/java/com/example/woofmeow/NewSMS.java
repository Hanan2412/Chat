package com.example.woofmeow;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import NormalObjects.User;
//receives info if sms was sent and/or arrived
@SuppressWarnings("Convert2Lambda")
public class NewSMS extends AppCompatActivity {

    private final int SEND_CONTACT = 1;
    private ActivityResultLauncher<Intent> openContacts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_sms_conversation);
        ImageButton startConversationBtn = findViewById(R.id.startConversationBtn);
        TextInputEditText phoneNumber = findViewById(R.id.phoneNumber);
        ImageButton openContactsBtn = findViewById(R.id.openContactsBtn);
        ImageButton goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        startConversationBtn.setEnabled(false);
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (Patterns.PHONE.matcher(editable.toString()).matches()) {
                    startConversationBtn.setEnabled(true);
                    startConversationBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
                else
                {
                    startConversationBtn.setEnabled(false);
                    startConversationBtn.setImageResource(R.drawable.ic_baseline_close_24);
                }
            }
        });

        startConversationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneNumber.getText() != null) {
                    Intent intent = prepareIntent(phoneNumber.getText().toString(),"");
                    startActivity(intent);
                    finish();
                }
            }
        });
        openContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(Intent.ACTION_PICK);
                contactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                openContacts.launch(contactIntent);
            }
        });
        openContacts = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                   Intent data = result.getData();
                   if (data != null) {
                       Uri contactUri = data.getData();
                       String[] projections = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                               ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                       if (contactUri != null) {
                           Cursor cursor = getContentResolver().query(contactUri, projections, null, null, null);
                           if (cursor != null && cursor.moveToFirst()) {
                               int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                               String contactNumber = cursor.getString(numberIndex);
                               String contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                               Intent intent = prepareIntent(contactNumber, contactName);
                               cursor.close();
                               startActivity(intent);
                           }
                       }
                   }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEND_CONTACT && resultCode == RESULT_OK) {

            if (data != null) {
                Uri contactUri = data.getData();
                String[] projections = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                if (contactUri != null) {
                    Cursor cursor = getContentResolver().query(contactUri, projections, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String contactNumber = cursor.getString(numberIndex);
                        String contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        Intent intent = prepareIntent(contactNumber, contactName);
                        cursor.close();
                        startActivity(intent);
                    }
                }
            }
        }
    }

    private Intent prepareIntent(String phoneNumber, String name) {
        Intent intent = new Intent(NewSMS.this, ConversationActivity2.class);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("conversationID", createConversationID());
        User user = new User();
        if (!name.equals(""))
        {
            String[] fullName = name.split(" ");
            user.setName(fullName[0]);
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < fullName.length; i++)
                builder.append(fullName[i]);
            user.setLastName(builder.toString());
        }
        else {
            user.setName(phoneNumber);
            user.setLastName("");
        }
        user.setPhoneNumber(phoneNumber);
        user.setUserUID(UUID.randomUUID().toString());
        List<User> recipients = new ArrayList<>();
        recipients.add(user);
        intent.putExtra("recipients", (ArrayList<User>) recipients);
        return intent;
    }

    private String createConversationID() {
        return "S_" + System.currentTimeMillis();
    }
}
