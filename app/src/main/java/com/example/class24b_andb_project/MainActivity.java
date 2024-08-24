package com.example.class24b_andb_project;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.colorpicker.ColorPickerManager;

public class MainActivity extends AppCompatActivity {
    private ColorPickerManager colorPickerManager;
    private FrameLayout colorPickerContainer;
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main);
        colorPickerManager = new ColorPickerManager(this);

        colorPickerManager.setOnColorPickedListener(new ColorPickerManager.OnColorPickedListener() {
            @Override
            public void onColorPicked(int color) {
                mainLayout.setBackgroundColor(color); // Update the background according to color preview
            }
        });

        colorPickerContainer = findViewById(R.id.color_picker_container); // Get the ColorPickerView from the manager
        colorPickerContainer.addView(colorPickerManager.getColorPickerView()); // Embed the color picker
    }
}