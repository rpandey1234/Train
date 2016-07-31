package com.franklinho.vidtrain_android.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class mirrors the Video ParseObject, except it is Parcelable
 */
public class VideoModel implements Parcelable {

    private final String _videoId;
    private final String _userUrl;
    private final String _thumbnailUrl;

    public VideoModel(Video video) {
        _videoId = video.getObjectId();
        _userUrl = video.getUser().getProfileImageUrl();
        _thumbnailUrl = video.getThumbnail().getUrl();
    }

    public String getId() {
        return _videoId;
    }

    public String getUserUrl() {
        return _userUrl;
    }

    public String getThumbnailUrl() {
        return _thumbnailUrl;
    }

    protected VideoModel(Parcel in) {
        _videoId = in.readString();
        _userUrl = in.readString();
        _thumbnailUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_videoId);
        dest.writeString(_userUrl);
        dest.writeString(_thumbnailUrl);
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
