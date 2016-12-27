package com.freelancer_jprogrammer.blogapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.freelancer_jprogrammer.blogapp.R;
import com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogSingleActivity extends AppCompatActivity implements View.OnClickListener {

    private String postKey;
    private DatabaseReference blogReference;

    private ImageView postImage;
    private TextView postTitle;
    private TextView postDescription;

    private Button removePostButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);
        //FirebaseUtils.enableOfflineSync( this );

        postKey = getIntent().getStringExtra( "BLOG_ID" );

        postImage = (ImageView) findViewById( R.id.postImage );
        postTitle = (TextView) findViewById( R.id.postTitle );
        postDescription = (TextView) findViewById( R.id.postDescription );

        removePostButton = (Button) findViewById( R.id.removePostButton );
        removePostButton.setOnClickListener( this );

        blogReference = FirebaseUtils.getBlogReference();

        blogReference.child( postKey ).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String postTitleText = (String)dataSnapshot.child( "title" ).getValue();
                        String postDescriptionText = (String)dataSnapshot.child( "description" ).getValue();
                        String postImageText = (String)dataSnapshot.child( "image" ).getValue();
                        String userIDText = (String)dataSnapshot.child( "userID" ).getValue();

                        postTitle.setText( postTitleText );
                        postDescription.setText( postDescriptionText );
                        Picasso.with( BlogSingleActivity.this ).load( postImageText ).into( postImage );

                        if( FirebaseUtils.getFirebaseAuth().getCurrentUser().getUid().equals( userIDText ) )
                        {
                            removePostButton.setVisibility(View.VISIBLE );
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    public void onClick(View view) {
        switch( view.getId() )
        {
            case R.id.removePostButton:
                DatabaseReference blogReference = FirebaseUtils.getBlogReference();
                blogReference.child( postKey ).removeValue();
                finish();
                break;
        }
    }
}
