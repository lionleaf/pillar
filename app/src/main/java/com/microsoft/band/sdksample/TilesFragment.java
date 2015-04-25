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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.microsoft.band.BandPendingResult;
import com.microsoft.band.BandResultCallback;
import com.microsoft.band.notification.MessageFlags;
import com.microsoft.band.tiles.BandIcon;
import com.microsoft.band.tiles.BandTheme;
import com.microsoft.band.tiles.BandTile;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TilesFragment extends Fragment implements FragmentListener, OnCheckedChangeListener, TextWatcher {

    private int mRemainingCapacity;
    private Collection<BandTile> mTiles;

    private TextView mTextRemainingCapacity;
    private Button mButtonAddTile;
    private Button mButtonRemoveTile;
    private CheckBox mCheckboxBadging;
    private CheckBox mCheckboxCustomTheme;
    private BandThemeView mThemeView;

    private EditText mEditTileName;
    private EditText mEditTitle;
    private EditText mEditBody;

    private Button mButtonSendMessage;
    private Button mButtonSendDialog;

    private CheckBox mCheckboxWithDialog;
    
    private ListView mListTiles;
    private TileListAdapter mTileListAdapter;
    private BandTile mSelectedTile;

    public TilesFragment() {
        mRemainingCapacity = -1;
    }

    @SuppressLint("InflateParams") @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tiles, container, false);
        mListTiles = (ListView)rootView.findViewById(R.id.listTiles);
        
        RelativeLayout header = (RelativeLayout)inflater.inflate(R.layout.fragment_tiles_header, null);

        mTextRemainingCapacity = (TextView)header.findViewById(R.id.textAvailableCapacity);
        mButtonAddTile = (Button)header.findViewById(R.id.buttonAddTile);
        mButtonAddTile.setOnClickListener(mButtonAddTileClickListener);
        mButtonRemoveTile = (Button)header.findViewById(R.id.buttonRemoveTile);
        mButtonRemoveTile.setOnClickListener(mButtonRemoveTileClickListener);
        mCheckboxBadging = (CheckBox)header.findViewById(R.id.cbBadging);

        mThemeView = (BandThemeView)header.findViewById(R.id.viewCustomTheme);
        mThemeView.setTheme(BandTheme.CYBER_THEME);
        mCheckboxCustomTheme = (CheckBox)header.findViewById(R.id.cbCustomTheme);
        mCheckboxCustomTheme.setOnCheckedChangeListener(this);

        mEditTileName = (EditText)header.findViewById(R.id.editTileName);
        mEditTileName.addTextChangedListener(this);

        RelativeLayout footer = (RelativeLayout)inflater.inflate(R.layout.fragment_tiles_footer, null);

        mEditTitle = (EditText)footer.findViewById(R.id.editTitle);
        mEditBody = (EditText)footer.findViewById(R.id.editBody);
        mCheckboxWithDialog = (CheckBox)footer.findViewById(R.id.cbWithDialog);

        mButtonSendMessage = (Button)footer.findViewById(R.id.buttonSendMessage);
        mButtonSendMessage.setOnClickListener(mButtonSendMessageClickListener);

        mButtonSendDialog = (Button)footer.findViewById(R.id.buttonSendDialog);
        mButtonSendDialog.setOnClickListener(mButtonShowDialogClickListener);
        
        mListTiles.addHeaderView(header);
        mListTiles.addFooterView(footer);
        
        mTileListAdapter = new TileListAdapter();
        mListTiles.setAdapter(mTileListAdapter);
        
        mListTiles.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= 1;  // ignore the header
                if (position >= 0 && position < mTileListAdapter.getCount()) {
                    mSelectedTile = (BandTile)mTileListAdapter.getItem(position);
                    refreshControls();
                }
            }
        });
       
        return rootView;
    }
    
    public void onFragmentSelected() {

        if (!isVisible()) {
            return;
        }

        refreshData();
        refreshControls();
    }

    //
    // Event handlers
    //

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mCheckboxCustomTheme) {
            mThemeView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
    }

    private OnClickListener mButtonAddTileClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                BandIcon tileIcon = BandIcon.toBandIcon(BitmapFactory.decodeResource(getResources(), R.raw.tile, options));

                BandIcon badgeIcon = mCheckboxBadging.isChecked() ? BandIcon.toBandIcon(BitmapFactory.decodeResource(
                    getResources(), R.raw.badge, options)) : null;

                BandTile tile = new BandTile.Builder(UUID.randomUUID(), mEditTileName.getText().toString(), tileIcon)
                    .setTileSmallIcon(badgeIcon)
                    .setTheme(mCheckboxCustomTheme.isChecked() ? mThemeView.getTheme() : null)
                    .build();

                BandPendingResult<Boolean> addpendingResult = Model.getInstance()
                    .getClient()
                    .getTileManager()
                    .addTile(getActivity(), tile);

                addpendingResult.registerResultCallback(new BandResultCallback<Boolean>() {

                    @Override
                    public void onResult(Boolean result, final Throwable failure) {
                        if (result != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), "Tile added", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), "Unable to add tile", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        if (failure != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Util.showExceptionAlert(getActivity(), "Add tile", (Exception)failure);
                                }
                            });
                        }

                        // Refresh our tile list and count
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                refreshData();
                                refreshControls();
                            }
                        });

                    }
                });
            } catch (Exception e) {
                Util.showExceptionAlert(getActivity(), "Add tile", e);
            }
        }
    };

    private OnClickListener mButtonRemoveTileClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                Model.getInstance()
                    .getClient()
                    .getTileManager()
                    .removeTile(mSelectedTile.getTileId())
                    .await();
                mSelectedTile = null;
                Toast.makeText(getActivity(), "Tile removed", Toast.LENGTH_SHORT).show();
                refreshData();
                refreshControls();
            } catch (Exception e) {
                Util.showExceptionAlert(getActivity(), "Remove tile", e);
            }
        }
    };

    private OnClickListener mButtonSendMessageClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                Model.getInstance()
                    .getClient()
                    .getNotificationManager()
                    .sendMessage(
                        mSelectedTile.getTileId(),
                        mEditTitle.getText().toString(),
                        mEditBody.getText().toString(),
                        new Date(),
                        mCheckboxWithDialog.isChecked() ? MessageFlags.SHOW_DIALOG : MessageFlags.NONE)
                    .await();

            } catch (Exception e) {
                Util.showExceptionAlert(getActivity(), "Send message", e);
            }
        }
    };

    private OnClickListener mButtonShowDialogClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                Model.getInstance()
                    .getClient()
                    .getNotificationManager()
                    .showDialog(
                        mSelectedTile.getTileId(),
                        mEditTitle.getText().toString(),
                        mEditBody.getText().toString())
                    .await();

            } catch (Exception e) {
                Util.showExceptionAlert(getActivity(), "Show dialog", e);
            }
        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Some controls are enabled only when an associated EditText has text
        refreshControls();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    //
    // Helper methods
    //

    private void refreshData() {
        if (Model.getInstance().isConnected()) {
            try {
                mRemainingCapacity = Model.getInstance()
                    .getClient()
                    .getTileManager()
                    .getRemainingTileCapacity()
                    .await();

            } catch (Exception e) {
                mRemainingCapacity = -1;
                Util.showExceptionAlert(getActivity(), "Check capacity", e);
            }

            try {
                mTiles = Model.getInstance()
                    .getClient()
                    .getTileManager()
                    .getTiles()
                    .await();

                if (!mTiles.contains(mSelectedTile)) {
                    mSelectedTile = null;
                }

                mTileListAdapter.setTileList(mTiles);
            } catch (Exception e) {
                mTiles = null;
                mSelectedTile = null;
                Util.showExceptionAlert(getActivity(), "Get tiles", e);
            }
        } else {
            mRemainingCapacity = -1;
            mTiles = null;
        }
    }

    private void refreshControls() {
        boolean connected = Model.getInstance().isConnected();

        mTextRemainingCapacity.setText(mRemainingCapacity < 0 ? "?" : String.valueOf(mRemainingCapacity));
        
        mButtonRemoveTile.setEnabled(connected && mSelectedTile != null);

        mButtonAddTile.setEnabled(
            connected &&
            mRemainingCapacity > 0 &&
            mEditTileName.getText().length() > 0);

        mButtonSendDialog.setEnabled(
            connected &&
            mSelectedTile != null &&
            (mEditTitle.getText().length() > 0 || mEditBody.getText().length() > 0));

        mButtonSendMessage.setEnabled(
            connected &&
            mSelectedTile != null &&
            (mEditTitle.getText().length() > 0 || mEditBody.getText().length() > 0));
    }
    
    @SuppressLint("InflateParams")
    private class TileListAdapter extends BaseAdapter {

        private List<BandTile> mList;
        
        public void setTileList(Collection<BandTile> tiles) {
            if (mList == null) {
                mList = new ArrayList<BandTile>();
            }

            mList.clear();
            mList.addAll(tiles);
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (mList != null) ? mList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return (mList != null) ? mList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.item_tilelist, null);
            }

            BandTile tile = mList.get(position);
            
            ImageView tileImage = (ImageView)view.findViewById(R.id.imageTileListImage);
            TextView tileTitle = (TextView)view.findViewById(R.id.textTileListTitle);
            
            if (tile.getTileIcon() != null) {
                tileImage.setImageBitmap(tile.getTileIcon().getIcon());
            } else {
                BandIcon tileIcon = BandIcon.toBandIcon(BitmapFactory.decodeResource(getResources(), R.raw.badge));
                tileImage.setImageBitmap(tileIcon.getIcon());
            }
            
            tileImage.setBackgroundColor(Color.BLUE);
            tileTitle.setText(tile.getTileName());

            return view;
        }
    }
}
