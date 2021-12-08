package com.example.woofmeow;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;

import NormalObjects.User;
//receives info if sms was sent and/or arrived
@SuppressWarnings("Convert2Lambda")
public class NewSMS extends AppCompatActivity {

    private final int SEND_CONTACT = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_sms_conversation);

        Button btn = findViewById(R.id.startConversation);
        TextInputEditText phoneNumber = findViewById(R.id.phoneNumber);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber.getText() != null) {
                    Intent intent = prepareIntent(phoneNumber.getText().toString(),"");
                    startActivity(intent);
                }
            }
        });
        Button contact = findViewById(R.id.contact);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(Intent.ACTION_PICK);
                contactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(contactIntent, SEND_CONTACT);
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
        Intent intent = new Intent(NewSMS.this, ConversationActivity.class);
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
        intent.putExtra("smsUser", user);
        return intent;
    }

    private String createConversationID() {
        return "S_" + System.currentTimeMillis();
    }
}
