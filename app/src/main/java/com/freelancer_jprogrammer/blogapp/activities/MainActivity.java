package com.freelancer_jprogrammer.blogapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.freelancer_jprogrammer.blogapp.R;
import com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils;

import static com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils.checkLogin;
import static com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils.getAuthStateListener;
import static com.freelancer_jprogrammer.blogapp.utils.FirebaseUtils.getFirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private RecyclerView blogRecyclerView;

    public static boolean processLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUtils.redirect( this );
        //FirebaseUtils.enableOfflineSync( this );
        setContentView(R.layout.activity_main);

        blogRecyclerView = (RecyclerView) findViewById( R.id.blogRecyclerView );
        blogRecyclerView.setHasFixedSize( true );
        blogRecyclerView.setLayoutManager( new LinearLayoutManager( this ));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.main_menu, menu );
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId() )
        {
            case R.id.action_add:
                startActivity( new Intent( this, PostActivity.class ));
                break;
            case R.id.action_logout:
                FirebaseUtils.getFirebaseAuth().signOut();
                FirebaseUtils.logout();
                FirebaseUtils.checkLogin( this );
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getFirebaseAuth().addAuthStateListener( getAuthStateListener( this ) );
        blogRecyclerView.setAdapter( FirebaseUtils.getFirebaseRecyclerAdapter( this ) );
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
