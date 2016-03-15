package com.franklinho.vidtrain_android.adapters.holders;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.ProfileActivity;
import com.franklinho.vidtrain_android.activities.VidTrainDetailActivity;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.ParseUser;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by franklinho on 3/10/16.
 */
public class VidTrainViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
    @Bind(R.id.ivCollaborators) public ImageView ivCollaborators;
//    @Bind(R.id.vvPreview) public DynamicVideoPlayerView vvPreview;
//    @Bind(R.id.vvThumbnail) public ImageView vvThumbnail;
    @Bind(R.id.ibtnLike) public ImageButton ibtnLike;
    @Bind(R.id.tvLikeCount) public TextView tvLikeCount;
    @Bind(R.id.tvTitle) public TextView tvTitle;
    @Bind(R.id.tvVideoCount) public TextView tvVideoCount;
    @Bind(R.id.tvTime) public TextView tvTime;
    @Bind(R.id.vpPreview) public ViewPager vpPreview;
    @Bind(R.id.cpIndicator) public CirclePageIndicator cpIndicator;
    public int currentPage = 0;

    public Context context;
    public VidTrain vidTrain;
    public boolean liked = false;

    public VidTrainViewHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        context = itemView.getContext();

        itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vpPreview.post(new Runnable() {
                    public void run() {
                        int width = itemView.getWidth();
//                        int height = width;
//                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
//                        vpPreview.setLayoutParams(lp);
                        ViewGroup.LayoutParams lp = vpPreview.getLayoutParams();
                        lp.height = width;
                        vpPreview.setLayoutParams(lp);
                    }
                });
            }
        });
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(context, VidTrainDetailActivity.class);
        i.putExtra(VidTrainDetailActivity.VIDTRAIN_KEY, vidTrain.getObjectId());
        context.startActivity(i);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ivCollaborators)
    public void onCollaboratorClicked(View view) {
        ParseUser user = vidTrain.getUser();
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID, user.getObjectId());
        context.startActivity(intent);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.ibtnLike)
    public void onVidTrainLiked(View view) {
        final Animation animScale = AnimationUtils.loadAnimation(context, R.anim.anim_scale);
        if (liked) {
            User.postUnlike(ParseUser.getCurrentUser(), vidTrain.getObjectId().toString());
            liked = false;
            ibtnLike.setImageResource(R.drawable.heart_icon);
            int currentLikeCount = vidTrain.getLikes();
            if (currentLikeCount > 0) {
                vidTrain.setLikes(currentLikeCount - 1);
            } else {
                vidTrain.setLikes(0);
            }
        } else {
            User.postLike(ParseUser.getCurrentUser(), vidTrain.getObjectId().toString());
            liked = true;
            ibtnLike.setImageResource(R.drawable.heart_icon_red);
            int currentLikeCount = vidTrain.getLikes();
            vidTrain.setLikes( currentLikeCount + 1);

        }
        view.startAnimation(animScale);
        tvLikeCount.setText(context.getResources().getQuantityString(R.plurals.likes_count,
                vidTrain.getLikes(), vidTrain.getLikes()));

    }
}