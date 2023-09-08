package com.example.SuperTrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements   MusicListAdapter.OnItemLongClickListener ,  MusicListAdapter.OnSwipeToDeleteListener {
    // Your existing code


    private MusicListAdapter adapter;
    RecyclerView recyclerView;
    TextView noMusicTextView;
    ArrayList<AudioModel> songsList = new ArrayList<>();

    View musicControlsView = null;

    View selectTrackOptionsView = null;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String SONGS_LIST_KEY = "songsList";

    private   String groupName = "default";
    private SharedPreferences sharedPreferences;
    boolean isMusicPlaying = false;
    private boolean methodCalled = false;
    boolean isShowingControls = false;
    boolean isShowingAddTracks = true;

    private MusicListAdapter.OnItemLongClickListener itemLongClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MainActivity","Oncreate");
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        songsList = new ArrayList<>();
        
        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);
        
        retrieveSongsListFromSharedPreferences();
        // Inside your MainActivity's onCreate method
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this,adapter);

        adapter = new MusicListAdapter(songsList, getApplicationContext(),   this); // Pass MainActivity as OnSwipeToDeleteListen


        adapter.setOnItemLongClickListener(this); // "this" refers to the MainActivity implementing the interface

        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        isMusicPlaying = MusicStateSingleton.getInstance().isMusicPlaying();

        inflateMusicControls(isMusicPlaying);


        if(!checkPermission()){
            requestPermission();
            return;

        }


     /*   String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

      String selection = MediaStore.Audio.Media.IS_MUSIC +" != 0";
*/

        if (savedInstanceState != null) {
            // Restore the songsList from the bundle
            songsList = savedInstanceState.getParcelableArrayList("SONGS_LIST");
            Log.i("Oncreate","savedinstancestate: " + songsList.toString());
        }
    }
    public void setOnItemLongClickListener(MusicListAdapter.OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    @Override
    public void onItemLongClick(int position) {
        Log.d("TAG","onItemLongClick MAINACTIVITY: ");

        inflateSelectionOptionsDialog();
    }
    public void onItemSwiped(int position) {
        // Handle item swiped event here
        // You can perform any necessary actions or updates in response to an item being swiped
        if (position >= 0 && position < songsList.size()) {
            songsList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, songsList.size());
            Toast.makeText(this, "1 Item deleted! " + songsList.size() + " left", Toast.LENGTH_SHORT).show();
        }
    }



    public interface MusicControlListener {
        void playMusic();
        void playMusic(boolean flag);

        void PlayNextSong();

        void PlayNextSong(boolean flag);

        void PlayPreviousSong(boolean flag);

        void pausePlay();

        void ShowOptionsDialog();

        void onMusicPlayerFinished();

        AudioModel getcurrentsong();




        // Add other methods you want to call
    }

    private void saveSongsListToSharedPreferences() {
        Log.i("Mainactivity","saveSongsListToSharedPreferences");

        Gson gson = new Gson();
        String songsListJson = gson.toJson(songsList);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SONGS_LIST_KEY, songsListJson);
        editor.apply();
    }
    private void retrieveSongsListFromSharedPreferences() {
        String songsListJson = sharedPreferences.getString(SONGS_LIST_KEY, null);
        Log.i("Mainactivity","retrieveSongsListFromSharedPreferences");
        if (songsListJson != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<AudioModel>>() {}.getType();
            songsList = gson.fromJson(songsListJson, listType);
            MusicStateSingleton.getInstance().setsongListSize(songsList.size());

            if (adapter != null) {
                adapter.notifyDataSetChanged();
                Log.i("TAG", "notifyDataSetChanged ");
            }
        }
    }

    private void setupRecyclerView() {
      // MusicListAdapter adapter = new MusicListAdapter(songsList,getApplicationContext(),this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }




    public int count = 0;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("TAG","songlist: "+songsList.toString());
        // Save the songsList to the bundle
        outState.putParcelableArrayList("SONGS_LIST", songsList);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the songsList from the bundle
        songsList = savedInstanceState.getParcelableArrayList("SONGS_LIST");
        Log.i("onRestoreInstanceState","getParcelableArrayList" + savedInstanceState.getParcelableArrayList("SONGS_LIST").toString());
    }

    public void openFilePicker(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }
    private static final int PICK_AUDIO_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK) {


                    // Process the selected Uris (e.g., read file paths, create AudioModel objects)
                    // Update the songsList and RecyclerView as needed



            if (data != null) {
                ArrayList<Uri> selectedUris = new ArrayList<>();

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        Log.v("URi",uri.toString());
                        selectedUris.add(uri);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    selectedUris.add(uri);
                }

                processSelectedUris(selectedUris);
                populateSongsList(recyclerView);
                inflateMusicControls(isMusicPlaying);

            }
        }
    }
    public void populateSongsList(View view) {
        // Put the code here to populate the songsList
        // This code should be similar to what you removed from the onCreate method
        // You can modify it to match your requirements
        Log.i("TAG", "populateSongsList ");
        // After populating the songsList, make sure to update the RecyclerView adapter
        adapter.notifyDataSetChanged();
        MusicStateSingleton.getInstance().setsongListSize(songsList.size());
        Log.i("TAG", "SongListSize " + songsList.size());
        Log.i("TAG", "SingleSongListSize " +   MusicStateSingleton.getInstance().songListSize());



        // Call the setupRecyclerView() function here
        setupRecyclerView();

        Log.i("TAG", "isMusicPlaying: " + isMusicPlaying);
        isMusicPlaying = MusicStateSingleton.getInstance().isMusicPlaying();
        Log.i("TAG", "isMusicPlaying: " + isMusicPlaying);
        inflateMusicControls(isMusicPlaying);

    }

    private void processSelectedUris(ArrayList<Uri> selectedUris) {
        for (Uri uri : selectedUris) {
            if (isFolder(uri)) {
                // Handle selected folder
                handleSelectedFolder(uri);
            } else {
                // Handle selected file
                handleSelectedFile(uri);
            }
        }
    }

    private boolean isFolder(Uri uri) {
        // Check if the selected Uri represents a folder
        String mimeType = getContentResolver().getType(uri);
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);
    }

    private void handleSelectedFolder(Uri folderUri) {
        // Handle the selected folder, e.g., scan its contents for audio files
        // Update the songsList and RecyclerView as needed
    }

    private void handleSelectedFile(Uri uri) {
        // Handle the selected file, e.g., add it to the songsList
        // Update the songsList and RecyclerView as needed
        String title = getAudioTitleFromUri(uri);
        String path = getPathFromUri(uri);
        long duration = getAudioDurationFromUri(uri); // Retrieve duration for each Uri

        AudioModel audioModel = new AudioModel(path, title, String.valueOf(duration),"default",false);
        songsList.add(audioModel);
        Log.d("TAG","handleSelectedFile" + audioModel.makeString() );
    }



    private String getAudioTitleFromUri(Uri uri) {

       String path = getPathFromUri(uri); // Implement a method to extract the path from the URI
       String title = "";

       if (path != null) {
           Cursor cursor = getContentResolver().query(
                   MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                   new String[]{MediaStore.Audio.Media.TITLE},
                   MediaStore.Audio.Media.DATA + " = ?",
                   new String[]{path},
                   null
           );

           if (cursor != null && cursor.moveToFirst()) {
               int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
               title = cursor.getString(titleIndex);
               cursor.close();
           }
       }

       return title;
   }

    private long getAudioDurationFromUri(Uri uri) {

        String path = getPathFromUri(uri); // Implement a method to extract the path from the URI
        long duration=-1;

        if (path != null) {
            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media.DURATION},
                    MediaStore.Audio.Media.DATA + " = ?",
                    new String[]{path},
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {

                    int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                    Log.i("CursorData1", "DurationIndex: " + durationIndex);
                    duration = cursor.getLong(durationIndex);

                    Log.i("CursorData2", "Duration: " + duration);
                } else {
                    Log.e("CursorData3", "Cursor is null or empty");
                }

            }
        return duration;
    }
    private String getPathFromUri(Uri uri) {
        String filePath = null;

        Log.i("getPathFromUri1", "Uri: " + uri.toString());
        if ("content".equals(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIdx);


                    if (cursor != null) {
                        int rowCount = cursor.getCount();
                        int columnCount = cursor.getColumnCount();
                        Log.i("getPathFromUriCursorData", "RowCount: " + rowCount + ", ColumnCount: " + columnCount);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("getPathFromUriCursorData", "caught e");
            }
        } else if ("file".equals(uri.getScheme())) {
            filePath = uri.getPath();
            Log.e("getPathFromUriCursorData", "else if");
        }
        Log.i("getPathFromUri2", "Uri.getpath: " + uri.getPath());

        return filePath;
    }


    boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(MainActivity.this,"READ PERMISSION IS REQUIRED,PLEASE ALLOW FROM SETTTINGS",Toast.LENGTH_SHORT).show();
        }else
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("TAG", "onResume ");

        if (adapter != null) {
            adapter.notifyDataSetChanged();
            Log.i("onResume", "notifyDataSetChanged ");
        }
        populateSongsList(recyclerView);
        isMusicPlaying = MusicStateSingleton.getInstance().isMusicPlaying();

       inflateMusicControls(isMusicPlaying);


    }
    private void inflateSelectionOptionsDialog() {
        Log.i("TAG", "showSelectionOptionsDialog");

        RelativeLayout container = findViewById(R.id.main_activity);
        LayoutInflater inflater = getLayoutInflater();
        // Initialize it to null

        // Set layout parameters for bottom alignment
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM); // Align to the bottom

        Log.i("TAG", "method called? " +methodCalled);

        //adjustRecyclerview();

        // Find the selectTracksButton in your activity_main layout
        Button selectTracksButton = findViewById(R.id.selectTracksButton);
        if(isShowingAddTracks){selectTracksButton.setVisibility(View.INVISIBLE);}



            selectTrackOptionsView = inflater.inflate(R.layout.selection_options, container, false);
            selectTrackOptionsView.setBackgroundResource(R.drawable.rounded_background);
            selectTrackOptionsView.setClipToOutline(true);
            // Initialize click listeners
            initClickListeners(selectTrackOptionsView);

            // Add the music controls view to the container
            container.addView(selectTrackOptionsView);
            Log.i("TAG", "container.addView(musicControlsView) // added the music controls view");

            int buttonMarginFromTop = getResources().getDimensionPixelSize(R.dimen.button_margin_from_top);
//CONRTOLS LAYOUT
            RelativeLayout.LayoutParams controlLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            controlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            controlLayoutParams.setMargins(5, buttonMarginFromTop, 5, 5);
        selectTrackOptionsView.setLayoutParams(controlLayoutParams);
            // Calculate button position and set layout parameters for selectTracksButton




        // Remove existing music controls view if it's not null
        if (!isMusicPlaying && selectTrackOptionsView != null) {
            Log.i("TAG", "removeView(musicControlsView) // deflate the music controls view");


            container.removeView(musicControlsView);

        }
    }


    private void inflateMusicControls(boolean isMusicPlaying) {
        Log.i("TAG", "inflateMusicControls");

        RelativeLayout container = findViewById(R.id.main_activity);
        LayoutInflater inflater = getLayoutInflater();
       // Initialize it to null

        // Set layout parameters for bottom alignment
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM); // Align to the bottom
        Log.i("TAG", "method called? " +methodCalled);

            if(isMusicPlaying)  adjustRecyclerview();

        // Find the selectTracksButton in your activity_main layout
        Button selectTracksButton = findViewById(R.id.selectTracksButton);

        MusicControlListener musicControlListener = new MusicPlayerActivity();
        Log.i("TAG", "inflateMusicControls is music playing? " +isMusicPlaying);

        // Check if music is playing
        if (isMusicPlaying) {
            Log.i("TAG", "inflateMusicControls // Inflate the music controls view");
            isShowingControls = true;
            // Inflate the music controls view
            musicControlsView = inflater.inflate(R.layout.floating_controls_layout, container, false);
            musicControlsView.setBackgroundResource(R.drawable.rounded_background);
            musicControlsView.setClipToOutline(true);
            // Initialize click listeners
            initClickListeners(musicControlsView, musicControlListener,true);

            // Add the music controls view to the container
            container.addView(musicControlsView);
            Log.i("TAG", "container.addView(musicControlsView) // added the music controls view");

              int buttonMarginFromTop = getResources().getDimensionPixelSize(R.dimen.button_margin_from_top);
//CONRTOLS LAYOUT
            RelativeLayout.LayoutParams controlLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            controlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            controlLayoutParams.setMargins(5, buttonMarginFromTop, 5, 5);
            musicControlsView.setLayoutParams(controlLayoutParams);
            // Calculate button position and set layout parameters for selectTracksButton
// TRACK ADD LAYOUT
            RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            buttonLayoutParams.addRule(RelativeLayout.ABOVE, musicControlsView.getId());
            buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);


            buttonLayoutParams.setMargins(0, 0, 0, 300);
            selectTracksButton.setLayoutParams(buttonLayoutParams);
        }


        Log.i("TAG", "deflate CHECK STATUS"+ (!isMusicPlaying && musicControlsView != null) );
        Log.i("TAG", "deflate CHECK STATUS !isMusicPlaying               "+!isMusicPlaying);
                Log.i("TAG", "deflate CHECK STATUS musicControlsView is Visible "+ (musicControlsView != null));

        // Remove existing music controls view if it's not null
        if (!isMusicPlaying && musicControlsView != null) {
            Log.i("TAG", "removeView(musicControlsView) // deflate the music controls view");


            container.removeView(musicControlsView);

        }
    }

    private void    adjustRecyclerview( ) {
        if (methodCalled ) {
            return; // Skip further execution
        }
Log.d("TAG","ADJUST RECYCLERVIEW");
        // Find the RecyclerView and Controls View
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
      //  View controlsView = findViewById(R.id.floatingConstraint); // Replace with your actual controls view ID

// Calculate the height adjustment (90dp)
        int marginFromBottom = getResources().getDimensionPixelSize(R.dimen.margin_from_bottom); // Replace with your dimension resource

// Get the LayoutParams of the RecyclerView
        ViewGroup.LayoutParams recyclerViewLayoutParams = recyclerView.getLayoutParams();

// Adjust the height by adding the margin from the bottom
        recyclerViewLayoutParams.height = recyclerView.getHeight() - marginFromBottom;

// Apply the modified layout parameters
        recyclerView.setLayoutParams(recyclerViewLayoutParams);
        methodCalled = true;
    }

    private void initClickListeners(View selectTrackOptionsView) {
        // FOR THE SELECTED ITEMS OPTIONS
        TextView groupTv = selectTrackOptionsView.findViewById(R.id.floation_group);
        TextView deleteTv = selectTrackOptionsView.findViewById(R.id.floating_delete);
        TextView cancelTv = selectTrackOptionsView.findViewById(R.id.floating_cancel);

        groupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Group Clicked", Toast.LENGTH_SHORT).show();
               //GROUP NAME ET DIALOG
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.group_name_dialog);

                EditText etGroupName = dialog.findViewById(R.id.etGroupName);
                Button btnCreateGroup = dialog.findViewById(R.id.btnCreateGroup);

                // Set click listener for the "Create Group" button
                btnCreateGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle the group creation here
                        Log.d("TAG"," btnCreateGroup.setOnClickListener public void onClick(View v) {" );

                        String groupName = etGroupName.getText().toString();

                        if (!groupName.isEmpty()) {
                            Log.d("TAG"," !groupName.isEmpty()" );

                            List<Integer> selectedItems = MusicStateSingleton.getInstance().getSelectedItems();

                            Toast.makeText(getApplicationContext(), "Creating group", Toast.LENGTH_SHORT).show();
                            Log.d("TAG"," Selected valuse ");
                            Log.d("TAG", "songsList.size() = " + songsList.size());
                            Iterator<Integer> iterator = selectedItems.iterator();
                            while (iterator.hasNext()) {
                                Integer position = iterator.next();

                                Log.d("TAG", "position = " + position);


                                if (position >= 0 && position < songsList.size()) {
                                    // Get the corresponding AudioModel object
                                    AudioModel selectedAudio = songsList.get(position);

                                    // Update the groupName property to your desired group name
                                    selectedAudio.setGroupName(groupName); // Replace with the actual group name
                                    Log.d("TAG", "Audiomodel to string" + selectedAudio.makeString());

                                    songsList.set(position, selectedAudio);

                                    // Optionally, update any other properties of the selectedAudio if needed
                                }
                            }

                        /*    while (iterator.hasNext()) {
                                Integer position = iterator.next();

                                Log.d("TAG", "position >= 0" + (position >= 0));
                                Log.d("TAG", "position < songsList.size() " + (position < songsList.size()));

                                if (position >= 0 && position < songsList.size()) {
                                    // Get the corresponding AudioModel object
                                    AudioModel selectedAudio = songsList.get(position);

                                    // Update the groupName property to your desired group name
                                    selectedAudio.setGroupName(groupName); // Replace with the actual group name
                                    Log.d("TAG", "Audiomodel to string" + selectedAudio.makeString());

                                    songsList.set(position, selectedAudio);

                                    // Optionally, update any other properties of the selectedAudio if needed
                                }
                            }*/
                            dialog.dismiss(); // Dismiss the dialog after group creation
                            deflateSlectionView();
                            logTrackInfo(songsList); // show log track info
                            isShowingAddTracks =false;
                            Button selectTracksButton = findViewById(R.id.selectTracksButton);
                            if(isShowingAddTracks){selectTracksButton.setVisibility(View.VISIBLE);}

                        } else {
                            Toast.makeText(getApplicationContext(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.show(); // Show the dialog
            }
        });

        deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "deleteTv", Toast.LENGTH_SHORT).show();
                // Handle the delete action here
            }
        });

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "cancelTv", Toast.LENGTH_SHORT).show();
                // Handle the cancel action here
            }
        });

    }


        private void logTrackInfo(List<AudioModel> trackList) {
            if (trackList != null) {
                for (int i = 0; i < trackList.size(); i++) {
                    AudioModel track = trackList.get(i);
                    String trackName = track.getTitle();
                    String groupName = track.getGroupName(); // Assuming you have a method to get the group name from AudioModel

                    Log.d("TAG", "Position: " + i + ", Track: " + trackName + ", Group: " + groupName);
                }
            }
        }


    private void deflateSlectionView() {
    }

    private void initClickListeners(View musicControlsView, MusicControlListener musicControlListener, boolean isMusicPlaying) {
        ImageView playPauseButton = musicControlsView.findViewById(R.id.floatingpause_play);
        ImageView nextButton = musicControlsView.findViewById(R.id.floatingnext);
        ImageView previousButton = musicControlsView.findViewById(R.id.floatingprevious);
        TextView songTitle = musicControlsView.findViewById(R.id.song_titleTv) ;
        songTitle.setText(MusicStateSingleton.getInstance().getSongTitle());
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicStateSingleton.getInstance().isMusicPlaying()) {
                    playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                } else {
                    playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                }

                Log.i("TAG", "playPauseButton ");
                MusicStateSingleton.getInstance().setIsFromMainactivity(true);
                if (musicControlListener != null) {
                    musicControlListener.pausePlay();
                }

            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TAG", "nextButton ");
                MusicStateSingleton.getInstance().setIsFromMainactivity(true);
                Boolean flag = MusicStateSingleton.getInstance().isFromMainactivity();
                /*ArrayList<AudioModel> songlist;
                AudioModel audioModel = musicControlListener.getcurrentsong();*/
                Log.i("TAG", "musicControlListener.getcurrentsong() ");
                //String path = audioModel.getPath();
                if (musicControlListener != null) {
                    musicControlListener.PlayNextSong(flag);
                    Log.i("TAG", "PlayNextSong ");
                }
                adapter.notifyDataSetChanged();
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TAG", "previousButton ");
                MusicStateSingleton.getInstance().setIsFromMainactivity(true);
                Boolean flag = MusicStateSingleton.getInstance().isFromMainactivity();
                if (musicControlListener != null) {
                    musicControlListener.PlayPreviousSong(flag);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("TAG", "onPause ");
        saveSongsListToSharedPreferences();
    }
}