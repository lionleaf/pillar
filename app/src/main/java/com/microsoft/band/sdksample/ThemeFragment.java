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

import com.microsoft.band.tiles.BandTheme;

import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class ThemeFragment extends Fragment implements FragmentListener {

    protected static final int SELECT_IMAGE = 0;
    
    private View mRootView;
    private BandThemeView mViewTheme;

    private Button mButtonSelectBackground;

    private Button mButtonGetTheme;
    private Button mButtonSetTheme;

    private ImageView mImageBackground;

    private Button mButtonGetBackground;
    private Button mButtonSetBackground;

    private Bitmap mSelectedImage;

    public ThemeFragment() {
    }

    public void onFragmentSelected() {
        if (isVisible()) {
            refreshControls();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_theme, container, false);

        mViewTheme = (BandThemeView)mRootView.findViewById(R.id.viewTheme);
        mViewTheme.setTheme(BandTheme.VIOLET_THEME);

        mButtonGetTheme = (Button)mRootView.findViewById(R.id.buttonGetTheme);
        mButtonGetTheme.setOnClickListener(mButtonGetThemeClickListener);

        mButtonSetTheme = (Button)mRootView.findViewById(R.id.buttonSetTheme);
        mButtonSetTheme.setOnClickListener(mButtonSetThemeClickListener);

        mButtonSelectBackground = (Button)mRootView.findViewById(R.id.buttonSelectBackground);
        mButtonSelectBackground.setOnClickListener(mButtonSelectBackgroundClickListener);

        mImageBackground = (ImageView)mRootView.findViewById(R.id.imageBackground);

        mButtonGetBackground = (Button)mRootView.findViewById(R.id.buttonGetBackground);
        mButtonGetBackground.setOnClickListener(mButtonGetBackgroundClickListener);

        mButtonSetBackground = (Button)mRootView.findViewById(R.id.buttonSetBackground);
        mButtonSetBackground.setOnClickListener(mButtonSetBackgroundClickListener);

        return mRootView;
    }

    private OnClickListener mButtonGetBackgroundClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                mSelectedImage = Model.getInstance()
                    .getClient()
                    .getPersonalizationManager()
                    .getMeTileImage()
                    .await();

                mImageBackground.setImageBitmap(mSelectedImage);
                refreshControls();
            } catch (Exception ex) {
                Util.showExceptionAlert(getActivity(), "Get background image", ex);
            }
        }
    };

    private OnClickListener mButtonSetBackgroundClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                Model.getInstance()
                    .getClient()
                    .getPersonalizationManager()
                    .setMeTileImage(mSelectedImage)
                    .await();
            } catch (Exception ex) {
                Util.showExceptionAlert(getActivity(), "Set background image", ex);
            }
        }
    };

    private OnClickListener mButtonSelectBackgroundClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            startActivityForResult(
                new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                SELECT_IMAGE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                String imagePath = getPath(imageUri);
                Bitmap bm = BitmapFactory.decodeFile(imagePath);
                if (bm != null) {
                    mSelectedImage = bm;
                    mImageBackground.setImageBitmap(mSelectedImage);
                    refreshControls();
                }
            }
        }
    }

    // Convert a gallery URI to a regular file path
    private String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getActivity(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private OnClickListener mButtonGetThemeClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                BandTheme theme = Model.getInstance()
                    .getClient()
                    .getPersonalizationManager()
                    .getTheme()
                    .await();
                
                mViewTheme.setTheme(theme);
                refreshControls();
            } catch (Exception ex) {
                Util.showExceptionAlert(getActivity(), "Get theme", ex);
            }
        }
    };

    private OnClickListener mButtonSetThemeClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                Model.getInstance()
                    .getClient()
                    .getPersonalizationManager()
                    .setTheme(mViewTheme.getTheme())
                    .await();
                
                refreshControls();
            } catch (Exception ex) {
                Util.showExceptionAlert(getActivity(), "Set theme", ex);
            }
        }
    };

    private void refreshControls() {
        boolean connected = Model.getInstance().isConnected();

        mButtonGetTheme.setEnabled(connected);
        mButtonSetTheme.setEnabled(connected);

        mButtonGetBackground.setEnabled(connected);
        mButtonSetBackground.setEnabled(connected && mSelectedImage != null);
    }
}
