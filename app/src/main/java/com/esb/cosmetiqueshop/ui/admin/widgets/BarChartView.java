package com.esb.cosmetiqueshop.ui.admin.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.esb.cosmetiqueshop.R;

/**
 * Barres horizontales (répartition commandes).
 */
public class BarChartView extends View {

    private String[] labels = new String[0];
    private float[] values = new float[0];
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final int[] palette;

    public BarChartView(Context context) {
        super(context);
        palette = new int[]{
                ContextCompat.getColor(context, R.color.bordeaux),
                ContextCompat.getColor(context, R.color.rose_premium),
                ContextCompat.getColor(context, R.color.champagne_fonce),
                ContextCompat.getColor(context, R.color.succes)
        };
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        palette = new int[]{
                ContextCompat.getColor(context, R.color.bordeaux),
                ContextCompat.getColor(context, R.color.rose_premium),
                ContextCompat.getColor(context, R.color.champagne_fonce),
                ContextCompat.getColor(context, R.color.succes)
        };
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        trackPaint.setStyle(Paint.Style.FILL);
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.rose_poudre));
        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.texte));
        labelPaint.setTextSize(dp(12f));
        valuePaint.setColor(ContextCompat.getColor(getContext(), R.color.texte_secondaire));
        valuePaint.setTextSize(dp(11f));
        valuePaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setData(String[] labels, float[] values) {
        this.labels = labels != null ? labels : new String[0];
        this.values = values != null ? values : new float[0];
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int rows = Math.max(labels.length, 1);
        int h = (int) (rows * dp(36f) + dp(8f));
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (labels.length == 0) {
            return;
        }
        float max = 1f;
        for (float v : values) {
            if (v > max) {
                max = v;
            }
        }
        float leftLabel = dp(4f);
        float barLeft = dp(100f);
        float barRight = getWidth() - dp(36f);
        float rowH = dp(36f);
        for (int i = 0; i < labels.length; i++) {
            float top = i * rowH + dp(6f);
            canvas.drawText(labels[i], leftLabel, top + dp(16f), labelPaint);
            float trackTop = top + dp(4f);
            float trackBottom = trackTop + dp(14f);
            rect.set(barLeft, trackTop, barRight, trackBottom);
            canvas.drawRoundRect(rect, dp(8f), dp(8f), trackPaint);
            float w = (barRight - barLeft) * (values[i] / max);
            barPaint.setColor(palette[i % palette.length]);
            rect.set(barLeft, trackTop, barLeft + Math.max(w, dp(4f)), trackBottom);
            canvas.drawRoundRect(rect, dp(8f), dp(8f), barPaint);
            canvas.drawText(String.valueOf(Math.round(values[i])), getWidth() - dp(8f),
                    top + dp(16f), valuePaint);
        }
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
