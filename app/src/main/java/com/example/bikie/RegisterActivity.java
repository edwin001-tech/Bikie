package com.example.bikie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterActivity extends AppCompatActivity {
    private static final Object TAG = "RegisterActivity.class";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    EditText userName, emailAddress, accPassword;
    TextView loginLink;
    Button registerButton;
    SignInButton googleSignIn;
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        userName=findViewById(R.id.user_Name);
        emailAddress = findViewById(R.id.email_address);
        accPassword = findViewById(R.id._passcode);
        //textview
        loginLink = findViewById(R.id.tvLinkToSignIn);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new  Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        //buttons
        registerButton = findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(v -> registerUser());

        googleSignIn = findViewById(R.id.btnGoogleSignIn);
        googleSignIn.setSize(SignInButton.SIZE_STANDARD);
        googleSignIn.setOnClickListener(v -> googleSignIn());
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);


    }

    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d((String) TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        }
        catch (ApiException exception){
            Log.w((String) TAG, "signInResult:failed code=" + exception.getStatusCode());
            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken,null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d((String) TAG, "signInWithCredential success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else{
                            Log.w((String) TAG, "signInWithCredential failure");
                            updateUI(null);
                        }
                    }
                });

    }

    private void registerUser() {
        final String name = userName.getText().toString().trim();
        final String email = emailAddress.getText().toString().trim();
        final String password = accPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)){
            emailAddress.setError("name field can't be empty");
            emailAddress.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)){
            emailAddress.setError("email address required");
            emailAddress.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailAddress.setError("write valid email address");
            emailAddress.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)){
            accPassword.setError("password required");
            accPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        Log.d((String) TAG, "user account created successfully");
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        updateUI(firebaseUser);
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser!=null){
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }
    }
}