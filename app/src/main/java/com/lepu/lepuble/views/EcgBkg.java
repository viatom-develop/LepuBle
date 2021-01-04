package com.lepu.lepuble.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;


import androidx.annotation.ColorRes;

import com.lepu.lepuble.R;
import com.lepu.lepuble.ble.obj.Er1DataController;

public class EcgBkg extends View {
    private TextPaint mTextPaint;
    private Paint bPaint;
    private Paint redPaint;
    private Paint bkg;
    private Paint bkg_paint_1;
    private Paint bkg_paint_2;
    private float mTextWidth;
    private float mTextHeight;

    private Canvas canvas;

    public int mWidth;
    public int mHeight;
    public float mTop;
    public float mBottom;
    public int mBase;

    private int maxIndex;

    @ColorRes
    private int bgColor = R.color.colorWhite;
//    private int bgColor = R.color.ecg_bkg;
    @ColorRes
    private int gridColor5mm = R.color.color_ecg_grid_5mm;
//    private int gridColor5mm = R.color.ecg_line_1;
    @ColorRes
    private int gridColor1mm = R.color.color_ecg_grid_1mm;
//    private int gridColor1mm = R.color.ecg_line_2;

    public EcgBkg(Context context) {
        super(context);
        init(null, 0);
    }

    public EcgBkg(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EcgBkg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EcgView, defStyle, 0);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        iniPaint();
    }

    private void iniPaint() {
        redPaint = new Paint();
        redPaint.setColor(getColor(R.color.red_m));
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(4.0f);

        bkg = new Paint();
        bkg.setColor(getColor(bgColor));

        bkg_paint_1 = new Paint();
        bkg_paint_1.setStyle(Paint.Style.STROKE);
        bkg_paint_1.setStrokeWidth(2.0f);

        bkg_paint_2 = new Paint();
        bkg_paint_2.setColor(getColor(gridColor1mm));
        bkg_paint_2.setStyle(Paint.Style.STROKE);
        bkg_paint_2.setStrokeWidth(1.0f);

        bPaint = new Paint();
        bPaint.setTextAlign(Paint.Align.LEFT);
        bPaint.setTextSize(32);
        bPaint.setColor(getColor(R.color.Black));
        bPaint.setStyle(Paint.Style.STROKE);
        bPaint.setStrokeWidth(4.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        iniParam();

//        int paddingLeft = getPaddingLeft();
//        int paddingTop = getPaddingTop();
//        int paddingRight = getPaddingRight();
//        int paddingBottom = getPaddingBottom();

        this.canvas = canvas;
        drawBkg(canvas);
    }

    private void iniParam() {

        maxIndex = Er1DataController.maxIndex;

        //        maxIndex = (int) (getWidth() / 2 / SPEED * 2);
        //        dataSrc = new byte[maxIndex*2];

//        if (dataSrc == null) {
//            dataSrc = new float[maxIndex];
//        }

        //        float pxHeight = 20

        mWidth = getWidth();
        mHeight = getHeight();

        mBase = (mHeight / 2);
        mTop = (float) (mBase - 20 / Er1DataController.mm2px);
        mBottom = (float) (mBase + 20 / Er1DataController.mm2px);
    }

    private void drawBkg(Canvas canvas) {
//        Path path = new Path();
//        path.moveTo(0, mBase);
//        path.lineTo(0, mBottom);
//        path.lineTo(mWidth, mBottom);
//        path.lineTo(mWidth, mTop);
//        path.lineTo(0, mTop);
//        path.lineTo(0, mBase);
//        path.lineTo(mWidth, mBase);
//        canvas.drawPath(path, redPaint);

        canvas.drawColor(getColor(bgColor));

        bkg.setColor(getColor(bgColor));
        bkg_paint_1.setColor(getColor(gridColor5mm));
        bkg_paint_2.setColor(getColor(gridColor1mm));

        // 1mm y
        for (int i = 0; i < mHeight/2/(1/Er1DataController.mm2px); i ++) {
            Path p = new Path();
            p.moveTo(0, mBase + i*(1/Er1DataController.mm2px));
            p.lineTo(mWidth, mBase + i*(1/Er1DataController.mm2px));

            p.moveTo(0, mBase - i*(1/Er1DataController.mm2px));
            p.lineTo(mWidth, mBase - i*(1/Er1DataController.mm2px));

            canvas.drawPath(p, bkg_paint_2);
        }

        // 5mm y
        for (int i = 0; i < mHeight/2/(5/Er1DataController.mm2px); i++) {
            Path p = new Path();
            p.moveTo(0, mBase + i*(5/Er1DataController.mm2px));
            p.lineTo(mWidth, mBase + i*(5/Er1DataController.mm2px));

            p.moveTo(0, mBase - i*(5/Er1DataController.mm2px));
            p.lineTo(mWidth, mBase - i*(5/Er1DataController.mm2px));
            canvas.drawPath(p, bkg_paint_1);
        }

        // 20 mm y
        for (int i = 0; i < mHeight/2/(20/Er1DataController.mm2px); i++) {
//            Path p = new Path();
//            p.moveTo(0, mBase + i*(20/DataController.mm2px));
//            p.lineTo(mWidth, mBase + i*(20/DataController.mm2px));
//
//            p.moveTo(0, mBase - i*(20/DataController.mm2px));
//            p.lineTo(mWidth, mBase - i*(20/DataController.mm2px));
//
//            canvas.drawPath(p, redPaint);
        }

        // 1mm x
        for (int i = 0; i < mWidth/(1/ Er1DataController.mm2px) + 1; i++) {
            Path p = new Path();
            p.moveTo(i/ Er1DataController.mm2px, 0);
            p.lineTo(i/ Er1DataController.mm2px, mHeight);
            canvas.drawPath(p, bkg_paint_2);
        }

        // 5mm x
        for (int i = 0; i < mWidth/(5/ Er1DataController.mm2px) + 1; i++) {
            Path p = new Path();
            p.moveTo(i*5/ Er1DataController.mm2px, 0);
            p.lineTo(i*5/ Er1DataController.mm2px, mHeight);
            canvas.drawPath(p, bkg_paint_1);
        }


        // 25mm x
//        for (int i = 0; i<mWidth/(25/ DataController.mm2px) + 1; i++) {
//            Path p = new Path();
//            p.moveTo(i*25/ DataController.mm2px, 0);
//            p.lineTo(i*25/ DataController.mm2px, mHeight);
//            canvas.drawPath(p, redPaint);
//        }

    }

    private int getColor(int resource_id) {
        return getResources().getColor(resource_id);
    }

    public void setBgColor(@ColorRes int bgColor) {
        this.bgColor = bgColor;
    }

    public void setGridLine5mmColor(@ColorRes int gridColor5mm) {
        this.gridColor5mm = gridColor5mm;
    }

    public void setGridLine1mmColor(@ColorRes int gridColor1mm) {
        this.gridColor1mm = gridColor1mm;
    }

    public void setColors(@ColorRes int bgColor, @ColorRes int gridColor5mm, @ColorRes int gridColor1mm) {
        this.bgColor = bgColor;
        this.gridColor5mm = gridColor5mm;
        this.gridColor1mm = gridColor1mm;
        invalidate();
    }
}
