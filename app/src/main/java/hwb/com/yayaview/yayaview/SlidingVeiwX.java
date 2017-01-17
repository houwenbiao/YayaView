package hwb.com.yayaview.yayaview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import hwb.com.yayaview.R;

/**
 * Created by Administrator on 2017/1/3.
 */

public class SlidingVeiwX extends View
{
    private Paint mTextPaint;
    private String mText;
    private int mTextSize;
    private int mTextColor;
    private int viewWidth;//视图view宽度
    private int maxWidth;
    private int mAscent;
    private int downX;//按下时候的x位置

    private VelocityTracker velocityTracker;//速度追踪器
    private final Flinger flinger;
    private final int minimumVelocity;
    private final int maximumVelocity;


    public SlidingVeiwX(Context context)
    {
        this(context, null);
    }

    public SlidingVeiwX(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public SlidingVeiwX(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initView();
        this.flinger = new Flinger(context);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        /**
         * 获取自定义属性资源
         */
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingVeiwX);
        mTextColor = array.getColor(R.styleable.SlidingVeiwX_textColor, Color.RED);
        mTextSize = (int) array.getDimension(R.styleable.SlidingVeiwX_textSize, 50);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        if(array != null)
        {
            array.recycle();
        }
    }

    public void setText(String mText)
    {
        this.mText = mText;
        requestLayout();
        invalidate();
    }

    public void setTextSize(int size)
    {
        mTextSize = size;
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color)
    {
        mTextColor = color;
        requestLayout();
        invalidate();
    }

    /**
     * 视图初始化
     */
    private void initView()
    {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        setPadding(3, 3, 3, 3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        //作为叶子节点的view必须setMeasureDimesion否则抛异常
        //（或者调用super.onMeasure(w,h);但是对于子view调用super.onMeasure(w,h)无意义）
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    /**
     * 计算view的宽度
     * @param measureSpec
     * @return
     */
    private int measureWidth(int measureSpec)
    {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        viewWidth = specSize;
        //match_parent或者具体数值，直接使用；否则自己计算
        if(specMode == MeasureSpec.EXACTLY)
        {
            result = specSize;
            if((int) (mTextPaint.measureText(mText) + getPaddingLeft() + getPaddingRight()) > result)
            {
                maxWidth = (int) (mTextPaint.measureText(mText) + getPaddingLeft() + getPaddingRight());
            }
        }
        else
        {
            //计算文字宽度
            result = (int) (mTextPaint.measureText(mText) + getPaddingLeft() + getPaddingRight());
            maxWidth = result;
            if(specMode == MeasureSpec.AT_MOST)//wrap_content
            {
                //取出specSize和计算出的文字宽度最小值，如果result大则说明文字超出view的宽度大小
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * 计算view的高度
     * @param measureHeight
     * @return
     */
    private int measureHeight(int measureHeight)
    {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureHeight);
        int specSize = MeasureSpec.getSize(measureHeight);
        mAscent = (int) mTextPaint.ascent();//获取底部距离baseline的高度

        if(specMode == MeasureSpec.EXACTLY)
        {
            result = specSize;
        }
        else
        {
            result = (int) (mTextPaint.descent() - mAscent + getPaddingBottom() + getPaddingTop());
            if(specMode == MeasureSpec.AT_MOST)
            {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mTextPaint);
    }

    /**
     * TextView加上横向滚动条
     * ACTION_DOWN纪录按下x位置，
     * ACTION_MOVE用当按下x减去当前x获得需要滑动距离调用scrollBy滑动
     * 在ACTION_UP抬起时需要根据当前的速度来惯性的再滑动一段距离，
     * 所以需要纪录手指抬起的速度，和需要滑动的最大距离
     * @param event
     * @return
     */

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(velocityTracker == null)
        {
            velocityTracker = VelocityTracker.obtain();//初始化速度追踪器
        }
        velocityTracker.addMovement(event);//添加事件到速度追踪器中

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();//记录按下位置的x坐标
                if(! flinger.isFinished())//如果正在滚动马上停止
                {
                    flinger.forceFinished();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                //x方向上移动的距离
                int dx = (int) (downX - event.getX());
                downX = (int) event.getX();
                scrollBy(dx, 0);
                break;

            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = this.velocityTracker;
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);//计算当前速度（按1s为单位）
                int velocityX = (int) velocityTracker.getXVelocity();//获取x方向速度
                int velocityY = (int) velocityTracker.getYVelocity();
                if(Math.abs(velocityX) > minimumVelocity  || Math.abs(velocityY) > minimumVelocity)
                {
                    flinger.start(getScrollX(), getScrollY(), velocityX, 0, getMaxScrollx(), 0);
                }
                else
                {
                    if(this.velocityTracker != null)
                    {
                        this.velocityTracker.recycle();
                        this.velocityTracker = null;
                    }
                }
                break;

            default:

                break;
        }
        return true;
    }

    /**
     * 获取最大的滑动距离
     * @return
     */
    public int getMaxScrollx()
    {
        Log.i("yaya", "maxWidth:" + maxWidth);
        Log.i("yaya", "viewWidth:" + viewWidth);
        if(maxWidth - viewWidth > 0)
        {
            return maxWidth - viewWidth;
        }
        else
        {
            return 0;
        }
    }

    /**
     * 根据两个最大宽度获取可滑动最大距离
     * 对超出范围进行判断
     * @param dx
     * @param dy
     */
    public void scrollBy(int dx, int dy)
    {
        //超出最大范围
        if(getScrollX() + dx > getMaxScrollx())
        {
            super.scrollBy(getMaxScrollx() - getScrollX(), 0);
        }
        //超出最小范围
        else if(getScrollX() + dx < 0)
        {
            super.scrollBy(-getScrollX(),0);
        }
        else
        {
            super.scrollBy(dx, 0);
        }
    }

    private class Flinger implements Runnable
    {
        //滚动工具类
        private final Scroller scroller;
        private int lastX = 0;
        private int lastY = 0;
        Flinger(Context context)
        {
            scroller = new Scroller(context);
        }

        void start(int initX, int initY, int initialVelocityX, int initialVelocityY, int maxX, int maxY)
        {
            scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY);
            lastX = initX;
            lastY = initY;
            post(this);
        }


        @Override
        public void run()
        {
            if(scroller.isFinished())
            {
                return;
            }
            boolean more = scroller.computeScrollOffset();//获取是否需要继续滑动
            int x = scroller.getCurrX();//获取滑动中的当前x
            int y = scroller.getCurrY();
            int diffX = lastX - x;//取增量
            int diffY = lastY - y;
            if(diffX != 0 || diffY != 0)
            {
                scrollBy(diffX, diffY);
                lastX = x;
                lastY = y;//记录当前位置
            }
            //如果需要继续滑动，再次执行
            if(more)
            {
                post(this);
            }
        }
        boolean isFinished()
        {
            return scroller.isFinished();
        }

        void forceFinished()
        {
            if(!scroller.isFinished())
            {
                scroller.forceFinished(true);
            }
        }
    }
}
