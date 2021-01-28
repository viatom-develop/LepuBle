package com.lepu.lepuble.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.View;

import com.lepu.lepuble.R;
import com.lepu.lepuble.ble.obj.EcgDataController;


/**
 * normal ecg view, use EcgDataController
 * use in 12 lead
 */
public class EcgView12 extends View {

    private TextPaint mTextPaint;
    private Paint bPaint;
    private Paint linePaint;
    private Paint wPaint;
    private Paint redPaint;
    private Paint redPaint2;
    private Paint redPaint3;
    private float mTextWidth;
    private float mTextHeight;

    public int mWidth;
    public int mHeight;
    public float mTop;
    public float mBottom;
    public int mBase;

//    private static byte[] dataSrc;
//    private static int index = 0;
    private int maxIndex;

    private int lastAmpKey = 0;

    private float[] dataSrc = null;

    private GestureDetector detector;

    public EcgView12(Context context) {
        super(context);
        init(null, 0);
    }

    public EcgView12(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EcgView12(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EcgView, defStyle, 0);

        a.recycle();

        // Set up a default TextPaint object
        iniPaint();
    }

    private void iniPaint() {
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
		mTextPaint.setTextSize(24);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setStrokeWidth((float) 1.5);
		mTextPaint.setColor(getColor(R.color.Black));

        redPaint = new Paint();
        redPaint.setColor(getColor(R.color.red_m));
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(4.0f);

        redPaint2 = new Paint();
        redPaint2.setColor(getColor(R.color.red_b));
        redPaint2.setStyle(Paint.Style.STROKE);
        redPaint2.setStrokeWidth(2.0f);

        redPaint3 = new Paint();
        redPaint3.setColor(getColor(R.color.red_b));
        redPaint3.setStyle(Paint.Style.STROKE);
        redPaint3.setStrokeWidth(1.0f);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setTextSize(15);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth((float) 4.0f);
        linePaint.setColor(getColor(R.color.Black));

        wPaint = new Paint();
        wPaint.setColor(getColor(R.color.color_ecg_line));
        wPaint.setStyle(Paint.Style.STROKE);
        wPaint.setStrokeWidth(4.0f);
        wPaint.setTextAlign(Paint.Align.LEFT);
        wPaint.setTextSize(32);

        bPaint = new Paint();
        bPaint.setTextAlign(Paint.Align.LEFT);
        bPaint.setTextSize(32);
        bPaint.setColor(getColor(R.color.Black));
        bPaint.setStyle(Paint.Style.STROKE);
        bPaint.setStrokeWidth(4.0f);
    }

    public void setDataSrc(float[] fs) {
        this.dataSrc = fs;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        iniParam();

        drawRuler(canvas);

        drawWave(canvas);
    }

    private void iniParam() {
//        SPEED = DataController.SPEED;

        maxIndex = EcgDataController.getMaxIndex();

//        maxIndex = (int) (getWidth() / 2 / SPEED * 2);
//        dataSrc = new byte[maxIndex*2];

        if (dataSrc == null) {
            dataSrc = new float[maxIndex];
        }

//        float pxHeight = 20

        mWidth = getWidth();
        mHeight = getHeight();

        mBase = (mHeight / 2);
        mTop = (float) (mBase - 20/ EcgDataController.getMm2px());
        mBottom = (float) (mBase + 20/ EcgDataController.getMm2px());
    }

    private void drawRuler(Canvas canvas) {
        float chartStartX = (float) (1.0 / (5.0 *  EcgDataController.getMm2px()));
        float standardYTop = mBase - (EcgDataController.getAmpVal() * 0.5f / EcgDataController.getMm2px());
        float standardTBottom = mBase + (EcgDataController.getAmpVal() * 0.5f / EcgDataController.getMm2px());

        canvas.drawLine(chartStartX + 10, standardYTop, chartStartX+10, standardTBottom, linePaint);

        String rulerStr =  "1mV";
        canvas.drawText(rulerStr, chartStartX+15, standardTBottom + 20, mTextPaint);
    }

    private void drawWave(Canvas canvas) {
        Path p = new Path();
        p.moveTo(0, mBase);
        for (int i = 0; i < maxIndex; i++) {

            if (i == EcgDataController.getIndex() && i < maxIndex-5) {

                float y = (mBase - (EcgDataController.getAmpVal()*dataSrc[i+4]/ EcgDataController.getMm2px()));
//                y = y > mBottom ? mBottom : y;
//                y = y < mTop ? mTop : y;

                float x = (float) (i+4)/5/ EcgDataController.getMm2px();

                p.moveTo(x, y);
                i = i+4;
            } else {
                float y1 = mBase - (EcgDataController.getAmpVal()*dataSrc[i]/ EcgDataController.getMm2px());

//                y1 = y1 > mBottom ? mBottom : y1;
//                y1 = y1 < mTop ? mTop : y1;

                float x1 = (float) i/5/ EcgDataController.getMm2px();
                p.lineTo(x1, y1);
            }
        }

        canvas.drawPath(p, wPaint);

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
