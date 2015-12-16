package com.star15.moviesapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
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


    public RcvAdapter(Context context, List<Data> data) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.single_movie_item, viewGroup, false);
        ItemHolder itemHolder = new ItemHolder(view);
        return itemHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        ItemHolder itemHolder = (ItemHolder) viewHolder;

        Data current = data.get(i);
        Context cont = itemHolder.imgLogo.getContext();
        Picasso.with(cont).load(IMG_URL + current.imgIconUrl).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(itemHolder.imgLogo);


    }

    @Override
    public int getItemCount() {

        return data.size();

    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
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
