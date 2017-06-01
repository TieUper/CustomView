package com.lichfaker.scaleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static com.lichfaker.scaleview.BaseScaleView.ATTR;
import static com.lichfaker.scaleview.BaseScaleView.LF_SCALE_HEIGHT;
import static com.lichfaker.scaleview.BaseScaleView.LF_SCALE_MAX;
import static com.lichfaker.scaleview.BaseScaleView.LF_SCALE_MIN;

/**
 * 进度尺
 * Created by tie on 2017/5/31.
 */

public class ScaleView extends View {

    protected int mMax; //最大刻度
    protected int mMin; // 最小刻度
    protected int mCountScale; //滑动的总刻度
    protected int mScaleMargin; //刻度间距
    protected int mScaleHeight; //刻度线的高度
    protected int mScaleMaxHeight; //整刻度线高度
    protected int mRectWidth; //总宽度
    protected int mRectHeight; //高度
    private int leftMargin = 40; //边距

    protected OnScrollListener mScrollListener;

    //进度线移动的位置
    int mScrollX = leftMargin;

    private boolean isdown;


    public ScaleView(Context context) {
        super(context);
        init(null);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    protected void init(AttributeSet attrs) {
        // 获取自定义属性
        TypedArray ta = getContext().obtainStyledAttributes(attrs, ATTR);
        mMin = ta.getInteger(LF_SCALE_MIN, 200);
        mMax = ta.getInteger(LF_SCALE_MAX, 5000);
        //mScaleMargin = ta.getDimensionPixelOffset(LF_SCALE_MARGIN, 15);
        mScaleHeight = ta.getDimensionPixelOffset(LF_SCALE_HEIGHT, 20);
        ta.recycle();

        //总高度
        mRectHeight = mScaleHeight * 6;
        mScaleMaxHeight = mScaleHeight * 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {

        //总宽度
        int width = getWidth() - 2 * leftMargin;
        int count = (mMax - mMin) / 100;
        mScaleMargin = width / count;

        //总宽度
        mRectWidth = mScaleMargin * count;
        // 画笔
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.color_red));
        // 抗锯齿
        paint.setAntiAlias(true);
        // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        paint.setDither(true);
        // 空心
       // paint.setStyle(Paint.Style.STROKE);
        // 文字居中
        paint.setTextAlign(Paint.Align.CENTER);

        onDrawLine(canvas, paint);
        onDrawScale(canvas, paint); //画刻度
        onDrawPointer(canvas, paint); //画指针

        super.onDraw(canvas);
    }

    //画线
    protected void onDrawLine(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(dp2px(0.5f));
        canvas.drawLine(leftMargin, mRectHeight, mRectWidth + leftMargin, mRectHeight, paint);
    }

    //画刻度
    protected void onDrawScale(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(dp2px(0.5f));
        paint.setTextSize(sp2px(10));

        for (int i = mMin / 100; i <= mMax / 100; i++) {

            //第一个刻度从0开始
            int j = i - mMin / 100;
            if (i % 10 == 0 || i == mMin / 100) { //整值
                canvas.drawLine(j * mScaleMargin + leftMargin, mRectHeight, j * mScaleMargin + leftMargin, mRectHeight - mScaleMaxHeight, paint);
                //整值文字
                if (i == mMin / 100) {
                    canvas.drawText(String.valueOf(i * 100), j * mScaleMargin + dp2px(10) + leftMargin, mRectHeight - mScaleMaxHeight - 20, paint);
                } else if (i == mMax / 100) {
                    canvas.drawText(String.valueOf(i * 100), j * mScaleMargin - dp2px(14) + leftMargin, mRectHeight - mScaleMaxHeight - 20, paint);
                } else {
                    canvas.drawText(String.valueOf(i * 100), j * mScaleMargin + leftMargin, mRectHeight - mScaleMaxHeight - 20, paint);
                }
            } else {
                canvas.drawLine(j * mScaleMargin + leftMargin, mRectHeight, j * mScaleMargin + leftMargin, mRectHeight - mScaleHeight, paint);
            }
        }

    }

    //画指针
    protected void onDrawPointer(Canvas canvas, Paint paint) {

        paint.setStrokeWidth(dp2px(2));

        //滑动的刻度
        int tmpCountScale = (int) Math.rint(((double) mScrollX - leftMargin)/ (double) mScaleMargin); //四舍五入取整
        //总刻度
        mCountScale = tmpCountScale * 100 + mMin;
        if (mScrollListener != null) { //回调方法
            mScrollListener.onScaleScroll(mCountScale);
        }

        canvas.drawLine(mScrollX, mRectHeight,
                mScrollX, mRectHeight - dp2px(36), paint);
    }

    private int dp2px(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = ((int) event.getX() - leftMargin);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isNearBy(x)) {
                    isdown = true;
                } else {
                    isdown = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (x < 0) { //向右边滑动
                    x = 0;
                } else if (x > mRectWidth) { //向左边滑动
                    x = mRectWidth;
                }
                //  smoothScrollBy(dataX, 0);
                //手指触摸的时候变动
                if (isdown) {
                    int i = x % mScaleMargin;
                    int j = x / mScaleMargin;
                    if (i != 0) {
                        //每次移动一格的距离
                        x = j * mScaleMargin;
                    }
                    mScrollX = x + leftMargin;

                    postInvalidate();
                }
                //onDrawPointer();
                break;
            case MotionEvent.ACTION_UP:
                if (isdown) {
                    isdown = false;
                }
                break;
        }
        return true;
    }

    //检测手势范围
    private boolean isNearBy(int x) {
        return Math.abs(mScrollX - leftMargin -  x) < 30;
    }

    public interface OnScrollListener {
        void onScaleScroll(int scale);
    }

    /**
     * 设置回调监听
     *
     * @param listener
     */
    public void setOnScrollListener(OnScrollListener listener) {
        this.mScrollListener = listener;
    }
}
