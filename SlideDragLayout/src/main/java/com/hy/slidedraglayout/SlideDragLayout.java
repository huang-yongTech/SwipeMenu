package com.hy.slidedraglayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Created by huangyong on 2018/5/23
 * 自定义侧滑删除ViewGroup
 */
public class SlideDragLayout extends ViewGroup {
    public static final String TAG = "SlideDragLayout";

    //最小滑动距离，判断是滑动还是点击
    private int mScaleTouchSlop;
    //内容view
    private View mContentView;
    //菜单view
    private View mMenuView;

    private int mContentWidth;
    private int mMenuWidth;
    private ViewDragHelper mDragHelper;

    //缓存打开的菜单
    private SlideDragLayout mViewCache;

    private boolean mIsUnMoved;
    //记录SweepView的打开关闭状态
    private boolean mIsOpened;
    //多点触摸时，只允许一个item滑动
    private boolean mIsTouched;

    //滑动起始和结束坐标
    private PointF mFirstP;
    private PointF mLastP;
    private int mLastX;
    private int mLastY;

    private OnSlideDragListener onSlideDragListener;

    public SlideDragLayout(Context context) {
        this(context, null);
    }

    public SlideDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDragHelper = ViewDragHelper.create(this, 1.0f, new CallBack());
        mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mFirstP = new PointF();
        mLastP = new PointF();
    }

    /**
     * SweepView关闭打开状态监听器
     */
    public interface OnSlideDragListener {
        void onSlideStateChanged(SlideDragLayout view, boolean isOpened);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContentView = getChildAt(0);
        mMenuView = getChildAt(1);

        mContentWidth = mContentView.getMeasuredWidth();
        LayoutParams deleteParams = mMenuView.getLayoutParams();
        mMenuWidth = deleteParams.width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mContentView.measure(widthMeasureSpec, heightMeasureSpec);
        //delete按钮由于自己定义了宽度，需要自己重新获取宽度测量
        int deleteWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMenuWidth, MeasureSpec.EXACTLY);
        mMenuView.measure(deleteWidthMeasureSpec, heightMeasureSpec);

//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(mContentView.getMeasuredWidth() + mMenuWidth, mContentView.getMeasuredHeight());
//        } else if (widthMeasureSpec == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(mContentView.getMeasuredWidth() + mMenuWidth, heightSize);
//        } else if (heightMeasureSpec == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(widthSize, mContentView.getMeasuredHeight());
//        }
        setMeasuredDimension(widthMeasureSpec, mContentView.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mContentView.layout(0, 0, mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
        mMenuView.layout(mContentView.getMeasuredWidth(), 0,
                mContentView.getMeasuredWidth() + mMenuWidth, mMenuView.getMeasuredHeight());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsUnMoved = true;

                mFirstP.set(ev.getX(), ev.getY());
                mLastP.set(ev.getX(), ev.getY());

                //如果有侧滑菜单展开，拦截触摸事件
                if (mViewCache != null) {
                    if (mViewCache != this) {
                        mViewCache.smoothClose(-mMenuWidth, 0);
                    }

                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = mLastP.x - ev.getX();
                float deltaY = mLastP.y - ev.getY();

                //如果水平滑动距离大于竖直滑动距离，拦截触摸事件
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                mLastP.set(ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                mIsTouched = false;
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(ev.getX() - mFirstP.x) > mScaleTouchSlop) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(ev.getX() - mFirstP.x) > mScaleTouchSlop) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private class CallBack extends ViewDragHelper.Callback {

        /**
         * 捕获当前触摸的view
         * <p>
         * <p>If this method returns true, a call to {@link #onViewCaptured(View, int)}
         * will follow if the capture is successful.</p>
         *
         * @param child     Child the user is attempting to capture
         * @param pointerId ID of the pointer attempting the capture
         * @return true if capture should be allowed, false otherwise
         */
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child == mContentView || child == mMenuView;
        }

        /**
         * view水平拖动情况下的监听，获取view水平方向上拖动的距离，作用于touch down事件
         * 即当touch移动后的回调
         *
         * @param child 当前处理事件的子view
         * @param left  view水平拖动时距离坐标原点水平方向上的距离
         * @param dx    view拖动增量（相对于上一次view的位置）
         * @return 拖动后距离坐标原点水平方向上的距离
         */
        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            int contentWidth = mContentView.getMeasuredWidth();
            //处理边界事件
            if (child == mContentView) {
                if (left < 0 && -left > mMenuWidth) {
                    //左滑情况
                    return -mMenuWidth;
                } else if (left > 0) {
                    //右滑情况
                    return 0;
                }
            } else if (child == mMenuView) {
                if (left < contentWidth - mMenuWidth) {
                    //左滑情况
                    return contentWidth - mMenuWidth;
                } else if (left > contentWidth) {
                    //右滑情况
                    return contentWidth;
                }
            }
            return left;
        }

        /**
         * 当控件位置移动后的回调
         *
         * @param changedView 当前改变位置的view
         * @param left        当前view距离坐标原点水平方向上的距离
         * @param top         当前view距离坐标原点竖直方向上的距离
         * @param dx          水平增量
         * @param dy          竖直增量
         */
        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {

            ViewCompat.postInvalidateOnAnimation(SlideDragLayout.this);

            int contentWidth = mContentView.getMeasuredWidth();
            int contentHeight = mContentView.getMeasuredHeight();
            int deleteHeight = mMenuView.getMeasuredHeight();

            if (changedView == mContentView) {
                mMenuView.layout(contentWidth + left, 0, contentWidth + mMenuWidth + left, deleteHeight);
            } else if (changedView == mMenuView) {
                mContentView.layout(left - contentWidth, 0, left, contentHeight);
            }
        }

        /**
         * touch时间up时的回调
         *
         * @param releasedChild 松开了哪个字view
         * @param xvel          水平方向上的速率
         * @param yvel          竖直方向上的速率
         */
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            //获取水平方向滑动的距离
            int left = mContentView.getLeft();
//            int contentWidth = mContentView.getMeasuredWidth();

            if (Math.abs(left) < mMenuWidth / 2f) {
                //若滑动的距离小于删除按钮的一半，关闭侧滑
//                mDragHelper.smoothSlideViewTo(mContentView, 0, 0);
//                mDragHelper.smoothSlideViewTo(mMenuView, contentWidth, 0);
                smoothClose(0, 0);
            } else {
                //打开侧滑
//                mDragHelper.smoothSlideViewTo(mContentView, -mMenuWidth, 0);
//                mDragHelper.smoothSlideViewTo(mMenuView, contentWidth - mMenuWidth, 0);
                smoothExpand(-mMenuWidth, 0);
            }

            ViewCompat.postInvalidateOnAnimation(SlideDragLayout.this);
        }
    }

    /**
     * 展开菜单
     *
     * @param finalLeft 最终滑动到的x坐标位置
     * @param finalTop  最终滑动到的y坐标位置
     */
    private void smoothExpand(int finalLeft, int finalTop) {
        mDragHelper.smoothSlideViewTo(mContentView, finalLeft, finalTop);
        mDragHelper.smoothSlideViewTo(mMenuView, mContentWidth + finalLeft, finalTop);
    }

    /**
     * 关闭菜单
     *
     * @param finalLeft 最终滑动到的x坐标
     * @param finalTop  最终滑动到的y坐标
     */
    private void smoothClose(int finalLeft, int finalTop) {
        mDragHelper.smoothSlideViewTo(mContentView, finalLeft, finalTop);
        mDragHelper.smoothSlideViewTo(mMenuView, mContentWidth + finalLeft, finalTop);
    }

    /**
     * 当子view的位置发生变化时，父view会调用该方法重新绘制，直到子view位置变化完成
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(SlideDragLayout.this);
        }
    }

    public void setOnSlideDragListener(OnSlideDragListener onSlideDragListener) {
        this.onSlideDragListener = onSlideDragListener;
    }
}
