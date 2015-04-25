//Copyright (c) Microsoft Corporation All rights reserved.  
// 
//MIT License: 
// 
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
//documentation files (the  "Software"), to deal in the Software without restriction, including without limitation
//the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
//to permit persons to whom the Software is furnished to do so, subject to the following conditions: 
// 
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of
//the Software. 
// 
//THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
//THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
//CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//IN THE SOFTWARE.
package com.microsoft.band.sdksample;

import com.bunk3r.colorpicker.ColorPickerDialog;
import com.bunk3r.colorpicker.ColorPickerListener;
import com.microsoft.band.tiles.BandTheme;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TableLayout;

public class BandThemeView extends TableLayout {

    private static final String BASE_COLOR_NAME = "Base";
    private static final String HIGHLIGHT_COLOR_NAME = "Highlight";
    private static final String LOWLIGHT_COLOR_NAME = "Lowlight";
    private static final String SECONDARY_TEXT_COLOR_NAME = "SecondaryText";
    private static final String HIGH_CONTRAST_COLOR_NAME = "HighContrast";
    private static final String MUTED_COLOR_NAME = "Muted";

    private Button mButtonChangeBase;
    private Button mButtonChangeHighlight;
    private Button mButtonChangeLowlight;
    private Button mButtonChangeSecondaryText;
    private Button mButtonChangeHighContrast;
    private Button mButtonChangeMuted;

    private BandTheme mTheme;

    public BandThemeView(Context context) {
        super(context);
        loadViews();
    }

    public BandThemeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_bandtheme, this);
        loadViews();
    }
    
    private void loadViews() {
        mButtonChangeBase = (Button)findViewById(R.id.buttonChangeBase);
        mButtonChangeBase.setOnClickListener(mButtonChangeColorClickListener);

        mButtonChangeHighlight = (Button)findViewById(R.id.buttonChangeHighlight);
        mButtonChangeHighlight.setOnClickListener(mButtonChangeColorClickListener);

        mButtonChangeLowlight = (Button)findViewById(R.id.buttonChangeLowlight);
        mButtonChangeLowlight.setOnClickListener(mButtonChangeColorClickListener);

        mButtonChangeSecondaryText = (Button)findViewById(R.id.buttonChangeSecondaryText);
        mButtonChangeSecondaryText.setOnClickListener(mButtonChangeColorClickListener);

        mButtonChangeHighContrast = (Button)findViewById(R.id.buttonChangeHighContrast);
        mButtonChangeHighContrast.setOnClickListener(mButtonChangeColorClickListener);

        mButtonChangeMuted = (Button)findViewById(R.id.buttonChangeMuted);
        mButtonChangeMuted.setOnClickListener(mButtonChangeColorClickListener);
    }
    
    public void setTheme(BandTheme theme) {
        mTheme = theme;
        setColorForThemeElement(BASE_COLOR_NAME, theme.getBaseColor());
        setColorForThemeElement(HIGHLIGHT_COLOR_NAME, theme.getHighlightColor());
        setColorForThemeElement(LOWLIGHT_COLOR_NAME, theme.getLowlightColor());
        setColorForThemeElement(SECONDARY_TEXT_COLOR_NAME, theme.getSecondaryTextColor());
        setColorForThemeElement(HIGH_CONTRAST_COLOR_NAME, theme.getHighContrastColor());
        setColorForThemeElement(MUTED_COLOR_NAME, theme.getMutedColor());
    }
    
    public BandTheme getTheme() {
        return mTheme;
    }

    private OnClickListener mButtonChangeColorClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            showColorPicker(button.getContext(), (String)button.getTag());
        }
    };

    private void showColorPicker(Context context, String key) {
        ColorPickerDialog dialog = new ColorPickerDialog(context);
        dialog.setInitialColor(getColorForThemeElement(key));

        dialog.setColorPickerLister(new ColorPickerListener() {
            @Override
            public void onSelectionCancel(String key) {
            }

            @Override
            public void onColorSelected(String key, int color) {
                if (BASE_COLOR_NAME.equals(key)) {
                    mTheme.setBaseColor(color);
                } else if (HIGHLIGHT_COLOR_NAME.equals(key)) {
                    mTheme.setHighlightColor(color);
                } else if (LOWLIGHT_COLOR_NAME.equals(key)) {
                    mTheme.setLowlightColor(color);
                } else if (HIGH_CONTRAST_COLOR_NAME.equals(key)) {
                    mTheme.setHighContrastColor(color);
                } else if (SECONDARY_TEXT_COLOR_NAME.equals(key)) {
                    mTheme.setSecondaryTextColor(color);
                } else if (MUTED_COLOR_NAME.equals(key)) {
                    mTheme.setMutedColor(color);
                }
                
                setColorForThemeElement(key, color);
            }
        }, key);

        dialog.show();
    }

    // Get the background color for the named theme element (Base, Highlight, etc.)
    private int getColorForThemeElement(String element) {
        GridLayout grid = (GridLayout)findViewWithTag("grid" + element);
        return ((ColorDrawable)grid.getBackground()).getColor();
    }

    // Set the background color for the named theme element (Base, Highlight, etc.)
    private void setColorForThemeElement(String element, int color) {
        GridLayout grid = (GridLayout)findViewWithTag("grid" + element);
        if (grid != null) {
            grid.setBackgroundColor(color | 0xff000000);
        }
    }
}
