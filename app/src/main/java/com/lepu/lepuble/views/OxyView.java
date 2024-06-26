package com.lepu.lepuble.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.lepu.lepuble.R;
import com.lepu.lepuble.ble.obj.OxyDataController;

public class OxyView extends View {
    private Paint wPaint;
    private Paint pulseBarPaint;

    public int mWidth;
    public int mHeight;
    public float mTop;
    public float mBottom;
    public int mBase;

    private int maxIndex;

    private int[] dataSrc = null;

    public OxyView(Context context) {
        super(context);
        init(null, 0);
    }

    public OxyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OxyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.OxyView, defStyle, 0);

        a.recycle();

        // Set up a default TextPaint object
        iniPaint();
    }

    private void iniPaint() {
        wPaint = new Paint();
        wPaint.setColor(getColor(R.color.colorPrimaryDark));
        wPaint.setStyle(Paint.Style.STROKE);
        wPaint.setStrokeWidth(4.0f);
        wPaint.setTextAlign(Paint.Align.LEFT);
        wPaint.setTextSize(32);

        pulseBarPaint = new Paint();
        pulseBarPaint.setColor(getColor(R.color.Black));
        pulseBarPaint.setStyle(Paint.Style.FILL);

    }

    public void setDataSrc(int[] fs) {
        this.dataSrc = fs;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        iniParam();

        drawWave(canvas);
    }

    private void iniParam() {
//        SPEED = DataController.SPEED;

        /**
         * 留20个位置给脉搏柱
         */
        maxIndex = OxyDataController.maxIndex-20;

//        maxIndex = (int) (getWidth() / 2 / SPEED * 2);
//        dataSrc = new byte[maxIndex*2];

        if (dataSrc == null) {
            dataSrc = OxyDataController.iniDataSrc(maxIndex);
        }

//        float pxHeight = 20

        mWidth = getWidth();
        mHeight = getHeight();

//        mBase = (mHeight / 2);
//        mTop = (float) 0;
        mBottom = mHeight;
    }

    private void drawWave(Canvas canvas) {
        Path p = new Path();
//        p.moveTo(0, mBase);
        for (int i = 0; i < maxIndex; i++) {

            if (i == OxyDataController.index && i < maxIndex-5) {

                float y = mBottom * (dataSrc[i+4]/ 200.0f);
//                y = y > mBottom ? mBottom : y;
//                y = y < mTop ? mTop : y;

                float x = (float) (i+4)/5/ OxyDataController.mm2px;

                p.moveTo(x, y);
                i = i+4;
            } else {
                float y1 = mBottom * (dataSrc[i]/ 200.0f);

//                y1 = y1 > mBottom ? mBottom : y1;
//                y1 = y1 < mTop ? mTop : y1;

                float x1 = (float) i/5/ OxyDataController.mm2px;
                if (i == 0) {
                    p.moveTo(0, y1);
                } else{
                    p.lineTo(x1, y1);
                }
            }
        }

        canvas.drawPath(p, wPaint);

        /**
         * 当前index的值即为脉搏柱的高度
         */
        int pulseL = OxyDataController.maxIndex-15;
        int pulseR = OxyDataController.maxIndex-5;
        float pulseLX = (float) pulseL/5/ OxyDataController.mm2px;
        float pulseRX = (float) pulseR/5/ OxyDataController.mm2px;

        // 纵向y值归一化
        int pulseVal = dataSrc[OxyDataController.index];

        int pulseMax = 121;
        int pulseMin = 60;
//        if (pulseVal == pulseMax || pulseVal == 0)
//            return;
        if (pulseVal < pulseMin) {
            pulseVal = pulseMin;
        } else if (pulseVal > pulseMax) {
            pulseVal = pulseMax;
        }

        float pulseY = ((float) (pulseVal-pulseMin)/(pulseMax-pulseMin))*200.0f;

        canvas.drawRect(pulseLX, pulseY, pulseRX, mBottom, pulseBarPaint);

//        canvas.drawText("" + DataController.index, 0,100,bPaint);
    }

    public void clear() {
//        DataController.clear();
//        dataSrc = new float[maxIndex];
        this.invalidate();
    }

    private int getColor(int resource_id) {
        return getResources().getColor(resource_id);
    }
}
