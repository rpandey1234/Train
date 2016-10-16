package com.trainapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckedTextView;

import com.trainapp.R;
import com.trainapp.adapters.VidtrainAdapter.SendVidtrainViewHolder;
import com.trainapp.models.VidTrain;
import com.trainapp.utilities.Utility;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Adapter for vidtrains when sending a new video
 */
public class VidtrainAdapter  extends RecyclerView.Adapter<SendVidtrainViewHolder> {

    private final Activity _activity;
    private Context _context;
    private List<VidTrain> _vidtrains;
    private List<VidTrain> _selectedVidtrains;

    public VidtrainAdapter(Activity activity, List<VidTrain> vidtrains) {
        _context = activity.getApplicationContext();
        _activity = activity;
        _vidtrains = vidtrains;
        _selectedVidtrains = new ArrayList<>();
    }

    @Override
    public SendVidtrainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(_context).inflate(R.layout.send_vidtrain_item, parent, false);
        return new SendVidtrainViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return _vidtrains.size();
    }

    @Override
    public void onBindViewHolder(SendVidtrainViewHolder holder, int position) {
        holder.bind(position);
    }

    public List<VidTrain> getSelectedVidtrains() {
        return _selectedVidtrains;
    }

    public class SendVidtrainViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.vidtrainName) CheckedTextView _vidtrainName;
        private int _position;

        public SendVidtrainViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(int position) {
            _position = position;
            VidTrain vidtrain = _vidtrains.get(position);
            _vidtrainName.setText(vidtrain.getGeneratedTitle(_context.getResources()));
        }

        @OnClick(R.id.vidtrainName)
        public void checkboxClicked(View view) {
            // dismiss the soft keyboard
            View currentFocus = _activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager imm = (InputMethodManager) _activity.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
            VidTrain vidtrain = _vidtrains.get(_position);
            if (_vidtrainName.isChecked()) {
                // Remove it
                int vidtrainIndex = Utility.indexOf(_selectedVidtrains, vidtrain);
                _selectedVidtrains.remove(vidtrainIndex);
            } else {
                _selectedVidtrains.add(vidtrain);
            }
            _vidtrainName.toggle();
        }
    }
}
