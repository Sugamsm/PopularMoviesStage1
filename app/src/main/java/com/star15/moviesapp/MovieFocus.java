package com.star15.moviesapp;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

public class MovieFocus extends AppCompatActivity {

    MovieFocusFrag focusFrag;
    private Toolbar toolbar;
    RelativeLayout rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_focus_layout);
        rv = (RelativeLayout) findViewById(R.id.root_cont);
        focusFrag = (MovieFocusFrag) getFragmentManager().findFragmentById(R.id.movie_focus_frag);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
