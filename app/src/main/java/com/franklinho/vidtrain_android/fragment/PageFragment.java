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

        View view = null;
        if (mPage == 1) {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        } else if (mPage == 2) {
            view = inflater.inflate(R.layout.fragment_list, container, false);
        } else if (mPage == 3) {
            view = inflater.inflate(R.layout.fragment_popular, container, false);
        }

        LinearLayout linearLayout = (LinearLayout) view;
        return view;
    }
}
