package com.example.SuperTrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;

import com.example.SuperTrack.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements   MusicListAdapter.OnSwipeToDeleteListener {
    // Your existing code


    private MusicListAdapter adapter;
    RecyclerView recyclerView;
    TextView noMusicTextView;
    ArrayList<AudioModel> songsList = new ArrayList<>();

    private static final String PREFS_NAME = "MyPrefs";
    private static final String SONGS_LIST_KEY = "songsList";

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        songsList = new ArrayList<>();
        
        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);
        
        retrieveSongsListFromSharedPreferences();
        // Inside your MainActivity's onCreate method
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this,adapter);

        adapter = new MusicListAdapter(songsList, getApplicationContext(),   this); // Pass MainActivity as OnSwipeToDeleteListen


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        Log.i("MainActivity","Oncreate");
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
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                Log.i("retrieveSongsListFromSharedPreferences", "notifyDataSetChanged ");
            }
        }
    }

    private void setupRecyclerView() {
      // MusicListAdapter adapter = new MusicListAdapter(songsList,getApplicationContext(),this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /*private void showPlaybackOptionsDialog(AudioModel clickedSong) {


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_playback_options, null);
            builder.setView(dialogView);

            // Find views in the dialog layout and set up listeners
            RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group);
            RadioButton startNowRadio = dialogView.findViewById(R.id.radio_start_now);
            RadioButton startLaterRadio = dialogView.findViewById(R.id.radio_start_later);
            RadioButton stopLaterRadio = dialogView.findViewById(R.id.radio_stop_later);
            // Other views for specifying times (EditTexts, TimePickers, etc.)

            // Set up listeners for radio buttons
            startNowRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Handle Start Now option
                if (isChecked) {
                    // Start playback immediately
                    playMusic();
                    alertDialog.dismiss();
                }
            });

            startLaterRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Handle Start Later option
                if (isChecked) {
                    // Show views for specifying start time
                    // Hide views for specifying stop time
                }
            });

            stopLaterRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Handle Stop Later option
                if (isChecked) {
                    // Show views for specifying stop time
                    // Hide views for specifying start time
                }
            });

            // Set up other listeners for specifying times (e.g., TimePicker, Button clicks, etc.)

            alertDialog = builder.create();
            alertDialog.show();

    }*/


    public int count = 0;
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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("onSaveInstanceState","songlist: "+songsList.toString());
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

            }
        }
    }
    public void populateSongsList(View view) {
        // Put the code here to populate the songsList
        // This code should be similar to what you removed from the onCreate method
        // You can modify it to match your requirements
        Log.i("populateSongsList", " ");
        // After populating the songsList, make sure to update the RecyclerView adapter
        adapter.notifyDataSetChanged();

        // Call the setupRecyclerView() function here
        setupRecyclerView();

    }
    /*private void processSelectedUris(ArrayList<Uri> selectedUris) {
        for (Uri uri : selectedUris) {
            String title = getAudioTitleFromUri(uri);
            String path = getPathFromUri(uri);
            long duration = getAudioDurationFromUri(uri); // Retrieve duration for each Uri

            AudioModel audioModel = new AudioModel(path, title, String.valueOf(duration));
            songsList.add(audioModel);
        }

        if (songsList.size() > 0) {
            noMusicTextView.setVisibility(View.GONE);
            *//*recyclerView.setAdapter(new MusicListAdapter(songsList, getApplicationContext(),this));*//*
            populateSongsList(recyclerView);
        } else {
            noMusicTextView.setVisibility(View.VISIBLE);
        }
    }*/
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

        AudioModel audioModel = new AudioModel(path, title, String.valueOf(duration));
        songsList.add(audioModel);
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

              Log.i("onResume", " ");

        if (adapter != null) {
                adapter.notifyDataSetChanged();
            Log.i("onResume", "notifyDataSetChanged ");
                            }
        populateSongsList(recyclerView);

        }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("onPause", " ");
        saveSongsListToSharedPreferences();
    }
}