package com.example.MyGridView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class DragGridView extends GridView {

    //手指相对于GridView的坐标位置
    private int gridViewX, gridViewY;
    // 手指相对于item的坐标
    private int itemViewX, itemViewY;
    // 手指相对于window的坐标
    private int rawx, rawy;

    private Vibrator mVibrator;

    public DragGridView(Context context) {
        super(context);
        init(context);
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        windowManager = (WindowManager) getContext().getApplicationContext().
                getSystemService(Context.WINDOW_SERVICE);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        //将布局文件中设置的间距dip转为px
        //	mHorizontalSpacing = DataTools.dip2px(context, mHorizontalSpacing);
    }


    // down事件先执行这个方法
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 单点触摸屏幕按下事件，记录此时x和y的数据
        Log.e("yijun", "--myDragGird onInterceptTouchEvent--");
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 注意此时接受该方法的是GirdView。所以getX()获取的是相对于GirdView左上角的坐标
            gridViewX = (int) ev.getX();
            gridViewY = (int) ev.getY();

            rawx = (int) ev.getRawX();
            rawy = (int) ev.getRawY();
            // 监听长按事件
            setOnItemClickListener(ev);

            setOnItemClickListener(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }


    /**
     * from: 开始拖动的item的position
     */
    private int startPosition;
    /**
     * to:手指结束拖动时候的位置
     */
    private int dropPosition;

    /**
     * item的高
     */
    private int itemHeight;
    /**
     * item的宽
     */
    private int itemWidth;

    private Bitmap mDragBitmap;

    View onView;

    /*
     * 长按点击监听
	 */
    private void setOnItemClickListener(final MotionEvent ev) {

        setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                // 如果位置有效
                if (position != AdapterView.INVALID_POSITION) {
                    // 记录第一次点击的位置
                    startPosition = position;// 开始拖动item的位置
                    // 获取当前位置的ViewGroup或者item
                    ViewGroup dragViewGroup = (ViewGroup) getChildAt(startPosition
                            - getFirstVisiblePosition());


                    // 获取当前item的宽和高
                    itemHeight = dragViewGroup.getHeight();
                    itemWidth = dragViewGroup.getWidth();
                    // 屏幕上的x和y dragViewGroup.getLeft()是当前item相对于父控件GirdView的间距
                    itemViewX = gridViewX - dragViewGroup.getLeft();// item相对自己左上角的x值，以自己的view的左上角为(0,0)
                    itemViewY = gridViewY - dragViewGroup.getTop();// item相对自己的左上角
                    // 设置震动时间
                    mVibrator.vibrate(50);
                    // 开始拖动getRawX()获取相对于屏幕左上角的位置

                    // 隐藏当前的item
                    dragViewGroup.setVisibility(View.INVISIBLE);
                    requestDisallowInterceptTouchEvent(true);

                    //开启mDragItemView绘图缓存  
                    dragViewGroup.setDrawingCacheEnabled(true);
                    //获取mDragItemView在缓存中的Bitmap对象 
                    mDragBitmap = Bitmap.createBitmap(dragViewGroup.getDrawingCache());
                    //这一步很关键，释放绘图缓存，避免出现重复的镜像  
                    dragViewGroup.destroyDrawingCache();

                    startDrag(mDragBitmap, rawx - itemViewX, rawy - itemViewY);

                    Log.d("yijun", "rawx - itemViewX" + rawx + "--" + itemViewX + "--" + gridViewX
                            + "--" + dragViewGroup.getLeft() + "/" + (rawx - itemViewX));
                    onView = dragViewGroup;
                    return true;
                }

                return false;
            }
        });

    }


    //绘画
    /**
     * windowManager管理器
     */
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams windowParams = null;
    float dragScale = 1f;

    protected void startDrag(Bitmap dragBitmap, int x, int y) {
        windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.TOP | Gravity.LEFT;
        // 计算item左上角的坐标值
        windowParams.x = x;
        windowParams.y = y;

        // 放大dragScale倍，可以设置拖动后的倍数
        windowParams.width = (int) (dragScale * dragBitmap.getWidth());
        windowParams.height = (int) (dragScale * dragBitmap.getHeight());

        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;
        //item生成
        ImageView iv = new ImageView(getContext());
        iv.setImageBitmap(dragBitmap);
        windowManager = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);// "window"
        windowManager.addView(iv, windowParams);

        dragImageView = iv;

    }

    private View dragImageView;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (dragImageView != null
                && startPosition != AdapterView.INVALID_POSITION) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:// 当手势移动的时候
                    Log.e("yijun", "--on moving--");
                    onDrag((int) ev.getRawX(), (int) ev.getRawY());
                    //移动其他的item
                    onSwapItem((int) ev.getRawX(), (int) ev.getRawY());
                    break;
                case MotionEvent.ACTION_UP:
                    // 手指抬起的时候让drawImageView从windowManage里删除
                    stopDrag();

                    onView.setVisibility(View.VISIBLE);
                    requestDisallowInterceptTouchEvent(false);
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    private void onDrag(int rawx, int rawy) {
        if (dragImageView != null) {
            // 设置窗口的透明度
            windowParams.alpha = 0.6f;
            // 重新计算此时item的x和y坐标的位置
            windowParams.x = rawx - itemViewX;
            windowParams.y = rawy - itemViewY;
            // 更新view的布局，也就是重新绘制它的位置
            windowManager.updateViewLayout(dragImageView, windowParams);
        }
    }


    /**
     * 停止之前的拖动，把之前拖动的那个item从windowManage里面remove掉
     **/
    private void stopDrag() {
        if (dragImageView != null) {
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }
    }

    private void onSwapItem(int moveX, int moveY) {
        int tempItem = pointToPosition(moveX, moveY);


        Log.i("yijun", "tempItem" + tempItem);
        if (tempItem != -1) {
            View view = getChildAt(tempItem);
            view.setVisibility(View.INVISIBLE);
            onView.setVisibility(View.VISIBLE);
            onView = view;
        }


    }
}