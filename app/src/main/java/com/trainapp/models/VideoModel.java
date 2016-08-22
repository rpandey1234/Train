package com.trainapp.models;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.trainapp.R;

import java.util.concurrent.TimeUnit;

/**
 * This class mirrors the Video ParseObject, except it is Parcelable
 */
public class VideoModel implements Parcelable {

    private final String _videoId;
    private final String _userUrl;
    private final String _userId;
    private final String _thumbnailUrl;
    private final String _videoUrl;
    private final long _createdAtTime;

    public VideoModel(Video video) {
        _videoId = video.getObjectId();
        _userId = video.getUser().getObjectId();
        _userUrl = video.getUser().getProfileImageUrl();
        _thumbnailUrl = video.getThumbnail().getUrl();
        _videoUrl = video.getVideoFile().getUrl();
        _createdAtTime = video.getCreatedAt().getTime();
    }

    public String getVideoId() {
        return _videoId;
    }

    public String getUserUrl() {
        return _userUrl;
    }

    public String getUserId() {
        return _userId;
    }

    public String getThumbnailUrl() {
        return _thumbnailUrl;
    }

    public String getVideoUrl() {
        return _videoUrl;
    }

    public long getCreatedAtTime() {
        return _createdAtTime;
    }

    public String getTimeLeft(Resources resources) {
        long timePassed = System.currentTimeMillis() - getCreatedAtTime();
        long timeRemaining = Video.TIME_TO_EXPIRE - timePassed;
        long totalHours = TimeUnit.MILLISECONDS.toHours(timeRemaining);

        int days = (int) TimeUnit.MILLISECONDS.toDays(timeRemaining);
        int hours = (int) (TimeUnit.MILLISECONDS.toHours(timeRemaining) - TimeUnit.DAYS.toHours(
                days));
        int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(timeRemaining) - TimeUnit.HOURS
                .toMinutes(totalHours));
        String daysLeft = resources.getQuantityString(R.plurals.days_plural, days, days);
        String hoursLeft = resources.getQuantityString(R.plurals.hours_plural, hours, hours);
        String minLeft = resources.getQuantityString(R.plurals.minutes_plural, minutes, minutes);
        if (days == 0) {
            return resources.getString(R.string.time_left_multiple, hoursLeft, minLeft);
        } else if (days == 1) {
            return resources.getString(R.string.time_left_multiple, daysLeft, hoursLeft);
        }
        return resources.getString(R.string.time_left, daysLeft);
    }

    protected VideoModel(Parcel in) {
        _videoId = in.readString();
        _userUrl = in.readString();
        _userId = in.readString();
        _thumbnailUrl = in.readString();
        _videoUrl = in.readString();
        _createdAtTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_videoId);
        dest.writeString(_userUrl);
        dest.writeString(_userId);
        dest.writeString(_thumbnailUrl);
        dest.writeString(_videoUrl);
        dest.writeLong(_createdAtTime);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VideoModel> CREATOR =
            new Parcelable.Creator<VideoModel>() {
                @Override
                public VideoModel createFromParcel(Parcel in) {
                    return new VideoModel(in);
                }

                @Override
                public VideoModel[] newArray(int size) {
                    return new VideoModel[size];
                }
            };
}
