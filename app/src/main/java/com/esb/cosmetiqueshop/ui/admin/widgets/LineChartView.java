package com.esb.cosmetiqueshop.ui.admin.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.esb.cosmetiqueshop.R;

/**
 * Courbe simple (CA / tendances) sans lib externe.
 */
public class LineChartView extends View {

    private float[] values = new float[0];
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2.5f));
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.bordeaux));
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(0x33C94F7C);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1f));
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.gris_bordure));

        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(ContextCompat.getColor(getContext(), R.color.champagne_fonce));
    }

    public void setValues(float[] values) {
        this.values = values != null ? values : new float[0];
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float pad = dp(12f);
        float left = pad;
        float right = w - pad;
        float top = pad;
        float bottom = h - pad;

        for (int i = 0; i < 4; i++) {
            float y = top + (bottom - top) * i / 3f;
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        if (values.length < 2) {
            return;
        }
        float max = 1f;
        for (float v : values) {
            if (v > max) {
                max = v;
            }
        }

        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < values.length; i++) {
            float x = left + (right - left) * i / (values.length - 1f);
            float y = bottom - (bottom - top) * (values[i] / max);
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, bottom);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        fillPath.lineTo(right, bottom);
        fillPath.close();
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        for (int i = 0; i < values.length; i++) {
            float x = left + (right - left) * i / (values.length - 1f);
            float y = bottom - (bottom - top) * (values[i] / max);
            canvas.drawCircle(x, y, dp(3.5f), pointPaint);
        }
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
