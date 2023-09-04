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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class MusicPlayerActivity extends AppCompatActivity {

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



        ShowOptionsDialoge();

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

                        imageView.setScaleType(ImageView.ScaleType.MATRIX);
                        // Calculate the translation needed to center the image horizontally
                        float offsetX = (imageView.getWidth()-1000 ) / 2.0f; // imageWidth is the width of your image


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

    animatorSet.start();
}
    private void ShowOptionsDialoge() {
        Log.i("MusicPlayerActivity","ShowOptions####");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Dialog);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_playback_options, null);
        builder.setView(dialogView);

        Log.i("MusicPlayerActivity","ShowOptions####DONE");

        // Find radio buttons in the dialog layout
        TextView startNowTv = dialogView.findViewById(R.id.Tv_start_now);
        TextView startLaterTv = dialogView.findViewById(R.id.Tv_start_later);
        TextView stopLaterTv = dialogView.findViewById(R.id.Tv_stop_later);


        startNowTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Handle touch down event (similar to onCheckedChanged for RadioButton)
                        isCountingdown = false;

                      //  countDownTv.setVisibility(View.GONE);
                        if (!songsList.isEmpty()) {
                            // Access the item in the list
                            currentSong = songsList.get(MyMediaPlayer.currentIndex);
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

        titleTv.setText(currentSong.getTitle());

        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

        pausePlay.setOnClickListener(v-> pausePlay());
        nextBtn.setOnClickListener(v-> playNextSong());
        previousBtn.setOnClickListener(v-> playPreviousSong());
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


    private void playMusic(){
        playgif();
        mediaPlayer.reset();
        setupAnimation();
        //ImageView imageView = findViewById(R.id.music_icon_big);

        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
            //Animation enlargeShrinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.enlarge_shrink);
           // imageView.startAnimation(enlargeShrinkAnimation);

            // Delay to allow the enlargement animation to complete
         /*   new Handler().postDelayed(new Runnable() {

                public void run() {
                    // Reset the enlargement animation
                    imageView.clearAnimation();
                }
            }, enlargeShrinkAnimation.getDuration());*/
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void playgif(){


        Glide.with(this)
                .load(R.raw.background_animation) // Replace with your GIF resource
                .into(musicIcon);
    }
    private void startPlaybackWithDelay(long delayMillis) {
        long startTime = System.currentTimeMillis() + delayMillis;
        long endTime = startTime + mediaPlayer.getDuration(); // End time is when playback naturally ends

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + (int) delayMillis);
            mediaPlayer.start();

            // Schedule a task to stop playback at the end time
            handler.postDelayed(stopPlaybackRunnable, endTime - System.currentTimeMillis());

            // Provide feedback to the user
            Toast.makeText(this, "Playback will start in " + (delayMillis / 1000) + " seconds", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   /* private void updateCountdown() {
        long currentTimeMillis = System.currentTimeMillis();
        long selectedStartTimeMillis = 0;
        long remainingTimeMillis = selectedStartTimeMillis - currentTimeMillis;

        if (remainingTimeMillis > 0) {
        //    countdownTextView.setText("Playback starts in: " + formatTime(remainingTimeMillis));
            countdownHandler.postDelayed(() -> updateCountdown(), 1000);
        } else {
        //    countdownTextView.setVisibility(View.GONE);
            playMusic();
        }
    }*/
  /*  private void startPlaybackWithDelay(long delayMillis) {
        // Calculate the start and end points based on the current time and the delay
        long startTime = System.currentTimeMillis() + delayMillis;
        long endTime = startTime + trackDuration; // Assuming trackDuration is known

        // Start playback using the calculated start and end points
        MyMediaPlayer.getInstance().play(startTime, endTime);

        // Provide feedback to the user
        Toast.makeText(this, "Playback will start in " + (delayMillis / 1000) + " seconds", Toast.LENGTH_SHORT).show();
    }*/


    private void playNextSong(){

        if(MyMediaPlayer.currentIndex== songsList.size()-1)
            return;
        countdownHandler.removeCallbacksAndMessages(null);
        MyMediaPlayer.currentIndex +=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
       ShowOptionsDialoge();

    }

    private void playPreviousSong(){
        if(MyMediaPlayer.currentIndex== 0)
            return;
        countdownHandler.removeCallbacksAndMessages(null);
        MyMediaPlayer.currentIndex -=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
        ShowOptionsDialoge();
    }

    private void pausePlay(){
        ImageView imageView = findViewById(R.id.music_icon_big);

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            imageView.clearAnimation();
        }
        else {
            mediaPlayer.start();
            Animation enlargeShrinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.enlarge_shrink);
            imageView.startAnimation(enlargeShrinkAnimation);
        }
    }

    @Override
    protected void onDestroy() {

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        super.onDestroy();

    }

    @Override
    protected void onPause() {

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        super.onPause();
    }
    /*private String formatTime(long millis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date(millis));
    }*/
   /* private String formatTime(long millis) {
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }*/

  /*  public static String convertToMMSS(String duration){
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }*/
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
  /*  public static String convertToHHMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }*/
}