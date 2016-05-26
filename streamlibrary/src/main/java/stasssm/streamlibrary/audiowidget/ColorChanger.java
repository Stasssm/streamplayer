package stasssm.streamlibrary.audiowidget;

import android.graphics.Color;

/**
 * Helper class for changing color.
 */
class ColorChanger {

	private final float[] fromColorHsv;
	private final float[] toColorHsv;
	private final float[] resultColorHsv;

	public ColorChanger() {
		fromColorHsv = new float[3];
		toColorHsv = new float[3];
		resultColorHsv = new float[3];
	}

    public ColorChanger fromColor(int fromColor) {
		Color.colorToHSV(fromColor, fromColorHsv);
		return this;
	}

	public ColorChanger toColor(int toColor) {
		Color.colorToHSV(toColor, toColorHsv);
		return this;
	}

	public int nextColor(float dt) {
		for (int k = 0; k < 3; k++) {
			resultColorHsv[k] = fromColorHsv[k] + (toColorHsv[k] - fromColorHsv[k]) * dt;
		}
		return Color.HSVToColor(resultColorHsv);
	}
}
