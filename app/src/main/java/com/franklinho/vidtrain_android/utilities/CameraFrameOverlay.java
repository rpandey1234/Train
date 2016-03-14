package com.franklinho.vidtrain_android.utilities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by VRAJA03 on 3/13/2016.
 */

public class CameraFrameOverlay extends View {
    private Paint mLinePaint;

    public CameraFrameOverlay(Context context) {
        super(context);

        initView();
    }

    public CameraFrameOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    public CameraFrameOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView();
    }

    private void initView() {
        mLinePaint = new Paint();

        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(UiUtils.dipsToPixels(getContext(), 8));
        mLinePaint.setARGB(255, 255, 255, 255); // white
    }

    @Override
    public void draw(Canvas canvas) {
        // top horizontal
        canvas.drawLine(0f, 0f, getWidth(), 0f, mLinePaint);
        // top left vertical
        canvas.drawLine(0f, 0f, 0f, UiUtils.dipsToPixels(getContext(), 60),
                mLinePaint);
        // top right vertical
        canvas.drawLine(getWidth(), 0f, getWidth(),
                UiUtils.dipsToPixels(getContext(), 60), mLinePaint);

        // bottom horizontal
        canvas.drawLine(0f, getHeight(), getWidth(), getHeight(), mLinePaint);
        // bottom left vertical
        canvas.drawLine(0f, getHeight(), 0f,
                getHeight() - UiUtils.dipsToPixels(getContext(), 60),
                mLinePaint);
        // bottom right vertical
        canvas.drawLine(getWidth(), getHeight(), getWidth(), getHeight()
                - UiUtils.dipsToPixels(getContext(), 60), mLinePaint);

        super.draw(canvas);
    }
}
