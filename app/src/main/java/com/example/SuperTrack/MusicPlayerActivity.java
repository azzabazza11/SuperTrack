package com.example.SuperTrack;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.bumptech.glide.Glide;

public class MusicPlayerActivity extends AppCompatActivity implements MainActivity.MusicControlListener {
    // Implement the methods from the interface here
    // ...


    TextView titleTv, currentTimeTv, totalTimeTv, countDownTv, countDownTv2;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon;
    private SurfaceView backgroundSurfaceView;

    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    private AlertDialog alertDialog;

    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    final Handler handler = new Handler();
    int x = 0;
    Calendar calendarTimeSelected = Calendar.getInstance();
    private final Handler countdownHandler = new Handler();
    private Calendar selectedEndTimeCal = Calendar.getInstance();

    private Calendar selectedStartTimeCal = Calendar.getInstance();

    boolean isCountingdown, isStartingLater;

    private PowerManager.WakeLock wakeLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        Log.i("MusicPlayerActivity", "Oncreate");
        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        countDownTv = findViewById(R.id.CountDownTv);
        countDownTv2 = findViewById(R.id.CountDownTv2);

        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);
        backgroundSurfaceView = findViewById(R.id.backgroundSurfaceView);

        titleTv.setSelected(true);

        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");
        // Register the BroadcastReceiver

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, filter);

        //wakelock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YourApp:WakeLockTag");
        // Calculate the delay time for countdown
        // Close the dialog
        // Set up the countdown on the MusicPlayerActivity
        MusicStateSingleton.getInstance().setSonglist(songsList);
        Log.i("TAG", "MusicStateSingleton.getInstance().setSonglist(songsList);" + songsList.toString());

        Log.i("TAG", "MusicStateSingleton.getInstance().getSonglist();" + MusicStateSingleton.getInstance().getArraySonglist().toString());

        MainActivity.MusicControlListener musicControlListener = this;

        ShowOptionsDialog();

        setResourcesWithMusic();

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));
                    ImageView imageView = findViewById(R.id.music_icon_big);

                    if (mediaPlayer.isPlaying()) {
                        pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);

                        // Apply rotation animation
                        //musicIcon.setRotation(x++);
                        int parentWidth = ((View) imageView.getParent()).getWidth();
                        int imageViewWidth = imageView.getWidth();
                        float offsetX = (parentWidth - imageViewWidth) / 2.0f;

                        imageView.setScaleType(ImageView.ScaleType.MATRIX);
                        // Calculate the translation needed to center the image horizontally


                        //Crop image
                        int offsetY = 50;
                        Matrix matrix = new Matrix();
                        imageView.setScaleType(ImageView.ScaleType.MATRIX);
                        matrix.postTranslate(offsetX, offsetY); // Apply translation to center the image and Set the offset to crop from the bottom

                        imageView.setImageMatrix(matrix);
                        //imageView.setRotation(x++);
                        // Apply enlargement and shrinking animation



                    } else {
                        pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                        musicIcon.setRotation(0);
                    }
                }
                new Handler().postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

   /* private void startForeground(int i, Notification notification) {
    }*/
private void setupAnimation(){
// Get the ImageView you want to animate
    ImageView imageView = findViewById(R.id.music_icon_big);
// Assuming you have an ImageView called 'imageView'
    imageView.setScaleX(0.0f); // Initial scale X squashes0.5
    imageView.setScaleY(0.0f); // Initial scale Y

// Define the pivot point for scaling (center of the image)
    imageView.setPivotX(imageView.getWidth() / 2.0f);
    imageView.setPivotY(imageView.getHeight() / 2.0f);

// Calculate the translation needed to center the image horizontally
    float translateX = (imageView.getWidth() / 4.0f); // Half of half of the image width

// Create a scale animation
    ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageView, "scaleX", 1.2f);
    scaleXAnimator.setDuration(7000); // Set the duration in milliseconds
    scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE); // Set the number of times to repeat (1 time)
    scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE); // Reverse the animation after each cycle

    ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageView, "scaleY", 1.2f);
    scaleYAnimator.setDuration(7000); // Set the duration in milliseconds
    scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE); // Set the number of times to repeat (1 time)
    scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE); // Reverse the animation after each cycle

// Create a translation animation
    ObjectAnimator translateXAnimator = ObjectAnimator.ofFloat(imageView, "translationX", 0.0f, translateX);
    translateXAnimator.setDuration(6000); // Set the duration in milliseconds

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleXAnimator, scaleYAnimator,translateXAnimator);

    animatorSet.start();

 /*
    ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageView, "scaleX", 1.0f, 1.5f);
    scaleXAnimator.setDuration(7000); // Set the duration in milliseconds
    scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE); // Set the number of times to repeat (1 time)
    scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE); // Reverse the animation after each cycle

    ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageView, "scaleY", 1.0f, 1.5f);
    scaleYAnimator.setDuration(7000); // Set the duration in milliseconds
    scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE); // Set the number of times to repeat (1 time)
    scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE); // Reverse the animation after each cycle

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleXAnimator, scaleYAnimator);

    animatorSet.start();*/
}
    public void ShowOptionsDialog() {
        Log.i("MusicPlayerActivity","ShowOptions####");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Dialog);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_playback_options, null);
        builder.setView(dialogView);

        Log.i("MusicPlayerActivity","ShowOptionsDialoge");

        // Find radio buttons in the dialog layout
        TextView startNowTv = dialogView.findViewById(R.id.Tv_start_now);
        TextView startLaterTv = dialogView.findViewById(R.id.Tv_start_later);
        TextView stopLaterTv = dialogView.findViewById(R.id.Tv_stop_later);
        TextView titleTv = dialogView.findViewById(R.id.titleTv);

        String songTitle = MusicStateSingleton.getInstance().getSongTitle() ;
        if (songTitle.length() > 30) {
            songTitle = songTitle.substring(0, 30); // Trim the string to 30 characters
        }
        /*ArrayList<AudioModel> song = MusicStateSingleton.getInstance().getArraySonglist();
        AudioModel model = song.get(MusicStateSingleton.getInstance().getCurrentPos());*/
        String duration = MusicStateSingleton.getInstance().getDuration();

        titleTv.setText(songTitle+ "     "+ duration);

        startNowTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i("MusicPlayerActivity","MotionEvent.ACTION_DOWN");

                        // Handle touch down event (similar to onCheckedChanged for RadioButton)
                        isCountingdown = false;

                      //  countDownTv.setVisibility(View.GONE);
                        if (!songsList.isEmpty()) {
                            Log.i("MusicPlayerActivity","!songsList.isEmpty()");

                            // Access the item in the list
                            currentSong = songsList.get(MyMediaPlayer.currentIndex);
                            //SET SINGLETON CURRENT SONGPATH
                            MusicStateSingleton.getInstance().setCurrentSongPath(currentSong.getPath());

                            countDownTv.setVisibility(View.INVISIBLE);
                            playMusic(); // Start playback immediately
                            alertDialog.dismiss(); // Close the dialog
                        } else {
                            // Handle the case when the list is empty
                            Toast.makeText(MusicPlayerActivity.this, "No songs available", Toast.LENGTH_SHORT).show();
                        }
                        return true; // Return true to consume the touch event
                    default:
                        return false; // Return false to allow other touch events to be handled
                }
            }
        });

        startLaterTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Handle touch down event for "Start Later" TextView
                        Calendar selectedStartTimeCal = Calendar.getInstance();

                        // Calculate the next hour
                        int currentHour = selectedStartTimeCal.get(Calendar.HOUR_OF_DAY);
                        int nextHour = currentHour + 1;

                        if (nextHour >= 24) {
                            nextHour = 0; // Wrap around to 0 if it's midnight or later
                        }

                        // Create a TimePickerDialog with the calculated next hour as the initial hour
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                MusicPlayerActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                        selectedStartTimeCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                                        selectedStartTimeCal.set(Calendar.MINUTE, selectedMinute);
                                        displayEndTime(selectedHour, selectedMinute);

                                        long startTimeMillis = selectedStartTimeCal.getTimeInMillis();
                                        long delayMillis = startTimeMillis - System.currentTimeMillis();

                                        alertDialog.dismiss(); // Close the dialog

                                        // Set up the countdown on the MusicPlayerActivity
                                        countDownTv.setVisibility(View.VISIBLE);
                                        countDownTv2.setVisibility(View.VISIBLE);
                                        isCountingdown = true;
                                        isStartingLater = true;
                                        commenceCountdown(delayMillis);
                                    }
                                },
                                nextHour, // Set the calculated next hour as the initial hour
                                0, // Set minutes to 0 if you want to start on the hour
                                false
                        );
                        timePickerDialog.show();
                        return true; // Return true to consume the touch event
                    default:
                        return false; // Return false to allow other touch events to be handled
                }
            }
        });

        stopLaterTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Handle touch down event for "Stop Later" TextView
                        Calendar selectedEndTimeCal = Calendar.getInstance();

                        // Calculate the next hour by adding an hour
                        int currentHour = selectedEndTimeCal.get(Calendar.HOUR_OF_DAY);
                        int nextHour = currentHour + 1;

                        // Round to the closest hour
                        if (nextHour >= 24) {
                            nextHour = 0; // Wrap around to 0 if it's midnight or later
                        }

                        // Create a TimePickerDialog with the calculated next hour as the initial hour
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                MusicPlayerActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                        displayEndTime(selectedHour, selectedMinute);

                                        // Calculate the delay time for countdown
                                        Calendar selectedEndTimeCal = Calendar.getInstance();
                                        selectedEndTimeCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                                        selectedEndTimeCal.set(Calendar.MINUTE, selectedMinute);

                                        long currentMillis = System.currentTimeMillis();
                                        long endTimeMillis = selectedEndTimeCal.getTimeInMillis();

                                        long timeDifferenceMillis = endTimeMillis - currentMillis;

                                        if (Long.parseLong(currentSong.getDuration()) <= timeDifferenceMillis) {
                                            long delayMillis = endTimeMillis - currentMillis - Long.parseLong(currentSong.getDuration());

                                            // Set up the countdown on the MusicPlayerActivity
                                            countDownTv.setVisibility(View.VISIBLE);
                                            countDownTv2.setVisibility(View.VISIBLE);
                                            isCountingdown = true;
                                            isStartingLater = true;
                                            alertDialog.dismiss();

                                            Intent serviceIntent = new Intent(getApplicationContext(), CountdownService.class);
                                            serviceIntent.setAction("START_COUNTDOWN");
                                            serviceIntent.putExtra("DELAY_MILLIS", delayMillis);
                                            serviceIntent.putExtra("ISLater", isStartingLater);
                                            serviceIntent.putExtra("ISCountingDown", isCountingdown);

                                            startService(serviceIntent);

                                            commenceCountdown(delayMillis);
                                        } else {
                                            Toast.makeText(MusicPlayerActivity.this, "Please select a valid duration", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                },
                                nextHour, // Set the calculated next hour as the initial hour
                                0, // Set minutes to 0 if you want to start on the hour
                                false
                        );
                        timePickerDialog.show();
                        return true; // Return true to consume the touch event
                    default:
                        return false; // Return false to allow other touch events to be handled
                }
            }
        });

        // Show the dialog
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onMusicPlayerFinished() {

    }

   /* @Override
    public void PlayNextSong() {

    }

    @Override
    public void PlayPreviousSong() {

    }*/

    // Define the method for formatting the time
    private void displayEndTime(int hourOfDay, int minute) {
        String timeFormat;
        if (hourOfDay >= 12) {
            timeFormat = "PM";
            if (hourOfDay > 12) {
                hourOfDay -= 12;
            }
        } else {
            timeFormat = "AM";
            if (hourOfDay == 0) {
                hourOfDay = 12;
            }
        }

        String formattedTime = String.format(Locale.getDefault(), "%2d:%02d %s", hourOfDay, minute, timeFormat);
        countDownTv2.setText("Track Ends: "+formattedTime);
    }
    private Pair<Integer,Integer> selectTime(){

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                MusicPlayerActivity.this, // Replace with your activity instance
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        // Handle the selected time
                        // This method will be called when the user sets the time in the dialog
                        // You can use the selectedHour and selectedMinute values as needed
                          calendarTimeSelected.set(Calendar.HOUR_OF_DAY,selectedHour);
                          calendarTimeSelected.set(Calendar.MINUTE,selectedHour);


                    }
                },
                hour,
                minute,
                false // Set to true if you want to use the 24-hour format, false for 12-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();

        return new Pair<>(hour, minute) ;
    }
    private BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Device went to sleep or screen locked
                // Start the CountdownService here
                startService(new Intent(MusicPlayerActivity.this, CountdownService.class));
            }
        }
    };



    private void commenceCountdown(long delayMillis) {
        Log.i("MusicPlayer", "commenceCountdown isCountingdown:" + isCountingdown);


        if (isCountingdown) {
            String delayFormatted = convertToMMSS(String.valueOf(delayMillis));

            if (isStartingLater) {
                Toast.makeText(this, "Playback will start in " + delayFormatted + " seconds", Toast.LENGTH_SHORT).show();
                Log.i("MusicPlayer", "commenceCountdown1");
                countDownTv.setText("Playback starts in: " + convertToMMSS(String.valueOf(delayMillis)));
            } else {
                long endTimeMillis = System.currentTimeMillis() + delayMillis;
                String endTimeFormatted =  convertToMMSS(String.valueOf(endTimeMillis));
                String message = "Track Ending at " + endTimeFormatted + " Track starts in " + delayFormatted;

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                Log.i("MusicPlayer", "commenceCountdown2");
                countDownTv.setText(message);
            }
        }

        wakeLock.acquire();
            countdownHandler.postDelayed(new Runnable() {
                long remainingMillis = delayMillis;

                @Override
                public void run() {
                    Log.i("MusicPlayer","postDelayedcommenceCountdown3");
                    Log.i("MusicPlayer","flag isStartingLater" + isStartingLater);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    Calendar calendar = Calendar.getInstance();

                    // Format the calendar's time and get the formatted string
                    String formattedTime = dateFormat.format(calendar.getTime());
                    if (remainingMillis > 0) {
                        if (isStartingLater) {
                            Log.i("MusicPlayer","postDelayedcommenceCountdown4");
                            countDownTv.setText("Playback begins: " + convertToMMSS(remainingMillis));
                        } else {
                            Log.i("MusicPlayer","postDelayedcommenceCountdown5");
                            countDownTv.setText("Finishing "+convertToMMSS(selectedEndTimeCal.getTimeInMillis())+" Playback stops in: " + convertToMMSS(remainingMillis));
                        }

                        remainingMillis -= 1000;
                        countdownHandler.postDelayed(this, 1000);
                    } else {
                        countDownTv.setVisibility(View.GONE);
                        playMusic(); // Start the playback
                    }
                }
            }, 1000);
        }





    void setResourcesWithMusic(){

        currentSong = songsList.get(MyMediaPlayer.currentIndex);
        //SETUP SINGLETON WITH CURRENT SONG
        MusicStateSingleton.getInstance().setCurrentSongPath(currentSong.getPath());
        MusicStateSingleton.getInstance().setSongTitle(currentSong.getTitle());
        //MusicStateSingleton.getInstance().setSonglist(songsList);
        titleTv.setText(currentSong.getTitle());

        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

        pausePlay.setOnClickListener(v-> pausePlay());

        nextBtn.setOnClickListener(v -> PlayNextSong());
        previousBtn.setOnClickListener(v-> PlayPreviousSong());
        countDownTv.setVisibility(View.INVISIBLE);
       // playMusic();


    }
    private Runnable stopPlaybackRunnable = new Runnable() {
        @Override
        public void run() {
            // Code to stop playback at the specified end time
            mediaPlayer.pause(); // Pause playback when the end time is reached
            wakeLock.release();
            countDownTv.setVisibility(View.INVISIBLE);
        }
    };
    public AudioModel getcurrentsong(){
        return currentSong;
    }

    public void playMusic(){
        String songTitle = MusicStateSingleton.getInstance().getSongTitle() ;
        if (songTitle.length() > 30) {
            songTitle = songTitle.substring(0, 30); // Trim the string to 30 characters
        }
        /*ArrayList<AudioModel> song = MusicStateSingleton.getInstance().getArraySonglist();
        AudioModel model = song.get(MusicStateSingleton.getInstance().getCurrentPos());*/
        String duration = MusicStateSingleton.getInstance().getDuration();
            playgif();
            setupAnimation();
            countDownTv2.setText(songTitle +" "+ duration);
            /*ImageView imageView = findViewById(R.id.music_icon_big);
            Animation enlargeShrinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.enlarge_shrink);
            imageView.startAnimation(enlargeShrinkAnimation);*/

            // Delay to allow the enlargement animation to complete
         /*   new Handler().postDelayed(new Runnable() {

                public void run() {
                    // Reset the enlargement animation
                    imageView.clearAnimation();
                }
            }, enlargeShrinkAnimation.getDuration());*/

            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(currentSong.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                seekBar.setProgress(0);
                seekBar.setMax(mediaPlayer.getDuration());
                MusicStateSingleton.getInstance().setMusicPlaying(true);
                Log.d("TAG","MusicPlayerActivity playMusic true");

            } catch (IOException e) {
                e.printStackTrace();
            }

    }

public void playMusic(boolean flag){
        Log.d("TAG","MusicPlayerActivity playMusic");




            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource( currentSong.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (IOException e) {
                e.printStackTrace();
            }





    }


    public void PlayNextSong() {
        Log.d("TAG","PlayNextSong from MusicPlayerActivity" );

        int songlistsize = MusicStateSingleton.getInstance().songListSize();
        Log.d("TAG","SongListSize"+songlistsize);


        if(MyMediaPlayer.currentIndex== songlistsize-1)
            return;
        countdownHandler.removeCallbacksAndMessages(null);
        MyMediaPlayer.currentIndex +=1;

        Log.d("TAG","MyMediaPlayer.currentIndex"+MyMediaPlayer.currentIndex);

        mediaPlayer.reset();

        if(!MusicStateSingleton.getInstance().isFromMainactivity()) {
            setResourcesWithMusic();
            ShowOptionsDialog();

        }else{playMusic();}
    }

    @Override
    public void PlayNextSong(boolean flag) {

        ArrayList<AudioModel> songs = MusicStateSingleton.getInstance().getArraySonglist();

        Log.d("TAG","PlayNextSong from MAIN"  );

        int songlistsize = MusicStateSingleton.getInstance().songListSize();
        Log.d("TAG","SongListSize"+songlistsize);
        Log.d("TAG","current index"+MyMediaPlayer.currentIndex);


        if(MyMediaPlayer.currentIndex== songlistsize-1)
            return;
        countdownHandler.removeCallbacksAndMessages(null);
       // if(MyMediaPlayer.currentIndex < MusicStateSingleton.getInstance().songListSize()-1 ) {
            MyMediaPlayer.currentIndex += 1;
            currentSong = songs.get(MyMediaPlayer.currentIndex);
      //  }
        Log.d("TAG","MyMediaPlayer.currentIndex"+MyMediaPlayer.currentIndex);

        mediaPlayer.reset();

        if(!MusicStateSingleton.getInstance().isFromMainactivity()) {
            setResourcesWithMusic();
            ShowOptionsDialog();

        }else{playMusic(true);}
    }


    public void PlayPreviousSong() {

        int songlistsize = MusicStateSingleton.getInstance().songListSize();
        if(MyMediaPlayer.currentIndex== 0)
            return;
        countdownHandler.removeCallbacksAndMessages(null);
        MyMediaPlayer.currentIndex -=1;
        mediaPlayer.reset();
        playMusic(true);
    }
    public void PlayPreviousSong(boolean flag) {
        ArrayList<AudioModel> songs = MusicStateSingleton.getInstance().getArraySonglist();

        int songlistsize = MusicStateSingleton.getInstance().songListSize();
        if(MyMediaPlayer.currentIndex== 0)
            return;

        countdownHandler.removeCallbacksAndMessages(null);
        currentSong = songs.get(MyMediaPlayer.currentIndex - 1);

        MyMediaPlayer.currentIndex -=1;
        mediaPlayer.reset();
        playMusic(true);

    }

    public void playgif(){


        Glide.with(this)
                .load(R.raw.background_animation) // Replace with your GIF resource
                .into(musicIcon);
    }


    public void pausePlay(){
        Log.i("TAG","MusicPlayerActivity pausePlay()");

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        Log.d("TAG","MediaPlayer is playing before?" + MusicStateSingleton.getInstance().isMusicPlaying());
        Log.d("TAG","isFromMainActivity?" + MusicStateSingleton.getInstance().isFromMainactivity());

        if(mediaPlayer.isPlaying()) {
            MusicStateSingleton.getInstance().setMusicPlaying(false);
            Log.d("TAG","now mediaplayer isplaying: {"+ MusicStateSingleton.getInstance().isMusicPlaying());
            mediaPlayer.pause();
            if(!MusicStateSingleton.getInstance().isFromMainactivity()) {
                ImageView imageView = findViewById(R.id.music_icon_big);
             imageView.clearAnimation();

            }

        }
        else {


            MusicStateSingleton.getInstance().setMusicPlaying(true);

            Log.d("TAG"," MediaPlayer is playing?" + MusicStateSingleton.getInstance().isMusicPlaying());

            mediaPlayer.start();
             if(!MusicStateSingleton.getInstance().isFromMainactivity()) setupAnimation();

        }

        MusicStateSingleton.getInstance().setIsFromMainactivity(false);
    }

    @Override
    protected void onDestroy() {
        Log.d("TAG","MusicPlayerActivityonDestroy");

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        super.onDestroy();

    }

    @Override
    protected void onPause() {
        Log.d("TAG","MusicPlayerActivityonPause");

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        super.onPause();
    }

    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);

        if (hours > 0) {
            return String.format("%2d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%2d:%02d", minutes, seconds);
        }
    }

    public static String convertToMMSS(long duration) {
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
    public static String convertToHHMMSSAM(long duration) {
        Long millis = duration;
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);

        String timeFormat;
        if (hours >= 12) {
            timeFormat = "PM";
            hours = hours - 12;
        } else {
            timeFormat = "AM";
        }

        if (hours == 0) {
            hours = 12;
        }

        return String.format("%2d:%02d:%02d %s", hours, minutes, seconds, timeFormat);
    }
    public static String convertToHHMMSSAM(Calendar calendar) {
        long durationInMillis = calendar.getTimeInMillis();
        long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % TimeUnit.MINUTES.toSeconds(1);

        String timeFormat;
        if (hours >= 12) {
            timeFormat = "PM";
            hours = hours - 12;
        } else {
            timeFormat = "AM";
        }

        if (hours == 0) {
            hours = 12;
        }

        return String.format("%2d:%02d:%02d %s", hours, minutes, seconds, timeFormat);
    }

}