package com.trainapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * This mirrors the Vidtrain ParseObject class, except it is Parcelable
 */
public class VidtrainModel implements Parcelable {

    private final int _videoCount;
    private final String _vidTrainId;
    private final String _title;
    // Normal ordering of video (oldest first)
    private final List<VideoModel> _videoModels;
    // Reverse ordering of videos (newest first)
    private final List<VideoModel> _videoModelsToShow;

    public VidtrainModel(VidTrain vidTrain, int numVideosShown) {
        _vidTrainId = vidTrain.getObjectId();
        _title = vidTrain.getTitle();
        _videoCount = vidTrain.getVideosCount();
        List<Video> videos = vidTrain.getVideos();
        _videoModels = new ArrayList<>();
        _videoModelsToShow = new ArrayList<>();
        for (int i = 0; i < videos.size(); i++) {
            if (i < numVideosShown) {
                Video video = videos.get(videos.size() - 1 - i);
                _videoModelsToShow.add(new VideoModel(video));
            }
            _videoModels.add(new VideoModel(videos.get(i)));
        }
    }

    public int getVideoCount() {
        return _videoCount;
    }

    public String getId() {
        return _vidTrainId;
    }

    public String getTitle() {
        return _title;
    }

    public List<VideoModel> getVideoModels() {
        return _videoModels;
    }

    // Reverse ordering of videos (newest first), and only containing the number of videos to show
    public List<VideoModel> getVideoModelsToShow() {
        return _videoModelsToShow;
    }

    // If this videoId is contained in the videos shown of this vidtrain
    public boolean containsVideo(String videoId) {
        for (VideoModel videoModel : _videoModelsToShow) {
            if (videoModel.getVideoId().equals(videoId)) {
                return true;
            }
        }
        return false;
    }

    protected VidtrainModel(Parcel in) {
        _videoCount = in.readInt();
        _vidTrainId = in.readString();
        _title = in.readString();
        if (in.readByte() == 0x01) {
            _videoModels = new ArrayList<>();
            in.readList(_videoModels, VideoModel.class.getClassLoader());
        } else {
            _videoModels = null;
        }
        if (in.readByte() == 0x01) {
            _videoModelsToShow = new ArrayList<>();
            in.readList(_videoModelsToShow, VideoModel.class.getClassLoader());
        } else {
            _videoModelsToShow = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_videoCount);
        dest.writeString(_vidTrainId);
        dest.writeString(_title);
        if (_videoModels == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(_videoModels);
        }
        if (_videoModelsToShow == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(_videoModelsToShow);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VidtrainModel> CREATOR = new Parcelable.Creator<VidtrainModel>() {
        @Override
        public VidtrainModel createFromParcel(Parcel in) {
            return new VidtrainModel(in);
        }

        @Override
        public VidtrainModel[] newArray(int size) {
            return new VidtrainModel[size];
        }
    };
}
