package com.stream.commentperfect;

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

    Paint mPaint;

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
    Point endPoint;


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

        int width = getWidth();
        int height = getHeight();

        // 实际半经
        int cr = maxCircleSize + paintWidth / 2;
        startPoint.x = cr;
        startPoint.y = getHeight() / 2;
        endPoint.x = getWidth() - cr;
        endPoint.y = getHeight() / 2;


        int x = startPoint.x;
        int y = startPoint.y;

        int gap = (endPoint.x - startPoint.x) / (maxScore - minScore);

        //如果有分数
        if (score != minScore - 1) {
            mPaint.setColor(selectColor);

            mAnotherPath.moveTo(startPoint.x, endPoint.y);
            int pointPosition = 0;
            // 设置初始化点坐标
            mPointList.get(pointPosition).x = startPoint.x;
            mPointList.get(pointPosition).y = startPoint.y;

            mAnotherPath.addCircle(startPoint.x, startPoint.y, maxCircleSize, Path.Direction.CCW);

            for (int i = minScore; i < score; i++) {
                x += gap;
                pointPosition ++;
                mPointList.get(pointPosition).x = x;
                mPointList.get(pointPosition).y = startPoint.y;

                mAnotherPath.lineTo(x - maxCircleSize, y);
                mAnotherPath.addCircle(x, y, maxCircleSize, Path.Direction.CCW);
            }

            canvas.drawPath(mAnotherPath, mPaint);

            mPath.moveTo(x + cr, y);

            mPaint.setColor(defaultColor);

            for (int i = score; i < maxScore; i++) {
                x += gap;
                pointPosition ++;
                mPointList.get(pointPosition).x = x;
                mPointList.get(pointPosition).y = startPoint.y;

                mPath.lineTo(x - maxCircleSize, y);
                mPath.addCircle(x, y, maxCircleSize, Path.Direction.CCW);
            }

            canvas.drawPath(mPath, mPaint);              // 绘制Path


        } else {

            int pointPosition = 0;
            // 设置初始化点坐标
            mPointList.get(pointPosition).x = startPoint.x;
            mPointList.get(pointPosition).y = startPoint.y;

            mPath.moveTo(startPoint.x, endPoint.y);
            mPath.addCircle(startPoint.x, startPoint.y, maxCircleSize, Path.Direction.CCW);

            for (int i = minScore; i < maxScore; i++) {
                x += gap;
                mPath.lineTo(x - maxCircleSize, y);
                mPath.addCircle(x, y, maxCircleSize, Path.Direction.CCW);
                pointPosition ++;
                mPointList.get(pointPosition).x = x;
                mPointList.get(pointPosition).y = startPoint.y;

            }
            canvas.drawPath(mPath, mPaint);              // 绘制Path
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 离开
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_DOWN) {
            stillMove = false;
            int nowScore = nearPostion(event.getX(), event.getY());
            if (score != nowScore) {
                setScore(nowScore);
            }

        }

        // 移动
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int nowScore = nearPostion(event.getX(), event.getY());
            if (score != nowScore) {
                setScore(nowScore);
            }
        }
        return true;
    }

    private int nearPostion(float x, float y) {

        if (x > lastX - 25 && x < lastX + 25 && y > lastY - 25 && y < lastY + 25) {
            return score;
        }
        if (mPointList != null) {
            for (int i = 0; i < mPointList.size(); i++) {
                Point p = mPointList.get(i);
                if (p != null) {
                    Log.d("testCall", "p:" + i + ", " + p.x + ", " + p.y);

                    if (x > p.x - 50 && x < p.x + 50 && y > p.y - 50 && y < p.y + 50) {
                        lastX = x;
                        lastY = y;
                        stillMove = true;
                        return i + minScore;
                    }

                    if (stillMove) {
                        if (x > p.x - 50 && x < p.x + 50) {
                            lastX = x;
                            lastY = y;
                            return i + minScore;
                        }
                    }

                }
            }
        }
        return score;
    }

}
