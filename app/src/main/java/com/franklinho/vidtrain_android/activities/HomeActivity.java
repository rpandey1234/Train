package com.franklinho.vidtrain_android.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.fragments.FragmentPagerAdapter;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {
    private static final int VIDEO_CAPTURE = 101;
    public static final String UNIQUE_ID_INTENT = "UNIQUE_ID";

    @Bind(R.id.viewpager) ViewPager viewPager;
    @Bind(R.id.sliding_tabs) TabLayout tabLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.viewReveal) View viewReveal;
    @Bind(R.id.fabCreate) FloatingActionButton fabCreate;
    private Transition.TransitionListener transitionListener;
    boolean revealStarted = false;
    MenuItem miActionProgressItem;
    String uniqueId = Long.toString(System.currentTimeMillis());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        relayoutViewPager();


        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

//        actionBar.setLogo(R.layout.space_between_icon);
//        actionBar.setDisplayUseLogoEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
//        actionBar.setDefaultDisplayHomeAsUpEnabled(false);
//        actionBar.setHomeAsUpIndicator(0);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setDisplayShowTitleEnabled(false);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(),
                HomeActivity.this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    removePagerPadding();
                } else {
                    relayoutViewPager();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);
        fabCreate.setVisibility(View.INVISIBLE);


        fabCreate.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                fabCreate.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                enterReveal();
            }
        });




//        transitionListener = new Transition.TransitionListener() {
//            @Override
//            public void onTransitionStart(Transition transition) {
//
//            }
//
//            @Override
//            public void onTransitionEnd(Transition transition) {
//                enterReveal();
//            }
//
//            @Override
//            public void onTransitionCancel(Transition transition) {
//
//            }
//
//            @Override
//            public void onTransitionPause(Transition transition) {
//
//            }
//
//            @Override
//            public void onTransitionResume(Transition transition) {
//
//            }
//        };
//
//        getWindow().getEnterTransition().addListener(transitionListener);
//

    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View v = super.onCreateView(name, context, attrs);
        return v;
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

            int cx = (int) fabCreate.getX() + fabCreate.getWidth()/2;
            int cy = (int) fabCreate.getY() + fabCreate.getHeight()/2;

//            float finalRadius = (float) Math.hypot(cx, cy);
            float finalRadius = getWindow().getDecorView().getHeight();
            Animator anim = ViewAnimationUtils.createCircularReveal(viewReveal, cx, cy, 0, finalRadius);
            anim.setInterpolator(new AccelerateInterpolator());
            viewReveal.setVisibility(View.VISIBLE);
            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    Intent intent = new Intent(getBaseContext(), VideoCaptureActivity.class);
                    intent.putExtra(UNIQUE_ID_INTENT, uniqueId);
                    startActivityForResult(intent, VIDEO_CAPTURE);
//                    startActivityForResult(Utility.getVideoIntent(), VIDEO_CAPTURE);
                }
            });



        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Intent i = new Intent(this, CreationDetailActivity.class);
                Log.d(VidtrainApplication.TAG, "from HomeActivity: " + uniqueId);
                i.putExtra("videoPath", Utility.getOutputMediaFile(uniqueId).getPath());
                startActivity(i);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",  Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",  Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        relayoutViewPager();
        viewReveal.setVisibility(View.GONE);
        fabCreate.setVisibility(View.INVISIBLE);


        fabCreate.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                fabCreate.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                enterReveal();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
//        fabCreate.setVisibility(View.INVISIBLE);
        exitReveal();
    }

    public void enterReveal() {

        int cx = fabCreate.getMeasuredWidth() / 2;
        int cy = fabCreate.getMeasuredHeight() / 2;

        int finalRadius = Math.max(fabCreate.getWidth(), fabCreate.getHeight()) / 2;


        Animator anim = ViewAnimationUtils.createCircularReveal(fabCreate, cx, cy, 0, finalRadius);


        fabCreate.setVisibility(View.VISIBLE);
        anim.setInterpolator(new BounceInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                getWindow().getEnterTransition().removeListener(transitionListener);
            }
        });

        anim.start();



    }

    public void exitReveal() {


        int cx = fabCreate.getMeasuredWidth() / 2;
        int cy = fabCreate.getMeasuredHeight() / 2;

        int initialRadius = Math.max(fabCreate.getWidth(), fabCreate.getHeight()) / 2;


        Animator anim = ViewAnimationUtils.createCircularReveal(fabCreate, cx, cy, initialRadius, 0);


        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabCreate.setVisibility(View.INVISIBLE);
            }
        });


        anim.start();
    }


    public void transitionSafeEnterReveal() {
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

        getWindow().getEnterTransition().addListener(transitionListener);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        return super.onPrepareOptionsMenu(menu);
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    public void relayoutViewPager() {
        ViewTreeObserver vto = toolbar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tabLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Rect rectangle = new Rect();
                Window window = getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                int statusBarHeight = rectangle.top;
                int contentViewTop =
                        window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int titleBarHeight = contentViewTop - statusBarHeight;


                int relativeBottom = getRelativeBottom(toolbar);
                viewPager.setPadding(0, 0, 0, relativeBottom + tabLayout.getHeight() - 52);
                viewPager.invalidate();
                viewPager.requestLayout();

            }
        });
    }

    public void removePagerPadding() {
        viewPager.setPadding(0, 0, 0, 0);
        viewPager.invalidate();
        viewPager.requestLayout();
    }

    private int getRelativeBottom(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeBottom((View) myView.getParent());
    }

}
