package com.example.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {
    private Bitmap bitmap;
    private int selectedColor;
    private OnColorSelectedListener listener;
    private float markerX = 250;
    private float markerY = 250;
    private Paint fillPaint;
    private Paint strokePaint;

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.WHITE);
        fillPaint.setAlpha(128);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(8);
        strokePaint.setColor(Color.GRAY);  // Default stroke color
        strokePaint.setAlpha(248);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null)
            createColorWheel();

        canvas.drawBitmap(bitmap, 0, 0, null);

        if (markerX >= 0 && markerY >= 0) { // Draw the marker if a valid position is set
            canvas.drawCircle(markerX, markerY, 30, fillPaint); // Draw the filled circle
            canvas.drawCircle(markerX, markerY, 30, strokePaint); // Draw the circle border (stroke)
        }
    }

    private void createColorWheel() {
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;
        int centerX = width / 2;
        int centerY = height / 2;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] colors = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int dx = x - centerX;
                int dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= radius) {
                    double angle = Math.atan2(dy, dx);
                    float hue = (float) (angle / (2 * Math.PI) * 360);
                    if (hue < 0)
                        hue += 360;
                    float saturation = (float) (distance / radius);
                    float brightness = 1.0f;
                    int color = Color.HSVToColor(new float[]{hue, saturation, brightness});
                    colors[y * width + x] = color;
                }
                else
                    colors[y * width + x] = Color.TRANSPARENT;
            }
        }

        bitmap.setPixels(colors, 0, width, 0, 0, width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        int radius = Math.min(getWidth(), getHeight()) / 2;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        int dx = x - centerX;
        int dy = y - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= radius) {
            selectedColor = bitmap.getPixel(x, y);
            if (distance > radius - 35) {  // 30 is the radius of the marker + spare 5 pixels
                double ratio = (radius - 35) / distance;
                markerX = (float) (centerX + dx * ratio);
                markerY = (float) (centerY + dy * ratio);
            }
            else {
                markerX = x;
                markerY = y;
            }
            invalidate();  // Redraw the view to show the marker
            if (listener != null)
                listener.onColorSelected(selectedColor);
        }
        return true;
    }

    public void setMarkerPosition(float hue, float saturation) {
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;
        int centerX = width / 2;
        int centerY = height / 2;

        double angle = Math.toRadians(hue);
        float distance = saturation * radius;

        markerX = (float) (centerX + distance * Math.cos(angle));
        markerY = (float) (centerY + distance * Math.sin(angle));

        invalidate(); // Redraw the view to show the updated marker position
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}
