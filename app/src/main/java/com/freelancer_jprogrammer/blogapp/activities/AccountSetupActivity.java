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
import android.widget.TextView;
import android.widget.Toast;

import com.freelancer_jprogrammer.blogapp.R;
import com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

public class AccountSetupActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText nameField;
    private EditText displayNameField;

    private TextView imageText;

    public static final int GALLERY_REQUEST_CODE = 270;

    private ImageButton selectBtn;
    private Button updateButton;

    private File tempOutputFile;
    private Uri tempFileUrl;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        nameField = (EditText) findViewById( R.id.nameField );
        displayNameField = (EditText) findViewById( R.id.displayNameField );

        imageText = (TextView) findViewById( R.id.imageText );

        selectBtn = (ImageButton) findViewById( R.id.selectBtn );
        selectBtn.setOnClickListener(this);

        updateButton = (Button) findViewById( R.id.updateButton );
        updateButton.setOnClickListener( this );

        progressDialog = new ProgressDialog( this );
        progressDialog.setMessage( "Setting Up Account....." );
        progressDialog.setCanceledOnTouchOutside( false );


    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.selectBtn:
                tempOutputFile = new File(getExternalCacheDir(), "image" +
                        String.format( "%06d", new Random().nextInt( 100000 ) ) + ".jpg");
                Intent galleryIntent = new Intent( Intent.ACTION_PICK );
                galleryIntent.setType( "image/*" );
                Intent chooser = Intent.createChooser(galleryIntent, "Chooser Image");
                startActivityForResult( chooser, GALLERY_REQUEST_CODE );
                break;
            case R.id.updateButton:
                if( validateFields().equals( "" ) )
                {
                    startAccountSetup();
                }
                break;
        }
    }

    private void startAccountSetup()
    {
        final String name = nameField.getText().toString().trim();
        final String displayName = displayNameField.getText().toString().trim();
        progressDialog.show();
        String userID = FirebaseUtils.getFirebaseAuth().getCurrentUser().getUid();
        final DatabaseReference ref = FirebaseUtils.getCurrentUserReference( userID );
        if( tempFileUrl != null )
        {
            StorageReference profileRef = FirebaseUtils.getProfileImageReference( tempFileUrl.getLastPathSegment() );
            profileRef.putFile( tempFileUrl )
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    ref.child( "name" ).setValue( name );
                                    ref.child( "displayName" ).setValue( displayName );
                                    ref.child( "photoUrl" ).setValue( taskSnapshot.getDownloadUrl().toString() );
                                    progressDialog.dismiss();
                                    FirebaseUtils.login();
                                    FirebaseUtils.checkLogin( AccountSetupActivity.this );
                                }
                            }
                    )
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText( AccountSetupActivity.this, "Failed To Setup Account!", Toast.LENGTH_SHORT ).show();
                                }
                            }
                    );
        }
        else
        {
            ref.child( "name" ).setValue( name );
            ref.child( "displayName" ).setValue( displayName );
            ref.child( "photoUrl" ).setValue( "default" );
            progressDialog.dismiss();
            FirebaseUtils.login();
            FirebaseUtils.checkLogin( AccountSetupActivity.this );
        }
    }

    private String validateFields()
    {
        String name = nameField.getText().toString().trim();
        String displayName = displayNameField.getText().toString().trim();
        String message = "Empty Fields Not Allowed!";
        if( name.equals( "" ) )
        {
            nameField.setError( message );
            return message;
        }

        if( displayName.equals( "" ) )
        {
            displayNameField.setError( message );
            return message;
        }

        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode != RESULT_OK )
        {
            tempOutputFile.delete();
            return;
        }
        switch ( requestCode )
        {
            case GALLERY_REQUEST_CODE:
                Uri imageUri = data.getData();
                tempFileUrl = Uri.fromFile(tempOutputFile);
                Crop.of( imageUri, tempFileUrl ).asSquare().start( this );
                break;
            case Crop.REQUEST_CROP:
                Bitmap bmp = null;
                try {
                    bmp = BitmapFactory.
                            decodeStream(getContentResolver().openInputStream(Uri.fromFile(tempOutputFile)));
                    bmp = FirebaseUtils.getScaledBitmap( selectBtn.getWidth(), selectBtn.getHeight(), bmp );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if( bmp != null )
                {
                    selectBtn.setImageBitmap( bmp );
                    imageText.setVisibility( View.GONE );
                }
                break;
            default:
                tempFileUrl = null;
                break;
        }
    }
}
