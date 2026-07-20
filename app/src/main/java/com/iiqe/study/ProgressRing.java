package com.iiqe.study;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

final class ProgressRing extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); private int done, goal, accent;
    ProgressRing(Context context, int done, int goal, int accent) { super(context); this.done=done; this.goal=Math.max(1,goal); this.accent=accent; setMinimumWidth(dp(86));setMinimumHeight(dp(86)); }
    private int dp(int v) { return (int)(v*getResources().getDisplayMetrics().density+.5f); }
    @Override protected void onDraw(Canvas canvas) { super.onDraw(canvas);float size=Math.min(getWidth(),getHeight());float stroke=dp(8);float inset=stroke/2f;RectF oval=new RectF(inset,inset,size-inset,size-inset);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(stroke);paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(Color.rgb(229,217,209));canvas.drawArc(oval,-90,360,false,paint);paint.setColor(accent);canvas.drawArc(oval,-90,360f*Math.min(done,goal)/goal,false,paint);
        paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);paint.setColor(Color.rgb(31,36,40));paint.setTextSize(dp(20));paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);canvas.drawText(String.valueOf(done),size/2,size/2+dp(4),paint);paint.setColor(Color.rgb(103,113,121));paint.setTextSize(dp(11));paint.setTypeface(android.graphics.Typeface.DEFAULT);canvas.drawText("/ "+goal,size/2,size/2+dp(21),paint);
    }
}
