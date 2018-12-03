package com.minhpd.dragdetectorlayout;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;

public class DragDetectorLayout extends FrameLayout {
    static final String TAG = DragDetectorLayout.class.getSimpleName();

    public static final int BOTTOM_TO_TOP = 0;
    public static final int TOP_TO_BOTTOM = 1;
    public static final int LEFT_TO_RIGHT = 2;
    public static final int RIGHT_TO_LEFT = 3;

    boolean maybeStartTracking;
    boolean startedTracking;
    int startedTrackingX;
    int startedTrackingY;
    float lastTouchX;
    float lastTouchY;

    VelocityTracker velocityTracker;
    boolean canInterceptTouch = true;
    boolean forceInterceptTouch = false;
    int minFlingVelocity = 1000;
    boolean disableTouch = false;
    int direction = -1;

    OnDragListener onDragListener;

    boolean isDragging;

    public DragDetectorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        minFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    public void setOnDragListener(OnDragListener listener) {
        onDragListener = listener;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (!forceInterceptTouch) {
            onTouchEvent(null);
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        } else {
            ViewParent parent = getParent();
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return canInterceptTouch && onTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (disableTouch) {
            maybeStartTracking = false;
            startedTracking = false;
            isDragging = false;
            return false;
        }

        if (event != null) {
            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN && !startedTracking && !maybeStartTracking) {
                maybeStartTracking = true;
                startedTrackingX = (int) event.getX();
                startedTrackingY = (int) event.getY();
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isDragging = false;

                if (velocityTracker != null) {
                    velocityTracker.clear();
                }
            } else if (action == MotionEvent.ACTION_CANCEL
                    || action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_POINTER_UP) {
                startedTracking = false;
                maybeStartTracking = false;

                if (velocityTracker != null) {
                    velocityTracker.addMovement(event);
                }

                if (isDragging && velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000);
                    final float x = event.getX();
                    final float y = event.getY();
                    lastTouchX = x;
                    lastTouchY = y;
                    final float vx = velocityTracker.getXVelocity();
                    final float vy = velocityTracker.getYVelocity();

                    if (onDragListener != null) {
                        if (direction == BOTTOM_TO_TOP || direction == TOP_TO_BOTTOM) {
                            boolean open = (direction == BOTTOM_TO_TOP) && (vy < 0 && ((y - startedTrackingY) != 0) && (Math.abs(vy) > minFlingVelocity));
                            onDragListener.onReleaseDragging(x, y, vx, vy, !open, false);
                        } else {
                            boolean flingFastEnough = Math.abs(vx) > minFlingVelocity;
                            int minDistance = getMeasuredWidth() / 2;
                            float translationX = x - startedTrackingX;
                            boolean open = (direction == LEFT_TO_RIGHT) && ((Math.abs(translationX) >= minDistance) || (vx > 0 && translationX != 0 && flingFastEnough));
                            boolean close = (direction == RIGHT_TO_LEFT) && ((Math.abs(x) <= minDistance) || (vx < 0 && translationX != 0 && flingFastEnough));
                            onDragListener.onReleaseDragging(x, y, vx, vy, !open, !close);
                        }
                    }
                }
                isDragging = false;
            } else if (action == MotionEvent.ACTION_MOVE) {
                float x = event.getX();
                float y = event.getY();
                int transX = (int) x - startedTrackingX;
                int transY = (int) y - startedTrackingY;
                float dx = x - lastTouchX;
                float dy = y = lastTouchY;
                float dragThreshold = getPixelsInCM(0.4f, false);

                if (maybeStartTracking && !startedTracking) {
                    direction = -1;
                    boolean isMoved = false;
                    if (!isMoved && transY > 0 && Math.abs(transY) >= dragThreshold && Math.abs(transY) / 3 > Math.abs(transX)) {
                        Log.v(TAG, "move top to bottom");
                        isMoved = true;
                        direction = TOP_TO_BOTTOM;
                    } else if (!isMoved && transY < 0 && Math.abs(transY) >= dragThreshold && Math.abs(transY) / 3 > Math.abs(transX)) {
                        Log.v(TAG, "move bottom to top");
                        isMoved = true;
                        direction = BOTTOM_TO_TOP;
                    } else if (!isMoved && transX > 0 && Math.abs(transX) >= dragThreshold && Math.abs(transX) / 3 > Math.abs(transY)) {
                        Log.v(TAG, "move left to right");
                        isMoved = true;
                        direction = LEFT_TO_RIGHT;
                    } else if (!isMoved && transX < 0 && Math.abs(transX) >= dragThreshold && Math.abs(transX) / 3 > Math.abs(transY)) {
                        Log.v(TAG, "move right to left");
                        isMoved = true;
                        direction = RIGHT_TO_LEFT;
                    }

                    if (isMoved) {
                        maybeStartTracking = false;
                        startedTracking = true;
                        startedTrackingX = (int) event.getX();
                        startedTrackingY = (int) event.getY();
                        if (velocityTracker == null) {
                            velocityTracker = VelocityTracker.obtain();
                        }
                        velocityTracker.addMovement(event);

                        if (!isDragging) {
                            isDragging = true;
                            if (onDragListener != null) {
                                onDragListener.onStartDragging(startedTrackingX, startedTrackingY, direction);
                            }
                        }
                    }
                } else if (startedTracking) { // animation scroll comes here
                    if (isDragging) {
                        if (onDragListener != null) {
                            lastTouchX = x;
                            lastTouchY = y;
                            onDragListener.onDragging(transX, transY, dx, dy);
                        }
                        if (null != velocityTracker) {
                            velocityTracker.addMovement(event);
                        }
                    }
                }
            }
        } else if (event == null) {
            maybeStartTracking = false;
            startedTracking = false;
            isDragging = false;
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
        }

        return true;
    }

    float getPixelsInCM(float cm, boolean isX) {
        return (cm / 2.54f) * (isX ? Resources.getSystem().getDisplayMetrics().xdpi
                : Resources.getSystem().getDisplayMetrics().ydpi);
    }

    public interface OnDragListener {
        void onStartDragging(float startX, float startY, int direction);

        void onDragging(float translationX, float translationY, float dx, float dy);

        void onReleaseDragging(float x, float y, float vx, float vy, boolean cancelOpen, boolean cancelClose);
    }
}
