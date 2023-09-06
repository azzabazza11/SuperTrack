package com.example.SuperTrack;

import java.util.ArrayList;

public class MusicStateSingleton {
    private static MusicStateSingleton instance;
    private boolean isMusicPlaying;
    private boolean isFromMainactivity;
    private String currentSongTitle;
    private int currentPos;

    private int songListSize;

    private String currentSongPath;
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
    public void setSongTitle(String songtitle){
        currentSongTitle = songtitle;
    }
    public String getSongTitle(){
        return currentSongTitle;
    }
    public ArrayList<AudioModel> getSonglist(){
        return songlist;
    }
    public void setSonglist(ArrayList<AudioModel> list) {
        this.songlist = list; // Assign the parameter to the instance variable
    }
}