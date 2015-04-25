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

import com.bunk3r.colorpicker.hue.HuePicker;

/**
 * Created by Bunk3r on 10/25/2014.
 */
public interface ColorPicker {
	/**
	 * Sets the new base color and recalculates the color palette
	 * 
	 * @param color in form of ARGB, where the Alpha channel is optional
	 */
	void setColor(int color);

    /**
     * Sets the hue picker that will be use in conjunction with the color picker
     *
     * @param huePicker send null if the hue picker wants to be removed
     */
    void setHuePicker(HuePicker huePicker);
	
	/**
	 * Sets the listener that will be used whenever something changes in the HuePicker
	 * 
	 * @param listener (if null passed, it will stop reporting changes)
	 */
	void setOnColorChangedListener(OnColorChangedListener listener);
}
