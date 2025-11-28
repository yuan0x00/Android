package com.rapid.android.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class DiscRotationSelector extends View {
    private Paint mPaint;
    private Paint mBorderPaint;
    private OnRotationChangeListener mRotationListener;

    // 触摸相关变量
    private float mLastTouchX, mLastTouchY;
    private float mCurrentAngle = 0f;
    private boolean mIsRotating = false;

    public DiscRotationSelector(Context context) {
        super(context);
        init();
    }

    public DiscRotationSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DiscRotationSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 主圆形画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);

        // 边框画笔
        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(Color.WHITE);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(2f);

        // 设置触摸监听
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouch(event);
            }
        });
    }

    /**
     * 处理触摸事件
     */
    private boolean handleTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 检查触摸点是否在圆盘内
                if (isPointInCircle(x, y, centerX, centerY, getWidth() / 2)) {
                    mLastTouchX = x;
                    mLastTouchY = y;
                    mIsRotating = true;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsRotating) {
                    // 计算角度变化
                    float deltaAngle = calculateAngleDelta(x, y, centerX, centerY);
                    mCurrentAngle += deltaAngle;

                    // 规范化角度到 0-360 度
                    mCurrentAngle = mCurrentAngle % 360;
                    if (mCurrentAngle < 0) {
                        mCurrentAngle += 360;
                    }

                    // 回调监听器
                    if (mRotationListener != null) {
                        mRotationListener.onRotationChanged(mCurrentAngle);
                    }

                    // 更新最后触摸点
                    mLastTouchX = x;
                    mLastTouchY = y;

                    // 重绘视图
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsRotating = false;
                if (mRotationListener != null) {
                    mRotationListener.onRotationEnd(mCurrentAngle);
                }
                break;
        }
        return false;
    }

    /**
     * 检查点是否在圆内
     */
    private boolean isPointInCircle(float x, float y, float centerX, float centerY, float radius) {
        float distance = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
        return distance <= radius;
    }

    /**
     * 计算角度变化
     */
    private float calculateAngleDelta(float currentX, float currentY, float centerX, float centerY) {
        // 计算上次触摸点的角度
        float lastAngle = (float) Math.toDegrees(Math.atan2(mLastTouchY - centerY, mLastTouchX - centerX));

        // 计算当前触摸点的角度
        float currentAngle = (float) Math.toDegrees(Math.atan2(currentY - centerY, currentX - centerX));

        // 计算角度差
        float delta = currentAngle - lastAngle;

        // 处理跨越 180° 边界的情况
        if (delta > 180) {
            delta -= 360;
        } else if (delta < -180) {
            delta += 360;
        }

        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int squareSize = Math.min(width, height);
        setMeasuredDimension(squareSize, squareSize);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // 获取View的中心坐标和半径
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(centerX, centerY);

        // 绘制主圆形
        canvas.drawCircle(centerX, centerY, radius, mPaint);

        // 绘制边框
        canvas.drawCircle(centerX, centerY, radius, mBorderPaint);

        // 绘制指示器（可选）
        drawIndicator(canvas, centerX, centerY, radius);
    }

    /**
     * 绘制旋转指示器
     */
    private void drawIndicator(Canvas canvas, int centerX, int centerY, int radius) {
        Paint indicatorPaint = new Paint();
        indicatorPaint.setColor(Color.RED);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setAntiAlias(true);

        // 计算指示器位置
        float indicatorRadius = 20f;
        float indicatorX = centerX + (radius - indicatorRadius - 10) * (float) Math.cos(Math.toRadians(mCurrentAngle));
        float indicatorY = centerY + (radius - indicatorRadius - 10) * (float) Math.sin(Math.toRadians(mCurrentAngle));

        canvas.drawCircle(indicatorX, indicatorY, indicatorRadius, indicatorPaint);
    }

    /**
     * 获取当前旋转角度
     */
    public float getRotationAngle() {
        return mCurrentAngle;
    }

    /**
     * 设置旋转角度
     */
    public void setRotationAngle(float angle) {
        mCurrentAngle = angle;
        invalidate();
    }

    /**
     * 设置旋转监听器
     */
    public void setOnRotationChangeListener(OnRotationChangeListener listener) {
        mRotationListener = listener;
    }

    /**
     * 旋转变化监听器接口
     */
    public interface OnRotationChangeListener {
        void onRotationChanged(float angle);

        void onRotationEnd(float angle);
    }
}