package com.franklinho.vidtrain_android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.franklinho.vidtrain_android.R;

/**
 * Created by sshah on 2/3/16.
 */
public class PageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        int layout;
        switch (mPage) {
            case 0:
                layout = R.layout.fragment_map;
                break;
            case 1:
                layout = R.layout.fragment_list;
                break;
            case 2:
            default:
                layout = R.layout.fragment_popular;
                break;
        }
        View view = inflater.inflate(layout, container, false);

        LinearLayout linearLayout = (LinearLayout) view;
        return view;
    }
}
