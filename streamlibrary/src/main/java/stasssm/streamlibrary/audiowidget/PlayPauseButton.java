package stasssm.streamlibrary.audiowidget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.Random;

import stasssm.streamlibrary.R;
import stasssm.streamlibrary.main.FFmpegMediaPlayer;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.main.PlayerService.PlayerListener;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.playercore.PlayerController;

/**
 * Collapsed state view.
 */
@SuppressLint("ViewConstructor")
class PlayPauseButton extends View implements PlaybackState.PlaybackStateListener,PlayerListener {

	private static final float BUBBLES_ANGLE_STEP = 18.0f;
	private static final float ANIMATION_TIME_F = 8 * Configuration.FRAME_SPEED;
	private static final long ANIMATION_TIME_L = (long) ANIMATION_TIME_F;
	private static final float COLOR_ANIMATION_TIME_F = ANIMATION_TIME_F / 4f;
	private static final float COLOR_ANIMATION_TIME_START_F = (ANIMATION_TIME_F - COLOR_ANIMATION_TIME_F) / 2;
	private static final float COLOR_ANIMATION_TIME_END_F = COLOR_ANIMATION_TIME_START_F + COLOR_ANIMATION_TIME_F;
	private static final int TOTAL_BUBBLES_COUNT = (int) (360 / BUBBLES_ANGLE_STEP);
	static final long PROGRESS_CHANGES_DURATION = (long) (6 * Configuration.FRAME_SPEED);
    private static final long PROGRESS_STEP_DURATION = (long) (3 * Configuration.FRAME_SPEED);

	private static final int INDEX_PLAY = 0;
	private static final int INDEX_PREV = 1;
	private static final int INDEX_NEXT = 2;


    private final Paint buttonPaint;
	private final Paint bubblesPaint;
	private final Paint progressPaint;
	private final int pausedColor;
	private final int playingColor;
	private final float[] bubbleSizes;
	private final float[] bubbleSpeeds;
	private final float[] bubbleSpeedCoefficients;
	private final Random random;
	private final ColorChanger colorChanger;
	private final Drawable playDrawable;
	private final Drawable pauseDrawable;
	private final Drawable nextDrawable ;
	private final Drawable prevDrawable ;
	private final Drawable additionalDrawable ;
	private final RectF bounds;
	private final Rect[] rects = new Rect[3] ;
	private final float radius;
	private final PlaybackState playbackState;
	private final ValueAnimator touchDownAnimator;
	private final ValueAnimator touchUpAnimator;
	private final ValueAnimator bubblesAnimator;
	private final ValueAnimator progressAnimator;
    private final float buttonPadding;
    private final float bubblesMinSize;
    private final float bubblesMaxSize;
	private final int centerButtonRadius;
	private final int nextPrevButtonSize ;
	private final int nextPrevEdgesPadding ;
	private final int additionalIconWidth ;
	private final int additionalIconHeight ;
	private final int additionalIconPaddingFromCenter;

	private boolean animatingBubbles;
	private float randomStartAngle;
	private float buttonSize = 1.0f;
	private float progress = 0.0f;
	private float animatedProgress = 0;
	private boolean progressChangesEnabled;

    public PlayPauseButton(@NonNull Configuration configuration) {
		super(configuration.context());
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		this.playbackState = configuration.playbackState();
		this.random = configuration.random();
		this.buttonPaint = new Paint();
		this.buttonPaint.setColor(configuration.lightColor());
		this.buttonPaint.setStyle(Paint.Style.FILL);
		this.buttonPaint.setAntiAlias(true);
		this.buttonPaint.setShadowLayer(
				configuration.shadowRadius(),
				configuration.shadowDx(),
				configuration.shadowDy(),
				configuration.shadowColor()
		);
        this.bubblesMinSize = configuration.bubblesMinSize();
        this.bubblesMaxSize = configuration.bubblesMaxSize();
		this.bubblesPaint = new Paint();
		this.bubblesPaint.setStyle(Paint.Style.FILL);
		this.progressPaint = new Paint();
		this.progressPaint.setAntiAlias(true);
		this.progressPaint.setStyle(Paint.Style.STROKE);
		this.progressPaint.setStrokeWidth(configuration.progressStrokeWidth());
		this.progressPaint.setColor(configuration.progressColor());
		this.pausedColor = configuration.lightColor();
		this.playingColor = configuration.darkColor();
		this.radius = configuration.radius();
        this.buttonPadding = configuration.buttonPadding();
        this.bounds = new RectF();
		this.bubbleSizes = new float[TOTAL_BUBBLES_COUNT];
		this.bubbleSpeeds = new float[TOTAL_BUBBLES_COUNT];
		this.bubbleSpeedCoefficients = new float[TOTAL_BUBBLES_COUNT];
		this.colorChanger = new ColorChanger();
		this.playDrawable = configuration.playDrawable().getConstantState().newDrawable().mutate();
		this.pauseDrawable = configuration.pauseDrawable().getConstantState().newDrawable().mutate();
		this.prevDrawable = configuration.prevDrawable().getConstantState().newDrawable().mutate();
		this.nextDrawable = configuration.nextDrawable().getConstantState().newDrawable().mutate();
		this.additionalDrawable = configuration.additionalDrawable().getConstantState().newDrawable().mutate();
		this.pauseDrawable.setAlpha(0);
		this.playbackState.addPlaybackStateListener(this);
		this.centerButtonRadius = (int) getResources().getDimension(R.dimen.aw_play_pause_size)/2;
		this.nextPrevButtonSize = (int) getResources().getDimension(R.dimen.aw_next_prev_button_size);
		this.nextPrevEdgesPadding = (int) getResources().getDimension(R.dimen.aw_next_prev_edges_padding);
		this.additionalIconWidth = (int) getResources().getDimension(R.dimen.aw_additional_icon_width);
		this.additionalIconHeight = (int) getResources().getDimension(R.dimen.aw_additional_icon_height);
		this.additionalIconPaddingFromCenter = (int) getResources().getDimension(R.dimen.aw_additional_icon_padding_from_center);
		final ValueAnimator.AnimatorUpdateListener listener = animation -> {
			buttonSize = (float) animation.getAnimatedValue();
			invalidate();
		};
		this.touchDownAnimator = ValueAnimator.ofFloat(1, 0.9f).setDuration(Configuration.TOUCH_ANIMATION_DURATION);
		this.touchDownAnimator.addUpdateListener(listener);
		this.touchUpAnimator = ValueAnimator.ofFloat(0.9f, 1).setDuration(Configuration.TOUCH_ANIMATION_DURATION);
		this.touchUpAnimator.addUpdateListener(listener);
		this.bubblesAnimator = ValueAnimator.ofInt(0, (int)ANIMATION_TIME_L).setDuration(ANIMATION_TIME_L);
        this.bubblesAnimator.setInterpolator(new LinearInterpolator());
		this.bubblesAnimator.addUpdateListener(animation -> {
			long position = animation.getCurrentPlayTime();
			float fraction = animation.getAnimatedFraction();
			updateBubblesPosition(position, fraction);
			invalidate();
		});
		this.bubblesAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				animatingBubbles = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				animatingBubbles = false;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				super.onAnimationCancel(animation);
				animatingBubbles = false;
			}
		});
		this.progressAnimator = new ValueAnimator();
		this.progressAnimator.addUpdateListener(animation -> {
			animatedProgress = (float) animation.getAnimatedValue();
			invalidate();
		});
		setAlpha(0.88f);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		PlayerController.getInstance().attachListener(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		PlayerController.getInstance().detachListener(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int size = MeasureSpec.makeMeasureSpec((int) (radius * 4), MeasureSpec.EXACTLY);
		super.onMeasure(size, size);
	}

	private void updateBubblesPosition(long position, float fraction) {
		int alpha = (int) DrawableUtils.customFunction(fraction, 0, 0, 0, 0.3f, 255, 0.5f, 225, 0.7f, 0, 1f);
		bubblesPaint.setAlpha(alpha);
		if (DrawableUtils.isBetween(position, COLOR_ANIMATION_TIME_START_F, COLOR_ANIMATION_TIME_END_F)) {
			//float colorDt = DrawableUtils.normalize(position, COLOR_ANIMATION_TIME_START_F, COLOR_ANIMATION_TIME_END_F);
			//buttonPaint.setColor(colorChanger.nextColor(colorDt));
			if (playbackState.state() == Configuration.STATE_PLAYING) {
				//pauseDrawable.setAlpha((int) DrawableUtils.between(255 * colorDt, 0, 255));
				//playDrawable.setAlpha((int) DrawableUtils.between(255 * (1 - colorDt), 0, 255));
			} else {
				//playDrawable.setAlpha((int) DrawableUtils.between(255 * colorDt, 0, 255));
				//pauseDrawable.setAlpha((int) DrawableUtils.between(255 * (1 - colorDt), 0, 255));
			}
		}
		for (int i=0; i<TOTAL_BUBBLES_COUNT; i++) {
			bubbleSpeeds[i] = fraction * bubbleSpeedCoefficients[i];
		}
	}

	private int getTouchedAreaIndex(int x, int y) {
		int index = -1;
		for (int i = 0; i < rects.length; i++) {
			if (rects[i].contains(x, y)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public void onClick(float x , float y) {
		int touchIndex = getTouchedAreaIndex((int)x,(int)y);
		PlayerService playerService = PlayerService.getSharedService();
		if (playerService != null) {
			switch (touchIndex) {
				case INDEX_PLAY: {
					if (isAnimationInProgress()) {
						return;
					}
					playerService.togglePlayPause();
					if (playbackState.state() == Configuration.STATE_PLAYING) {
						colorChanger
								.fromColor(playingColor)
								.toColor(pausedColor);
						bubblesPaint.setColor(pausedColor);
					} else {
						colorChanger
								.fromColor(pausedColor)
								.toColor(playingColor);
						bubblesPaint.setColor(playingColor);
					}
					startBubblesAnimation();
					break;
				}
				case INDEX_NEXT: {
					playerService.playNext();
					break;
				}
				case INDEX_PREV : {
					playerService.playPrevious();
				}
			}
		}
	}

	private void startBubblesAnimation() {
		randomStartAngle = 360 * random.nextFloat();
		for (int i=0; i<TOTAL_BUBBLES_COUNT; i++) {
			float speed = 0.5f + 0.5f * random.nextFloat();
			float size = bubblesMinSize + (bubblesMaxSize - bubblesMinSize) * random.nextFloat();
			float radius = size / 2f;
			bubbleSizes[i] = radius;
			bubbleSpeedCoefficients[i] = speed;
		}
		bubblesAnimator.start();
	}

	public boolean isAnimationInProgress() {
		return animatingBubbles;
	}

	public void onTouchDown(float x , float y) {
		touchDownAnimator.start();
	}

	public void onTouchUp(float x , float y) {
		touchUpAnimator.start();
	}

	@Override
	public void onDraw(@NonNull Canvas canvas) {
		float cx = getWidth() >> 1;
		float cy = getHeight() >> 1;
		canvas.scale(buttonSize, buttonSize, cx, cy);
		if (animatingBubbles) {
			for (int i=0; i<TOTAL_BUBBLES_COUNT; i++) {
				float angle = randomStartAngle + BUBBLES_ANGLE_STEP * i;
				float speed = bubbleSpeeds[i];
				float x = DrawableUtils.rotateX(cx, cy * (1 - speed), cx, cy, angle);
				float y = DrawableUtils.rotateY(cx, cy * (1 - speed), cx, cy, angle);
				canvas.drawCircle(x, y, bubbleSizes[i], bubblesPaint);
			}
		}
		boolean isPlaying = PlayerService.getSharedService().isPlaying();
		if (!isPlaying) {
			playDrawable.setAlpha(255);
			pauseDrawable.setAlpha(0);
            // in case widget was drawn without animation in different state
            if (buttonPaint.getColor() != pausedColor) {
                buttonPaint.setColor(pausedColor);
            }
		} else {
			playDrawable.setAlpha(0);
			pauseDrawable.setAlpha(255);
            // in case widget was drawn without animation in different state
            if (buttonPaint.getColor() != playingColor) {
                buttonPaint.setColor(playingColor);
            }
		}

		canvas.drawCircle(cx, cy, radius, buttonPaint);
		float padding = progressPaint.getStrokeWidth() / 2f;
		bounds.set(cx - radius + padding, cy - radius + padding, cx + radius - padding, cy + radius - padding);
		canvas.drawArc(bounds, -90, animatedProgress, false, progressPaint);

		int l = (int) (cx - centerButtonRadius);
		int t = (int) (cy - centerButtonRadius);
		int r = (int) (cx + centerButtonRadius);
		int b = (int) (cy + centerButtonRadius);
		if (!isPlaying) {
			playDrawable.setBounds(l, t, r, b);
			playDrawable.draw(canvas);
			rects[INDEX_PLAY] = playDrawable.getBounds();
		}
		if (isPlaying) {
			pauseDrawable.setBounds(l, t, r, b);
			pauseDrawable.draw(canvas);
			rects[INDEX_PLAY] = pauseDrawable.getBounds();
		}

		int prevL = (int) cx - (nextPrevEdgesPadding+nextPrevButtonSize) ;
		int prevT = (int) cy - nextPrevButtonSize/2 ;
		int prevR =	(int) cx - nextPrevEdgesPadding ;
		int prevB = (int) cy + nextPrevButtonSize/2 ;
		prevDrawable.setBounds(prevL,prevT,prevR,prevB);
		prevDrawable.draw(canvas);
		rects[INDEX_PREV] = prevDrawable.getBounds();

		int nextL = (int) cx + nextPrevEdgesPadding ;
		int nextT = (int) cy - nextPrevButtonSize/2 ;
		int nextR =	(int) cx + (nextPrevEdgesPadding+nextPrevButtonSize) ;
		int nextB = (int) cy + nextPrevButtonSize/2 ;
		nextDrawable.setBounds(nextL,nextT,nextR,nextB);
		nextDrawable.draw(canvas);
		rects[INDEX_NEXT] = nextDrawable.getBounds();

		int adL =(int) cx -(additionalIconWidth/2);
		int adT = (int) cy - (additionalIconPaddingFromCenter + additionalIconHeight);
		int adR = (int) cx+(additionalIconWidth/2) ;
		int adB = (int) cy - additionalIconPaddingFromCenter ;

		additionalDrawable.setBounds(adL, adT, adR, adB);
		additionalDrawable.draw(canvas);


	}

	@Override
	public void onStateChanged(int oldState, int newState, Object initiator) {
		if (initiator instanceof AudioWidget)
			return;
		if (newState == Configuration.STATE_PLAYING) {
			buttonPaint.setColor(playingColor);
			//pauseDrawable.setAlpha(255);
			//playDrawable.setAlpha(0);
		} else {
			buttonPaint.setColor(pausedColor);
			//pauseDrawable.setAlpha(0);
			//playDrawable.setAlpha(255);
		}
		postInvalidate();
	}

	@Override
	public void onProgressChanged(int position, int duration, float percentage) {
		if (percentage > progress) {
            float old = progress;
            post(() -> {
                if (animateProgressChanges(old * 360, percentage * 360, PROGRESS_STEP_DURATION)) {
                    progress = percentage;
                }
            });
        } else {
            this.progress = percentage;
            this.animatedProgress = percentage * 360;
            postInvalidate();
        }

	}

	public void enableProgressChanges(boolean enable) {
		if (progressChangesEnabled == enable)
			return;
		progressChangesEnabled = enable;
		if (progressChangesEnabled) {
			animateProgressChangesForce(0, progress * 360, PROGRESS_CHANGES_DURATION);
		} else {
			animateProgressChangesForce(progress * 360, 0, PROGRESS_CHANGES_DURATION);
		}
	}

	private void animateProgressChangesForce(float oldValue, float newValue, long duration) {
        if (progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        animateProgressChanges(oldValue, newValue, duration);
    }

	private boolean animateProgressChanges(float oldValue, float newValue, long duration) {
		if (progressAnimator.isRunning()) {
			return false;
		}
		progressAnimator.setFloatValues(oldValue, newValue);
		progressAnimator.setDuration(duration);
		progressAnimator.start();
        return true;
	}

    public TouchManager.BoundsChecker newBoundsChecker(int offsetX, int offsetY) {
        return new BoundsCheckerImpl(radius, offsetX, offsetY);
    }

	@Override
	public void onSongChanged(StreamSong item, int position) {

	}

	@Override
	public void onPlayerPrepared() {

	}

	@Override
	public void onStateChanged(boolean isPlaying) {
		if (isPlaying) {
			pauseDrawable.setAlpha(255);
			playDrawable.setAlpha(0);
			playbackState.state(Configuration.STATE_PLAYING,this);
		} else {
			pauseDrawable.setAlpha(0);
			playDrawable.setAlpha(255);
			playbackState.state(Configuration.STATE_PAUSED, this);
		}
		postInvalidate();
	}

	@Override
	public void onSongPreparing() {

	}

	@Override
	public void onSongPrepared() {

	}

	@Override
	public void mediaPlayerChanged(FFmpegMediaPlayer mediaPlayer) {

	}

	@Override
	public void onBufferingUpdate(FFmpegMediaPlayer mp, int percent) {

	}

	private static final class BoundsCheckerImpl extends AudioWidget.BoundsCheckerWithOffset {

        private float radius;

        public BoundsCheckerImpl(float radius, int offsetX, int offsetY) {
            super(offsetX, offsetY);
            this.radius = radius;
        }

        @Override
        public float stickyLeftSideImpl(float screenWidth) {
            return -radius;
        }

        @Override
        public float stickyRightSideImpl(float screenWidth) {
            return screenWidth - radius * 3;
        }

        @Override
        public float stickyBottomSideImpl(float screenHeight) {
            return screenHeight - radius * 3;
        }

        @Override
        public float stickyTopSideImpl(float screenHeight) {
            return -radius;
        }
    }
}