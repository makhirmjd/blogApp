package com.freelancer_jprogrammer.blogapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.freelancer_jprogrammer.blogapp.R;
import com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText emailField;
    private EditText passwordField;

    private Button signupButton;




    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        emailField = (EditText) findViewById( R.id.emailField );
        passwordField = (EditText) findViewById( R.id.passwordField );

        signupButton = (Button) findViewById( R.id.signupButton );
        signupButton.setOnClickListener( this );

        progressDialog = new ProgressDialog( this );
        progressDialog.setMessage( "Creating User....." );
        progressDialog.setCanceledOnTouchOutside( false );
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent( this, MainActivity.class );
        startActivity( intent );
        finish();
    }

    @Override
    public void onClick(View view) {
        switch ( view.getId() )
        {
            case R.id.signupButton:
                if( validateFields().equals( "" ) )
                {
                    startRegister();
                }
                break;
        }
    }

    private void startRegister() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        progressDialog.show();
        FirebaseUtils.getFirebaseAuth().createUserWithEmailAndPassword( email, password )
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull final Task<AuthResult> task) {
                                if( task.isSuccessful() )
                                {
                                    Intent intent = new Intent( RegisterActivity.this, AuthenticationActivity.class );
                                    startActivity( intent );
                                    finish();
                                    /*if( tempFileUrl != null )
                                    {
                                        StorageReference filePath = FirebaseUtils.getProfileImageReference();
                                        filePath.putFile( tempFileUrl )
                                                .addOnSuccessListener(
                                                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                progressDialog.dismiss();
                                                                String userID = FirebaseUtils.getFirebaseAuth()
                                                                        .getCurrentUser().getUid();
                                                                DatabaseReference currentUserReference =
                                                                        FirebaseUtils.getCurrentUserReference( userID );
                                                                currentUserReference.child( "name" ).setValue( name );
                                                                currentUserReference.child( "image" ).setValue( taskSnapshot.getDownloadUrl() );
                                                                Toast.makeText( RegisterActivity.this,
                                                                        "Sign up Successful!", Toast.LENGTH_SHORT ).show();
                                                                Intent intent = new Intent( RegisterActivity.this, MainActivity.class );
                                                                startActivity( intent );
                                                            }
                                                        }
                                                )
                                                .addOnFailureListener(
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText( RegisterActivity.this,
                                                                        "Sign up Failed!", Toast.LENGTH_SHORT ).show();
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                );
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        String userID = FirebaseUtils.getFirebaseAuth()
                                                .getCurrentUser().getUid();
                                        DatabaseReference currentUserReference =
                                                FirebaseUtils.getCurrentUserReference( userID );
                                        currentUserReference.child( "name" ).setValue( name );
                                        currentUserReference.child( "image" ).setValue( "default" );
                                        Toast.makeText( RegisterActivity.this,
                                                "Sign up Successful!", Toast.LENGTH_SHORT ).show();
                                        Intent intent = new Intent( RegisterActivity.this, MainActivity.class );
                                        startActivity( intent );
                                        finish();

                                    }*/
                                }
                                else
                                {
                                    Toast.makeText( RegisterActivity.this,
                                            "Sign up Failed!", Toast.LENGTH_SHORT ).show();
                                }
                            }
                        }
                );
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
}
