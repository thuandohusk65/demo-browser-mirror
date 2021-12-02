package com.ynsuper.screenmirroring.utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.ynsuper.screenmirroring.R;

/**
 * Created by Administrator on 2017/8/5.
 */
public class WaveView extends View {

    private static final String TAG = "WaveView";

    private int waveColor;

    private int textColor;

    private String waveString = "Ready Connect";

    private int waveCount;

    private Bitmap waveCenterIcon;

    private Paint paint;

    private Paint paintText;

    private int mWidth;

    private int mHeight;

    private int centerX;

    private int centerY;

    private float radius;

    private float innerRadius;

    private int centerIconWidth;

    private int centerIconHeight;

    private float[] waveDegreeArr;

    private boolean isRunning = true;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttrs(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(waveColor);
        paint.setStyle(Paint.Style.FILL);
        waveDegreeArr = new float[waveCount];

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTextSize(50);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(waveColor);
    }

    private void readAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        try {
            waveColor = typedArray.getColor(R.styleable.WaveView_waveColor, 0xffff0000);
            waveCount = typedArray.getInt(R.styleable.WaveView_waveCount, 4);
            textColor = typedArray.getColor(R.styleable.WaveView_textColor, 0xffff0000);
            waveString = typedArray.getString(R.styleable.WaveView_text);
            Drawable centerDrawable = typedArray.getDrawable(R.styleable.WaveView_waveCenterIcon);
            waveCenterIcon = ((BitmapDrawable) centerDrawable).getBitmap();
        } catch (Exception e) {

        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        centerX = mWidth / 2;
        centerY = mHeight / 2;
        radius = Math.min(mWidth, mHeight) / 2f;
        centerIconWidth = waveCenterIcon.getWidth();
        centerIconHeight = waveCenterIcon.getHeight();
        innerRadius = Math.max(centerIconWidth, centerIconHeight) * 1.2f;

        for (int i = 0; i < waveCount; i++) {
            waveDegreeArr[i] = innerRadius + (radius - innerRadius) / waveCount * i;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(dp2Px(120), MeasureSpec.EXACTLY);
        }
        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dp2Px(120), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawTextCenter(canvas);
        drawWave(canvas);
        drawCenterCircle(canvas);
        drawCenterIcon(canvas);


    }

    private void drawTextCenter(Canvas canvas) {
//        canvas.drawText("Ready Connect", centerX, centerY + 100, paintText);
    }

    private void drawCenterCircle(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, innerRadius, paint);
    }

    private void drawWave(Canvas canvas) {
        for (int i = 0; i < waveCount; i++) {
            paint.setAlpha((int) (255 - 255 * waveDegreeArr[i] / radius));
            canvas.drawCircle(centerX, centerY, waveDegreeArr[i], paint);
        }
        for (int i = 0; i < waveDegreeArr.length; i++) {
            if ((waveDegreeArr[i] += 4) > radius) {
                waveDegreeArr[i] = innerRadius;
            }
        }
        if (isRunning) {
            postInvalidateDelayed(100);
        }
    }

    private void drawCenterIcon(Canvas canvas) {
        paint.setAlpha(255);
        int left = centerX - centerIconWidth / 2;
        int top = centerY - centerIconHeight / 2;
        canvas.drawBitmap(waveCenterIcon, left, top, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                // 处理事件逻辑
                handleEvent(event);
                return true;
        }
        return true;
    }

    private void handleEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        Log.i(TAG, "handleEvent: " + "(" + touchX + "," + touchY + ")");
        float distanceX = Math.abs(touchX - centerX);
        float distanceY = Math.abs(touchY - centerY);
        float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        if (distance < innerRadius) {
            if (listener != null) {
                listener.onCenterWaveClick();
            }
        }
    }

    OnCenterWaveClickListener listener;

    public interface OnCenterWaveClickListener {
        void onCenterWaveClick();
    }

    public void setOnCenterWaveClickListener(OnCenterWaveClickListener listener) {
        this.listener = listener;
    }

    public void toggle() {
        isRunning = !isRunning;
        invalidate();
    }

    public boolean isWaveRunning() {
        return isRunning;
    }


    private int dp2Px(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

}