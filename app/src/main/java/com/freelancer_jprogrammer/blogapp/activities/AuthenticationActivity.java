package com.freelancer_jprogrammer.blogapp.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.freelancer_jprogrammer.blogapp.R;
import com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.identity.intents.AddressConstants;

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 2;

    private Button registerButton;
    private Button emailLoginButton;
    private Button googleLoginButton;

    private EditText emailField;
    private EditText passwordField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        registerButton = (Button) findViewById( R.id.registerButton );
        registerButton.setOnClickListener( this );

        emailLoginButton = (Button) findViewById( R.id.emailLoginButton );
        emailLoginButton.setOnClickListener( this );

        googleLoginButton = (Button) findViewById( R.id.googleLoginButton );
        googleLoginButton.setOnClickListener( this );

        emailField = (EditText) findViewById( R.id.emailField );
        passwordField = (EditText) findViewById( R.id.passwordField );
    }

    @Override
    public void onClick(View view) {
        switch ( view.getId() )
        {
            case R.id.registerButton:
                Intent intent = new Intent( this, RegisterActivity.class );
                startActivity( intent );
                finish();
                break;
            case R.id.emailLoginButton:
                if( validateFields().equals( "" ) )
                {
                    String email = emailField.getText().toString().trim();
                    String password = passwordField.getText().toString().trim();
                    FirebaseUtils.signInWithEmailAndPassword( email, password, AuthenticationActivity.this );
                }
                break;
            case R.id.googleLoginButton:
                googleSignIn();
                break;
        }
    }

    private String validateFields()
    {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String message = "Empty Fields Not Allowed!";

        if( email.equals( "" ) )
        {
            emailField.setError( message );
            return message;
        }
        else if(!FirebaseUtils.validateEmailAddress( email ))
        {
            message = "Invalid Email Address";
            emailField.setError( message );
            return message;
        }

        if( password.equals( "" ) )
        {
            passwordField.setError( message );
            return message;
        }

        return "";
    }

    private void googleSignIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(FirebaseUtils.getGogleApiClient( this ));
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                FirebaseUtils.firebaseAuthWithGoogle(account, this);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }
}
