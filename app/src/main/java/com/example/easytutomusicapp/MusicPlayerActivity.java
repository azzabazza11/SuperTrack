package com.example.easytutomusicapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Notification;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTv, currentTimeTv, totalTimeTv, countDownTv;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        Log.i("MusicPlayerActivity", "Oncreate");
        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        countDownTv = findViewById(R.id.CountDownTv);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);

        titleTv.setSelected(true);

        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, filter);

        // Calculate the delay time for countdown
        // Close the dialog
        // Set up the countdown on the MusicPlayerActivity
        TimePickerDialog.OnTimeSetListener endTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Log.i("MusicPlayerActivity", "endTimeSetListenerOntimesET");

                selectedEndTimeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedEndTimeCal.set(Calendar.MINUTE, minute);

                // Calculate the delay time for countdown
                long endTimeMillis = selectedEndTimeCal.getTimeInMillis();
                long delayMillis = endTimeMillis - System.currentTimeMillis() - Long.parseLong(currentSong.getDuration());

                alertDialog.dismiss(); // Close the dialog

                // Set up the countdown on the MusicPlayerActivity
                countDownTv.setVisibility(View.VISIBLE);
                isStartingLater = false;
                commenceCountdown(delayMillis);
            }
        };


        ShowOptionsDialoge();

        setResourcesWithMusic();

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));

                    if (mediaPlayer.isPlaying()) {
                        pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                        musicIcon.setRotation(x++);
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
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                MusicPlayerActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                        Calendar selectedStartTimeCal = Calendar.getInstance();
                                        selectedStartTimeCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                                        selectedStartTimeCal.set(Calendar.MINUTE, selectedMinute);

                                        long startTimeMillis = selectedStartTimeCal.getTimeInMillis();

                                        long delayMillis = startTimeMillis - System.currentTimeMillis();

                                        alertDialog.dismiss(); // Close the dialog

                                        // Set up the countdown on the MusicPlayerActivity
                                        countDownTv.setVisibility(View.VISIBLE);
                                        isCountingdown = true;
                                        isStartingLater = true;
                                        commenceCountdown(delayMillis);
                                    }
                                },
                                selectedStartTimeCal.get(Calendar.HOUR_OF_DAY),
                                selectedStartTimeCal.get(Calendar.MINUTE),
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
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                MusicPlayerActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                        Calendar selectedEndTimeCal = Calendar.getInstance();
                                        selectedEndTimeCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                                        selectedEndTimeCal.set(Calendar.MINUTE, selectedMinute);

                                        long currentMillis = System.currentTimeMillis();
                                        long endTimeMillis = selectedEndTimeCal.getTimeInMillis();

                                        // Calculate the difference between current time and end time
                                        long timeDifferenceMillis = endTimeMillis - currentMillis;

                                        // Check if the desired duration is less than the time difference
                                        if (Long.parseLong(currentSong.getDuration()) <= timeDifferenceMillis) {
                                            long delayMillis = endTimeMillis - System.currentTimeMillis() - Long.parseLong(currentSong.getDuration());

                                            // Set up the countdown on the MusicPlayerActivity
                                            countDownTv.setVisibility(View.VISIBLE);
                                            isCountingdown = true;
                                            isStartingLater = true;
                                            alertDialog.dismiss();
                                            Log.i("MusicPlayer","Intent1");

                                            Intent serviceIntent = new Intent(getApplicationContext(), CountdownService.class);
                                            serviceIntent.setAction("START_COUNTDOWN");
                                            serviceIntent.putExtra("DELAY_MILLIS", delayMillis); // Calculate delayMillis as needed
                                            serviceIntent.putExtra("ISLater",isStartingLater);
                                            serviceIntent.putExtra("ISCountingDown",isCountingdown);

                                            startService(serviceIntent);

                                           commenceCountdown(delayMillis);
                                        } else {
                                            Toast.makeText(MusicPlayerActivity.this, "Please select a valid duration", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                },
                                selectedEndTimeCal.get(Calendar.HOUR_OF_DAY),
                                selectedEndTimeCal.get(Calendar.MINUTE),
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
                            countDownTv.setText("Playback starts in: " + convertToMMSS(remainingMillis));
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
            countDownTv.setVisibility(View.INVISIBLE);
        }
    };


    private void playMusic(){

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }


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
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
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
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static String convertToMMSS(long duration) {
        Long millis = duration;
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

  /*  public static String convertToHHMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }*/
}