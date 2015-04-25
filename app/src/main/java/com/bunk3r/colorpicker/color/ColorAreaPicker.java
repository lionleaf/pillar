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
package com.bunk3r.colorpicker.color;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bunk3r.colorpicker.hue.HuePicker;
import com.bunk3r.colorpicker.hue.OnHueChangedListener;

/**
 * Created by Bunk3r on 10/25/2014.
 */
public class ColorAreaPicker extends View implements ColorPicker, OnHueChangedListener {

    /**
     * Default values for the control
     */
    private static final int NUMBER_OF_GRADIENTS = 256;
    private static final int DEFAULT_SELECTED_COLOR_RADIUS = 4;
    private static final int DEFAULT_WIDTH = 256;
    private static final int DEFAULT_HEIGHT = 256;

    // True if inflated trough XML, false if created programmatically
    private boolean mWasInflated;

    // The ratio between the number of hues and the size of the view
    private float mWidthDensityMultiplier;
    private float mHeightDensityMultiplier;

    // Paint objects used throughout the view
    private Paint mGradientsPaint;
    private Paint mInnerCirclePaint;
    private Paint mOutterCirclePaint;
    private Paint mBitmapPaint;

    // Holds the width of the Slider's bitmap
    private int mInnerCircleWidth;

    // This is the color currently selected
    private int mCurrentColor;

    // The color used as the base for all calculations
    private int mBaseColor;

    // Location of the last selected color
    private int mCurrentX = 0, mCurrentY = 0;
    private boolean mHasMoved = false;

    // The size of the picker area
    private int mPickerWidth = 0, mPickerHeight = 0;

    // Objects needed for caching the colors
    private Matrix mColorBitmapMatrix;
    private Bitmap mColorsBitmap;
    private Canvas mPreRenderingCanvas;

    // Hue picker that will notify if the current hue has changed
    private HuePicker mHuePicker;

    // This object will be notified of any change in the color currently selected
    private OnColorChangedListener mColorChangedListener;

    /**
     * Use this constructor to generate a DEFAULT_SIZE color picker area
     *
     * @param context to be used for inflating and to search for resources
     */
    public ColorAreaPicker(Context context) {
        super(context);

        if (context != null) {
            int screenDensity = (int) Math.ceil(context.getResources().getDisplayMetrics().density);
            mWidthDensityMultiplier = screenDensity;
            mHeightDensityMultiplier = screenDensity;
        }

        init(false);
    }

    public ColorAreaPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorAreaPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(true);
    }

    /**
     * Sets all the initial configuration and pre-rendering for the view
     *
     * @param wasInflated if it was or not inflated via XML
     */
    private void init(boolean wasInflated) {
        mWasInflated = wasInflated;

        mInnerCirclePaint = new Paint();
        mOutterCirclePaint = new Paint();
        mGradientsPaint = new Paint();
        mBitmapPaint = new Paint();

        mInnerCircleWidth = DEFAULT_SELECTED_COLOR_RADIUS;
        mInnerCirclePaint.setStyle(Paint.Style.STROKE);
        mInnerCirclePaint.setColor(Color.BLACK);

        mOutterCirclePaint.setStyle(Paint.Style.STROKE);
        mOutterCirclePaint.setColor(Color.WHITE);

        mColorBitmapMatrix = new Matrix();
        mColorsBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        mPreRenderingCanvas = new Canvas(mColorsBitmap);
    }

    // Update the main field colors depending on the current selected hue
    private void updateMainColors(int color) {
        mBaseColor = color;

        final int baseRed = Color.red(mBaseColor);
        final int baseGreen = Color.green(mBaseColor);
        final int baseBlue = Color.blue(mBaseColor);

        // draws the NUMBER_OF_GRADIENTS into a bitmap for later use
        int[] colors = new int[2];
        colors[1] = Color.BLACK;
        for (int x = 0; x < 256; ++x) {
            colors[0] = Color.rgb(
                    255 - (255 - baseRed) * x / 255,
                    255 - (255 - baseGreen) * x / 255,
                    255 - (255 - baseBlue) * x / 255);
            final Shader gradientShader = new LinearGradient(0,
                    0,
                    0,
                    256,
                    colors,
                    null,
                    Shader.TileMode.CLAMP);
            mGradientsPaint.setShader(gradientShader);
            mPreRenderingCanvas.drawLine(x, 0, x, 256, mGradientsPaint);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // We only modify the configuration if something changes
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            mPickerWidth = width;
            mPickerHeight = height;

            mWidthDensityMultiplier = (float) width / NUMBER_OF_GRADIENTS;
            mHeightDensityMultiplier = (float) height / NUMBER_OF_GRADIENTS;
            mColorBitmapMatrix.setScale(mWidthDensityMultiplier, mHeightDensityMultiplier);
            mInnerCirclePaint.setStrokeWidth(mWidthDensityMultiplier);
            mOutterCirclePaint.setStrokeWidth(mWidthDensityMultiplier + 1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // we use default size if it's inflated through code
        if (!mWasInflated) {
            widthMeasureSpec = (int) (DEFAULT_WIDTH * mWidthDensityMultiplier);
            heightMeasureSpec = (int) (DEFAULT_HEIGHT * mHeightDensityMultiplier);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draws the scaled version of the hues
        canvas.drawBitmap(mColorsBitmap, mColorBitmapMatrix, mBitmapPaint);

        // Display the circle around the currently selected color in the main field
        if (mHasMoved) {
            canvas.drawCircle(mCurrentX, mCurrentY, mInnerCircleWidth * mWidthDensityMultiplier, mOutterCirclePaint);
            canvas.drawCircle(mCurrentX, mCurrentY, mInnerCircleWidth * mWidthDensityMultiplier, mInnerCirclePaint);
        }
    }

    @Override
    public void setColor(int color) {
        notifyHuePicker(color);

        updateMainColors(color);

        updateCurrentColor();

        notifyColor();

        invalidate();
    }

    @Override
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mColorChangedListener = listener;
        notifyColor();
    }

    @Override
    public void setHuePicker(HuePicker huePicker) {
        mHuePicker = huePicker;
        mHuePicker.setOnHueChangedListener(this);
    }

    @Override
    public void onHueChanged(int color) {
        setColor(color);
    }

    /*
    Notifies the hue picker when the base color has been change on the color picker
     */
    private void notifyHuePicker(int color) {
        if (mHuePicker != null) {
            mHuePicker.setColor(color);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the action is anything different that DOWN or MOVE we ignore the rest of the gesture
        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_MOVE) {
            return false;
        }

        // Transform the coordinates to a position inside the view
        float x = event.getX();
        float y = event.getY();
        mHasMoved = true;

        // Adjust X coordinate
        if (x < 0) {
            x = 0;
        } else if (x >= mPickerWidth) {
            x = mPickerWidth - 1;
        }

        // Adjust Y coordinate
        if (y < 0) {
            y = 0;
        } else if (y >= mPickerHeight) {
            y = mPickerHeight - 1;
        }

        mCurrentX = (int) x;
        mCurrentY = (int) y;

        updateCurrentColor();

        notifyColor();

        // Re-draw the view
        invalidate();

        return true;
    }

    private void updateCurrentColor() {
        if (mHasMoved) {
            final int transX = (int) (mCurrentX / mWidthDensityMultiplier);
            final int transY = (int) (mCurrentY / mHeightDensityMultiplier);
            mCurrentColor = mColorsBitmap.getPixel(transX, transY);
        } else {
            mCurrentColor = mBaseColor;
        }
    }

    /**
     * Notifies the listener if something changed
     */
    private void notifyColor() {
        if (mColorChangedListener != null) {
            mColorChangedListener.onColorChanged(mCurrentColor);
        }
    }
}
