package com.example.SuperTrack;

import java.util.List;

public class GroupModel {
    private String groupName;
    private boolean isExpanded;
    private List<AudioModel> songList;

    // Constructors
    public GroupModel(String groupName, boolean isExpanded, List<AudioModel> songList) {
        this.groupName = groupName;
        this.isExpanded = isExpanded;
        this.songList = songList;
    }

    public GroupModel() {
        // Default constructor
    }

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public List<AudioModel> getSongList() {
        return songList;
    }

    public void setSongList(List<AudioModel> songList) {
        this.songList = songList;
    }

    // Method to iterate and modify the songList
    public void modifySongList() {
        if (songList != null) {
            for (AudioModel audioModel : songList) {
                // Modify audioModel as needed
                // Example: audioModel.setSomeProperty(newValue);
            }
        }
    }
}