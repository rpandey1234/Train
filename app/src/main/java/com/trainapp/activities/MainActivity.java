package com.trainapp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.trainapp.R;
import com.trainapp.fragments.ConversationsFragment;
import com.trainapp.utilities.PermissionHelper;
import com.trainapp.utilities.Utility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String VIDEO_PATH = "VIDEO_PATH";
    public static final int RESULT_TOO_SHORT = 99;

    @Bind(R.id.toolbar)
    Toolbar _toolbar;
    @Bind(R.id.conversations_fragment)
    FrameLayout _conversationsFragment;
    @Bind(R.id.rootLayout)
    CoordinatorLayout _rootLayout;
    @Bind(R.id.fab_create)
    FloatingActionButton _fabCreate;
    @Bind(R.id.viewReveal)
    View _viewReveal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        badgeCount(this, 0);
        ButterKnife.bind(this);
        setSupportActionBar(_toolbar);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.conversations_fragment, ConversationsFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionLogout) {
            new Builder(this)
                    .setTitle(R.string.logout)
                    .setMessage(getString(R.string.logout_confirm))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ParseUser.logOutInBackground(new LogOutCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Intent intent = new Intent(getApplicationContext(),
                                            LogInActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        } else if (id == R.id.actionFriendList) {
            startActivity(new Intent(getApplicationContext(), FriendListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab_create)
    public void startCreateFlow(View view) {
        if (PermissionHelper.allPermissionsAlreadyGranted(this)) {
            goVideoCapture();
        } else {
            PermissionHelper.requestVideoPermission(this, _rootLayout);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQUEST_VIDEO) {
            if (PermissionHelper.allPermissionsGranted(permissions, grantResults)) {
                goVideoCapture();
            }
        }
    }

    private void goVideoCapture() {
        final Intent intent = new Intent(getBaseContext(), VideoCaptureActivity.class);
        intent.putExtra(Utility.UNIQUE_ID_INTENT, Long.toString(System.currentTimeMillis()));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int cx = (int) _fabCreate.getX() + _fabCreate.getWidth() / 2;
            int cy = (int) _fabCreate.getY() + _fabCreate.getHeight() / 2;
            float finalRadius = getWindow().getDecorView().getHeight();
            Animator animator = ViewAnimationUtils.createCircularReveal(_viewReveal, cx, cy, 0,
                    finalRadius);
            animator.setInterpolator(new AccelerateInterpolator());
            _viewReveal.setVisibility(View.VISIBLE);
            animator.start();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    startActivityForResult(intent, Utility.VIDEO_CAPTURE);
                }
            });
        } else {
            startActivityForResult(intent, Utility.VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        _viewReveal.setVisibility(View.GONE);
        if (requestCode == Utility.VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                String uid = data.getStringExtra(Utility.UNIQUE_ID_INTENT);
                Intent i = new Intent(this, CreationDetailActivity.class);
                i.putExtra(VIDEO_PATH, Utility.getOutputMediaFile(uid).getPath());
                startActivity(i);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.recording_cancelled, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_TOO_SHORT) {
                Toast.makeText(this, R.string.not_long_enough, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.recording_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void badgeCount(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        //TODO - I am manually setting to 100. We need to include the logic here
        //TODO - If you pass zero then badger will be in invicible mode

        //count=0;
        count = 100;
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }


    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }


}
