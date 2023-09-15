package com.example.SuperTrack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;



public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder>  {

    private ArrayList<AudioModel> songsList;
    Context context;
    //private AlertDialog alertDialog;
    OnDeleteClickListener onDeleteClickListener;
    private OnItemClickListener onItemClickListener; // New listener interface
    private OnSwipeToDeleteListener onSwipeToDeleteListener; // New listener interface
    private OnItemLongClickListener itemLongClickListener;// New listener interface
    RecyclerView.ViewHolder viewHolder;
    private int positionHolder;
    boolean isInSelectionMode;

    Button PlayNow, PlayLater, EndLater;
    List<Integer> selectedItems = new ArrayList<>();



    public interface OnDeleteClickListener {
        void onDeleteClick(int position);

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnSwipeToDeleteListener {
        void onItemSwiped(int position);

    }
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);

    }
    public void deleteItem(int position) {
        Log.i(getClass().toString(),"deleteItem()!1");
        songsList.remove(position);
        notifyItemRemoved(position);

    }
   /* public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }*/
    public void setOnItemLongClickListener(MainActivity mainActivity) {
        this.itemLongClickListener = mainActivity;
    }


   public MusicListAdapter(ArrayList<AudioModel> songsList, Context context,  OnSwipeToDeleteListener onSwipeToDeleteListener) {
    // Your existing constructor code
       Log.i("MusicListAdapter", "Context constructed: " + context.toString());
       this.songsList = songsList;
       this.context = context;
       this.onSwipeToDeleteListener = onSwipeToDeleteListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
        public void onBindViewHolder( MusicListAdapter.ViewHolder holder, int position) {

        int positionholder = holder.getAdapterPosition();
        AudioModel songData = songsList.get(position);

        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);


            String truncatedTitle = truncateTitle(songData.getTitle(), 30);

            holder.titleTextView.setText(truncatedTitle);
            //holder.titleTextView.setText(songData.getTitle());
            holder.titleTextView.setTypeface(typeface);

            holder.durationTv.setText(holder.convertToMMSS(Long.parseLong(songData.getDuration())));
            holder.durationTv.setTypeface(typeface);
            //update SELCTED ITEMS locally from Singleton
            selectedItems = MusicStateSingleton.getInstance().getSelectedItems();
            isInSelectionMode = MusicStateSingleton.getInstance().isInSelectionMode();
        if (MusicStateSingleton.getInstance().getSelectedItems().contains(position)) {
            // Set the background color for selected items
            holder.itemView.setBackgroundColor(Color.parseColor("#fafafa"));
        } else {
            // Set the background color for non-selected items"#E3EFCC"
            holder.itemView.setBackgroundColor(Color.parseColor("#fafafa"));
        }


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

        positionholder = holder.getAdapterPosition();
        //Log.i("TAG", "onBindViewHolder holder.getAdapterPosition(): " + positionholder );
//SHOW HIGHLIGHTED tEXT FOR CURRENT SONG
        if(MyMediaPlayer.currentIndex==positionholder){
                holder.titleTextView.setTextColor(Color.parseColor("#C95C2B"));//CURRENTLYSELECTEDSONG
            }else{
                holder.titleTextView.setTextColor(Color.parseColor("#000000"));//OTHER SONGS
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Log.i("TAG", "onBindViewHolder onClick: "  );
                    if(isInSelectionMode){
                        int positionholder = holder.getAdapterPosition();
                        Toast.makeText(context, "Item  clicked!", Toast.LENGTH_SHORT).show();
                        if (selectedItems.contains(positionholder)) {
                            selectedItems.remove(Integer.valueOf(positionholder)); // Deselect the item
                        } else {
                            selectedItems.add(positionholder); // Select the item
                        }
                        notifyItemChanged(positionholder); // Notify the adapter to update the view
                        // Handle long click action here
                        MusicStateSingleton.getInstance().setSelectedItems(selectedItems);
                    }else
                    //navigate to another acitivty
                    if (!songsList.isEmpty()) {
                    MyMediaPlayer.getInstance().reset();
                    MusicStateSingleton.getInstance().setSongTitle(holder.titleTextView.getText().toString());
                    MusicStateSingleton.getInstance().setDuration(holder.durationTv.getText().toString());
                    MusicStateSingleton.getInstance().setMusicPlaying(false);
                        Log.i("TAG", "onBindViewHolder onClick:isMusicPlaying False "  );

                        MyMediaPlayer.currentIndex = holder.getAdapterPosition();
                    Intent intent = new Intent(context,MusicPlayerActivity.class);
                    intent.putExtra("LIST",songsList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                } else {
                    // Handle the case when the list is empty
                    Toast.makeText(context, "no songs available", Toast.LENGTH_SHORT).show();
                }
            if(isInSelectionMode)     Log.d("TAG"," selectedItems" + selectedItems.toString());

                }
            });

        //This code does the highlighting on click
        //Log.d("TAGMLA"," is in selection mode  "+ isInSelectionMode);
        if(isInSelectionMode){
            if (selectedItems.contains(position)) {
                //if is in selected items list
                Log.d("TAGMLA","  select on click ");
                holder.itemView.setBackgroundColor(Color.parseColor("#E3EFCC"));
            } else {
                // if not in list
                Log.d("TAGMLA","  deselect on click ");
            holder.itemView.setBackgroundColor(Color.parseColor("#fafafa"));
            }
        }


// Inside onBindViewHolder
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                isInSelectionMode = !isInSelectionMode;
                MusicStateSingleton.getInstance().setIsInSelectionMode(true);
                Toast.makeText(context, " SELECTIONMODE " + isInSelectionMode, Toast.LENGTH_SHORT).show();





                int positionholder = holder.getAdapterPosition();

                Log.d("TAG"," onlongclick: is item Selected "+songData.isSelected());
                if (songData.isSelected()) {
                    Log.d("TAG"," deselect onlongclick ");
                    holder.itemView.setBackgroundColor(Color.parseColor("#E3EFCC"));
                } else {
                    Log.d("TAG"," select onlongclick ");
                    holder.itemView.setBackgroundColor(Color.parseColor("#fafafa"));
                }
                selectedItems.add(positionholder);
                MusicStateSingleton.getInstance().setSelectedItems(selectedItems);
                if (itemLongClickListener != null) {
                    itemLongClickListener.onItemLongClick(positionholder);
                    Log.d("TAG"," itemLongClickListener ");
                }
                Toast.makeText(context, "Item long clicked!", Toast.LENGTH_SHORT).show();
                /*AudioModel selectedSong = songsList.get(positionholder);
                selectedSong.setIsSelected(!selectedSong.isSelected());*/

                Log.d("TAG"," selectedItems" + selectedItems.toString());
                    notifyItemChanged(positionholder); // Notify the adapter to update the view
                    // Handle long click action here
                    return true; // Return true to consume the long click event
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


