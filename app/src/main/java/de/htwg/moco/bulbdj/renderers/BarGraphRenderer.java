package de.htwg.moco.bulbdj.renderers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Class displays the data as an canvas bar element.
 * The bars are oriented as a circle.
 *
 * @author Daniel Steidinger
 * @version 1.0
 */
public class BarGraphRenderer {
    /**
     * The lines to draw.
     */
    private float[] mFFTPoints;

    /**
     * The divisions for the count of lines to draw.
     */
    private int divisions;

    /**
     * Instance of {@Paint} class.
     */
    private Paint paint;

    /**
     * The radius of the circle to draw.
     */
    private int radius;

    /**
     * Default constructor.
     *
     * @param divisions the divisions for the count of lines to draw.
     *                  Need to be a factor of 2.
     * @param paint the paint instance.
     * @param radius the radius of the circle to draw.
     */
    public BarGraphRenderer(int divisions, Paint paint, int radius) {
        super();
        this.divisions = divisions;
        this.paint = paint;
        this.radius = radius;
    }

    /**
     * Renders the raw fft data as a bar graph in a circle.
     * @param canvas the canvas element.
     * @param data the raw fft data.
     * @param rect the rect.
     */
    public void render(Canvas canvas, double[] data, Rect rect) {
        if (mFFTPoints == null || mFFTPoints.length < data.length * 4) {
            mFFTPoints = new float[data.length * 4];
        }

        int limit = data.length / divisions;
        int width = rect.width();

        for (int i = 0; i < limit; i++) {
            double rfk = data[divisions * i];
            double ifk = data[divisions * i + 1];
            double magnitude = (rfk * rfk + ifk * ifk);
            int dbValueRaw = (int) (10 * Math.log10(magnitude));
            dbValueRaw += 30;               // Sensitivity. Lowest db Value is -30db.
            int dbValuePositive = Math.max(0, dbValueRaw);
            int maxDbValue = 80;            // Max Sensitivity is 50db.
            float dbValue = (float) dbValuePositive / maxDbValue * (width / 2 - radius);
            dbValue = Math.min(dbValue, width / 2 - radius);


            float angel = (float) (360.0 / limit) * i - 90;
            float startX = (float) (Math.cos(Math.toRadians(angel)) * radius + width / 2F);
            float startY = (float) (Math.sin(Math.toRadians(angel)) * radius + width / 2F);
            float endX = (float) (Math.cos(Math.toRadians(angel)) * (radius + (dbValue)) + width / 2F);
            float endY = (float) (Math.sin(Math.toRadians(angel)) * (radius + (dbValue)) + width / 2F);

            mFFTPoints[i * 4] = startX;
            mFFTPoints[i * 4 + 2] = endX;

            mFFTPoints[i * 4 + 1] = startY;
            mFFTPoints[i * 4 + 3] = endY;
        }

        canvas.drawLines(mFFTPoints, paint);
    }
}
