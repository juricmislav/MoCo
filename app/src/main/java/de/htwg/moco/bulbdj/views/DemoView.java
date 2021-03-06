package de.htwg.moco.bulbdj.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import de.htwg.moco.bulbdj.R;

/**
 * Class handles the recorded data and draws an BarGraphRenderer.
 *
 * @author Daniel Steidinger
 * @version 1.0
 */
public class DemoView extends View {

    /**
     * The Paints.
     */
    private Paint paintR = new Paint();
    private Paint paintG = new Paint();
    private Paint paintB = new Paint();

    /**
     * The rects to draw.
     */
    private Rect rectR; // = new Rect(30,30,80,80);
    private Rect rectG; // = new Rect(100,30,150,80);
    private Rect rectB; // = new Rect(170,30,220,80);
    private Rect srcBitmapRect;
    private Bitmap bitmapBulb;

    /**
     * The r,g,b values.
     */
    private int r = 0, g = 0, b = 0;

    /**
     * The canvas element.
     */
    private Canvas canvas;

    /**
     * Default constructor
     */
    public DemoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    /**
     * Initialize all colors.
     */
    public void init() {

        bitmapBulb = BitmapFactory.decodeResource(getResources(), R.drawable.bulb);
        srcBitmapRect = new Rect(0,0,bitmapBulb.getWidth()-1, bitmapBulb.getHeight() - 1);

        paintR.setColor(Color.argb(255, 255, 0, 0));
        paintG.setColor(Color.argb(255, 0, 255, 0));
        paintB.setColor(Color.argb(255, 0, 0, 255));
    }

    /**
     * Update visualizer.
     * @param bulbs the bulbs colors
     */
    public void updateVisualizer(int[] bulbs) {
        if (bulbs.length > 2) {
            this.r = bulbs[0];
            this.g = bulbs[1];
            this.b = bulbs[2];
        } else if (bulbs.length > 1) {
            this.r = bulbs[0];
            this.g = bulbs[1];
            this.b = bulbs[0];
        } else if (bulbs.length > 0) {
            this.r = bulbs[0];
            this.g = bulbs[0];
            this.b = bulbs[0];
        }

        // Draw again
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;

        int width = (int) (bitmapBulb.getWidth() / ((float) bitmapBulb.getHeight() / getHeight()));
        int height = getHeight();

        rectR = new Rect(0, width / 3, width, height - 1);
        rectG = new Rect(getWidth() / 2 - width / 2, width / 3, getWidth() / 2 + width / 2, height - 1);
        rectB = new Rect(getWidth() - width, width / 3, getWidth(), height - 1);

        paintR.setColor(r);
        paintG.setColor(g);
        paintB.setColor(b);

        this.canvas.drawRect(rectR, paintR);
        this.canvas.drawRect(rectG, paintG);
        this.canvas.drawRect(rectB, paintB);

        this.canvas.drawBitmap(bitmapBulb, srcBitmapRect, rectR, null);
        this.canvas.drawBitmap(bitmapBulb, srcBitmapRect, rectG, null);
        this.canvas.drawBitmap(bitmapBulb, srcBitmapRect, rectB, null);
    }

    /**
     * Reset all colors.
     */
    public void stop() {
        if (canvas != null) {
            updateVisualizer(new int[]{0, 0, 0});
        }
    }
}
