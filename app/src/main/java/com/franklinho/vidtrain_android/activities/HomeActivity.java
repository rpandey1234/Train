package com.franklinho.vidtrain_android.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.fragments.FragmentPagerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {
    private static final int VIDEO_CAPTURE = 101;

    @Bind(R.id.viewpager) ViewPager viewPager;
    @Bind(R.id.sliding_tabs) TabLayout tabLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.viewReveal) View viewReveal;
    @Bind(R.id.fabCreate) FloatingActionButton fabCreate;
    Transition.TransitionListener transitionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(),
                HomeActivity.this));

        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);
        fabCreate.setVisibility(View.INVISIBLE);

        transitionListener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                enterReveal();

            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.miProfile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showCreateFlow(View view) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            //startActivityForResult(Utility.getVideoIntent(), VIDEO_CAPTURE);
            int cx = (int) fabCreate.getX() + fabCreate.getWidth()/2;
            int cy = (int) fabCreate.getY() + fabCreate.getHeight()/2;

//            float finalRadius = (float) Math.hypot(cx, cy);
            float finalRadius = getWindow().getDecorView().getHeight();
            Animator anim = ViewAnimationUtils.createCircularReveal(viewReveal, cx, cy, 0, finalRadius);
            anim.setInterpolator(new AccelerateInterpolator());
            viewReveal.setVisibility(View.VISIBLE);
            anim.start();
            final Intent in = new Intent(getBaseContext(), VideoCaptureActivity.class);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    startActivityForResult(in, 1);
                }
            });



        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "Success",  Toast.LENGTH_LONG).show();
//        if (requestCode == VIDEO_CAPTURE) {
//            if (resultCode == RESULT_OK) {
//                Intent i = new Intent(this, CreationDetailActivity.class);
//                i.putExtra("videoPath", Utility.getOutputMediaFile().getPath());
//                startActivity(i);
//            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(this, "Video recording cancelled.",  Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(this, "Failed to record video",  Toast.LENGTH_LONG).show();
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewReveal.setVisibility(View.GONE);
    }

    void enterReveal() {
        View myView = fabCreate;

        int cx = myView.getMeasuredWidth() / 2;
        int cy = myView.getMeasuredHeight() / 2;

        int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;


        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);


        myView.setVisibility(View.VISIBLE);
        anim.setInterpolator(new BounceInterpolator());
        anim.start();


        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                getWindow().getEnterTransition().removeListener(transitionListener);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
