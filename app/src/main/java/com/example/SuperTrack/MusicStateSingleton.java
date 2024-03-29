package com.example.SuperTrack;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MusicStateSingleton {
    private static MusicStateSingleton instance;
    private boolean isMusicPlaying;
    private boolean isFromMainactivity;

    private boolean isInSelectionMode;
    private String currentSongTitle;
    private long delayMillis;
    private int currentPos;

    private int songListSize;
    List<Integer> selectedItems = new ArrayList<>();
    private String currentSongPath;
    private String duration;
    private ArrayList<AudioModel> songlist;
    private MusicStateSingleton() {
        // Private constructor to prevent instantiation
    }

    public static MusicStateSingleton getInstance() {
        if (instance == null) {
            instance = new MusicStateSingleton();
        }
        return instance;
    }

    public boolean isMusicPlaying() {
        return isMusicPlaying;
    }

    public void setMusicPlaying(boolean playing) {
        isMusicPlaying = playing;
    }

    public boolean isFromMainactivity() {
        return isFromMainactivity;
    }

    public void setIsFromMainactivity(boolean flag) {
        isFromMainactivity = flag;
    }
    public String getCurrentSongPath() {
        return currentSongPath;
    }

    public void setCurrentSongPath(String path) {
        currentSongPath = path;
    }

    public int songListSize() {
        return songListSize;
    }

    public void setsongListSize(int size) {
        songListSize = size;
    }
    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int pos) {

        currentPos = pos;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(long delay) {

        this.delayMillis = delay;
    }

    public  List<Integer> getSelectedItems() {

        return this.selectedItems;
    }

    public void setSelectedItems(List<Integer> list) {

        this.selectedItems  = list;
    }
    public void emptySelectedItemsList(){

        this.selectedItems = new ArrayList<>();
    }
    public String getDuration() {
        return duration;
    }

    public void setDuration(String dur) {
        duration = dur;
    }
    public void setSongTitle(String songtitle){
        currentSongTitle = songtitle;
    }
    public String getSongTitle(){
        return currentSongTitle;
    }
    public ArrayList<AudioModel> getArraySonglist(){
        return songlist;
    }
    public void setSonglist(ArrayList<AudioModel> list) {
        this.songlist = list; // Assign the parameter to the instance variable
    }

    public void setIsInSelectionMode(boolean b) {
         Log.d("TAG", "setIsInSelectionMode: " + b );
        this.isInSelectionMode = b;
    }
    public boolean isInSelectionMode() {
        return isInSelectionMode;
    }
}