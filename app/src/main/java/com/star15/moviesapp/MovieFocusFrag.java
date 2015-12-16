package com.star15.moviesapp;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MovieFocusFrag extends Fragment {
    private static String IMG_URL = "http://image.tmdb.org/t/p/w500";
    public static String LOAD_Vids = "http://api.themoviedb.org/3/movie/";
    private static String API_KEY = "";
    ImageView backdrop, reviewer_logo, poster;
    ImageButton favB;
    String[] reviewer_urls, save_vids, save_revs, save_names, save_vnames, entire;
    List<HashMap<String, String>> trail_list, reviews_list;
    HashMap<String, String> trailers, reviews;
    boolean foundVids = false, foundRevs = false, foundAny = false, net_error = false;
    Data current;
    List<Data> data;
    Button button;
    Intent intent_1;
    boolean toRun = false;
    LinearLayout ll, ll_rev, header, overview_lv, rev_internal, trail_click, ll_head_0, ll_head_1, ll_head_2, rev_click;
    TextView review, reviewer, reviews_head, overTv, overInfo, releaseDate, Vote, OriginalTitle, runtime, retry;
    ProgressBar pb;
    String link = "http://www.youtube.com/watch?v=";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            toRun = true;
        } else {
            foundAny = savedInstanceState.getBoolean("found");
            net_error = savedInstanceState.getBoolean("net");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DBAdapt.init(getActivity());
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.movie_focus_frag, container, false);
        backdrop = (ImageView) v.findViewById(R.id.backdropImg);
        header = (LinearLayout) v.findViewById(R.id.head_lv);
        overview_lv = (LinearLayout) v.findViewById(R.id.overview_lv);
        ll = (LinearLayout) v.findViewById(R.id.llcont);
        ll_rev = (LinearLayout) v.findViewById(R.id.ll_revs);
        pb = (ProgressBar) v.findViewById(R.id.load_pb);
        pb.setVisibility(View.GONE);
        button = (Button) v.findViewById(R.id.retryB);
        retry = (TextView) v.findViewById(R.id.networkErrorTV);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectInfo.NetConn(getActivity())) {
                    header.removeAllViews();
                    overview_lv.removeAllViews();
                    ll.removeAllViews();
                    ll_rev.removeAllViews();
                    new LoadDetails().execute(LOAD_Vids + current.movie_id + "/videos?api_key=" + API_KEY);
                    retry.setVisibility(View.GONE);
                    button.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
                }
            }
        });
        if (foundAny) {
            pb.setVisibility(View.VISIBLE);
            entire = savedInstanceState.getStringArray("all");
            foundVids = savedInstanceState.getBoolean("vids_bool");
            foundRevs = savedInstanceState.getBoolean("revs_bool");
            if (foundVids) {
                save_vids = savedInstanceState.getStringArray("trailers");
                save_vnames = savedInstanceState.getStringArray("trail_names");
            }
            if (foundRevs) {
                save_revs = savedInstanceState.getStringArray("revs");
                save_names = savedInstanceState.getStringArray("names");
                reviewer_urls = savedInstanceState.getStringArray("urls");
            }
            retry.setVisibility(View.GONE);
            button.setVisibility(View.GONE);

            SetData(entire);

        } else {
            if (net_error) {
                if (CheckIntent()) {
                    this.current = getData();
                    Picasso.with(getActivity()).load(IMG_URL + current.backdropURL).placeholder(R.mipmap.placeholder).into(backdrop);
                    SetHeader(current);
                    SetOverView(current);
                }
                retry.setVisibility(View.VISIBLE);
                button.setVisibility(View.VISIBLE);
            } else {
                if (CheckIntent()) {
                    setDataAdapter(getData());
                }
            }
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.share_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (save_vids.length != 0) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Link");
            intent.putExtra(Intent.EXTRA_TEXT, Uri.parse(link + save_vids[0]));
            startActivity(Intent.createChooser(intent, "Share Link"));
        } else {
            Toast.makeText(getActivity(), "No Trailers Found to Share!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
    public boolean CheckIntent() {
        if (getActivity().getIntent().getBooleanExtra("act", false)) {
            return true;
        } else {
            return false;
        }
    }

    public void SetData(String[] entireData) {
        Data saved = new Data();
        saved.imgIconUrl = entireData[0];
        saved.title = entireData[1];
        saved.backdropURL = entireData[2];
        saved.releaseDate = entireData[3];
        saved.overview = entireData[4];
        saved.original_title = entireData[5];
        saved.movie_id = Integer.valueOf(entireData[6]);
        saved.runtime = Integer.valueOf(entireData[7]);
        saved.vote = Integer.valueOf(entireData[8]);
        Picasso.with(getActivity()).load(IMG_URL + saved.backdropURL).placeholder(R.mipmap.placeholder).into(backdrop);
        pb.setVisibility(View.GONE);
        SetHeader(saved);
        SetOverView(saved);
        SetSavedVids();
        SetSavedRevs();

    }

    public Data getData() {

        Data current = new Data();
        intent_1 = getActivity().getIntent();
        current.title = intent_1.getStringExtra("title");
        current.overview = intent_1.getStringExtra("overview");
        current.imgIconUrl = intent_1.getStringExtra("poster");
        current.backdropURL = intent_1.getStringExtra("backdrop");
        current.releaseDate = intent_1.getStringExtra("release");
        current.original_title = intent_1.getStringExtra("original");
        current.vote = intent_1.getIntExtra("vote", 0);
        current.movie_id = intent_1.getIntExtra("movie_id", 0);

        return current;
    }

    public void SetSavedRevs() {
        reviews_list = new ArrayList<>();
        for (int i = 0; i < save_revs.length; i++) {
            reviews = new HashMap<>();
            reviews.put("author", save_names[i]);
            reviews.put("content", save_revs[i]);
            reviews_list.add(reviews);
        }

        SetReviews(reviews_list, reviewer_urls);
    }

    public void SetSavedVids() {
        trail_list = new ArrayList<>();
        for (int i = 0; i < save_vids.length; i++) {
            trailers = new HashMap<>();
            trailers.put("name", save_vnames[i]);
            trailers.put("link", save_vids[i]);
            trail_list.add(trailers);
        }

        setTrailers(trail_list);
    }

    public void setDataAdapter(Data current) {
        Picasso.with(getActivity()).load(IMG_URL + current.backdropURL).placeholder(R.mipmap.placeholder).into(backdrop);
        this.current = current;
        getActivity().setTitle(current.title);
        retry.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        if (ConnectInfo.NetConn(getActivity())) {
            header.removeAllViews();
            overview_lv.removeAllViews();
            ll.removeAllViews();
            ll_rev.removeAllViews();
            new LoadDetails().execute(LOAD_Vids + current.movie_id + "/videos?api_key=" + API_KEY);
        } else {
            Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
            pb.setVisibility(View.GONE);
            net_error = true;
            SetHeader(current);
            SetOverView(current);
            retry.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
        }
    }

    public void setTrailers(List<HashMap<String, String>> mList) {

        TextView trailer_head, trailer;
        ImageView trailer_logo;
        View child = getActivity().getLayoutInflater().inflate(R.layout.single_trailer, null);
        trail_click = (LinearLayout) child.findViewById(R.id.ll_click_trail);
        trailer_head = (TextView) child.findViewById(R.id.mTag);
        trailer_logo = (ImageView) child.findViewById(R.id.trailer_logo);
        trailer = (TextView) child.findViewById(R.id.trailer);
        for (int i = 0; i < mList.size(); i++) {
            ll.addView(child);
            if (i != 0) {
                trailer_head.setVisibility(View.GONE);
            }

            HashMap<String, String> map = new HashMap<>();
            map = mList.get(i);
            trailer.setText(map.get("name"));
            final HashMap<String, String> finalMap = map;
            trail_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link + finalMap.get("link")));
                    startActivity(intent);
                }
            });

        }
        if (mList.size() == 0) {
            trailer_logo.setVisibility(View.GONE);
            trailer.setText("No Trailers Found....");
            ll.addView(child);
        }
    }

    public void SetReviews(List<HashMap<String, String>> mList, final String[] urls) {

        for (int i = 0; i < mList.size(); i++) {
            HashMap<String, String> map;
            map = mList.get(i);
            View v = getActivity().getLayoutInflater().inflate(R.layout.single_review, null);
            reviews_head = (TextView) v.findViewById(R.id.revTag);
            review = (TextView) v.findViewById(R.id.review);
            reviewer = (TextView) v.findViewById(R.id.reviewer_name);
            reviewer_logo = (ImageView) v.findViewById(R.id.reviewer_logo);
            rev_internal = (LinearLayout) v.findViewById(R.id.internalLL);
            rev_click = (LinearLayout) v.findViewById(R.id.rev_click);

            if (i != 0) {
                reviews_head.setVisibility(View.GONE);
            }
            reviewer.setText(map.get("author"));
            review.setText(map.get("content"));
            final int finalI = i;
            rev_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[finalI]));
                    startActivity(intent);
                }
            });
            ll_rev.addView(v);
        }
        if (mList.size() == 0) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.single_review, null);
            reviews_head = (TextView) v.findViewById(R.id.revTag);
            review = (TextView) v.findViewById(R.id.review);
            reviewer = (TextView) v.findViewById(R.id.reviewer_name);
            reviewer_logo = (ImageView) v.findViewById(R.id.reviewer_logo);
            rev_internal = (LinearLayout) v.findViewById(R.id.internalLL);
            reviewer.setText("No Reviews Found....");
            reviewer_logo.setVisibility(View.GONE);
            ll_rev.addView(v);
        }
    }


    public void SetOverView(Data current) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.overview_item, null);
        overInfo = (TextView) v.findViewById(R.id.overviewInfo);
        overTv = (TextView) v.findViewById(R.id.overviewTV);
        overTv.setText(R.string.overview);
        overInfo.setText(current.overview);
        overview_lv.addView(v);

    }

    public void SetHeader(final Data newCurrent) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.header_, null);
        ll_head_0 = (LinearLayout) v.findViewById(R.id.header_main_ll);
        ll_head_1 = (LinearLayout) v.findViewById(R.id.first_info);
        ll_head_2 = (LinearLayout) v.findViewById(R.id.sec_info);
        poster = (ImageView) v.findViewById(R.id.focusPoster);
        Vote = (TextView) v.findViewById(R.id.voteAverage);
        releaseDate = (TextView) v.findViewById(R.id.releaseDate);
        runtime = (TextView) v.findViewById(R.id.runtime);
        OriginalTitle = (TextView) v.findViewById(R.id.original_title);
        favB = (ImageButton) v.findViewById(R.id.favB);

        Picasso.with(getActivity()).load(IMG_URL + newCurrent.imgIconUrl).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(poster);
        releaseDate.setText("Release Date : " + newCurrent.releaseDate);
        OriginalTitle.setText(newCurrent.original_title);
        Vote.setText("Votes : " + newCurrent.vote);
        header.addView(v);


        if (newCurrent.runtime == 0) {
            runtime.setText("Runtime : NA");
        } else {
            runtime.setText("Runtime : " + newCurrent.runtime + " Mins");
        }

        if (DBAdapt.getFavData(newCurrent.movie_id)) {
            favB.setImageResource(R.mipmap.fav_button_clicked);
        }
        favB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!DBAdapt.getFavData(newCurrent.movie_id)) {
                    favB.setImageResource(R.mipmap.fav_button_clicked);
                    DBAdapt.addData(newCurrent.imgIconUrl, newCurrent.title, newCurrent.original_title, newCurrent.releaseDate, newCurrent.vote, newCurrent.movie_id, newCurrent.backdropURL, newCurrent.overview, newCurrent.runtime);
                    Toast.makeText(getActivity(), newCurrent.title + " addded to Favourites!", Toast.LENGTH_LONG).show();
                } else {
                    favB.setImageResource(R.mipmap.fav_button);
                    DBAdapt.delete(newCurrent.movie_id);
                    Toast.makeText(getActivity(), newCurrent.title + " removed from Favourites!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("found", foundAny);
        outState.putBoolean("net", net_error);
        if (foundAny) {
            outState.putStringArray("all", entire);
            outState.putBoolean("vids_bool", foundVids);
            outState.putBoolean("revs_bool", foundRevs);
            if (foundVids) {
                outState.putStringArray("trailers", save_vids);
                outState.putStringArray("trail_names", save_vnames);
            }
            if (foundRevs) {
                outState.putStringArray("names", save_names);
                outState.putStringArray("revs", save_revs);
                outState.putStringArray("urls", reviewer_urls);
            }
        }
        super.onSaveInstanceState(outState);
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
        entire[7] = String.valueOf(newData.runtime);
        entire[8] = String.valueOf(newData.vote);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public class LoadDetails extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            JsonParser jsonParser = new JsonParser();
            JsonParser jsonRevs = new JsonParser();
            JsonParser newJson = new JsonParser();
            // data = new ArrayList<>();
            JSONObject json = jsonParser.getJSON(params[0]);
            JSONObject jsonRev = jsonRevs.getJSON(LOAD_Vids + current.movie_id + "/reviews?api_key=" + API_KEY);
            JSONObject runJson = newJson.getJSON(LOAD_Vids + current.movie_id + "?api_key=" + API_KEY);
            try {
                current.runtime = runJson.getInt("runtime");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray infoData = null;
            if (json != null) {
                try {
                    infoData = json.getJSONArray("results");
                    trail_list = new ArrayList<>();
                    save_vids = new String[infoData.length()];
                    save_vnames = new String[infoData.length()];

                    for (int i = 0; i < infoData.length(); i++) {
                        JSONObject c = infoData.getJSONObject(i);
                        trailers = new HashMap<>();
                        trailers.put("name", c.getString("name"));
                        trailers.put("link", c.getString("key"));
                        save_vids[i] = c.getString("key");
                        save_vnames[i] = c.getString("name");
                        trail_list.add(trailers);
                    }
                    foundVids = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    foundVids = false;
                }
            }

            if (jsonRev != null) {
                JSONArray revData = null;
                try {
                    revData = jsonRev.getJSONArray("results");
                    reviews_list = new ArrayList<>();
                    reviewer_urls = new String[revData.length()];
                    save_names = new String[revData.length()];
                    save_revs = new String[revData.length()];

                    for (int j = 0; j < revData.length(); j++) {
                        JSONObject d = revData.getJSONObject(j);
                        reviews = new HashMap<>();
                        reviewer_urls[j] = d.getString("url");
                        reviews.put("author", d.getString("author"));
                        reviews.put("content", d.getString("content"));
                        reviews_list.add(reviews);
                    }
                    foundRevs = true;
                } catch (Exception e) {
                    foundRevs = false;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pb.setVisibility(View.GONE);
            if (foundVids || foundRevs) {
                net_error = false;
                foundAny = true;
                Save(current);
                setTrailers(trail_list);
                SetReviews(reviews_list, reviewer_urls);
                SetHeader(current);
                SetOverView(current);
            } else {
                foundAny = false;
                net_error = true;
                button.setVisibility(View.VISIBLE);
                retry.setVisibility(View.VISIBLE);
            }

        }
    }
}
