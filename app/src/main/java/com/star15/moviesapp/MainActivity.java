package com.star15.moviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RcvAdapter.ClickListener, View.OnClickListener {
    private Toolbar toolbar;
    private RecyclerView rcv;
    List<Data> data;
    private static String COMMON_URL = "https://api.themoviedb.org/3/discover/movie?";
    private static String API_KEY = "";
    private static String LOAD_URL = "";
    private static String TAG_NAME = "results";
    private static String NOW_PLAYING_URL = COMMON_URL + "primary_release_date.gte=2015-09-15&primary_release_date.lte=2015-10-22&api_key=a0e80b0b7435ca6c84689f8a839e263f";
    String title, imgUrl, overview, backdropImg, ReleaseDate;
    public static String ACT_NAME = "MAIN";
    int vote;
    boolean error_notif = false;
    TextView loadTV, retryTv;
    Button retryB;
    ProgressBar loadPB;
    LoadMovies load;
    int[] votes;
    private static boolean alive = false;
    boolean dataFound = false;
    boolean cancelled = false;
    RcvAdapter adapter;
    Intent intent;
    String OriginalTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alive = true;
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle(R.string.now_playing);
        rcv = (RecyclerView) findViewById(R.id.movies_list);
        loadTV = (TextView) findViewById(R.id.loadingtv);
        retryTv = (TextView) findViewById(R.id.networkErrorTV);
        retryB = (Button) findViewById(R.id.retryB);
        retryB.setOnClickListener(this);
        loadPB = (ProgressBar) findViewById(R.id.loadPB);
        final GridLayoutManager manager = new GridLayoutManager(this, 2);
        rcv.setLayoutManager(manager);
        LOAD_URL = uriBuild("now_playing");
        load = new LoadMovies();
        load.execute(LOAD_URL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        alive = true;
        cancelled = false;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
            new LoadMovies().execute(LOAD_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public String uriBuild(String Type) {
        if (Type.equals("most_pop")) {
            LOAD_URL = COMMON_URL + "sort_by=popularity.desc&api_key=" + API_KEY;
            setTitle(R.string.most_popular);
            return LOAD_URL;
        } else if (Type.equals("rated")) {
            LOAD_URL = COMMON_URL + "sort_by=vote_average.desc&api_key=" + API_KEY;
            setTitle(R.string.high_rated);
            return LOAD_URL;
        } else {
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);

            LOAD_URL = COMMON_URL + "primary_release_date.gte=2015-" + (month - 1) + "-" + (day - 1) + "&primary_release_date.gte=2015-" + (month) + "-" + (day) + "&api_key=" + API_KEY;
            setTitle(R.string.now_playing);
            return LOAD_URL;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.now_play:
                if (NetConn()) {
                    if (LOAD_URL.equals(uriBuild("now_playing"))) {
                        Toast.makeText(this, R.string.menu_error, Toast.LENGTH_LONG).show();
                    } else {
                        LoadNow();
                    }
                } else {
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
                    LOAD_URL = "";
                }
                break;
            case R.id.most_pop:
                if (NetConn()) {
                    if (LOAD_URL.equals(uriBuild("most_pop"))) {
                        Toast.makeText(this, R.string.menu_error, Toast.LENGTH_LONG).show();
                    } else {
                        LoadMostPop();
                    }
                } else {
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
                    LOAD_URL = "";
                }
                break;
            case R.id.sort_rated:
                if (NetConn()) {
                    if (LOAD_URL.equals(uriBuild("rated"))) {
                        Toast.makeText(this, R.string.menu_error, Toast.LENGTH_LONG).show();
                    } else {
                        LoadRated();
                    }
                } else {
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
                    LOAD_URL = "";
                }
                break;
        }

        return true;
    }

    public void LoadNow() {
        if (load != null) {
            if (load.getStatus() == AsyncTask.Status.RUNNING || load.getStatus() == AsyncTask.Status.PENDING) {
                load.cancel(true);
            }
        }
        load = new LoadMovies();
        load.execute(uriBuild("now_playing"));
    }

    public void LoadMostPop() {
        if (load != null) {
            if (load.getStatus() == AsyncTask.Status.RUNNING || load.getStatus() == AsyncTask.Status.PENDING) {
                load.cancel(true);
            }
        }
        load = new LoadMovies();
        load.execute(uriBuild("most_pop"));

    }

    public void LoadRated() {
        if (load != null) {
            if (load.getStatus() == AsyncTask.Status.RUNNING || load.getStatus() == AsyncTask.Status.PENDING) {
                load.cancel(true);
            }
        }
        load = new LoadMovies();
        load.execute(uriBuild("rated"));

    }

    @Override
    protected void onPause() {
        super.onPause();
        alive = false;
        if (load != null) {
            if (load.getStatus() == AsyncTask.Status.RUNNING || load.getStatus() == AsyncTask.Status.PENDING) {
                load.cancel(true);
            }
        }
    }

    public void updateRCV(List<Data> data) {
        if (alive && !cancelled) {
            loadTV.setVisibility(View.GONE);
            loadPB.setVisibility(View.GONE);
            rcv.setVisibility(View.VISIBLE);
            adapter = new RcvAdapter(MainActivity.this, data, ACT_NAME);
            rcv.setAdapter(adapter);
            adapter.setClickListener(this);


        }
    }


    public boolean NetConn() {

        ConnectivityManager connectivity = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }


    @Override
    public void itemClicked(View view, int position) {
        Data current = data.get(position);

        intent = new Intent(MainActivity.this, MovieFocus.class);
        intent.putExtra("title", current.title);
        intent.putExtra("overview", current.overview);
        intent.putExtra("poster", current.imgIconUrl);
        intent.putExtra("backdrop", current.backdropURL);
        intent.putExtra("release", current.releaseDate);
        intent.putExtra("original", current.original_title);
        intent.putExtra("vote", current.vote);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        LOAD_URL = uriBuild("now_playing");
        new LoadMovies().execute(LOAD_URL);
    }

    public class LoadMovies extends AsyncTask<String, Void, Void> {
        @Override
        protected void onCancelled() {
            super.onCancelled();
            cancelled = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (error_notif) {
                retryTv.setVisibility(View.GONE);
                retryB.setVisibility(View.GONE);
            }
            rcv.setVisibility(View.GONE);
            loadTV.setVisibility(View.VISIBLE);
            loadPB.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            JsonParser jsonParser = new JsonParser();
            data = new ArrayList<>();
            JSONObject json = jsonParser.getJSON(params[0]);
            JSONArray infoData = null;
            if (json != null) {
                try {
                    infoData = json.getJSONArray(TAG_NAME);
                    for (int i = 0; i < infoData.length(); i++) {
                        JSONObject c = infoData.getJSONObject(i);
                        Data current = new Data();

                        overview = c.getString("overview");
                        vote = c.getInt("vote_average");
                        title = c.getString("title");
                        imgUrl = c.getString("poster_path");
                        backdropImg = c.getString("backdrop_path");
                        ReleaseDate = c.getString("release_date");
                        OriginalTitle = c.getString("original_title");
                        current.backdropURL = backdropImg;
                        current.releaseDate = ReleaseDate;
                        current.overview = overview;
                        current.title = title;
                        current.imgIconUrl = imgUrl;
                        current.vote = vote;
                        current.original_title = OriginalTitle;
                        data.add(current);
                        dataFound = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dataFound) {

                updateRCV(data);
            } else {
                loadTV.setVisibility(View.GONE);
                loadPB.setVisibility(View.GONE);
                retryTv.setVisibility(View.VISIBLE);
                retryB.setVisibility(View.VISIBLE);
                error_notif = true;
            }

        }
    }
}
