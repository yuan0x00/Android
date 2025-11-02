package com.rapid.android.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.rapid.android.R;

import org.jspecify.annotations.NonNull;

import java.util.Random;

public class CircleProgressView extends View {

    private final RectF rectF = new RectF();
    private final StringBuilder progressTextBuilder = new StringBuilder();
    // 绘制属性
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    // 视图尺寸
    private float centerX, centerY;
    private float radius;
    // 进度相关
    private float currentProgress = 0f;
    private float maxProgress = 100f;
    // 动画
    private ValueAnimator progressAnimator;
    // 颜色
    private int backgroundColor = 0xFFE0E0E0;
    private int progressColor = 0xFF2196F3;
    private int textColor = 0xFF333333;
    // 样式
    private float strokeWidth = 20f;
    private float textSize = 50f;


    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        // 使用defStyleAttr来支持默认样式
        TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.CircleProgressView,
                defStyleAttr,
                0
        );

        try {
            progressColor = a.getColor(R.styleable.CircleProgressView_progressColor, progressColor);
            backgroundColor = a.getColor(R.styleable.CircleProgressView_backgroundColor, backgroundColor);
            textColor = a.getColor(R.styleable.CircleProgressView_textColor, textColor);
            strokeWidth = a.getDimension(R.styleable.CircleProgressView_strokeWidth, strokeWidth);
            textSize = a.getDimension(R.styleable.CircleProgressView_textSize, textSize);
            maxProgress = a.getFloat(R.styleable.CircleProgressView_maxProgress, maxProgress);
        } finally {
            a.recycle();
        }

        setupPaints();

        setOnClickListener(v -> setProgress(new Random().nextInt(100)));

    }

    private void setupPaints() {
        // 背景圆环的Paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setAntiAlias(true); // 抗锯齿

        // 进度圆环的Paint
        progressPaint = new Paint();
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND); // 圆角端点

        // 文字Paint
        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredSize = 300; // 期望的默认大小，单位dp

        // 将dp转换为px
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int desiredSizePx = (int) (desiredSize * metrics.density);

        // 使用resolveSize来处理测量规格
        int width = resolveSize(desiredSizePx, widthMeasureSpec);
        int height = resolveSize(desiredSizePx, heightMeasureSpec);

        // 确保是正方形
        int finalSize = Math.min(width, height);
        setMeasuredDimension(finalSize, finalSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 计算中心点
        centerX = w / 2f;
        centerY = h / 2f;

        // 计算半径，考虑描边宽度
        radius = Math.min(w, h) / 2f - strokeWidth;
        // 预先计算好RectF
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // 1. 绘制背景圆环
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // 2. 绘制进度圆环
        if (currentProgress > 0) {
            float sweepAngle = 360 * (currentProgress / maxProgress);
            canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
        }

        // 3. 绘制进度文字
        progressTextBuilder.setLength(0);
        progressTextBuilder.append((int) currentProgress).append("%");
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        float textY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2;

        canvas.drawText(progressTextBuilder.toString(), centerX, textY, textPaint);
    }

    public void setProgress(float progress, boolean withAnimation) {
        float targetProgress = Math.min(progress, maxProgress);

        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }

        if (withAnimation) {
            // 使用属性动画
            progressAnimator = ValueAnimator.ofFloat(currentProgress, targetProgress);
            progressAnimator.setDuration(800);
            progressAnimator.setInterpolator(new DecelerateInterpolator());
            progressAnimator.addUpdateListener(animation -> {
                currentProgress = (float) animation.getAnimatedValue();
                invalidate(); // 重绘视图
            });
            progressAnimator.start();
        } else {
            currentProgress = targetProgress;
            invalidate();
        }
    }

    public float getProgress() {
        return currentProgress;
    }

    public void setProgress(float progress) {
        setProgress(progress, true);
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }


}
