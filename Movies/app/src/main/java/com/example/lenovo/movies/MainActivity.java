package com.example.lenovo.movies;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements MovieListener{

    public static boolean mTwoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MovieFragment movieFragment = new MovieFragment();
        movieFragment.mListener = this;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.movie_fragment_id, movieFragment,"")
                .commit();

        if(findViewById(R.id.detail_fragment_id) != null) mTwoPane = true;
        else mTwoPane = false;

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.Sort_by) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateData(Movie movie, boolean connection, boolean fave) {

        DetailFragment detailFragment = new DetailFragment();

        Bundle bundle = new Bundle();
        bundle.putString("title", movie.TITLE);
        bundle.putString("overview", movie.OVERVIEW);
        bundle.putString("poster", movie.poster);
        bundle.putString("releaseyear", movie.RELEASEYEAR);
        bundle.putDouble("voteaverage", movie.VOTEAVERAGE);
        bundle.putInt("id", movie.id);
        bundle.putBoolean("fave", fave);//true if movie is fave
        bundle.putBoolean("connection", connection);

        detailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_fragment_id, detailFragment,"")
                .commit();
    }
}
