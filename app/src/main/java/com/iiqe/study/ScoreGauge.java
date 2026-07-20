package com.iiqe.study;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

final class ScoreGauge extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int percent;

    ScoreGauge(Context context, int percent) {
        super(context);
        this.percent = Math.max(0, Math.min(100, percent));
        setMinimumHeight(dp(190));
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float stroke = dp(16);
        float radius = Math.min(width - dp(44), height * 1.45f);
        float centerX = width / 2f;
        float centerY = height - dp(22);
        RectF arc = new RectF(centerX - radius / 2f, centerY - radius / 2f, centerX + radius / 2f, centerY + radius / 2f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setColor(Color.rgb(183, 59, 65));
        canvas.drawArc(arc, 180, 108, false, paint);
        paint.setColor(Color.rgb(213, 157, 44));
        canvas.drawArc(arc, 288, 18, false, paint);
        paint.setColor(Color.rgb(45, 114, 86));
        canvas.drawArc(arc, 306, 54, false, paint);

        double radians = Math.toRadians(180 + percent * 1.8);
        float pointerRadius = radius / 2f - dp(10);
        float endX = centerX + (float) Math.cos(radians) * pointerRadius;
        float endY = centerY + (float) Math.sin(radians) * pointerRadius;
        paint.setColor(Color.rgb(31, 36, 40));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(3));
        canvas.drawLine(centerX, centerY, endX, endY, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, dp(7), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(34));
        canvas.drawText(percent + "%", centerX, centerY - dp(36), paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setTextSize(dp(13));
        paint.setColor(Color.rgb(103, 113, 121));
        canvas.drawText("模拟考试成绩", centerX, centerY - dp(14), paint);
    }
}
