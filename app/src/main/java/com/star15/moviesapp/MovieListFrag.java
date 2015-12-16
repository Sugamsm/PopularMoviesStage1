package com.star15.moviesapp;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MovieListFrag extends Fragment implements RcvAdapter.ClickListener, View.OnClickListener {

    private RecyclerView rcv;
    List<Data> data;
    private static String COMMON_URL = "https://api.themoviedb.org/3/discover/movie?";
    private static String API_KEY = "a0e80b0b7435ca6c84689f8a839e263f";
    private static String LOAD_URL = "";
    private static String LAST_TRIED = "";
    private static String TAG_NAME = "results";
    String title, imgUrl, overview, backdropImg, ReleaseDate, Review, Reviewer, RevUrl, VidUrl;
    int vote, click_item = 0;
    String[] entire, imgUrls;
    boolean error_notif = false, clicked = false;
    TextView loadTV, retryTv, fav;
    Button retryB;
    ProgressBar loadPB;
    LoadMovies load;
    int movie_id;
    private static boolean alive = true;
    boolean dataFound = false;
    boolean cancelled = false;
    RcvAdapter adapter;

    DataCom com;
    String OriginalTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.movies_list_layout, container, false);
        rcv = (RecyclerView) v.findViewById(R.id.movies_list);
        loadTV = (TextView) v.findViewById(R.id.loadingtv);
        retryTv = (TextView) v.findViewById(R.id.networkErrorTV);
        retryB = (Button) v.findViewById(R.id.retryB);
        retryB.setOnClickListener(this);
        loadPB = (ProgressBar) v.findViewById(R.id.loadPB);
        fav = (TextView) v.findViewById(R.id.fav_tv);
        fav.setVisibility(View.GONE);
        final GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        rcv.setLayoutManager(manager);
        if (savedInstanceState != null) {
            entire = savedInstanceState.getStringArray("arr_data");
            clicked = savedInstanceState.getBoolean("clicked");
            imgUrls = savedInstanceState.getStringArray("urls");
            updateRCV(getLinks(imgUrls));
            if (clicked) {
                click_item = savedInstanceState.getInt("click_item");
                com.communicate(arrange(entire), 0);
            }


        } else {

            LOAD_URL = uriBuild("now_playing");
            load = new LoadMovies();
            load.execute(LOAD_URL);
        }

        DBAdapt.init(getActivity());
        return v;
    }

    public List<Data> arrange(String[] entireData) {
        Data saved = new Data();
        saved.imgIconUrl = entireData[0];
        saved.title = entireData[1];
        saved.backdropURL = entireData[2];
        saved.releaseDate = entireData[3];
        saved.overview = entireData[4];
        saved.original_title = entireData[5];
        saved.movie_id = Integer.valueOf(entireData[6]);
        saved.vote = Integer.valueOf(entireData[7]);

        data = new ArrayList<>();
        data.add(saved);
        return data;
    }

    public List<Data> getLinks(String[] urls) {
        data = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            Data curr = new Data();
            curr.imgIconUrl = urls[i];
            data.add(curr);
        }

        return data;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.now_play:
                fav.setVisibility(View.GONE);
                if (ConnectInfo.NetConn(getActivity())) {
                    if (LOAD_URL.equals(uriBuild("now_playing"))) {
                        Toast.makeText(getActivity(), R.string.menu_error, Toast.LENGTH_LONG).show();
                    } else {
                        LoadNow();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
                    LOAD_URL = "";
                }
                break;
            case R.id.most_pop:
                fav.setVisibility(View.GONE);
                if (ConnectInfo.NetConn(getActivity())) {
                    if (LOAD_URL.equals(uriBuild("most_pop"))) {
                        Toast.makeText(getActivity(), R.string.menu_error, Toast.LENGTH_LONG).show();
                    } else {
                        LoadMostPop();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
                    LOAD_URL = "";
                }
                break;
            case R.id.sort_rated:
                fav.setVisibility(View.GONE);
                if (ConnectInfo.NetConn(getActivity())) {
                    if (LOAD_URL.equals(uriBuild("rated"))) {
                        Toast.makeText(getActivity(), R.string.menu_error, Toast.LENGTH_LONG).show();
                    } else {
                        LoadRated();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
                    LOAD_URL = "";
                }
                break;
            case R.id.fav_item:
                fav.setVisibility(View.GONE);
                if (DBAdapt.datafound()) {
                    LoadSaved(true);
                } else {
                    LoadSaved(false);
                }
                break;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (dataFound) {
            outState.putBoolean("got_data", dataFound);
            outState.putString("last", LAST_TRIED);
            outState.putStringArray("arr_data", entire);
            outState.putBoolean("clicked", clicked);
            outState.putStringArray("urls", imgUrls);
            if (clicked) {
                outState.putInt("pos", click_item);
            }
        }


    }

    public void LoadSaved(boolean got) {
        getActivity().setTitle(R.string.favs);
        LOAD_URL = "";
        retryTv.setVisibility(View.GONE);
        retryB.setVisibility(View.GONE);
        if (got) {
            data = new ArrayList<>();
            data = DBAdapt.getFav();
            updateRCV(data);
        } else {
            loadTV.setVisibility(View.GONE);
            loadPB.setVisibility(View.GONE);
            fav.setVisibility(View.VISIBLE);
            rcv.setVisibility(View.GONE);
        }
    }

    public void LoadNow() {
        if (load != null) {
            if (load.getStatus() == AsyncTask.Status.RUNNING || load.getStatus() == AsyncTask.Status.PENDING) {
                load.cancel(true);
                cancelled = false;
            }
        }

        load = new LoadMovies();
        load.execute(uriBuild("now_playing"));
    }

    public void LoadMostPop() {
        if (load != null) {
            if (load.getStatus() == AsyncTask.Status.RUNNING || load.getStatus() == AsyncTask.Status.PENDING) {
                load.cancel(true);
                cancelled = false;
            }
        }
        load = new LoadMovies();
        load.execute(uriBuild("most_pop"));

    }

    public void LoadRated() {
        if (load != null) {
            if (load.getStatus() == AsyncTask.Status.RUNNING || load.getStatus() == AsyncTask.Status.PENDING) {
                load.cancel(true);
                cancelled = false;
            }
        }
        load = new LoadMovies();
        load.execute(uriBuild("rated"));

    }

    public void Save(Data newData) {
        entire = new String[10];
        entire[0] = newData.imgIconUrl;
        entire[1] = newData.title;
        entire[2] = newData.backdropURL;
        entire[3] = newData.releaseDate;
        entire[4] = newData.overview;
        entire[5] = newData.original_title;
        entire[6] = String.valueOf(newData.movie_id);
        entire[7] = String.valueOf(newData.vote);
    }

    public void setDataCom(DataCom dc) {
        this.com = dc;
    }

    @Override
    public void onResume() {
        alive = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        alive = false;
        super.onPause();
    }

    public String uriBuild(String Type) {
        if (Type.equals("most_pop")) {
            LOAD_URL = COMMON_URL + "sort_by=popularity.desc&api_key=" + API_KEY;
            getActivity().setTitle(R.string.most_popular);
            LAST_TRIED = LOAD_URL;
            return LOAD_URL;
        } else if (Type.equals("rated")) {
            LOAD_URL = COMMON_URL + "sort_by=vote_average.desc&api_key=" + API_KEY;
            getActivity().setTitle(R.string.high_rated);
            LAST_TRIED = LOAD_URL;
            return LOAD_URL;
        } else {
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);

            LOAD_URL = COMMON_URL + "primary_release_date.gte=2015-" + (month - 1) + "-" + (day - 1) + "&primary_release_date.gte=2015-" + (month) + "-" + (day) + "&api_key=" + API_KEY;
            getActivity().setTitle(R.string.now_playing);
            LAST_TRIED = LOAD_URL;
            return LOAD_URL;
        }
    }

    public void updateRCV(List<Data> data) {
        if (alive && !cancelled) {
            loadTV.setVisibility(View.GONE);
            loadPB.setVisibility(View.GONE);
            rcv.setVisibility(View.VISIBLE);
            adapter = new RcvAdapter(getActivity(), data);
            rcv.setAdapter(adapter);
            adapter.setClickListener(this);
        }
    }


    @Override
    public void itemClicked(View view, int position) {
        clicked = true;
        click_item = position;
        com.communicate(data, position);
    }

    @Override
    public void onClick(View v) {
        if (LAST_TRIED.equals("")) {
            LOAD_URL = uriBuild("now_playing");
            retryTv.setVisibility(View.GONE);
            retryB.setVisibility(View.GONE);
            rcv.setVisibility(View.GONE);
            loadTV.setVisibility(View.VISIBLE);
            loadPB.setVisibility(View.VISIBLE);
            new LoadMovies().execute(LOAD_URL);
        } else {
            new LoadMovies().execute(LAST_TRIED);
        }
    }

    public class LoadMovies extends AsyncTask<String, Void, Void> {
        @Override
        protected void onCancelled() {
            super.onCancelled();
            cancelled = true;
            rcv.setVisibility(View.GONE);
            loadTV.setVisibility(View.VISIBLE);
            loadPB.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rcv.setVisibility(View.GONE);
            retryB.setVisibility(View.GONE);
            retryTv.setVisibility(View.GONE);
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
                    imgUrls = new String[infoData.length()];
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
                        movie_id = c.getInt("id");
                        imgUrls[i] = imgUrl;
                        current.backdropURL = backdropImg;
                        current.releaseDate = ReleaseDate;
                        current.overview = overview;
                        current.title = title;
                        current.imgIconUrl = imgUrl;
                        current.vote = vote;
                        current.movie_id = movie_id;
                        current.original_title = OriginalTitle;
                        data.add(current);
                        Save(current);
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

    public interface DataCom {
        public void communicate(List<Data> mList, int pos);
    }

}
