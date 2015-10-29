package com.star15.moviesapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieFocus extends AppCompatActivity {
    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView backdrop;
    private static String IMG_URL = "http://image.tmdb.org/t/p/w500";
    Intent intent;
    public static String ACT_NAME = "FOCUS";
    RecyclerView recyclerView;
    RcvAdapter adapter;
    String Title;
    List<Data> data;
    boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_focus);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.rootContainer);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapseTB);
        toolbar = (Toolbar) findViewById(R.id.app_bar_movie_focus);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.movie_details);
        backdrop = (ImageView) findViewById(R.id.backdropImg);

        intent = getIntent();
        getData();
        Picasso.with(this).load(IMG_URL + intent.getStringExtra("backdrop")).placeholder(R.mipmap.placeholder).into(backdrop);
        adapter = new RcvAdapter(this, data, ACT_NAME);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        collapsingToolbarLayout.setTitle(intent.getStringExtra("title"));

    }

    public void getData() {

        Data current = new Data();
        current.title = intent.getStringExtra("title");
        current.overview = intent.getStringExtra("overview");
        current.imgIconUrl = intent.getStringExtra("poster");
        current.backdropURL = intent.getStringExtra("backdrop");
        current.releaseDate = intent.getStringExtra("release");
        current.original_title = intent.getStringExtra("original");
        current.vote = intent.getIntExtra("vote", 0);
        data = new ArrayList<>();
        data.add(current);
    }
}
