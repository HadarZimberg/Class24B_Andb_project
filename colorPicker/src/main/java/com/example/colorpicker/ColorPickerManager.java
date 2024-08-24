package com.example.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;

public class ColorPickerManager {
    private final Context context;
    private OnColorPickedListener colorPickedListener;
    private View colorPickerView;
    private ColorPickerView colorsWheel;
    private ShapeableImageView colorPreview;
    private ColorSliderView shadeBar;
    private ColorSliderView transparencyBar;
    private float currentShade = 1.0f; // Default to full brightness
    private int currentTransparency = 255; // Default to full opacity
    private NumberPicker alphaNumberPicker;
    private NumberPicker redNumberPicker;
    private NumberPicker greenNumberPicker;
    private NumberPicker blueNumberPicker;
    private HexNumberPicker hexNumberPicker1;
    private HexNumberPicker hexNumberPicker2;
    private HexNumberPicker hexNumberPicker3;
    private HexNumberPicker hexNumberPicker4;
    private HexNumberPicker hexNumberPicker5;
    private HexNumberPicker hexNumberPicker6;
    private boolean isUpdatingPickers = false;
    private ShapeableImageView heartButton;
    private ShapeableImageView[] savedColorViews;
    private int currentSavedColorIndex;
    private final SharedPreferencesManager sharedPreferencesManager;

    public ColorPickerManager(Context context) {
        this.context = context;
        sharedPreferencesManager = new SharedPreferencesManager(context);
        currentSavedColorIndex = sharedPreferencesManager.loadColorIndex(0); // Load the saved index
        initializeColorPicker();
        loadSavedColors(); // From sharedPreferencesManager
    }

    private void loadSavedColors() {
        for (int i = 0; i < savedColorViews.length; i++) {
            int savedColor = sharedPreferencesManager.loadColor(i, Color.TRANSPARENT);
            if (savedColor != Color.TRANSPARENT)
                savedColorViews[i].setBackgroundColor(savedColor);
        }
    }

    private void initializeColorPicker() {
        LayoutInflater inflater = LayoutInflater.from(context); // Inflate the custom color picker layout from the module's XML
        colorPickerView = inflater.inflate(R.layout.view_color_picker, null);
        findViews();
        initializeNumberPickers();
        setListeners();
    }

    private void setListeners() {
        setColorsWheelListener();
        setShadeBarListener();
        setTransparencyBarListener();
        setHeartButtonListener();
        setNumberPickersListeners();
        setSavedColorListener();
    }

    private void setSavedColorListener() {
        for (ShapeableImageView savedColorView : savedColorViews) {
            savedColorView.setOnClickListener(v -> {
                if (savedColorView.getBackground() instanceof ColorDrawable) {
                    int color = ((ColorDrawable) savedColorView.getBackground()).getColor();
                    if (color != Color.TRANSPARENT) {
                        animateClick(v);
                        updateColorPreview(color);
                        updateNumberPickersToCurrentColor(color);
                        shadeBar.setBaseColor(color);
                        transparencyBar.setBaseColor(color);
                        updateSlidersAndColorWheelMarkers(color);
                    }
                }
            });
        }
    }

    private void setNumberPickersListeners() {
        alphaNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            currentTransparency = Math.round((newVal / 100.0f) * 255);
            transparencyBar.setBaseColor(Color.argb(currentTransparency, redNumberPicker.getValue(), greenNumberPicker.getValue(), blueNumberPicker.getValue()));
            updateColorFromRgbPickers();
        });

        redNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromRgbPickers());
        greenNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromRgbPickers());
        blueNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromRgbPickers());

        hexNumberPicker1.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromHexPickers());
        hexNumberPicker2.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromHexPickers());
        hexNumberPicker3.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromHexPickers());
        hexNumberPicker4.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromHexPickers());
        hexNumberPicker5.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromHexPickers());
        hexNumberPicker6.setOnValueChangedListener((picker, oldVal, newVal) -> updateColorFromHexPickers());
    }

    private void setHeartButtonListener() {
        heartButton.setOnClickListener(v -> {
            animateClick(v);
            heartButton.setImageResource(R.drawable.heart);
            new android.os.Handler().postDelayed(() -> heartButton.setImageResource(R.drawable.empty_heart), 200); // Delay before resetting the heart to empty for clicking effect

            int color = ((ColorDrawable) colorPreview.getBackground()).getColor();
            int alpha = Color.alpha(color);
            boolean colorExists = false;
            boolean hasTransparentColor = false;

            for (ShapeableImageView savedColorView : savedColorViews) { // Combined loop for both checks
                if (savedColorView.getBackground() instanceof ColorDrawable) {
                    int savedColor = ((ColorDrawable) savedColorView.getBackground()).getColor();
                    if (savedColor == color) {
                        colorExists = true;
                        break; // If the color already exists, no need to continue checking
                    }
                    if (Color.alpha(savedColor) == 0)
                        hasTransparentColor = true;
                }
            }

            if (alpha == 0 && hasTransparentColor) { // Check if trying to save another transparent color
                Toast.makeText(context, "A transparent color is already saved", Toast.LENGTH_SHORT).show();
                return; // Do not save the new transparent color
            }

            if (colorExists)  // If the color exists, show a toast and do not add it again
                Toast.makeText(context, "Color already saved", Toast.LENGTH_SHORT).show();
            else { // If the color does not exist, add it to the next available spot
                savedColorViews[currentSavedColorIndex].setBackgroundColor(color);
                sharedPreferencesManager.saveColor(currentSavedColorIndex, color); // Save the color in SharedPreferences
                currentSavedColorIndex = (currentSavedColorIndex + 1) % savedColorViews.length; // Increment the index, and if it reaches the end of the array, reset to 0
                sharedPreferencesManager.saveColorIndex(currentSavedColorIndex); // Save the updated index
            }
        });
    }

    private void setColorsWheelListener() {
        colorsWheel.setOnColorSelectedListener(new ColorPickerView.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                float[] hsv = new float[3]; // Adjust the color's brightness based on the current shade value
                Color.colorToHSV(color, hsv);
                hsv[2] = currentShade; // Apply the current shade value (brightness)
                int adjustedColor = Color.HSVToColor(hsv); // Recalculate the color with the applied shade
                adjustedColor = applyCurrentShadeAndTransparency(adjustedColor);
                shadeBar.setBaseColor(adjustedColor);
                transparencyBar.setBaseColor(adjustedColor);
                updateColorPreview(adjustedColor);
                updateNumberPickersToCurrentColor(adjustedColor);
            }
        });
    }

    private void setTransparencyBarListener() {
        transparencyBar.setTransparencySlider(true);  // Set transparency mode
        transparencyBar.setOnSliderChangedListener(color -> {
            currentTransparency = Color.alpha(color);
            int alphaValue = Math.round((currentTransparency / 255.0f) * 100);
            alphaNumberPicker.setValue(alphaValue);
            updateColorPreview(applyCurrentShadeAndTransparency(color));
        });
    }

    private void setShadeBarListener() {
        shadeBar.setOnSliderChangedListener(color -> {
            currentShade = getShadeValueFromColor(color);
            int adjustedColor = applyCurrentShadeAndTransparency(color);
            updateColorPreview(adjustedColor);
            transparencyBar.setBaseColor(adjustedColor); // Update the transparency bar to reflect the new shade
            updateNumberPickersToCurrentColor(adjustedColor);
        });
    }

    private void updateSlidersAndColorWheelMarkers(int color) {
        // Update the shade slider marker
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float brightness = hsv[2];
        shadeBar.setSliderPosition(brightness);

        // Update the transparency slider marker
        int alpha = Color.alpha(color);
        float transparencyPosition = alpha / 255.0f;
        transparencyBar.setSliderPosition(transparencyPosition);

        // Update the color wheel marker
        float hue = hsv[0];
        float saturation = hsv[1];
        colorsWheel.setMarkerPosition(hue, saturation);
    }

    private void updateColorFromRgbPickers() {
        if (isUpdatingPickers)
            return;
        isUpdatingPickers = true;

        int red = redNumberPicker.getValue();
        int green = greenNumberPicker.getValue();
        int blue = blueNumberPicker.getValue();

        int color = Color.argb(currentTransparency, red, green, blue);
        currentShade = getShadeValueFromColor(color); // Set the current shade based on the RGB values to prevent brightening
        int adjustedColor = applyCurrentShadeAndTransparency(color);
        updateColorPreview(adjustedColor);
        updateHexPickersFromColor(color); // Update Hexadecimal Pickers with raw RGB color
        shadeBar.setBaseColor(adjustedColor); // Update Shade Bar
        transparencyBar.setBaseColor(adjustedColor); // Update Transparency Bar
        updateSlidersAndColorWheelMarkers(adjustedColor); // Update sliders and color wheel marker positions
        isUpdatingPickers = false;
    }

    private void updateNumberPickersToCurrentColor(int color) {
        if (isUpdatingPickers)
            return;

        isUpdatingPickers = true;

        alphaNumberPicker.setValue(Math.round((Color.alpha(color) / 255.0f) * 100));
        redNumberPicker.setValue(Color.red(color));
        greenNumberPicker.setValue(Color.green(color));
        blueNumberPicker.setValue(Color.blue(color));
        updateHexPickersFromColor(color);

        isUpdatingPickers = false;
    }

    private void updateHexPickersFromColor(int color) {
        String hexValue = String.format("%06X", (0xFFFFFF & color));
        hexNumberPicker1.setValue(Character.digit(hexValue.charAt(0), 16));
        hexNumberPicker2.setValue(Character.digit(hexValue.charAt(1), 16));
        hexNumberPicker3.setValue(Character.digit(hexValue.charAt(2), 16));
        hexNumberPicker4.setValue(Character.digit(hexValue.charAt(3), 16));
        hexNumberPicker5.setValue(Character.digit(hexValue.charAt(4), 16));
        hexNumberPicker6.setValue(Character.digit(hexValue.charAt(5), 16));
    }

    private void updateColorFromHexPickers() {
        if (isUpdatingPickers) return;
        isUpdatingPickers = true;

        String hexValue = String.format("%01X%01X%01X%01X%01X%01X",
                hexNumberPicker1.getValue(),
                hexNumberPicker2.getValue(),
                hexNumberPicker3.getValue(),
                hexNumberPicker4.getValue(),
                hexNumberPicker5.getValue(),
                hexNumberPicker6.getValue());

        int color = Color.parseColor("#" + hexValue);

        currentShade = getShadeValueFromColor(color); // Set the current shade based on the color derived from hex values
        int adjustedColor = applyCurrentShadeAndTransparency(color);
        redNumberPicker.setValue(Color.red(color));
        greenNumberPicker.setValue(Color.green(color));
        blueNumberPicker.setValue(Color.blue(color));
        updateColorPreview(adjustedColor); // Ensure the color preview updates
        updateNumberPickersToCurrentColor(adjustedColor); // Update other pickers to reflect the new color
        updateSlidersAndColorWheelMarkers(adjustedColor); // Update sliders and color wheel marker positions
        isUpdatingPickers = false;
    }

    private void updateColorPreview(int color) {
        if (colorPreview != null)
            colorPreview.setBackgroundColor(color);
        if (colorPickedListener != null) // Notify the listener with the selected color
            colorPickedListener.onColorPicked(color);
    }

    private void findViews() {
        colorsWheel = colorPickerView.findViewById(R.id.main_CPV_wheel);
        colorPreview = colorPickerView.findViewById(R.id.main_SIV_preview);
        shadeBar = colorPickerView.findViewById(R.id.main_CSV_shade);
        transparencyBar = colorPickerView.findViewById(R.id.main_CSV_transparency);
        alphaNumberPicker = colorPickerView.findViewById(R.id.main_NP_alpha);
        redNumberPicker = colorPickerView.findViewById(R.id.main_NP_red);
        greenNumberPicker = colorPickerView.findViewById(R.id.main_NP_green);
        blueNumberPicker = colorPickerView.findViewById(R.id.main_NP_blue);
        hexNumberPicker1 = colorPickerView.findViewById(R.id.main_HNP_1);
        hexNumberPicker2 = colorPickerView.findViewById(R.id.main_HNP_2);
        hexNumberPicker3 = colorPickerView.findViewById(R.id.main_HNP_3);
        hexNumberPicker4 = colorPickerView.findViewById(R.id.main_HNP_4);
        hexNumberPicker5 = colorPickerView.findViewById(R.id.main_HNP_5);
        hexNumberPicker6 = colorPickerView.findViewById(R.id.main_HNP_6);
        heartButton = colorPickerView.findViewById(R.id.main_BTN_heart);
        savedColorViews = new ShapeableImageView[14];
        savedColorViews[0] = colorPickerView.findViewById(R.id.main_SIV_saved1);
        savedColorViews[1] = colorPickerView.findViewById(R.id.main_SIV_saved2);
        savedColorViews[2] = colorPickerView.findViewById(R.id.main_SIV_saved3);
        savedColorViews[3] = colorPickerView.findViewById(R.id.main_SIV_saved4);
        savedColorViews[4] = colorPickerView.findViewById(R.id.main_SIV_saved5);
        savedColorViews[5] = colorPickerView.findViewById(R.id.main_SIV_saved6);
        savedColorViews[6] = colorPickerView.findViewById(R.id.main_SIV_saved7);
        savedColorViews[7] = colorPickerView.findViewById(R.id.main_SIV_saved8);
        savedColorViews[8] = colorPickerView.findViewById(R.id.main_SIV_saved9);
        savedColorViews[9] = colorPickerView.findViewById(R.id.main_SIV_saved10);
        savedColorViews[10] = colorPickerView.findViewById(R.id.main_SIV_saved11);
        savedColorViews[11] = colorPickerView.findViewById(R.id.main_SIV_saved12);
        savedColorViews[12] = colorPickerView.findViewById(R.id.main_SIV_saved13);
        savedColorViews[13] = colorPickerView.findViewById(R.id.main_SIV_saved14);
    }

    private int applyCurrentShadeAndTransparency(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (!isUpdatingPickers) // Adjust only if the change is not coming from RGB pickers directly
            hsv[2] = currentShade; // Set brightness value
        return Color.HSVToColor(currentTransparency, hsv); // Combine with current transparency
    }

    private float getShadeValueFromColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv[2]; // Return brightness value
    }

    private void initializeNumberPickers() {
        alphaNumberPicker.setMinValue(0);
        alphaNumberPicker.setMaxValue(100);
        alphaNumberPicker.setValue(100); // Initial transparency value (full opacity)

        redNumberPicker.setMinValue(0);
        redNumberPicker.setMaxValue(255);
        redNumberPicker.setValue(255); // Initial red value

        greenNumberPicker.setMinValue(0);
        greenNumberPicker.setMaxValue(255);
        greenNumberPicker.setValue(255); // Initial green value

        blueNumberPicker.setMinValue(0);
        blueNumberPicker.setMaxValue(255);
        blueNumberPicker.setValue(255); // Initial blue value

        setupHexPicker(hexNumberPicker1);
        setupHexPicker(hexNumberPicker2);
        setupHexPicker(hexNumberPicker3);
        setupHexPicker(hexNumberPicker4);
        setupHexPicker(hexNumberPicker5);
        setupHexPicker(hexNumberPicker6);
    }

    private void setupHexPicker(NumberPicker hexPicker) {
        hexPicker.setMinValue(0);
        hexPicker.setMaxValue(15);
        hexPicker.setFormatter(i -> Integer.toHexString(i).toUpperCase());
        hexPicker.setValue(15); // Initial hex value
    }

    public View getColorPickerView() {
        return colorPickerView;
    }

    public void setOnColorPickedListener(OnColorPickedListener listener) {
        this.colorPickedListener = listener;
    }

    public interface OnColorPickedListener {
        void onColorPicked(int color);
    }

    private void animateClick(View view) {
        view.setOutlineProvider(null);
        view.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(200)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100));
    }
}