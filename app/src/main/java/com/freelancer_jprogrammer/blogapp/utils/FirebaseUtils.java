package com.freelancer_jprogrammer.blogapp.utils;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.freelancer_jprogrammer.blogapp.R;
import com.freelancer_jprogrammer.blogapp.activities.AccountSetupActivity;
import com.freelancer_jprogrammer.blogapp.activities.AuthenticationActivity;
import com.freelancer_jprogrammer.blogapp.activities.BlogSingleActivity;
import com.freelancer_jprogrammer.blogapp.activities.MainActivity;
import com.freelancer_jprogrammer.blogapp.adapter.BlogViewHolder;
import com.freelancer_jprogrammer.blogapp.model.Blog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Muhammad on 12/18/2016.
 */

public class FirebaseUtils {
    private static final String STORAGE_ROOT_DIRECTORY = "gs://blogapp-992dc.appspot.com";
    private static final String DATABASE_ROOT_DIRECTORY = "https://blogapp-992dc.firebaseio.com/";
    private static final String IMAGE_STORAGE_PATH = "blog_images/";
    private static final String PROFILE_IMAGES_STORAGE_PATH = "profile_images/";

    private static FirebaseDatabase firebaseDatabase;
    private static DatabaseReference databaseReference;
    private static StorageReference storageReference;
    private static FirebaseAuth firebaseAuth;
    private static FirebaseAuth.AuthStateListener authStateListener;
    private static FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter;

    private static Picasso built;

    private static GoogleSignInOptions googleSignInOptions;
    private static GoogleApiClient googleApiClient;

    private static boolean isLoggedIn;

    private static AppCompatActivity activity;

    public static FirebaseDatabase getFirebaseDatabase() {
        if( firebaseDatabase == null )
        {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled( true );
        }
        return firebaseDatabase;
    }

    public static DatabaseReference getDatabaseReference()
    {
        initFirebaseDatabase();
        if( databaseReference == null )
        {
            databaseReference = getFirebaseDatabase().getReferenceFromUrl( DATABASE_ROOT_DIRECTORY );
            databaseReference.keepSynced( true );
        }

        return databaseReference;
    }

    private static void initFirebaseDatabase()
    {
        getFirebaseDatabase();
    }

    public static StorageReference getStorageReference()
    {
        initFirebaseDatabase();
        if( storageReference == null )
        {
            storageReference = FirebaseStorage.getInstance().getReferenceFromUrl( STORAGE_ROOT_DIRECTORY );
        }

        return storageReference;
    }

    public static StorageReference getImageReference( String image )
    {
        return getStorageReference().child( IMAGE_STORAGE_PATH + image );
    }

    public static StorageReference getProfileImageReference( String imageName )
    {
        return getStorageReference().child( PROFILE_IMAGES_STORAGE_PATH + imageName );
    }

    public static DatabaseReference getBlogReference()
    {
        DatabaseReference blogReference = getDatabaseReference().child( "Blog" );
        blogReference.keepSynced( true );
        return blogReference;
    }

    public static DatabaseReference getUsersReference()
    {
        DatabaseReference databaseUsers = getDatabaseReference().child( "Users" );
        databaseUsers.keepSynced( true );
        return databaseUsers;
    }

    public static DatabaseReference getLikesReference()
    {
        DatabaseReference databaseLikes = getDatabaseReference().child( "Likes" );
        databaseLikes.keepSynced( true );
        return databaseLikes;
    }

    public static DatabaseReference getCurrentUserReference( String userID )
    {
        DatabaseReference currentUserReference = getUsersReference().child( userID );
        currentUserReference.keepSynced( true );
        return currentUserReference;
    }

    public static DatabaseReference getNewPostHandle()
    {
        return getBlogReference().push();
    }

    public static void makeNewPost( final Context context, final String title, final String description, final String imageUriPath )
    {
        final DatabaseReference newPost = getNewPostHandle();
        DatabaseReference currentUserReference = getCurrentUserReference( getFirebaseAuth().getCurrentUser().getUid() );
        currentUserReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newPost.child( "title" ).setValue( title );
                        newPost.child( "description" ).setValue( description );
                        newPost.child( "image" ).setValue( imageUriPath );
                        newPost.child( "userID" ).setValue( getFirebaseAuth().getCurrentUser().getUid() );
                        newPost.child( "userName" ).setValue( dataSnapshot.child( "name" ).getValue() )
                                .addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if( task.isSuccessful() )
                                                {
                                                    Toast.makeText( context, "Post Successful", Toast.LENGTH_SHORT ).show();
                                                    ((AppCompatActivity)context).finish();
                                                }
                                                else
                                                {
                                                    Toast.makeText( context, "Post Failed", Toast.LENGTH_SHORT ).show();
                                                }
                                            }
                                        }
                                );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    public static FirebaseAuth getFirebaseAuth()
    {
        initFirebaseDatabase();
        if( firebaseAuth == null )
        {
            firebaseAuth = FirebaseAuth.getInstance();
        }

        return firebaseAuth;
    }

    public static FirebaseAuth.AuthStateListener getAuthStateListener(final AppCompatActivity activity )
    {
        FirebaseUtils.activity = activity;
        if( authStateListener == null )
        {
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if( firebaseAuth.getCurrentUser() == null )
                    {
                        Toast.makeText( activity, "Auth is Null!", Toast.LENGTH_SHORT ).show();
                        logout();
                        checkLogin( FirebaseUtils.activity  );
                    }
                    else
                    {
                        Toast.makeText( activity, "Auth is Good!", Toast.LENGTH_SHORT ).show();
                    }
                }
            };
        }

        return authStateListener;
    }

    public static void signInWithEmailAndPassword(String email, String password, final Context context)
    {
        final ProgressDialog progressDialog = new ProgressDialog( context );
        progressDialog.setMessage( "Signing In....." );
        progressDialog.setCanceledOnTouchOutside( false );
        progressDialog.show();
        getFirebaseAuth().signInWithEmailAndPassword( email, password  )
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if( task.isSuccessful() )
                                {
                                    checkUserExists( context );
                                }
                                else
                                {
                                    Toast.makeText( context, "Failed To Login!", Toast.LENGTH_SHORT ).show();
                                }
                            }
                        }
                );
    }

    private static void checkUserExists(final Context context ) {

        final String userId = getFirebaseAuth().getCurrentUser().getUid();
        DatabaseReference usersRef = getCurrentUserReference( userId );
        usersRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if( dataSnapshot.hasChildren() )
                        {
                            System.out.println( "Has Child" );
                            try
                            {
                                login();
                                checkLogin( (AppCompatActivity) context );
                            }
                            catch ( Exception e )
                            {
                            }
                        }
                        else
                        {
                            System.out.println( "Has No Child" );
                            Toast.makeText( context, "You need to setup your account", Toast.LENGTH_SHORT ).show();
                            Intent intent = new Intent( context, AccountSetupActivity.class );
                            context.startActivity( intent );
                            ((AppCompatActivity)context).finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    public static void login()
    {
        isLoggedIn = true;
    }

    public static void logout()
    {
        isLoggedIn = false;
    }

    public static void checkLogin(AppCompatActivity activity )
    {
        if( isLoggedIn )
        {
            Intent intent = new Intent( activity, MainActivity.class);
            activity.startActivity( intent );
            activity.finish();
        }
        else
        {
            Intent intent = new Intent( activity, AuthenticationActivity.class);
            activity.startActivity( intent );
            activity.finish();
        }
    }

    public static void redirect(AppCompatActivity activity )
    {
        if( !isLoggedIn )
        {
            Intent intent = new Intent( activity, AuthenticationActivity.class);
            activity.startActivity( intent );
            activity.finish();
        }
    }

    public static boolean validateEmailAddress( String email )
    {
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        return  matcher.matches();
    }

    public static Bitmap getScaledBitmap(int width, int height, Bitmap originalImage )
    {
        Bitmap background = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);

        float originalWidth = originalImage.getWidth();
        float originalHeight = originalImage.getHeight();

        Canvas canvas = new Canvas(background);

        float scale = width / originalWidth;

        float xTranslation = 0.0f;
        float yTranslation = (height - originalHeight * scale) / 2.0f;

        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(originalImage, transformation, paint);

        return background;
    }

    private static GoogleSignInOptions getGoogleSignInOptions( Context context ) {

        if( googleSignInOptions == null )
        {
            googleSignInOptions = new GoogleSignInOptions.Builder( GoogleSignInOptions.DEFAULT_SIGN_IN )
                    .requestIdToken( context.getString(R.string.default_web_client_id) )
                    .requestEmail()
                    .build();
        }
        return googleSignInOptions;
    }

    public static GoogleApiClient getGogleApiClient(final Context context )
    {
        if( googleApiClient == null )
        {
            googleApiClient = new GoogleApiClient.Builder( context )
                    .enableAutoManage((AppCompatActivity)context, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Toast.makeText( context, "Connection Failed", Toast.LENGTH_SHORT ).show();
                        }
                    })
                    .addApi( Auth.GOOGLE_SIGN_IN_API, getGoogleSignInOptions( context ) )
                    .build();
        }
        return googleApiClient;
    }

    public static void firebaseAuthWithGoogle(GoogleSignInAccount acct, final Context context) {

        final ProgressDialog progressDialog = new ProgressDialog( context );
        progressDialog.setMessage( "Signing In........." );
        progressDialog.setCanceledOnTouchOutside( false );

        progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener((AppCompatActivity)context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressDialog.dismiss();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                           // Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            checkUserExists( context );
                        }
                    }
                });
    }

    public static FirebaseRecyclerAdapter<Blog, BlogViewHolder> getFirebaseRecyclerAdapter( final Context context )
    {
        if( firebaseRecyclerAdapter == null )
        {
            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                    Blog.class,
                    R.layout.blog_row,
                    BlogViewHolder.class,
                    getBlogReference()
            ) {
                @Override
                protected void populateViewHolder(final BlogViewHolder viewHolder, Blog model, int position) {
                    final String postKey = getRef( position ).getKey();
                    viewHolder.setTitle( model.getTitle() );
                    viewHolder.setDescription( model.getDescription() );
                    viewHolder.setImage( context, model.getImage() );
                    viewHolder.setUserName( model.getUserName() );

                    viewHolder.setLikeButton( postKey );

                    viewHolder.getView().setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent singleBlogIntent = new Intent( context, BlogSingleActivity.class);
                                    singleBlogIntent.putExtra( "BLOG_ID", postKey );
                                    context.startActivity( singleBlogIntent );
                                }
                            }
                    );

                    viewHolder.getLikeButton().setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    MainActivity.processLike = true;
                                    final DatabaseReference databaseLikes = getLikesReference();
                                    databaseLikes.addValueEventListener(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if( MainActivity.processLike )
                                                    {
                                                        if( dataSnapshot.child( postKey ).hasChild( getFirebaseAuth().getCurrentUser().getUid() ) )
                                                        {
                                                            MainActivity.processLike = false;
                                                            databaseLikes.child( postKey ).
                                                                    child( getFirebaseAuth().
                                                                            getCurrentUser().
                                                                            getUid() ).removeValue();
                                                        }
                                                        else
                                                        {
                                                            MainActivity.processLike = false;
                                                            databaseLikes.child( postKey ).
                                                                    child( getFirebaseAuth().
                                                                            getCurrentUser().
                                                                            getUid() ).setValue( "RandomValue" );
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            }
                                    );
                                }
                            }
                    );
                }
            };
        }

        return firebaseRecyclerAdapter;
    }

    public static Query getPostsFromCurrentUser()
    {
        return getBlogReference().orderByChild( "userID" ).equalTo( getFirebaseAuth().getCurrentUser().getUid() );
    }

    public static void enableOfflineSync( Context context )
    {
        if( built == null )
        {
            Picasso.Builder builder = new Picasso.Builder( context );
            builder.downloader( new OkHttpDownloader( context.getApplicationContext(), Integer.MAX_VALUE ));
            built = builder.build();
            built.setIndicatorsEnabled( false );
            built.setLoggingEnabled( true );
            Picasso.setSingletonInstance( built );
        }
    }
}
