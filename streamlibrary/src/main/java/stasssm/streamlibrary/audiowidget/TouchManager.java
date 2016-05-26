package stasssm.streamlibrary.audiowidget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import stasssm.streamlibrary.R;

/**
 * Touch detector for views.
 */
class TouchManager implements View.OnTouchListener {

    private final View view;
    private final BoundsChecker boundsChecker;
    private final WindowManager windowManager;
    private final StickyEdgeAnimator stickyEdgeAnimator;
    private final VelocityAnimator velocityAnimator;

    private GestureListener gestureListener;
    private GestureDetector gestureDetector;
    private Callback callback;
    private int screenWidth;
    private int screenHeight;
    private Float lastRawX, lastRawY;
    private boolean touchCanceled;

    public TouchManager(@NonNull View view, @NonNull BoundsChecker boundsChecker) {
        this.gestureDetector = new GestureDetector(view.getContext(), gestureListener = new GestureListener());
        gestureDetector.setIsLongpressEnabled(true);
        this.view = view;
        this.boundsChecker = boundsChecker;
        this.view.setOnTouchListener(this);
        Context context = view.getContext().getApplicationContext();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels - context.getResources().getDimensionPixelSize(R.dimen.aw_status_bar_height);
        stickyEdgeAnimator = new StickyEdgeAnimator();
        velocityAnimator = new VelocityAnimator();
    }

    public TouchManager screenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
        return this;
    }

    public TouchManager screenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
        return this;
    }

    public TouchManager callback(Callback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public boolean onTouch(@NonNull View v, @NonNull MotionEvent event) {
        boolean res = (!touchCanceled || event.getAction() == MotionEvent.ACTION_UP) && gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchCanceled = false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!touchCanceled) {
                gestureListener.onUpEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!touchCanceled) {
                gestureListener.onMove(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            gestureListener.onTouchOutsideEvent(event);
            touchCanceled = false;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            touchCanceled = true;
        }
        return res;
    }

    /**
     * Touch manager callback.
     */
    interface Callback {

        /**
         * Called when user clicks on view.
         * @param x click x coordinate
         * @param y click y coordinate
         */
        void onClick(float x, float y);

        /**
         * Called when user long clicks on view.
         * @param x click x coordinate
         * @param y click y coordinate
         */
        void onLongClick(float x, float y);

        /**
         * Called when user touches screen outside view's bounds.
         */
        void onTouchOutside();

        /**
         * Called when user touches widget but not removed finger from it.
         * @param x x coordinate
         * @param y y coordinate
         */
        void onTouched(float x, float y);

        /**
         * Called when user drags widget.
         * @param diffX movement by X axis
         * @param diffY movement by Y axis
         */
        void onMoved(float diffX, float diffY);

        /**
         * Called when user releases finger from widget.
         * @param x x coordinate
         * @param y y coordinate
         */
        void onReleased(float x, float y);

        /**
         * Called when sticky edge animation completed.
         */
        void onAnimationCompleted();
    }

    public static class SimpleCallback implements Callback {

        @Override
        public void onClick(float x, float y) {

        }

        @Override
        public void onLongClick(float x, float y) {

        }

        @Override
        public void onTouchOutside() {

        }

        @Override
        public void onTouched(float x, float y) {

        }

        @Override
        public void onMoved(float diffX, float diffY) {

        }

        @Override
        public void onReleased(float x, float y) {

        }

        @Override
        public void onAnimationCompleted() {

        }

    }

    /**
     * Interface that return sticky bounds for widget.
     */
    interface BoundsChecker {

        /**
         * Get sticky left position.
         * @param screenWidth screen width
         * @return sticky left position
         */
        float stickyLeftSide(float screenWidth);

        /**
         * Get sticky right position.
         * @param screenWidth screen width
         * @return sticky right position
         */
        float stickyRightSide(float screenWidth);

        /**
         * Get sticky top position.
         * @param screenHeight screen height
         * @return sticky top position
         */
        float stickyTopSide(float screenHeight);

        /**
         * Get sticky bottom position.
         * @param screenHeight screen height
         * @return sticky bottom position
         */
        float stickyBottomSide(float screenHeight);
    }

    /**
     * View's gesture listener.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private int prevX, prevY;
        private float velX, velY;
        private long lastEventTime;

        @Override
        public boolean onDown(MotionEvent e) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
            prevX = params.x;
            prevY = params.y;
            boolean result = !stickyEdgeAnimator.isAnimating();
            if (result) {
                if (callback != null) {
                    callback.onTouched(e.getX(), e.getY());
                }
            }
            return result;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (callback != null) {
                callback.onClick(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float diffX = e2.getRawX() - e1.getRawX();
            float diffY = e2.getRawY() - e1.getRawY();
            float l = prevX + diffX;
            float t = prevY + diffY;
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
            params.x = (int) l;
            params.y = (int) t;
            try {
                windowManager.updateViewLayout(view, params);
            } catch (IllegalArgumentException e) {
                // view not attached to window
            }
            if (callback != null) {
                callback.onMoved(distanceX, distanceY);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (callback != null) {
                callback.onLongClick(e.getX(), e.getY());
            }
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            float x = 0.0f;
            float y = 0.0f;
            int metaState = 0;
            MotionEvent event = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_CANCEL,
                    x,
                    y,
                    metaState
            );
            view.dispatchTouchEvent(event);
//            onUpEvent(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            velocityAnimator.animate(velX, velY);
            return true;
        }

        private void onMove(MotionEvent e2) {
            if (lastRawX != null && lastRawY != null) {
                long diff = e2.getEventTime() - lastEventTime;
                float dt = diff == 0 ? 0 : 1000f / diff;
                float newVelX = (e2.getRawX() - lastRawX) * dt;
                float newVelY = (e2.getRawY() - lastRawY) * dt;
                velX = DrawableUtils.smooth(velX, newVelX, 0.2f);
                velY = DrawableUtils.smooth(velY, newVelY, 0.2f);
            }
            lastRawX = e2.getRawX();
            lastRawY = e2.getRawY();
            lastEventTime = e2.getEventTime();
        }

        private void onUpEvent(MotionEvent e) {
            if (callback != null) {
                callback.onReleased(e.getX(), e.getY());
            }
            lastRawX = null;
            lastRawY = null;
            lastEventTime = 0;
            velX = velY = 0;
            if (!velocityAnimator.isAnimating()) {
                stickyEdgeAnimator.animate(boundsChecker);
            }
        }

        private void onTouchOutsideEvent(MotionEvent e) {
            if (callback != null) {
                callback.onTouchOutside();
            }
        }
    }

    /**
     * Helper class for animating fling gesture.
     */
    private class VelocityAnimator {
        private final ValueAnimator velocityAnimator;
        private final PropertyValuesHolder dxHolder;
        private final PropertyValuesHolder dyHolder;
        private final Interpolator interpolator;
        private WindowManager.LayoutParams params;
        private long prevPlayTime;

        public VelocityAnimator() {
            interpolator = new DecelerateInterpolator();
            dxHolder = PropertyValuesHolder.ofFloat("dx", 0, 0);
            dyHolder = PropertyValuesHolder.ofFloat("dy", 0, 0);
            dxHolder.setEvaluator(new FloatEvaluator());
            dyHolder.setEvaluator(new FloatEvaluator());
            velocityAnimator = ValueAnimator.ofPropertyValuesHolder(dxHolder, dyHolder);
            velocityAnimator.setInterpolator(interpolator);
            velocityAnimator.setDuration(400);
            velocityAnimator.addUpdateListener(animation -> {
                long curPlayTime = animation.getCurrentPlayTime();
                long dt = curPlayTime - prevPlayTime;
                float dx = (float) animation.getAnimatedValue("dx") * dt / 1000f;
                float dy = (float) animation.getAnimatedValue("dy") * dt / 1000f;
                prevPlayTime = curPlayTime;
                params.x += dx;
                params.y += dy;
                if (callback != null) {
                    callback.onMoved(dx, dy);
                }
                try {
                    windowManager.updateViewLayout(view, params);
                } catch (IllegalArgumentException e) {
                    animation.cancel();
                }
            });
            velocityAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    prevPlayTime = 0;
                    stickyEdgeAnimator.animate(boundsChecker);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    prevPlayTime = 0;
                    stickyEdgeAnimator.animate(boundsChecker);
                }
            });
        }

        public void animate(float velocityX, float velocityY) {
            if (isAnimating())
                return;
            params = (WindowManager.LayoutParams) view.getLayoutParams();
            dxHolder.setFloatValues(velocityX, 0);
            dyHolder.setFloatValues(velocityY, 0);
            velocityAnimator.start();
        }

        public boolean isAnimating() {
            return velocityAnimator.isRunning();
        }
    }

    /**
     * Helper class for animating sticking to screen edge.
     */
    private class StickyEdgeAnimator {
        private final PropertyValuesHolder dxHolder;
        private final PropertyValuesHolder dyHolder;
        private final ValueAnimator edgeAnimator;
        private final Interpolator interpolator;
        private WindowManager.LayoutParams params;

        public StickyEdgeAnimator() {
            interpolator = new OvershootInterpolator();
            dxHolder = PropertyValuesHolder.ofInt("x", 0, 0);
            dyHolder = PropertyValuesHolder.ofInt("y", 0, 0);
            dxHolder.setEvaluator(new IntEvaluator());
            dyHolder.setEvaluator(new IntEvaluator());
            edgeAnimator = ValueAnimator.ofPropertyValuesHolder(dxHolder, dyHolder);
            edgeAnimator.setInterpolator(interpolator);
            edgeAnimator.setDuration(400);
            edgeAnimator.addUpdateListener(animation -> {
                int x = (int) animation.getAnimatedValue("x");
                int y = (int) animation.getAnimatedValue("y");
                if (callback != null) {
                    callback.onMoved(x - params.x, y - params.y);
                }
                params.x = x;
                params.y = y;
                try {
                    windowManager.updateViewLayout(view, params);
                } catch (IllegalArgumentException e) {
                    // view not attached to window
                    animation.cancel();
                }
            });
            edgeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (callback != null) {
                        callback.onAnimationCompleted();
                    }
                }
            });
        }

        private void animate(BoundsChecker boundsChecker) {
            animate(boundsChecker, null);
        }

        public void animate(BoundsChecker boundsChecker, @Nullable Runnable afterAnimation) {
            if (edgeAnimator.isRunning())
                return;
            params = (WindowManager.LayoutParams) view.getLayoutParams();
            float cx = params.x + view.getWidth() / 2f;
            float cy = params.y + view.getWidth() / 2f;
            int x;
            if (cx < screenWidth / 2f) {
                x = (int) boundsChecker.stickyLeftSide(screenWidth);
            } else {
                x = (int) boundsChecker.stickyRightSide(screenWidth);
            }
            int y = params.y;
            int top = (int) boundsChecker.stickyTopSide(screenHeight);
            int bottom = (int) boundsChecker.stickyBottomSide(screenHeight);
            if (params.y > bottom || params.y < top) {
                if (cy < screenHeight / 2f) {
                    y = top;
                } else {
                    y = bottom;
                }
            }
            dxHolder.setIntValues(params.x, x);
            dyHolder.setIntValues(params.y, y);
            edgeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    edgeAnimator.removeListener(this);
                    if (afterAnimation != null) {
                        afterAnimation.run();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    edgeAnimator.removeListener(this);
                    if (afterAnimation != null) {
                        afterAnimation.run();
                    }
                }
            });
            edgeAnimator.start();
        }

        public boolean isAnimating() {
            return edgeAnimator.isRunning();
        }
    }

    void animateToBounds(BoundsChecker boundsChecker, @Nullable Runnable afterAnimation) {
        stickyEdgeAnimator.animate(boundsChecker, afterAnimation);
    }

    void animateToBounds() {
        stickyEdgeAnimator.animate(boundsChecker, null);
    }
}
