package com.freelancer_jprogrammer.blogapp.adapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.freelancer_jprogrammer.blogapp.R;
import com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class BlogViewHolder extends RecyclerView.ViewHolder {

    private View view;

    private TextView postTitle;
    private TextView postDescription;
    private ImageView postImage;
    private TextView postUserName;

    private ImageButton likeButton;
    private ProgressBar imageProgressBar;

    private DatabaseReference databaseLikes;

    public BlogViewHolder(View itemView) {
        super(itemView);
        view = itemView;

        postTitle = (TextView) view.findViewById( R.id.postTitle );
        postDescription = (TextView) view.findViewById( R.id.postDescription );
        postImage = (ImageView) view.findViewById( R.id.postImage );
        postUserName = (TextView) view.findViewById( R.id.postUserName );

        imageProgressBar = (ProgressBar) view.findViewById( R.id.imageProgressBar );

        likeButton = (ImageButton) view.findViewById( R.id.likeButton );

        databaseLikes = FirebaseUtils.getLikesReference();
    }

    public void setTitle( String title )
    {
        postTitle.setText( title );
    }

    public void setDescription( String description)
    {
        postDescription.setText( description );
    }

    public void setImage(final Context context, final String image )
    {
        FirebaseUtils.enableOfflineSync( context );
        //Picasso.with( context ).load( image ).into( target );

        Picasso.with( context ).load( image ).networkPolicy(NetworkPolicy.OFFLINE ).into(postImage,
                new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with( context ).load( image ).into( target );
                    }
                });
    }

    public void setUserName( String userName )
    {
        postUserName.setText( userName );
    }

    public View getView() {
        return view;
    }

    public ImageButton getLikeButton() {
        return likeButton;
    }

    public void setLikeButton(final String postKey) {
        databaseLikes.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if( dataSnapshot.child( postKey ).hasChild( FirebaseUtils.getFirebaseAuth().getCurrentUser().getUid() ) )
                        {
                            likeButton.setImageResource( R.drawable.ic_action_like_red );
                        }
                        else
                        {
                            likeButton.setImageResource( R.drawable.ic_action_like_gray );
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            imageProgressBar.setVisibility( View.GONE );
            postImage.setImageBitmap( bitmap );
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            imageProgressBar.setVisibility( View.VISIBLE );
        }
    };
}