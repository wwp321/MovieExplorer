package com.byron.movieexplorer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MovieItemAdapter extends RecyclerView.Adapter<MovieItemAdapter.ViewHolder> {
    List<MovieItem> movieItemList;

    public MovieItemAdapter(List<MovieItem> movieItemList) {
        this.movieItemList = movieItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieItem item = movieItemList.get(holder.getAdapterPosition());
                Timber.d("click movie:" + item.getTitle());

                Intent intent = new Intent(v.getContext(), MovieDetailActivity.class);
                intent.putExtra(MovieDetailActivity.MOVIE_DETAIL_LINK, item.getLink());
                intent.putExtra(MovieDetailActivity.MOVIE_DETAIL_TITLE, item.getTitle());
                view.getContext().startActivity(intent);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieItem item = movieItemList.get(holder.getAdapterPosition());

        if(item != null) {
            holder.movieTitle.setText(item.getTitle());
            holder.movieDate.setText(item.getDate());
            holder.movieDescription.setText(item.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return movieItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.movie_title)
        TextView movieTitle;

        @BindView(R.id.movie_date)
        TextView movieDate;

        @BindView(R.id.movie_description)
        TextView movieDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
