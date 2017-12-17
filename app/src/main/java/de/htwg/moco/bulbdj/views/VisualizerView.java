package de.htwg.moco.bulbdj.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.renderers.BarGraphRenderer;

/**
 * Class handles the recorded data and draws an BarGraphRenderer.
 *
 * @author Daniel Steidinger
 * @version 1.0
 */
public class VisualizerView extends View {

    private Paint paint = new Paint();
    private Paint fadeOutPaint = new Paint();
    private Bitmap canvasBitmap;
    private Canvas canvas;
    private boolean flash = true;

    private Rect rect = new Rect();
    private BarGraphRenderer renderer;
    private int radius = 120;

    private double[] bytes;

    public VisualizerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void init() {

        paint.setColor(Color.argb(0, 255, 255, 255));
        fadeOutPaint.setColor(Color.argb(100, 137, 207, 235));
        fadeOutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setColor(this.getContext().getResources().getColor(R.color.colorPrimary));   // Color.argb(255, 137, 207, 235)
        renderer = new BarGraphRenderer(2, paint, radius);
    }

    public void updateVisualizer(double[] bytes) {
        this.bytes = bytes;
        // Draw again
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        rect.set(0, 0, getWidth(), getHeight());

        if(canvasBitmap == null)
        {
            canvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        }
        if(this.canvas == null)
        {
            this.canvas = new Canvas(canvasBitmap);
        }

        if (bytes != null) {
            // Render
            renderer.render(this.canvas, bytes, rect);
        }

        // Old contents get invisible
        this.canvas.drawPaint(fadeOutPaint);

        // First execution, flash
        if(flash)
        {
            flash = false;
            this.canvas.drawPaint(paint);
        }

        canvas.drawBitmap(canvasBitmap, new Matrix(), null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

    public void stop() {
        this.bytes = null;
        Paint slowFadeOutPaint = new Paint();
        slowFadeOutPaint.setColor(Color.argb(100, 255, 255, 255));
        slowFadeOutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        for (int i = 0; i < 5; i++) {
            this.canvas.drawPaint(slowFadeOutPaint);
        }
    }
}
