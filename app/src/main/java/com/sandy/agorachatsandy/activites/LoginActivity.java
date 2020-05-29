package com.sandy.agorachatsandy.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.sandy.agorachatsandy.R;
import com.sandy.agorachatsandy.model.User;
import com.sandy.agorachatsandy.rtm.AGApplication;
import com.sandy.agorachatsandy.rtm.ChatManager;
import com.sandy.agorachatsandy.utils.MessageUtil;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.RtmClient;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    SignInButton signInButton;
    Button signOutButton;
    TextView statusTextView;
    GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount acct;
    private RtmClient mRtmClient;
    private ChatManager mChatManager;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        statusTextView = findViewById(R.id.status_textview);
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(this);
        mChatManager = AGApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.signOutButton:
                signOut();
                break;
        }
    }

    private void signIn() {
        Intent sianInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(sianInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            acct = result.getSignInAccount();
            statusTextView.setText("Hello, " + acct.getDisplayName() + " with id: " + acct.getId());

            final User user = new User(acct.getId());
            user.setFireDisplayName(acct.getDisplayName());

            mRtmClient.login(null, user.getFireDisplayName(), new io.agora.rtm.ResultCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LoginActivity.this, ChannelSelectionActivity.class);
                            intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, user);
                            startActivity(intent);
                        }
                    });
                }

                @Override
                public void onFailure(final ErrorInfo errorInfo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_failed) + " " + errorInfo.getErrorDescription(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }else {
            Toast.makeText(this, "Sign in failed" + result.getStatus(), Toast.LENGTH_SHORT).show();
            //handle failure
        }
    }
//63:FE:42:DD:EA:0E:42:DF:52:AE:6F:96:60:6C:69:99:64:2E:FF:0B

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        statusTextView.setText("Connection Failed: " + connectionResult);
    }

    public void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.equals(Status.RESULT_SUCCESS)) {
                    statusTextView.setText("Signed Out successfully");
                }else {
                    statusTextView.setText("Signout Failed. " + status.getStatusMessage());
                }
            }
        });

        mRtmClient.logout(null);
        MessageUtil.cleanMessageListBeanList();
    }
}
