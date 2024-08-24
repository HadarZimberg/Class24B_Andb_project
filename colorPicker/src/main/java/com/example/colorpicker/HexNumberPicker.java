package com.example.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

public class HexNumberPicker extends NumberPicker {

    private static final String[] HEX_VALUES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public HexNumberPicker(Context context) {
        super(context);
        init();
    }

    public HexNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HexNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setMinValue(0);
        this.setMaxValue(HEX_VALUES.length - 1);
        this.setDisplayedValues(HEX_VALUES);
        this.setValue(0);
        this.invalidate(); // Ensure the UI updates immediately
    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
        this.invalidate(); // Force a redraw
    }
}
