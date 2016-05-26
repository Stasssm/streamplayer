package stasssm.streamlibrary.audiowidget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import java.util.Random;

/**
 * Audio widget configuration class.
 */
class Configuration {

	public static final float FRAME_SPEED = 70.0f;

	public static final long LONG_CLICK_THRESHOLD = ViewConfiguration.getLongPressTimeout() + 128;
    public static final int STATE_STOPPED = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final long TOUCH_ANIMATION_DURATION = 100;

    private final int lightColor;
	private final int darkColor;
	private final int progressColor;
	private final int expandedColor;
	private final Random random;
	private final float width;
	private final float height;
	private final Drawable playDrawable;
	private final Drawable pauseDrawable;
	private final Drawable prevDrawable;
	private final Drawable nextDrawable;
	private final Drawable playlistDrawable;
	private final Drawable albumDrawable;
	private final Drawable additionalDrawable ;
	private final Context context;
	private final PlaybackState playbackState;
    private final int buttonPadding;
    private final float crossStrokeWidth;
    private final float progressStrokeWidth;
    private final float shadowRadius;
    private final float shadowDx;
    private final float shadowDy;
    private final int shadowColor;
    private final float bubblesMinSize;
    private final float bubblesMaxSize;
    private final int crossColor;
    private final int crossOverlappedColor;
    private final Interpolator accDecInterpolator;
    private final int prevNextExtraPadding;

	private Configuration(Builder builder) {
		this.context = builder.context;
		this.random = builder.random;
		this.width = builder.width;
		this.height = builder.radius;
		this.lightColor = builder.lightColor;
		this.darkColor = builder.darkColor;
		this.progressColor = builder.progressColor;
		this.expandedColor = builder.expandedColor;
		this.playlistDrawable = builder.playlistDrawable;
		this.playDrawable = builder.playDrawable;
		this.pauseDrawable = builder.pauseDrawable;
		this.prevDrawable = builder.prevDrawable;
		this.nextDrawable = builder.nextDrawable;
		this.albumDrawable = builder.albumDrawable;
		this.additionalDrawable = builder.additionalDrawable ;
		this.playbackState = builder.playbackState;
        this.buttonPadding = builder.buttonPadding;
        this.crossStrokeWidth = builder.crossStrokeWidth;
        this.progressStrokeWidth = builder.progressStrokeWidth;
        this.shadowRadius = builder.shadowRadius;
        this.shadowDx = builder.shadowDx;
        this.shadowDy = builder.shadowDy;
        this.shadowColor = builder.shadowColor;
        this.bubblesMinSize = builder.bubblesMinSize;
        this.bubblesMaxSize = builder.bubblesMaxSize;
        this.crossColor = builder.crossColor;
        this.crossOverlappedColor = builder.crossOverlappedColor;
        this.accDecInterpolator = builder.accDecInterpolator;
        this.prevNextExtraPadding = builder.prevNextExtraPadding;
	}

	public Context context() {
		return context;
	}

	public Random random() {
		return random;
	}

	@ColorInt
	public int lightColor() {
		return lightColor;
	}

	@ColorInt
	public int darkColor() {
		return darkColor;
	}

	@ColorInt
	public int progressColor() {
		return progressColor;
	}

	@ColorInt
	public int expandedColor() {
		return expandedColor;
	}

	public float widgetWidth() {
		return width;
	}

	public float radius() {
		return height;
	}

	public Drawable playDrawable() {
		return playDrawable;
	}

	public Drawable pauseDrawable() {
		return pauseDrawable;
	}

	public Drawable prevDrawable() {
		return prevDrawable;
	}

	public Drawable nextDrawable() {
		return nextDrawable;
	}

	public Drawable playlistDrawable() {
		return playlistDrawable;
	}

	public Drawable albumDrawable() {
		return albumDrawable;
	}

	public Drawable additionalDrawable() {return additionalDrawable ; }

	public PlaybackState playbackState() {
		return playbackState;
	}

    public float crossStrokeWidth() {
        return crossStrokeWidth;
    }

    public float progressStrokeWidth() {
        return progressStrokeWidth;
    }

    public int buttonPadding() {
        return buttonPadding;
    }

    public float shadowRadius() {
        return shadowRadius;
    }

    public float shadowDx() {
        return shadowDx;
    }

    public float shadowDy() {
        return shadowDy;
    }

    public int shadowColor() {
        return shadowColor;
    }

    public float bubblesMinSize() {
        return bubblesMinSize;
    }

    public float bubblesMaxSize() {
        return bubblesMaxSize;
    }

    public int crossColor() {
        return crossColor;
    }

    public int crossOverlappedColor() {
        return crossOverlappedColor;
    }

    public Interpolator accDecInterpolator() {
        return accDecInterpolator;
    }

    public int prevNextExtraPadding() {
        return prevNextExtraPadding;
    }

    public static final class Builder {

		private int lightColor;
		private int darkColor;
		private int progressColor;
		private int expandedColor;
		private float width;
		private float radius;
		private Context context;
		private Random random;
		private Drawable playDrawable;
		private Drawable pauseDrawable;
		private Drawable prevDrawable;
		private Drawable nextDrawable;
		private Drawable playlistDrawable;
		private Drawable albumDrawable;
		private Drawable additionalDrawable ;
		private PlaybackState playbackState;
        private int buttonPadding;
        private float crossStrokeWidth;
        private float progressStrokeWidth;
        private float shadowRadius;
        private float shadowDx;
        private float shadowDy;
        private int shadowColor;
        private float bubblesMinSize;
        private float bubblesMaxSize;
        private int crossColor;
        private int crossOverlappedColor;
        private Interpolator accDecInterpolator;
        private int prevNextExtraPadding;

        public Builder context(Context context) {
			this.context = context;
			return this;
		}

		public Builder playColor(@ColorInt int pauseColor) {
			this.lightColor = pauseColor;
			return this;
		}

		public Builder darkColor(@ColorInt int playColor) {
			this.darkColor = playColor;
			return this;
		}

		public Builder progressColor(@ColorInt int progressColor) {
			this.progressColor = progressColor;
			return this;
		}

		public Builder expandedColor(@ColorInt int expandedColor) {
			this.expandedColor = expandedColor;
			return this;
		}

		public Builder random(Random random) {
			this.random = random;
			return this;
		}

		public Builder widgetWidth(float width) {
			this.width = width;
			return this;
		}

		public Builder radius(float radius) {
			this.radius = radius;
			return this;
		}

		public Builder playDrawable(@Nullable Drawable playDrawable) {
			this.playDrawable = playDrawable;
			return this;
		}

		public Builder pauseDrawable(@Nullable Drawable pauseDrawable) {
			this.pauseDrawable = pauseDrawable;
			return this;
		}

		public Builder prevDrawable(@Nullable Drawable prevDrawable) {
			this.prevDrawable = prevDrawable;
			return this;
		}

		public Builder nextDrawable(@Nullable Drawable nextDrawable) {
			this.nextDrawable = nextDrawable;
			return this;
		}

		public Builder playlistDrawable(@Nullable Drawable plateDrawable) {
			this.playlistDrawable = plateDrawable;
			return this;
		}

		public Builder albumDrawable(@Nullable Drawable albumDrawable) {
			this.albumDrawable = albumDrawable;
			return this;
		}

		public Builder additionalDrawable(@Nullable Drawable additionalDrawable) {
			this.additionalDrawable = additionalDrawable;
			return this;
		}


		public Builder playbackState(PlaybackState playbackState) {
			this.playbackState = playbackState;
			return this;
		}

        public Builder buttonPadding(int buttonPadding) {
            this.buttonPadding = buttonPadding;
            return this;
        }

        public Builder crossStrokeWidth(float crossStrokeWidth) {
            this.crossStrokeWidth = crossStrokeWidth;
            return this;
        }

        public Builder progressStrokeWidth(float progressStrokeWidth) {
            this.progressStrokeWidth = progressStrokeWidth;
            return this;
        }

        public Builder shadowRadius(float shadowRadius) {
            this.shadowRadius = shadowRadius;
            return this;
        }

        public Builder shadowDx(float shadowDx) {
            this.shadowDx = shadowDx;
            return this;
        }

        public Builder shadowDy(float shadowDy) {
            this.shadowDy = shadowDy;
            return this;
        }

        public Builder shadowColor(@ColorInt int shadowColor) {
            this.shadowColor = shadowColor;
            return this;
        }

        public Builder bubblesMinSize(float bubblesMinSize) {
            this.bubblesMinSize = bubblesMinSize;
            return this;
        }

        public Builder bubblesMaxSize(float bubblesMaxSize) {
            this.bubblesMaxSize = bubblesMaxSize;
            return this;
        }

        public Builder crossColor(@ColorInt int crossColor) {
            this.crossColor = crossColor;
            return this;
        }

        public Builder crossOverlappedColor(@ColorInt int crossOverlappedColor) {
            this.crossOverlappedColor = crossOverlappedColor;
            return this;
        }

        public Builder accDecInterpolator(Interpolator accDecInterpolator) {
            this.accDecInterpolator = accDecInterpolator;
            return this;
        }

        public Builder prevNextExtraPadding(int prevNextExtraPadding) {
            this.prevNextExtraPadding = prevNextExtraPadding;
            return this;
        }

        public Configuration build() {
			return new Configuration(this);
		}
    }
}
