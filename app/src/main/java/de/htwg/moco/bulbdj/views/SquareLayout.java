package de.htwg.moco.bulbdj.views;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

/**
 * Class calculates the width and height for a prefect square.
 *
 * @author Daniel Steidinger
 * @version 1.0
 */
public class SquareLayout  extends ConstraintLayout {

    public SquareLayout(Context context) {
        super(context);
    }

    public SquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec < heightMeasureSpec ? widthMeasureSpec : heightMeasureSpec);
        setMeasuredDimension(size, size);
    }

}