package com.stream.commentperfect;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * description：自由拖动的评价控件
 * ===============================
 * creator：张川川
 * create time：2016/10/14 11:56
 * ===============================
 * reasons for modification：
 * Modifier：
 * Modify time：
 */

public class PathView extends View {

    // 缓存的Point
    float lastX;
    float lastY;
    // 灰色画笔
    Paint mPaint;
    //红色画笔
    Paint mAnotherPaint;

    Path mPath;

    Path mAnotherPath;


    // 默认最大分数 10
    private int maxScore = 10;

    // 默认最小分数
    private int minScore = 0;

    // 分数
    private int score = minScore - 1;

    // 圆的数目
    private int circleNumber;

    // 默认的圆的大小
    private int maxCircleSize = 20;

    // 默认线条的宽度
    private int paintWidth = 20;

    private int defaultColor = Color.GRAY;

    private int selectColor = Color.RED;

    // 是否已经初始化过
    private boolean hasInited;

    private List<Point> mPointList;

    private boolean stillMove;

    Point startPoint;
    Point nowPoint;
    Point endPoint;
    int gap = 0;
    int cr = 0;


    public PathView(Context context) {
        super(context);
    }

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScore(int score) {
        if (score > maxScore || score < minScore) {
            return;
        }
        this.score = score;
        Point p = mPointList.get(score - minScore);
        nowPoint.set(p.x, p.y);

        invalidate();
    }

    public int getScore(){
        return score;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
        initPathView();
    }

    public void initPathView() {

        circleNumber = maxScore - minScore + 1;

        if (startPoint == null) {
            startPoint = new Point();
        }

        if (nowPoint == null) {
            nowPoint = new Point();
        }

        if (endPoint == null) {
            endPoint = new Point();
        }

        // 存n个点
        if (mPointList == null) {
            mPointList = new ArrayList<>(circleNumber);
            for (int i = 0; i < circleNumber; i++) {
                Point p = new Point();
                mPointList.add(p);
            }
        }

        if (mPointList.size() < circleNumber) {
            for (int i = mPointList.size() - 1; i < circleNumber; i++) {
                mPointList.add(new Point());
            }
        }


        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(defaultColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(paintWidth);
        }

        if (mAnotherPaint == null) {
            mAnotherPaint = new Paint();
            mAnotherPaint.setAntiAlias(true);
            mAnotherPaint.setColor(selectColor);
            mAnotherPaint.setStyle(Paint.Style.STROKE);
            mAnotherPaint.setStrokeWidth(paintWidth);
        }

        if (mPath == null) {
            mPath = new Path();
        }

        if (mAnotherPath == null) {
            mAnotherPath = new Path();
        }

        hasInited = true;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (!hasInited) {
            return;
        }

        mPath.reset();
        mAnotherPath.reset();

        // 实际半经
        cr = maxCircleSize + paintWidth / 2;
        startPoint.x = cr;
        startPoint.y = getHeight() / 2;
        endPoint.x = getWidth() - cr;
        endPoint.y = getHeight() / 2;

        int x = startPoint.x;
        int y = startPoint.y;
        nowPoint.y = y;

        gap = (endPoint.x - startPoint.x) / (maxScore - minScore);

        // 初始化所有点坐标
        for (Point p : mPointList) {
            p.x = x;
            p.y = y;
            x += gap;
        }

        //如果有分数
        if (nowPoint.x != 0) {
            mAnotherPath.moveTo(startPoint.x, endPoint.y);
            mAnotherPath.addCircle(startPoint.x, startPoint.y, maxCircleSize, Path.Direction.CCW);
            score = minScore;
            for (int i = 1; i < mPointList.size(); i++) {
                Point p = mPointList.get(i);
                if (nowPoint.x > p.x) {
                    mAnotherPath.lineTo(p.x - maxCircleSize, p.y);
                    mAnotherPath.addCircle(p.x, p.y, maxCircleSize, Path.Direction.CCW);
                } else if (nowPoint.x == p.x) {
                    score = i + minScore;
                    mAnotherPath.lineTo(p.x - maxCircleSize, p.y);
                    mAnotherPath.addCircle(p.x, p.y, maxCircleSize, Path.Direction.CCW);
                }else {
                    mAnotherPath.lineTo(p.x - maxCircleSize, p.y);
                    mPath.moveTo(nowPoint.x + cr, nowPoint.y);
                    for (int j = i; j < mPointList.size(); j++) {
                        Point jp = mPointList.get(j);
                        mPath.lineTo(jp.x - maxCircleSize, jp.y);
                        mPath.addCircle(jp.x, jp.y, maxCircleSize, Path.Direction.CCW);
                    }
                    break;
                }

            }
            canvas.drawPath(mAnotherPath, mAnotherPaint);
            canvas.drawPath(mPath, mPaint);
        } else {

            mPath.moveTo(startPoint.x, endPoint.y);
            mPath.addCircle(startPoint.x, startPoint.y, maxCircleSize, Path.Direction.CCW);

            for (int i = 1; i < circleNumber; i++) {
                Point p = mPointList.get(i);
                mPath.lineTo(p.x - maxCircleSize, y);
                mPath.addCircle(p.x, y, maxCircleSize, Path.Direction.CCW);
            }
            canvas.drawPath(mPath, mPaint);              // 绘制Path
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 离开
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_DOWN) {

            int nowScore = nearPosition(event, event.getX(), event.getY());
            if (score != nowScore) {
                setScore(nowScore);
            }

        }

        // 移动
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int nowScore = nearPosition(event, event.getX(), event.getY());
            if (score != nowScore) {
                setScore(nowScore);
            }
        }



        return true;
    }

    private int nearPosition(MotionEvent event, float x, float y) {

        if (x < startPoint.x - cr || x >endPoint.x + cr) {
            return score;
        }

        if (!stillMove && (y > startPoint.y + 50 || y < startPoint.y - 50)) {
            return score;
        }

        //设置点击的坐标
        if (y > startPoint.y - 50 && y < startPoint.y + 50) {
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                stillMove = true;
            }
            nowPoint.x = (int) x;
            invalidate();
        } else if (stillMove) {
            nowPoint.x = (int) x;
            Log.d("zccTest", "x:"+x);
            invalidate();
        }


        // up的时候判断位置
        if (event.getAction() == MotionEvent.ACTION_UP) {
            stillMove = false;
            if (nowPoint.x > startPoint.x - cr && nowPoint.x < endPoint.x + cr) {
                int position = (((nowPoint.x - startPoint.x) / (gap / 2)) + 1 ) / 2;
                nowPoint.x = mPointList.get(position).x;
                Log.d("zccTest", "up :" + nowPoint.x);
            }
        }

        if (stillMove) {
            if (x > nowPoint.x - 25 && x < nowPoint.x + 25) {
                return score;
            }
        }

        if (x > nowPoint.x - 25 && x < nowPoint.x + 25 && y > nowPoint.y - 25 && y < nowPoint.y + 25) {
            return score;
        }



        // 查看分数有没有变化
        if (mPointList != null) {
            for (int i = 0; i < mPointList.size(); i++) {
                Point p = mPointList.get(i);
                if (p != null) {
                    Log.d("zccTest", "p:" + i + ", " + p.x + ", " + p.y);

                    if (x > p.x - 50 && x < p.x + 50 && y > p.y - 50 && y < p.y + 50) {

                        return i + minScore;
                    }

                    if (stillMove) {
                        if (x > p.x - 50 && x < p.x + 50) {
                            return i + minScore;
                        }
                    }

                }
            }
        }
        return score;
    }



    public void startAnimation(){
//        ObjectAnimator
    }

}
