package com.example.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorSliderView extends View {
    private static final float RADIUS = 90f; // Radius for rounded corners
    private static final float SLIDER_HEIGHT = 20f; // Desired height of the slider (rectangle)
    private static final float MARKER_RADIUS = 40f; // Radius of the marker
    private Bitmap bitmap;
    private Paint paint;
    private float sliderPosition = 1.0f; // Default to right of the slider
    private OnSliderChangedListener listener;
    private int baseColor = Color.WHITE;
    private boolean isTransparencySlider = false;
    private Paint gradientPaint;

    public ColorSliderView(Context context) {
        super(context);
        init();
    }

    public ColorSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setBaseColor(int color) {
        baseColor = color;
        if (getWidth() > 0 && getHeight() > 0)
            createGradient();
        invalidate();
    }

    public void setTransparencySlider(boolean transparencySlider) {
        isTransparencySlider = transparencySlider;
        if (getWidth() > 0 && getHeight() > 0)
            createGradient();
        invalidate();
    }

    private void createGradient() {
        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) // Check if width and height are valid before creating the bitmap
            return;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] colors = new int[width];
        for (int x = 0; x < width; x++) {
            float ratio = (float) x / (width - 1);
            if (isTransparencySlider) {
                int alpha = (int) (255 * ratio);
                colors[x] = Color.argb(alpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
            }
            else {
                float[] hsv = new float[3];
                Color.colorToHSV(baseColor, hsv);
                hsv[2] = ratio; // Update brightness (value) for shade slider
                colors[x] = Color.HSVToColor(hsv);
            }
        }

        for (int y = 0; y < height; y++) {
            bitmap.setPixels(colors, 0, width, 0, y, width, 1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null)
            createGradient();

        // Calculate the top and bottom of the slider, centered vertically in the view
        float sliderTop = (getHeight() - SLIDER_HEIGHT) / 2f;
        float sliderBottom = sliderTop + SLIDER_HEIGHT;

        // Draw the rounded rectangle gradient
        RectF rect = new RectF(0, sliderTop, getWidth(), sliderBottom);
        gradientPaint.setShader(new android.graphics.BitmapShader(bitmap, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, RADIUS, RADIUS, gradientPaint);

        // Draw the marker, slightly offset vertically to go out of bounds of the rectangle
        float x = MARKER_RADIUS + sliderPosition * (getWidth() - 2 * MARKER_RADIUS); // Clamp to keep within bounds
        float markerY = getHeight() / 2f; // Centered vertically relative to the whole view
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, markerY, MARKER_RADIUS, paint);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawCircle(x, markerY, MARKER_RADIUS / 2, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            sliderPosition = event.getX() / getWidth();
            sliderPosition = Math.max(0, Math.min(sliderPosition, 1)); // Clamp between 0 and 1
            if (listener != null) {
                int color = getColorFromPosition(sliderPosition);
                listener.onSliderChanged(color);
            }
            invalidate(); // Redraw to show the new marker position
            return true;
        }
        return false;
    }

    private int getColorFromPosition(float position) {
        if (isTransparencySlider) {
            int alpha = (int) (255 * position);
            return Color.argb(alpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
        }
        else {
            float[] hsv = new float[3];
            Color.colorToHSV(baseColor, hsv);
            hsv[2] = position; // Update brightness (value) based on slider position
            return Color.HSVToColor(hsv);
        }
    }

    public void setOnSliderChangedListener(OnSliderChangedListener listener) {
        this.listener = listener;
    }

    public void setSliderPosition(float position) {
        sliderPosition = position;
        invalidate(); // Redraw the view to show the updated marker position
    }

    public interface OnSliderChangedListener {
        void onSliderChanged(int color);
    }
}
