package com.hy.slideitemlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by huangyong on 2018/5/23
 * 自定义列表item侧滑菜单ViewGroup
 */
public class SlideItemLayout extends ViewGroup {
    public static final String TAG = "SlideItemLayout";

    private Scroller mScroller;
    //滑动速率计算器
    private VelocityTracker mVelocityTracker;
    //最小滑动距离，判断是滑动还是点击
    private int mScaleTouchSlop;
    //最大滑动速率，用于处理手指快速滑动的情况
    private int mMaxFlingVelocity;
    //内容view
    private View mContentView;

    private int mMenuWidth;

    //滑动临界值，超过该值展开，没超过关闭
    private int mSlideLimit;

    //多点触控时，保存第一个触点
    private int mPointerId;
    //多点触摸时，只允许一个item滑动（每次在DOWN里面判断，结束后即UP时清空）
    private static boolean mIsTouching;

    //缓存打开的菜单（设置为static表示该变量为列表中的item所共享）
    private static SlideItemLayout mViewCache;

    //标志位，判断是处于滑动状态还是点击状态
    //说明：在dispatch方法中，每次DOWN时设置为true；MOVE时判断，如果是滑动动作则设置为false；
    //最后在intercept方法里，UP时判断该变量，若仍为true，则说明是点击事件，若有菜单展开关闭菜单，若没有菜单展开，则处理点击事件
    private boolean mIsUnMoved = true;

    //判断item是否处于滑动状态，若处于滑动状态，则屏蔽一切点击事件
    private boolean mIsItemSlided;

    //是否仿IOS阻塞式滑动，默认为是
    private boolean mIsIos;
    //仿IOS阻塞式滑动标志位（即：有一个侧滑菜单打开时，点击界面其他地方或者改打开的侧滑菜单的内容部分，该侧滑菜单关闭）
    private boolean mIosInterceptFlag;

    //滑动起始和结束坐标
    private PointF mFirstP;
    private PointF mLastP;

    public SlideItemLayout(Context context) {
        this(context, null);
    }

    public SlideItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlideItemLayout, defStyleAttr, 0);
        //初始化是否仿IOS阻塞式滑动，默认为true
        mIsIos = array.getBoolean(R.styleable.SlideItemLayout_isIos, true);
        array.recycle();

        mScroller = new Scroller(context);
        mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaxFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

        mFirstP = new PointF();
        mLastP = new PointF();
    }

    public boolean isIsIos() {
        return mIsIos;
    }

    public void setIsIos(boolean isIos) {
        this.mIsIos = isIos;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //设置自己可以点击，从而获取触摸事件
        setClickable(true);

        //由于RecyclerView等列表存在item复用，因此每次都需要将宽高等数据重新初始化，侧滑菜单的宽高也是如此
        int contentWidth = 0;
        mMenuWidth = 0;
        int height = 0;

        int paddingStart = getPaddingStart();
        int paddingEnd = getPaddingEnd();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);

            if (childView.getVisibility() != GONE) {
                measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();

                if (heightMode == MeasureSpec.EXACTLY) {
                    //父View的高度测量模式为精确值（MATCH_PARENT或具体数值）时，设置其高度为测量所得的高度
                    height = Math.max(height, heightSize);
                } else {
                    //父view高度测量模式为WRAP_CONTENT情况时，设置其高度为子view的最大高度
                    height = Math.max(height, paddingTop + paddingBottom + params.topMargin + params.bottomMargin + childView.getMeasuredHeight());
                }

                if (i > 0) {
                    mMenuWidth += params.leftMargin + params.rightMargin + childView.getMeasuredWidth();
                    //设置菜单滑出距离为第一个子菜单宽度的一半
                    mSlideLimit = getChildAt(1).getMeasuredWidth() / 2;
                } else {
                    mContentView = childView;
                    contentWidth = params.leftMargin + params.rightMargin + childView.getMeasuredWidth();
                }
            }
        }

        setMeasuredDimension(contentWidth + paddingStart + paddingEnd, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingStart();
        int top = getPaddingTop();

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                child.layout(left + params.leftMargin,
                        top + params.topMargin,
                        left + params.leftMargin + child.getMeasuredWidth(),
                        top + params.topMargin + child.getMeasuredHeight());
                left += params.rightMargin + child.getMeasuredWidth();
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        acquireVelocityTracker(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIosInterceptFlag = false;//每次DOWN时，重新初始化仿IOS滑动拦截标志
                mIsUnMoved = true;//初始为未滑动状态
                mIsItemSlided = false;//判断手指起始落点，如果距离属于滑动了，则屏蔽一切点击事件

                //如果有别的手指触碰了这个view，则返回false
                //这样后续的MOVE等事件就不会再来找这个view了
                if (mIsTouching) {
                    return false;
                } else {
                    mIsTouching = true;
                }

                mFirstP.set(ev.getX(), ev.getY());
                mLastP.set(ev.getX(), ev.getY());

                //如果有侧滑菜单展开，拦截触摸事件
                if (mViewCache != null) {
                    //仿ios，有侧滑菜单弹出时，且不是当前点击的菜单，拦截点击事件并关闭展开的菜单
                    if (mViewCache != this) {
                        mViewCache.smoothClose();

                        //设置是否为IOS阻塞式滑动
                        mIosInterceptFlag = mIsIos;
                    }

                    //只要有一个侧滑菜单处于打开状态，就拦截事件，不让父层列表上下滑动
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                //多点触控时，只获取第一个触点，用于计算滑动速率
                mPointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                //仿ios阻塞模式开启并且当前有item处于打开状态，且不是当前触摸的item，就拦截该MOVE事件，MOVE事件也不该出现
                if (mIosInterceptFlag) {
                    break;
                }

                float deltaX = mLastP.x - ev.getX();
                float deltaY = mLastP.y - ev.getY();

                //在水平滑动过程中阻止父列表在竖直方向上的滑动
                if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > mScaleTouchSlop
                        || Math.abs(getScrollX()) > mScaleTouchSlop) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    //设置item状态为滑动状态
                    mIsUnMoved = false;
                }

                //开始滑动
                scrollBy((int) deltaX, 0);

                //滑动越界处理
                if (getScrollX() < 0) {
                    scrollTo(0, 0);
                } else if (getScrollX() > mMenuWidth) {
                    scrollTo(mMenuWidth, 0);
                }

                mLastP.set(ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_UP:
                //这里加入cancel事件是防止出现手指滑动到屏幕外面去的情况
            case MotionEvent.ACTION_CANCEL:
                if (Math.abs(ev.getX() - mFirstP.x) > mScaleTouchSlop) {
                    mIsItemSlided = true;
                }

                //判断是否是IOS阻塞式滑动，如果是并且已有侧滑菜单打开，则拦截滑动事件，即不执行滑动
                if (!mIosInterceptFlag) {
                    //设置每秒的最大滑动速率
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                    //获取当前滑动速率
                    float velocityX = mVelocityTracker.getXVelocity(mPointerId);

                    //如果手指的滑动速度超过1000速率，菜单根据滑动速率的正负直接展开或关闭
                    if (Math.abs(velocityX) > 1000) {
                        if (velocityX > 1000) {
                            smoothClose();
                        } else {
                            smoothExpand();
                        }
                    } else {
                        //否则根据实际滑动情况判断
                        //即滑动的距离超过菜单宽度的一半，展开菜单；否则关闭菜单
                        if (Math.abs(getScrollX()) > mSlideLimit) {
                            smoothExpand();
                        } else {
                            smoothClose();
                        }
                    }
                }

                releaseVelocityTracker();
                //没有人再触摸我了（现在的问题是没有走到这一步）
                mIsTouching = false;
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //仿IOS点击其他区域关闭展开的item，拦截DOWN事件，不让DOWN实现传递到onTouchEvent方法中
                if (mIosInterceptFlag) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //滑动时拦截,屏蔽滑动时子view的点击的事件
                if (Math.abs(ev.getX() - mFirstP.x) > mScaleTouchSlop) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (getScrollX() > mScaleTouchSlop) {
                    //侧滑时，屏蔽子view内容区域的点击事件（即点击内容区域时，关闭菜单），子view菜单区域正常点击
                    if (ev.getX() < getWidth() - getScrollX()) {
                        if (mIsUnMoved) {
                            smoothClose();
                        }
                        return true;
                    }
                }

                //如果item处于滑动状态，屏蔽一切点击事件（好像并没有什么用）
                if (mIsItemSlided) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 展开菜单
     */
    private void smoothExpand() {
        mViewCache = SlideItemLayout.this;

        //设置内容区域不可长按
        if (null != mContentView) {
            mContentView.setLongClickable(false);
        }

        mScroller.startScroll(getScrollX(), 0, 0, 0);
        mScroller.setFinalX(mMenuWidth);
        invalidate();
    }

    /**
     * 关闭菜单
     */
    private void smoothClose() {
        mViewCache = null;

        //设置内容区域可长按
        if (null != mContentView) {
            mContentView.setLongClickable(true);
        }

        mScroller.startScroll(getScrollX(), 0, 0, 0);
        mScroller.setFinalX(0);
        invalidate();
    }

    /**
     * 采用scroller处理item滑动时，需要重写该方法
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        //若还没有完成移动
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    /**
     * 在view离开屏幕时，手动回收展开菜单的缓存，防止内存泄漏，因为该缓存是静态的，需要手动回收
     */
    @Override
    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            mViewCache.smoothClose();
            mViewCache = null;
        }
        super.onDetachedFromWindow();
    }

    /**
     * @param event 向VelocityTracker添加Event
     * @see VelocityTracker#obtain()
     * @see VelocityTracker#addMovement(MotionEvent)
     */
    private void acquireVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 侧滑菜单展开时，屏蔽长按事件
     */
    @Override
    public boolean performLongClick() {
        if (Math.abs(getScrollX()) > mScaleTouchSlop) {
            return false;
        }
        return super.performLongClick();
    }

    /**
     * 当侧滑item有多个菜单时，并且需要点击菜单使得该展开的item关闭时，调用此方法
     */
    public void close() {
        if (this == mViewCache) {
            mViewCache.smoothClose();
            mViewCache = null;
        }
    }

//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//
//        mContentView = getChildAt(0);
//        mMenuView = getChildAt(1);
//
//        mContentWidth = mContentView.getMeasuredWidth();
//        LayoutParams deleteParams = mMenuView.getLayoutParams();
//        mMenuWidth = deleteParams.width;
//
//        mSlideLimit = mMenuWidth / 2;
//    }

//    /**
//     * 当子view的位置发生变化时，父view会调用该方法重新绘制，直到子view位置变化完成
//     * 采用ViewDragHelper处理view的滑动时需要重写该方法
//     */
//    @Override
//    public void computeScroll() {
//        super.computeScroll();
//        if (mDragHelper.continueSettling(true)) {
//            ViewCompat.postInvalidateOnAnimation(SlideItemLayout.this);
//        }
//    }

//    private class CallBack extends ViewDragHelper.Callback {
//
//        /**
//         * 捕获当前触摸的view
//         * <p>
//         * <p>If this method returns true, a call to {@link #onViewCaptured(View, int)}
//         * will follow if the capture is successful.</p>
//         *
//         * @param child     Child the user is attempting to capture
//         * @param pointerId ID of the pointer attempting the capture
//         * @return true if capture should be allowed, false otherwise
//         */
//        @Override
//        public boolean tryCaptureView(@NonNull View child, int pointerId) {
//            return child == mContentView || child == mMenuView;
//        }
//
//        /**
//         * view水平拖动情况下的监听，获取view水平方向上拖动的距离，作用于touch down事件
//         * 即当touch移动后的回调
//         *
//         * @param child 当前处理事件的子view
//         * @param left  view水平拖动时距离坐标原点水平方向上的距离
//         * @param dx    view拖动增量（相对于上一次view的位置）
//         * @return 拖动后距离坐标原点水平方向上的距离
//         */
//        @Override
//        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
//            int contentWidth = mContentView.getMeasuredWidth();
//            //处理滑动边界事件
//            if (child == mContentView) {
//                if (left < 0 && -left > mMenuWidth) {
//                    //左滑情况
//                    return -mMenuWidth;
//                } else if (left > 0) {
//                    //右滑情况
//                    return 0;
//                }
//            } else if (child == mMenuView) {
//                if (left < contentWidth - mMenuWidth) {
//                    //左滑情况
//                    return contentWidth - mMenuWidth;
//                } else if (left > contentWidth) {
//                    //右滑情况
//                    return contentWidth;
//                }
//            }
//            return left;
//        }
//
//        /**
//         * 当控件位置移动后的回调
//         *
//         * @param changedView 当前改变位置的view
//         * @param left        当前view距离坐标原点水平方向上的距离
//         * @param top         当前view距离坐标原点竖直方向上的距离
//         * @param dx          水平增量
//         * @param dy          竖直增量
//         */
//        @Override
//        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
//
//            ViewCompat.postInvalidateOnAnimation(SlideItemLayout.this);
//
//            int contentWidth = mContentView.getMeasuredWidth();
//            int contentHeight = mContentView.getMeasuredHeight();
//            int deleteHeight = mMenuView.getMeasuredHeight();
//
//            if (changedView == mContentView) {
//                mMenuView.layout(contentWidth + left, 0, contentWidth + mMenuWidth + left, deleteHeight);
//            } else if (changedView == mMenuView) {
//                mContentView.layout(left - contentWidth, 0, left, contentHeight);
//            }
//        }
//
//        /**
//         * touch时间up时的回调
//         *
//         * @param releasedChild 松开了哪个字view
//         * @param xvel          水平方向上的速率
//         * @param yvel          竖直方向上的速率
//         */
//        @Override
//        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
//            //获取水平方向滑动的距离
//            int left = mContentView.getLeft();
//            int contentWidth = mContentView.getMeasuredWidth();
//
//            if (Math.abs(left) < mMenuWidth / 2f) {
//                //若滑动的距离小于删除按钮的一半，关闭侧滑
//                mDragHelper.smoothSlideViewTo(mContentView, 0, 0);
//                mDragHelper.smoothSlideViewTo(mMenuView, contentWidth, 0);
//            } else {
//                //打开侧滑
//                mDragHelper.smoothSlideViewTo(mContentView, -mMenuWidth, 0);
//               mDragHelper.smoothSlideViewTo(mMenuView, contentWidth - mMenuWidth, 0);
//            }
//
//            ViewCompat.postInvalidateOnAnimation(SlideItemLayout.this);
//        }
//    }
}
