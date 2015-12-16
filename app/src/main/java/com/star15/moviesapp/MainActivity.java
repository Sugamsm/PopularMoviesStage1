package com.star15.moviesapp;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieListFrag.DataCom {
    private Toolbar toolbar;
    MovieListFrag listFrag;
    FragmentManager manager;
    MovieFocusFrag focusFrag;
    List<Data> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        manager = getFragmentManager();
        listFrag = (MovieListFrag) manager.findFragmentById(R.id.movie_list_frag);
        focusFrag = (MovieFocusFrag) manager.findFragmentById(R.id.movie_focus_frag);
        listFrag.setDataCom(this);
        //   deleteDatabase("fav_movies");
    }

    @Override
    public void communicate(List<Data> mList, int pos) {
        this.data = new ArrayList<>();
        this.data = mList;
        Data current = data.get(pos);
        if (focusFrag != null && focusFrag.isVisible()) {

            focusFrag.setDataAdapter(current);

        } else {
            Intent intent;
            intent = new Intent(this, MovieFocus.class);
            intent.putExtra("title", current.title);
            intent.putExtra("overview", current.overview);
            intent.putExtra("poster", current.imgIconUrl);
            intent.putExtra("backdrop", current.backdropURL);
            intent.putExtra("release", current.releaseDate);
            intent.putExtra("original", current.original_title);
            intent.putExtra("vote", current.vote);
            intent.putExtra("movie_id", current.movie_id);
            intent.putExtra("act", true);

            startActivity(intent);
        }
    }

}
