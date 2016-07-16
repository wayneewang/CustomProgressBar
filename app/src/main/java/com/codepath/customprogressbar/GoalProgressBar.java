package com.codepath.customprogressbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class GoalProgressBar extends View {
    private int progress;
    private int goal;
    private boolean isGoalReached;
    //目标只是器的高度
    private float goalIndicatorHeight;
    //目标指示器的厚度
    private float goalIndicatorWidth;
    //达到目标时进度条的颜色
    private int goalReachedColor;
    //未达到目标时的颜色
    private int goalNotReachedColor;
    //未达到进度的进度条的填充颜色
    private int unfilledSectionColor;
    //进度条高度
    private float barHeight;
    private int barThickness;
    private Paint progressPaint;
    private float goalIndicatorThickness;
    //progressBar的形状
    public enum  IndicatorType {
        Line, Circle, Square
    }

    private IndicatorType indicatorType;
    //动画
    private ValueAnimator barAnimator;

    public GoalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    //定义属性和绘制
    private void init(AttributeSet attrs) {
        //从属性集合中抽取自定义属性
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.GoalProgressBar, 0,0);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        setGoalIndicatorHeight(typedArray.getDimensionPixelSize(R.styleable.GoalProgressBar_goalIndicatorHeight,
                10));
        setGoalIndicatorThickness(typedArray.getDimensionPixelSize(R.styleable.GoalProgressBar_goalIndicatorWidth,
                5));
        setIndicatorType(IndicatorType.values()[typedArray.getInt(R.styleable.GoalProgressBar_indicatorType,
                IndicatorType.Line.ordinal())]);
        setGoalReachedColor(typedArray.getColor(R.styleable.GoalProgressBar_goalReachedColor, Color.BLUE));
        setGoalNotReachedColor(typedArray.getColor(R.styleable.GoalProgressBar_goalNotReachedColor,
                Color.BLACK));
        setUnfilledSectionColor(typedArray.getColor(R.styleable.GoalProgressBar_unfilledSectionColor,
                Color.RED));

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        //保存进度
        bundle.putInt("Progress", progress);
        bundle.putInt("goal", goal);
        //保存其他状态
        bundle.putParcelable("superState", super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            //获得进度
            setProgress(bundle.getInt("Progress"));
            setGoal(bundle.getInt("goal"));
            //获得其他状态
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int halfHeight = getHeight() / 2;
        updateGoalReached();
        int progressEndX = (int) (getWidth() * progress / 100f);
        //绘制已经达到的进度条
        progressPaint.setStrokeWidth(barHeight);
        progressPaint.setColor(isGoalReached ? goalReachedColor : goalNotReachedColor);
        canvas.drawLine(0 , halfHeight, progressEndX, halfHeight, progressPaint);//
        //绘制未填充区域
        progressPaint.setColor(unfilledSectionColor);
        canvas.drawLine(progressEndX, halfHeight, getWidth(), halfHeight, progressPaint);
        //绘制指示器
        float indicatorPosition = getWidth() * goal / 100f;
        progressPaint.setColor(goalReachedColor);
        progressPaint.setStrokeWidth(goalIndicatorWidth);
        switch (indicatorType) {
            case Line:
                canvas.drawLine(indicatorPosition, halfHeight - goalIndicatorHeight / 2, indicatorPosition,
                        halfHeight + goalIndicatorHeight / 2, progressPaint);
                break;
            case Circle:
                canvas.drawCircle(indicatorPosition, goalIndicatorHeight / 2, goalIndicatorHeight / 2,
                        progressPaint);
                break;
            case Square:
                canvas.drawRect(indicatorPosition - (goalIndicatorHeight / 2), 0, indicatorPosition +
                        (goalIndicatorHeight / 2), goalIndicatorHeight, progressPaint);
                break;
        }
    }
    //测量大小

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽度,由父布局确定
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //设置高度
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height;
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.EXACTLY://收到父布局的限制，必须和指定的同样大小
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST://不能超过父布局的大小
                height = (int) Math.min(goalIndicatorHeight, heightSize);
                break;
            default:
                //可以是任意大小
                height = (int) goalIndicatorHeight;
                break;
        }
        super.setMeasuredDimension(width, height);//存储测量的宽度和高度
    }

    public void setProgress(final int progress, boolean animate) {
        if (animate) {
           barAnimator = new ValueAnimator();

            barAnimator = ValueAnimator.ofFloat(0, 1);
            //动画时长
            barAnimator.setDuration(700);
            //重置进度为0，使其从0开始加载
            setProgress(0 , false);
            //使用减速动画效果
            barAnimator.setInterpolator(new DecelerateInterpolator());
            //使用setProgress更新进度
            barAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float interpolation = (float) animation.getAnimatedValue();
                    setProgress((int) (interpolation * progress), false);
                }
            });

            if (!barAnimator.isStarted()) {
                barAnimator.start();
            }

    } else {
        this.progress = progress;
        postInvalidate();
         }
    }


    public void setProgress(int progress) {
        this.progress = progress;
        setProgress(progress, true);
    }


    public void setIndicatorType(IndicatorType indicatorType) {
        this.indicatorType = indicatorType;
        postInvalidate();
    }

    public void setBarHeight(float barHeight) {
        this.barHeight = barHeight;
    }

    public void setGoalNotReachedColor(int goalNotReachedColor) {
        this.goalNotReachedColor = goalNotReachedColor;
        postInvalidate();
    }

    public void setGoalReachedColor(int goalReachedColor) {
        this.goalReachedColor = goalReachedColor;
        postInvalidate();
    }

    public void setGoal(int goal) {
        this.goal = goal;
        postInvalidate();
    }

    private void updateGoalReached() {
        isGoalReached = progress >= goal;
    }

    public void setBarThickness(int barThickness) {
        this.barThickness = barThickness;
        postInvalidate();
    }

    public void setUnfilledSectionColor(int unfilledSectionColor) {
        this.unfilledSectionColor = unfilledSectionColor;
        postInvalidate();
    }

    public void setGoalIndicatorThickness(float goalIndicatorThickness) {
        this.goalIndicatorThickness = goalIndicatorThickness;
        postInvalidate();
    }
    public void setGoalIndicatorHeight(float goalIndicatorHeight) {
        this.goalIndicatorHeight = goalIndicatorHeight;
        postInvalidate();
    }
}

