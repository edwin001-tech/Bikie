package com.example.bikie;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_LOGIN_USER = 9001;
    private static final String TAG = "LoginActivity" ;
    EditText etLoginEmail, etLoginPassword;
    Button btnLogin;
    SignInButton loginWithGoogle;
    GoogleSignInClient loginClient;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        etLoginEmail = findViewById(R.id.loginEmailAddress);
        etLoginPassword = findViewById(R.id.loginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginWithGoogle = findViewById(R.id.btnGoogleLogin);

        loginWithGoogle.setOnClickListener(v -> loginInWithGoogle());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        loginClient = GoogleSignIn.getClient(this,gso);

        btnLogin.setOnClickListener(v -> signInUser());


        }

    private void signInUser() {
        final String loginEmail = etLoginEmail.getText().toString().trim();
        final String loginPasscode = etLoginPassword.getText().toString().trim();
        if (TextUtils.isEmpty(loginEmail)){
            etLoginEmail.setError("email address required");
            etLoginEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()){
            etLoginEmail.setError("write valid email address");
            etLoginEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(loginPasscode)){
            etLoginPassword.setError("password required");
            etLoginPassword.requestFocus();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(loginEmail,loginPasscode)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()){
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

    private void loginInWithGoogle() {
        Intent loginIntent = loginClient.getSignInIntent();
        startActivityForResult(loginIntent, RC_LOGIN_USER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_LOGIN_USER){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleLoginResult(task);
        }
    }

    private void handleLoginResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
            Log.d(TAG, "firebaseWithGoogle:" +googleSignInAccount.getId());
            firebaseAuthWithGoogle(googleSignInAccount.getIdToken());
        }
        catch (ApiException exc){
            Log.w((String) TAG, "signInResult:failed code=" + exc.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        Log.d((String) TAG, "signInWithCredential success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else{
                        Log.w((String) TAG, "signInWithCredential failure");
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        updateUI(firebaseUser);
    }
}
