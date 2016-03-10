package com.franklinho.vidtrain_android.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.fragment.PopularFragment;
import com.franklinho.vidtrain_android.fragment.UserInfoFragment;
import com.franklinho.vidtrain_android.fragment.VidTrainListFragment;

import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {
    PopularFragment userProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            UserInfoFragment userInfoFragment = UserInfoFragment.newInstance();
            ft.replace(R.id.flUserInfo, userInfoFragment);

            // TODO: update this to be user's vidtrains
            userProfileFragment = PopularFragment.newInstance();
            ft.replace(R.id.flUserContent, userProfileFragment);

            ft.commit();
        }
    }
}
