package yaya.slidingview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yaya.slidingview.util.ColorUtil;

/**
 * Created by yaya on 2017/1/5.
 */

public class SlidingViewY extends View
{
    private int mMinTextSize = 100;//最小字体
    private int mMaxTextSize = 150;//最大字体

    //字体渐变颜色
    private int mStartColor = Color.BLACK;//中间选中item的颜色
    private int mEndColor = Color.GRAY;//上下两边的颜色
    private int mLineColor = Color.rgb(50, 165, 231);//中间横线的颜色
    private int mLineWidth = 3;//中间横线的宽度
    private int mVisibleItemCount = 3;//可见的item个数

    private boolean mIsInertiaScroll = true;//快速滑动时候是否惯性滑动一段距离
    private boolean mIsCirculation = true;//是否循环滚动

    private Paint mPaint;//画笔
    private Paint mPaintLine;//
    private int mMeasureWidth;
    private int mMeasureHeight;
    private int mSelected = 10;//当前选中的item下标，实际保持不变一直为中间位置的那个元素
    private List<String> mData;//显示的数据源
    private int mItemHeight = 0;//每个条目的高度 = mMeasureHeight / mVisibleItemCount;
    private int mCenterY;//中间item的起始y坐标
    private float mLastMoveY;//触摸的y坐标
    private float mMoveLength = 0;//item移动长度，负数向上移动，正数向下移动
    private GestureDetectorCompat mGestureDetetor;//手势检测，用于辅助检测用户的单击、滑动、长按、双击等手势
    private OnSelectedListener mListener;//选中之后的监听接口
    private Scroller mScroller;
    /*Scroller 封装了滚动相关的操作。你可以使用Scroller获取可以产生滚动效果的数据；
    例如，在响应一个滑动手势时，Scroller会帮你计算
    滚动偏移量，你可以根据获取的偏移量来设置你的view的位置，从而实现滚动效果
    主要方法有：
    startScroll(int startX, int startY, int dx, int dy)　//开始计算平滑滚动,dx、dy为滚动的距离，dx = finalX - startX;
    fling(int startX, int startY, int velocityX, int velocityY,
    int minX, int maxX, int minY, int maxY)　//根据滑动的速度，开始计算惯性滚动
    computeScrollOffset()//计算当前时间滚动的偏移量，如果返回true,则滚动还未结束
    getCurrY()　//获取当前Y轴坐标的偏移量
    getCurrX()　//获取当前X轴坐标的偏移量
    mScroller.abortAnimation()　//停止滚动*/
    private boolean mIsFling;//是否正在惯性滑动
    private boolean mIsMovingCenter;//是否正在滑向中间
    private int mLastScrollY = 0;//Scroller的y坐标

    public SlidingViewY(Context context)
    {
        this(context, null);
    }

    public SlidingViewY(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public SlidingViewY(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        mPaint.setStyle(Paint.Style.FILL);
        mPaintLine.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
        mPaintLine.setColor(mLineColor);
        mGestureDetetor = new GestureDetectorCompat(getContext(), new FlingOnGestureListener());
        mScroller = new Scroller(getContext());
        setmData(new ArrayList<>(Arrays.asList("00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00")));
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SlidingViewY);
        mMinTextSize = array.getDimensionPixelSize(R.styleable.SlidingViewY_min_textSize, mMinTextSize);
        mMaxTextSize = array.getDimensionPixelSize(R.styleable.SlidingViewY_max_textSize, mMaxTextSize);
        mStartColor = array.getColor(R.styleable.SlidingViewY_startColor, mStartColor);
        mEndColor = array.getColor(R.styleable.SlidingViewY_endColor, mEndColor);
        mVisibleItemCount = array.getInt(R.styleable.SlidingViewY_visible_item_count, mVisibleItemCount);
        mLineColor = array.getColor(R.styleable.SlidingViewY_lineColor, mLineColor);
        mLineWidth = array.getDimensionPixelSize(R.styleable.SlidingViewY_lineWidth, mLineWidth);
        array.recycle();//回收资源
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        drawItem(canvas, mSelected);
        int length = mVisibleItemCount / 2 + 1;
        for(int i = 1; i <= length && i <= mData.size() / 2; i++)
        {
            drawItem(canvas, mSelected - i);
            drawItem(canvas, mSelected + i);
        }
    }

    private void drawItem(Canvas canvas, int position)
    {
        int relative = position - mSelected;//不知道为啥？？？？？？？？？？？？？
        String text = mData.get(position);
        float x = 0;
        if(relative == -1)//上一个item
        {
            if(mMoveLength < 0)//向上滑动
            {
                mPaint.setTextSize(mMinTextSize);
            }
            else//向下滑动
            {
                mPaint.setTextSize(mMinTextSize + (mMaxTextSize - mMinTextSize) * mMoveLength / mItemHeight);
            }
        }
        else if(relative == 0)//选中的item
        {
            mPaint.setTextSize(mMinTextSize + (mMaxTextSize - mMinTextSize)
                    * (mItemHeight - Math.abs(mMoveLength)) / mItemHeight);
        }
        else if(relative == 1)
        {
            if(mMoveLength > 0)
            { // 向下滑动
                mPaint.setTextSize(mMinTextSize);
            }
            else
            { // 向上滑动
                mPaint.setTextSize(mMinTextSize + (mMaxTextSize - mMinTextSize)
                        * -mMoveLength / mItemHeight);
            }
        }
        else
        {
            mPaint.setTextSize(mMinTextSize);
        }
        x = (mMeasureWidth - mPaint.measureText(text)) / 2;
        // 绘制文字时，文字的baseline是对齐y坐标的，下面换算使其垂直居中。fmi.top值是相对baseline的
        Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
        float y = mCenterY + relative * mItemHeight + mItemHeight / 2
                - fmi.descent * 1.2f + (fmi.bottom - fmi.top) / 2;
        computeColor(relative);
        canvas.drawText(text, x, y + mMoveLength, mPaint);
        mPaintLine.setStrokeWidth(mLineWidth);
        mPaintLine.setColor(mLineColor);
        canvas.drawLine(0, mItemHeight, mMeasureWidth, mItemHeight, mPaintLine);
        canvas.drawLine(0, 2 * mItemHeight, mMeasureWidth, 2 * mItemHeight, mPaintLine);
    }

    /**
     * 计算字体颜色，渐变
     *
     * @param relative 　相对中间item的位置
     */
    private void computeColor(int relative)
    {

        int color = mEndColor; // 　其他默认为ｍEndColor

        if(relative == -1 || relative == 1)
        { // 上一个或下一个
            // 处理上一个item且向上滑动　或者　处理下一个item且向下滑动　，颜色为mEndColor
            if((relative == -1 && mMoveLength < 0)
                    || (relative == 1 && mMoveLength > 0))
            {
                color = mEndColor;
            }
            else
            { // 计算渐变的颜色
                float rate = (mItemHeight - Math.abs(mMoveLength))
                        / mItemHeight;
                color = ColorUtil.computeGradientColor(mStartColor, mEndColor, rate);
            }
        }
        else if(relative == 0)
        { // 中间item
            float rate = Math.abs(mMoveLength) / mItemHeight;
            color = ColorUtil.computeGradientColor(mStartColor, mEndColor, rate);
        }

        mPaint.setColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
        mItemHeight = mMeasureHeight / mVisibleItemCount;
        mCenterY = mVisibleItemCount / 2 * mItemHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mGestureDetetor.onTouchEvent(event);

        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                // 点击时取消所有滚动效果
                cancelScroll();
                mLastMoveY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if(Math.abs(event.getY() - mLastMoveY) < 0.1f)
                {
                    return true;
                }
                mMoveLength += event.getY() - mLastMoveY;
                mLastMoveY = event.getY();
                checkCirculation();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                mLastMoveY = event.getY();
                moveToCenter();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void computeScroll()
    {
        if(mScroller.computeScrollOffset())//正在滚动
        {
            // 可以把scroller看做模拟的触屏滑动操作，mLastScrollY为上次滑动的坐标
            mMoveLength = mMoveLength + mScroller.getCurrY() - mLastScrollY;
            mLastScrollY = mScroller.getCurrY();
            checkCirculation();
            invalidate();
        }
        else//滚动完毕
        {
            if(mIsFling)
            {
                mIsFling = false;
                moveToCenter();
            }
            else if(mIsMovingCenter)
            {
                mIsMovingCenter = false;
                notifySelected();
            }
        }
    }


    /**
     * 循环滚动
     */
    private void checkCirculation()
    {
        if(mMoveLength >= mItemHeight)
        { // 向下滑动,最后一个元素放在头部
            mData.add(0, mData.remove(mData.size() - 1));
            mMoveLength = 0;
        }
        else if(mMoveLength <= -mItemHeight)
        { // 向上滑动，第一个元素放在尾部
            mData.add(mData.remove(0));
            mMoveLength = 0;
        }
    }

    /**
     * 移动到中间位置
     */
    private void moveToCenter()
    {

        if(!mScroller.isFinished() || mIsFling)
        {
            return;
        }
        cancelScroll();

        // 向下滑动
        if(mMoveLength > 0)
        {
            if(mMoveLength < mItemHeight / 2)
            {
                scroll(mMoveLength, 0);
            }
            else
            {
                scroll(mMoveLength, mItemHeight);
            }
        }
        else
        {
            if(-mMoveLength < mItemHeight / 2)
            {
                scroll(mMoveLength, 0);
            }
            else
            {
                scroll(mMoveLength, -mItemHeight);
            }
        }
    }

    /**
     * 平滑滚动
     *
     * @param from
     * @param to
     */
    private void scroll(float from, int to)
    {

        mLastScrollY = (int) from;
        mIsMovingCenter = true;
        mScroller.startScroll(0, (int) from, 0, 0);
        mScroller.setFinalY(to);
        invalidate();
    }


    /**
     * 惯性滑动
     *
     * @param from
     * @param vY：y方向的滑动速度
     */
    private void fling(float from, float vY)
    {
        mLastScrollY = (int) from;
        mIsFling = true;
        //最多可以惯性滑动10个item
        mScroller.fling(0, (int) from, 0, (int) vY, 0, 0, -10 * mItemHeight, 10 * mItemHeight);
        invalidate();
    }

    /**
     * 停止滚动
     */
    public void cancelScroll()
    {
        mIsFling = mIsMovingCenter = false;
        mScroller.abortAnimation();//停止滚动
    }


    /**
     * 快速滑动时候，惯性滑动一段距离
     */
    private class FlingOnGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        public boolean onDown(MotionEvent event)
        {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            if(mIsInertiaScroll)
            {
                cancelScroll();
                fling(mMoveLength, velocityY);
            }
            return true;
        }
    }

    private void notifySelected()
    {
        if(mListener != null)
        {
            // 告诉监听器选择完毕
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    mListener.onSelected(mData, mSelected);
                }
            });
        }
    }


    /**
     * item选中接口
     */
    public interface OnSelectedListener
    {
        void onSelected(List<String> data, int position);
    }


    public int getMinTextSize()
    {
        return mMinTextSize;
    }

    public void setMinTextSize(int mMinTextSize)
    {
        this.mMinTextSize = mMinTextSize;
        invalidate();
    }

    public int getMaxTextSize()
    {
        return mMaxTextSize;
    }

    public void setMaxTextSize(int mMaxTextSize)
    {
        this.mMaxTextSize = mMaxTextSize;
        invalidate();
    }

    public int getStartColor()
    {
        return mStartColor;
    }

    public void setStartColor(int mStartColor)
    {
        this.mStartColor = mStartColor;
        invalidate();
    }

    public int getEndColor()
    {
        return mEndColor;
    }

    public void setEndColor(int mEndColor)
    {
        this.mEndColor = mEndColor;
        invalidate();
    }

    public List<String> getData()
    {
        return mData;
    }

    public void setmData(List<String> mData)
    {
        if(mData == null)
        {
            mData = new ArrayList<String>();
        }
        this.mData = mData;
        mSelected = mData.size() / 2;
        invalidate();
    }

    public String getSelectedItem()
    {
        return mData.get(mSelected);
    }

    public void setSelectedPosition(int position)
    {
        if(position < 0 || position > mData.size() - 1
                || position == mSelected)
        {
            return;
        }
        int count = Math.abs(mSelected - position);
        List<String> list = new ArrayList<String>();
        if(position < mSelected)
        {
            list.addAll(mData.subList(mData.size() - count, mData.size()));
            list.addAll(mData.subList(0, mData.size() - count));
        }
        else
        {
            list.addAll(mData.subList(count, mData.size()));
            list.addAll(mData.subList(0, count));
        }
        mData = list;
        invalidate();
        if(mListener != null)
        {
            mListener.onSelected(mData, mSelected);
        }
    }

    public void setOnSelectedListener(OnSelectedListener listener)
    {
        mListener = listener;
    }

    public OnSelectedListener getListener()
    {
        return mListener;
    }

    public boolean isInertiaScroll()
    {
        return mIsInertiaScroll;
    }

    public void setInertiaScroll(boolean inertiaScroll)
    {
        this.mIsInertiaScroll = inertiaScroll;
    }
}
