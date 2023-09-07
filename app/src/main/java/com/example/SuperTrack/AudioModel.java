package com.example.SuperTrack;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;



public class AudioModel implements Parcelable {
    String path;
    String title;
    String duration;

    String groupName;

    public AudioModel(String path, String title, String duration, String groupName) {
        this.path = path;
        this.title = title;
        this.duration = duration;
        this.groupName = groupName;
        Log.i("AudioModel","constr");
    }
    // Parcelable constructor
    protected AudioModel(Parcel in) {
        path = in.readString();
        title = in.readString();
        duration = in.readString();
        groupName = in.readString();
    }

    public static final Creator<AudioModel> CREATOR = new Creator<AudioModel>() {
        @Override
        public AudioModel createFromParcel(Parcel in) {
            return new AudioModel(in);
        }

        @Override
        public AudioModel[] newArray(int size) {
            return new AudioModel[size];
        }
    };
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(title);
        dest.writeString(duration);
    }
}
