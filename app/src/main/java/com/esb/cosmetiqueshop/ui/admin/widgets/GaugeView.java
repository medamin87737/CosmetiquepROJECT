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
 * Jauge circulaire 0–100 %.
 */
public class GaugeView extends View {

    private float progress = 0f;
    private String label = "";
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint captionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    public GaugeView(Context context) {
        super(context);
        init();
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(dp(10f));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.rose_poudre));

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(dp(10f));
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setColor(ContextCompat.getColor(getContext(), R.color.bordeaux));

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.bordeaux_fonce));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(18f));
        textPaint.setFakeBoldText(true);

        captionPaint.setColor(ContextCompat.getColor(getContext(), R.color.texte_secondaire));
        captionPaint.setTextAlign(Paint.Align.CENTER);
        captionPaint.setTextSize(dp(11f));
    }

    public void setProgress(float percent, String label) {
        this.progress = Math.max(0f, Math.min(100f, percent));
        this.label = label != null ? label : "";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float pad = dp(14f);
        float size = Math.min(getWidth(), getHeight()) - pad * 2f;
        float left = (getWidth() - size) / 2f;
        float top = (getHeight() - size) / 2f - dp(6f);
        arcRect.set(left, top, left + size, top + size);

        // Arc 220°
        canvas.drawArc(arcRect, 160f, 220f, false, trackPaint);
        canvas.drawArc(arcRect, 160f, 220f * (progress / 100f), false, arcPaint);

        float cx = getWidth() / 2f;
        float cy = top + size * 0.58f;
        canvas.drawText(Math.round(progress) + " %", cx, cy, textPaint);
        canvas.drawText(label, cx, cy + dp(16f), captionPaint);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
