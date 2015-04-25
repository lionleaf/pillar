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
package com.bunk3r.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.bunk3r.colorpicker.color.ColorAreaPicker;
import com.bunk3r.colorpicker.color.OnColorChangedListener;
import com.bunk3r.colorpicker.hue.HuePicker;
import com.microsoft.band.sdksample.R;

/**
 * Created by Bunk3r on 10/25/2014.
 */
public class ColorPickerDialog extends Dialog implements OnColorChangedListener {

    private ColorPickerListener mColorPickerListener;
    private String mKey;
    private HuePicker mHuePicker;
    private ColorAreaPicker mColorAreaPicker;

    private View mCurrentColorPreview;
    private View mSelectedColorPreview;

    private int mInitialColor;
    private int mSelectedColor;

    public ColorPickerDialog(Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public void setColorPickerLister(ColorPickerListener colorPickerLister, String key) {
        mColorPickerListener = colorPickerLister;
        mKey = key;
    }

    public void setInitialColor(int color) {
        mInitialColor = color;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color_picker);

        mHuePicker = (HuePicker) findViewById(R.id.hue_slider);
        mColorAreaPicker = (ColorAreaPicker) findViewById(R.id.color_area_picker);
        Button okButton = (Button) findViewById(R.id.ok_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        mCurrentColorPreview = findViewById(R.id.current_color);
        mSelectedColorPreview = findViewById(R.id.selected_color);

        mCurrentColorPreview.setBackgroundColor(mInitialColor);
        mSelectedColorPreview.setBackgroundColor(mInitialColor);

        mColorAreaPicker.setOnColorChangedListener(this);
        mColorAreaPicker.setHuePicker(mHuePicker);
        mColorAreaPicker.setColor(mInitialColor);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPickerListener.onColorSelected(mKey, mSelectedColor);
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPickerListener.onSelectionCancel(mKey);
                dismiss();
            }
        });
    }

    @Override
    public void onColorChanged(int color) {
        mSelectedColor = color;
        mSelectedColorPreview.setBackgroundColor(mSelectedColor);
    }
}