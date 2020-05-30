package com.sandy.agorachatsandy.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sandy.agorachatsandy.R;
import com.sandy.agorachatsandy.model.AgoraUser;
import com.sandy.agorachatsandy.rtm.AGApplication;
import com.sandy.agorachatsandy.rtm.AgoraChatManager;
import com.sandy.agorachatsandy.utils.MessageUtil;

import java.util.Random;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.RtmClient;

public class AgoraLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private RtmClient mRtmClient;
    public static Random RANDOM = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agora_activity_login);

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(this);
        AgoraChatManager mChatManager = AGApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.signOutButton:
                signOut();
                break;
        }
    }

    private void signIn() {
        final AgoraUser user = new AgoraUser("100" + RANDOM.nextInt(90000));
        user.setFireDisplayName("Sanidhya_" + RANDOM.nextInt(100000));
        TextView textView = findViewById(R.id.status_textview);
        textView.setText(user.getFireDisplayName());

        mRtmClient.login(null, user.getFireDisplayName(), new io.agora.rtm.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(AgoraLoginActivity.this, AgoraVideoCallActivity.class);
                        intent.putExtra("User", user);
                        intent.putExtra("Channel", "MbChannel1");
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onFailure(final ErrorInfo errorInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AgoraLoginActivity.this, getString(R.string.login_failed) + " " + errorInfo.getErrorDescription(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }


    public void signOut() {
        TextView textView = findViewById(R.id.status_textview);
        textView.setText("");
        mRtmClient.logout(null);
        MessageUtil.cleanMessageListBeanList();
    }
}