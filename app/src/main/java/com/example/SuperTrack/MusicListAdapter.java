package com.example.SuperTrack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;



public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder>{

    private ArrayList<AudioModel> songsList;
    Context context;
    //private AlertDialog alertDialog;
    OnDeleteClickListener onDeleteClickListener;
    private OnItemClickListener onItemClickListener; // New listener interface
    private OnSwipeToDeleteListener onSwipeToDeleteListener; // New listener interface
    RecyclerView.ViewHolder viewHolder;
    private int positionHolder;
    Button PlayNow, PlayLater, EndLater;


    public interface OnDeleteClickListener {
        void onDeleteClick(int position);

    }
    public void deleteItem(int position) {
        Log.i(getClass().toString(),"deleteItem()!1");
        songsList.remove(position);
        notifyItemRemoved(position);

    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


   public MusicListAdapter(ArrayList<AudioModel> songsList, Context context,  OnSwipeToDeleteListener onSwipeToDeleteListener) {
    // Your existing constructor code
       Log.i("MusicListAdapter", "Context constructed: " + context.toString());
       this.songsList = songsList;
       this.context = context;
       this.onSwipeToDeleteListener = onSwipeToDeleteListener;
    }
    public interface OnSwipeToDeleteListener {
        void onItemSwiped(int position);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
        public void onBindViewHolder( MusicListAdapter.ViewHolder holder, int position) {
            AudioModel songData = songsList.get(position);
            holder.titleTextView.setText(songData.getTitle());
            holder.durationTv.setText(holder.convertToMMSS(Long.parseLong(songData.getDuration())));

            String truncatedTitle = truncateTitle(songData.getTitle(), 35);

            holder.titleTextView.setText(truncatedTitle);

            Bitmap albumArtBitmap = getAlbumArtBytesForSong(songData); // Implement a method to get album art bytes

            if (albumArtBitmap != null) {
               // Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);
                holder.albumArtImageView.setImageBitmap(albumArtBitmap);
            } else {
                // If no album art is available, you can set a default image
                holder.albumArtImageView.setImageResource(R.drawable.music_icon);
            }

            ItemTouchHelper.SimpleCallback swipeToDeleteCallback = new SwipeToDeleteCallback( onSwipeToDeleteListener);
            new ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView((RecyclerView) holder.itemView.getParent());
            //Global positiion
            positionHolder = position;
            if(MyMediaPlayer.currentIndex==position){
                holder.titleTextView.setTextColor(Color.parseColor("#FF0000"));
            }else{
                holder.titleTextView.setTextColor(Color.parseColor("#000000"));
            }

            // Inside onBindViewHolder

           /* holder.deleteButton.setOnClickListener(view -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(position);

                    Log.i("MUSICADAPTER","OndeletEclick");
                }
            });*/
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //navigate to another acitivty
                    if (!songsList.isEmpty()) {
                    MyMediaPlayer.getInstance().reset();
                    MyMediaPlayer.currentIndex = position;
                    Intent intent = new Intent(context,MusicPlayerActivity.class);
                    intent.putExtra("LIST",songsList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                } else {
                    // Handle the case when the list is empty
                    Toast.makeText(context, "no songs available", Toast.LENGTH_SHORT).show();
                }


                }
            });

        }
    public Bitmap getAlbumArtBytesForSong(AudioModel song) {
        Log.i(getClass().toString(), "songuri" + song.getPath().toString());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(song.getPath());
            byte[] albumArtBytes = retriever.getEmbeddedPicture();

            if (albumArtBytes != null && albumArtBytes.length > 0) {
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);
                retriever.release();
                return albumArtBitmap;
            } else {
                // No album art available, return null or a default image
                retriever.release();
                return null;
            }
        } catch (Exception e) {
            // Handle exceptions that may occur when setting data source
            e.printStackTrace();
            retriever.release();
            return null;
        }
    }
    private String truncateTitle(String title, int maxLength) {
        if (title.length() > maxLength) {
            // Truncate the title and add three dots
            return title.substring(0, maxLength) + "...";
        } else {
            return title;
        }
    }
@Override
    public int getItemCount() {
        return songsList.size();
    }
      public class ViewHolder extends RecyclerView.ViewHolder {

          public ImageView albumArtImageView;
          TextView titleTextView;

          TextView durationTv;
        ImageView iconImageView;
        //ImageView deleteButton;

        public ViewHolder(View itemView) {
    super(itemView);
    titleTextView = itemView.findViewById(R.id.music_title_text);
    durationTv = itemView.findViewById(R.id.music_duration_text);
    iconImageView = itemView.findViewById(R.id.icon_view);
   // deleteButton = itemView.findViewById(R.id.delete_button);
    albumArtImageView = itemView.findViewById(R.id.icon_view);
    // Attach swipe gesture to the ViewHolder's root view
    ItemTouchHelper.SimpleCallback swipeToDeleteCallback = new SwipeToDeleteCallback( MusicListAdapter.this.onSwipeToDeleteListener);
    new ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView((RecyclerView) itemView.getParent());
}
          public  String convertToMMSS(long duration) {
              Long millis = duration;
              long hours = TimeUnit.MILLISECONDS.toHours(millis);
              long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
              long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);

              if (hours > 0) {
                  return String.format("%2d:%02d:%02d", hours, minutes, seconds);
              } else {
                  return String.format("%2d:%02d", minutes, seconds);
              }
          }
    }
}

