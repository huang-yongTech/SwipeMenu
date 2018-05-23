package com.gcit.hy.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by huangyong on 2018/5/23
 * 自定义侧滑删除ViewGroup
 */
public class SweepView extends ViewGroup {
    public static final String TAG = "SweepView";

    private View mContentView;
    private View mDeleteView;
    private int mDeleteWidth;
    private ViewDragHelper mDragHelper;
    private boolean isOpened;//记录SweepView的打开关闭状态

    private OnSweepListener onSweepListener;

    public SweepView(Context context) {
        this(context, null);
    }

    public SweepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SweepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDragHelper = ViewDragHelper.create(this, new CallBack());
    }

    /**
     * SweepView关闭打开状态监听器
     */
    public interface OnSweepListener {
        void onSweepChanged(SweepView view, boolean isOpened);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContentView = getChildAt(0);
        mDeleteView = getChildAt(1);

        LayoutParams deleteParams = mDeleteView.getLayoutParams();
        mDeleteWidth = deleteParams.width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mContentView.measure(widthMeasureSpec, heightMeasureSpec);
        //delete按钮由于自己定义了宽度，需要自己重新获取宽度测量
        int deleteWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mDeleteWidth, MeasureSpec.EXACTLY);
        mDeleteView.measure(deleteWidthMeasureSpec, heightMeasureSpec);

//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(mContentView.getMeasuredWidth() + mDeleteWidth, mContentView.getMeasuredHeight());
//        } else if (widthMeasureSpec == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(mContentView.getMeasuredWidth() + mDeleteWidth, heightSize);
//        } else if (heightMeasureSpec == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(widthSize, mContentView.getMeasuredHeight());
//        }
        setMeasuredDimension(widthMeasureSpec, mContentView.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mContentView.layout(0, 0, mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
        mDeleteView.layout(mContentView.getMeasuredWidth(), 0,
                mContentView.getMeasuredWidth() + mDeleteWidth, mDeleteView.getMeasuredHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDragHelper == null) {
            return super.onTouchEvent(event);
        }
        mDragHelper.processTouchEvent(event);
        return true;
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
            return child == mContentView || child == mDeleteView;
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
                if (left < 0 && -left > mDeleteWidth) {
                    //左滑情况
                    return -mDeleteWidth;
                } else if (left > 0) {
                    //右滑情况
                    return 0;
                }
            } else if (child == mDeleteView) {
                if (left < contentWidth - mDeleteWidth) {
                    //左滑情况
                    return contentWidth - mDeleteWidth;
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

            ViewCompat.postInvalidateOnAnimation(SweepView.this);

            int contentWidth = mContentView.getMeasuredWidth();
            int contentHeight = mContentView.getMeasuredHeight();
            int deleteHeight = mDeleteView.getMeasuredHeight();

            if (changedView == mContentView) {
                mDeleteView.layout(contentWidth + left, 0, contentWidth + mDeleteWidth + left, deleteHeight);
            } else if (changedView == mDeleteView) {
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
            int contentWidth = mContentView.getMeasuredWidth();

            if (Math.abs(left) < mDeleteWidth / 2f) {
                //若滑动的距离小于删除按钮的一半，关闭侧滑
                mDragHelper.smoothSlideViewTo(mContentView, 0, 0);
                mDragHelper.smoothSlideViewTo(mDeleteView, contentWidth, 0);
            } else {
                //打开侧滑
                mDragHelper.smoothSlideViewTo(mContentView, -mDeleteWidth, 0);
                mDragHelper.smoothSlideViewTo(mDeleteView, contentWidth - mDeleteWidth, 0);
            }

            ViewCompat.postInvalidateOnAnimation(SweepView.this);
        }
    }

    /**
     * 当子view的位置发生变化时，父view会调用该方法重新绘制，知道子view位置变化完成
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(SweepView.this);
        }
    }

    public void setOnSweepListener(OnSweepListener onSweepListener) {
        this.onSweepListener = onSweepListener;
    }
}
