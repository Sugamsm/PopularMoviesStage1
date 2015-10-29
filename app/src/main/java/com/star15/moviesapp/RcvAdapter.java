package com.star15.moviesapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class RcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    private final LayoutInflater inflater;
    private static String IMG_URL = "http://image.tmdb.org/t/p/w500";
    private ClickListener clickListener;
    List<Data> data = Collections.emptyList();
    String ActName;

    public RcvAdapter(Context context, List<Data> data, String ActName) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.ActName = ActName;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (ActName.equals("MAIN")) {
            View view = inflater.inflate(R.layout.single_movie_item, viewGroup, false);
            ItemHolder itemHolder = new ItemHolder(view);
            return itemHolder;
        } else {
            View view = inflater.inflate(R.layout.movie_focus_items, viewGroup, false);
            SingleMovie singleMovie = new SingleMovie(view);
            return singleMovie;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof SingleMovie) {
            SingleMovie singleMovie = (SingleMovie) viewHolder;
            Data current = data.get(i);
            Context context = singleMovie.poster.getContext();
            Picasso.with(context).load(IMG_URL + current.imgIconUrl).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(singleMovie.poster);
            singleMovie.releaseDate.setText("Release Date : " + current.releaseDate);
            singleMovie.overView.setText(current.overview);
            singleMovie.OriginalTitle.setText(current.original_title);
            singleMovie.Vote.setText("Votes : " + current.vote);

        } else {

            ItemHolder itemHolder = (ItemHolder) viewHolder;

            Data current = data.get(i);
            Context context = itemHolder.imgLogo.getContext();
            Picasso.with(context).load(IMG_URL + current.imgIconUrl).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(itemHolder.imgLogo);
        }

    }

    @Override
    public int getItemCount() {

        return data.size();

    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class SingleMovie extends RecyclerView.ViewHolder {
        TextView releaseDate, Vote, overView, OriginalTitle;
        ImageView poster;

        public SingleMovie(View itemView) {
            super(itemView);
            poster = (ImageView) itemView.findViewById(R.id.focusPoster);
            Vote = (TextView) itemView.findViewById(R.id.voteAverage);
            overView = (TextView) itemView.findViewById(R.id.overviewInfo);
            releaseDate = (TextView) itemView.findViewById(R.id.releaseDate);
            OriginalTitle = (TextView) itemView.findViewById(R.id.original_title);


        }
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgLogo;

        public ItemHolder(View itemView) {
            super(itemView);
            imgLogo = (ImageView) itemView.findViewById(R.id.movie_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());

            }

        }
    }

    public interface ClickListener {
        public void itemClicked(View view, int position);
    }

}
