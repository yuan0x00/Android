package com.rapid.android.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarrySkyView extends ViewGroup {
    private static final String TAG = "StarrySkyView";
    private static final int DEFAULT_STAR_COUNT = 150;
    private static final int DEFAULT_UPDATE_DELAY = 50;

    private List<Star> stars = new ArrayList<>();
    private Paint starPaint = new Paint();
    private Random random = new Random();
    private Handler handler = new Handler();
    private int starCount;
    private int width, height;
    private boolean isInitialized = false;

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateStars();
            invalidate(); // 只重绘星空背景
            handler.postDelayed(this, DEFAULT_UPDATE_DELAY);
        }
    };

    public StarrySkyView(Context context) {
        super(context);
        init();
    }

    public StarrySkyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StarrySkyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Log.d(TAG, "初始化StarrySkyView");
        setWillNotDraw(false); // 允许ViewGroup自己绘制
        setBackgroundColor(Color.TRANSPARENT); // 设置为透明，我们自己绘制背景

        starCount = DEFAULT_STAR_COUNT;
        starPaint.setAntiAlias(true);
        starPaint.setColor(Color.WHITE);

        post(new Runnable() {
            @Override
            public void run() {
                if (getWidth() > 0 && getHeight() > 0) {
                    width = getWidth();
                    height = getHeight();
                    createStars();
                    isInitialized = true;
                    Log.d(TAG, "视图尺寸: " + width + "x" + height);
                }
            }
        });
    }

    @Override
    public void addView(View child) {
        checkChildViewConstraints(child);
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        checkChildViewConstraints(child);
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        checkChildViewConstraints(child);
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        checkChildViewConstraints(child);
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        checkChildViewConstraints(child);
        super.addView(child, width, height);
    }

    private void checkChildViewConstraints(View child) {
        if (getChildCount() >= 1) {
            throw new IllegalStateException("StarrySkyView只能包含一个子View");
        }

        if (!(child instanceof ViewGroup)) {
            throw new IllegalArgumentException("StarrySkyView的子View必须是ViewGroup类型");
        }

        Log.d(TAG, "添加子View: " + child.getClass().getSimpleName());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged: " + w + "x" + h);
        this.width = w;
        this.height = h;

        if (w > 0 && h > 0) {
            createStars();
            isInitialized = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout: " + (r - l) + "x" + (b - t));

        // 布局子View，使其填满整个区域
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            int childWidth = r - l;
            int childHeight = b - t;
            child.layout(0, 0, childWidth, childHeight);
            Log.d(TAG, "子View布局完成: " + childWidth + "x" + childHeight);

            // 确保子View可见
            child.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 测量所有子View
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 1. 先绘制星空背景
        drawStarryBackground(canvas);

        // 2. 然后调用super.dispatchDraw来绘制子View
        // 这样子View就会绘制在星空之上
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 这个方法可能不会被调用，因为我们在dispatchDraw中处理了绘制
        // 但为了安全起见，我们还是在这里绘制星空
        if (!isInitialized) {
            drawStarryBackground(canvas);
        }
    }

    /**
     * 绘制星空背景
     */
    private void drawStarryBackground(Canvas canvas) {
        if (!isInitialized || width == 0 || height == 0) {
            // 绘制简单的黑色背景
            canvas.drawColor(Color.BLUE);
            return;
        }

        // 绘制黑色背景
        canvas.drawColor(Color.BLUE);

        // 绘制星星
        for (Star star : stars) {
            starPaint.setAlpha(star.alpha);
            canvas.drawCircle(star.x, star.y, star.radius, starPaint);
        }
    }

    private void createStars() {
        Log.d(TAG, "创建星星，数量: " + starCount);
        stars.clear();

        for (int i = 0; i < starCount; i++) {
            stars.add(createRandomStar());
        }
    }

    private Star createRandomStar() {
        float x = random.nextFloat() * width;
        float y = random.nextFloat() * height;
        float radius = 0.5f + random.nextFloat() * 2.5f;
        float speed = 0.1f + random.nextFloat() * 0.8f;
        int alpha = 100 + random.nextInt(156);
        float twinkleSpeed = 0.02f + random.nextFloat() * 0.08f;

        return new Star(x, y, radius, speed, alpha, twinkleSpeed);
    }

    private void updateStars() {
        if (!isInitialized) return;

        for (Star star : stars) {
            star.y += star.speed;

            if (star.y > height) {
                star.y = 0;
                star.x = random.nextFloat() * width;
                star.twinklePhase = random.nextFloat() * (float) (Math.PI * 2);
            }

            star.alpha = (int) (100 + 155 * (0.5f + 0.5f * Math.sin(star.twinklePhase)));
            star.twinklePhase += star.twinkleSpeed;

            if (star.twinklePhase > Math.PI * 2) {
                star.twinklePhase -= Math.PI * 2;
            }
        }
    }

    private void startAnimation() {
        Log.d(TAG, "启动动画");
        handler.removeCallbacks(updateRunnable);
        handler.post(updateRunnable);
    }

    private void stopAnimation() {
        Log.d(TAG, "停止动画");
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "附加到窗口");
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "从窗口分离");
        stopAnimation();
    }

    public void setStarCount(int count) {
        this.starCount = count;
        if (width > 0 && height > 0) {
            createStars();
            invalidate();
        }
    }

    public ViewGroup getChildViewGroup() {
        if (getChildCount() > 0) {
            return (ViewGroup) getChildAt(0);
        }
        return null;
    }

    private static class Star {
        float x, y;
        float radius;
        float speed;
        int alpha;
        float twinkleSpeed;
        float twinklePhase;

        Star(float x, float y, float radius, float speed, int alpha, float twinkleSpeed) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
            this.alpha = alpha;
            this.twinkleSpeed = twinkleSpeed;
            this.twinklePhase = (float) (Math.random() * Math.PI * 2);
        }
    }
}