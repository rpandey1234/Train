package com.franklinho.vidtrain_android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.fragment.AddVideoToVidTrainFragment;
import com.franklinho.vidtrain_android.fragment.NewVidTrainFragment;

public class CustomCameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            if (getIntent().getExtras() != null){
                if (getIntent().getExtras().getBoolean("newVidTrain") == true) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, AddVideoToVidTrainFragment.newInstance(this))
                            .commit();
                } else {

                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, NewVidTrainFragment.newInstance())
                            .commit();
                }

            } else {

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, NewVidTrainFragment.newInstance())
                        .commit();
            }

        }

    }
}
