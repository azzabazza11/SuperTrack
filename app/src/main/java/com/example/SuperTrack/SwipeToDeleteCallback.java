package com.example.SuperTrack;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private MusicListAdapter adapter; // Reference to your adapter

    final MusicListAdapter.OnSwipeToDeleteListener listener;
    /*public SwipeToDeleteCallback(MusicListAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }*/
    public SwipeToDeleteCallback(MusicListAdapter.OnSwipeToDeleteListener listener, MusicListAdapter adapter) {
        super(0, ItemTouchHelper.LEFT); // You can adjust these parameters as needed
        this.listener = listener;
        this.adapter = adapter;
    } public SwipeToDeleteCallback(MusicListAdapter.OnSwipeToDeleteListener listener) {
        super(0, ItemTouchHelper.LEFT); // You can adjust these parameters as needed
        this.listener = listener;

    }
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Log.i(getClass().toString(),"onSwiped()!2");
        if (adapter != null) {
            adapter.deleteItem(position);
        }

        if (listener != null) {
            listener.onItemSwiped(position);
        }
    }
  /*  @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Log.i(getClass().toString(),"onSwiped()!2");
        int position = viewHolder.getAdapterPosition();

        adapter.deleteItem(position);
    }*/

}
