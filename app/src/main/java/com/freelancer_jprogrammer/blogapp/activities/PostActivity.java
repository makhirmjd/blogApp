package com.freelancer_jprogrammer.blogapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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

import static com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils.checkLogin;
import static com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils.getAuthStateListener;
import static com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils.getFirebaseAuth;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int GALLERY_REQUEST_CODE = 260;
    private ImageButton selectBtn;
    private Button submitBtn;
    private EditText titleField;
    private EditText descField;

    private TextView imageText;

    private File tempOutputFile;
    private Uri tempFileUrl;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        //FirebaseUtils.enableOfflineSync( this );

        titleField = (EditText) findViewById( R.id.titleField );
        descField = (EditText) findViewById( R.id.descField );

        imageText = (TextView) findViewById( R.id.imageText );

        submitBtn = (Button) findViewById( R.id.submitBtn );
        submitBtn.setOnClickListener( this );

        selectBtn = (ImageButton) findViewById( R.id.selectBtn );
        selectBtn.setOnClickListener(this);


        progressDialog = new ProgressDialog( this );
        progressDialog.setMessage( "Posting Message: Hold On....." );
        progressDialog.setCanceledOnTouchOutside( false );
    }

    @Override
    public void onClick(View view) {
        switch( view.getId() )
        {
            case R.id.selectBtn:
                tempOutputFile = new File(getExternalCacheDir(), "image" +
                        String.format( "%06d", new Random().nextInt( 100000 ) ) + ".jpg");
                Intent galleryIntent = new Intent( Intent.ACTION_PICK );
                galleryIntent.setType( "image/*" );
                Intent chooser = Intent.createChooser(galleryIntent, "Chooser Image");
                startActivityForResult( chooser, GALLERY_REQUEST_CODE );
                break;
            case R.id.submitBtn:
                startPosting();
                break;
        }
    }

    private void startPosting() {
        if( validateFields().equals( "" ) && tempFileUrl != null )
        {
            StorageReference filePath = FirebaseUtils.getImageReference( tempFileUrl.getLastPathSegment() );
            progressDialog.show();

            filePath.putFile( tempFileUrl )
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    progressDialog.dismiss();
                                    String title = titleField.getText().toString().trim();
                                    String description = descField.getText().toString().trim();
                                    FirebaseUtils.makeNewPost( PostActivity.this, title, description, taskSnapshot.getDownloadUrl().toString() );
                                }
                            }
                    )
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText( PostActivity.this, "Post Failed", Toast.LENGTH_SHORT ).show();
                                }
                            }
                    );
        }
    }

    private String validateFields()
    {
        String title = titleField.getText().toString().trim();
        String description = descField.getText().toString().trim();
        String message = "Empty Fields Not Allowed!";
        if( title.equals( "" ) )
        {
            titleField.setError( message );
            return message;
        }

        if( description.equals( "" ) )
        {
            descField.setError( message );
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


    @Override
    protected void onStart() {
        super.onStart();
        getFirebaseAuth().addAuthStateListener( getAuthStateListener( this ) );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if( getFirebaseAuth() != null )
        {
            getFirebaseAuth().removeAuthStateListener( getAuthStateListener( this ) );
        }
    }
}
