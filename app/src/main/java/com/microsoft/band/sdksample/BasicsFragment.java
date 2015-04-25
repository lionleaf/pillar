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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.ConnectionResult;
import com.microsoft.band.BandDeviceInfo;
import com.microsoft.band.BandException;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.notification.VibrationType;

import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class BasicsFragment extends Fragment implements FragmentListener {

    private Button mButtonConnect;
    private Button mButtonChooseBand;

    private Button mButtonGetHwVersion;
    private Button mButtonGetFwVersion;

    private TextView mTextHwVersion;
    private TextView mTextFwVersion;

    private Button mButtonVibrate;
    private Button mButtonVibratePattern;

    private BandDeviceInfo[] mPairedBands;
    private int mSelectedBandIndex = 0;

    private VibrationType mSelectedVibrationType = VibrationType.NOTIFICATION_ALARM;

    public BasicsFragment() {
    }

    public final void onFragmentSelected() {
        if (isVisible()) {
            refreshControls();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_basics, container, false);

        mButtonConnect = (Button) rootView.findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(mButtonConnectClickListener);

        mButtonChooseBand = (Button) rootView.findViewById(R.id.buttonChooseBand);
        mButtonChooseBand.setOnClickListener(mButtonChooseBandClickListener);

        mButtonGetHwVersion = (Button) rootView.findViewById(R.id.buttonGetHardwareVersion);
        mButtonGetHwVersion.setOnClickListener(mButtonGetHwVersionClickListener);

        mButtonGetFwVersion = (Button) rootView.findViewById(R.id.buttonGetFirmwareVersion);
        mButtonGetFwVersion.setOnClickListener(mButtonGetFwVersionClickListener);

        mTextHwVersion = (TextView) rootView.findViewById(R.id.textHardwareVersion);
        mTextFwVersion = (TextView) rootView.findViewById(R.id.textFirmwareVersion);

        mButtonVibrate = (Button) rootView.findViewById(R.id.buttonVibrate);
        mButtonVibrate.setOnClickListener(mButtonVibrateClickListener);

        mButtonVibratePattern = (Button) rootView.findViewById(R.id.buttonVibrationPattern);
        mButtonVibratePattern.setText(mSelectedVibrationType.toString());
        mButtonVibratePattern.setOnClickListener(mButtonVibratePatternClickListener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPairedBands = BandClientManager.getInstance().getPairedBands();

        // If one or more bands were removed, making our band selection invalid,
        // reset the selection to the first in the list.
        if (mSelectedBandIndex >= mPairedBands.length) {
            mSelectedBandIndex = 0;
        }

        refreshControls();
    }

    //
    // The connect call must be done on a background thread because it
    // involves a callback that must be handled on the UI thread.
    //
    private class ConnectTask extends AsyncTask<BandClient, Void, ConnectionResult> {
        @Override
        protected ConnectionResult doInBackground(BandClient... clientParams) {
            try {
                return clientParams[0].connect().await();
            } catch (InterruptedException e) {
                return ConnectionResult.TIMEOUT;
            } catch (BandException e) {
                return ConnectionResult.INTERNAL_ERROR;
            }
        }

        protected void onPostExecute(ConnectionResult result) {
            if (result != ConnectionResult.OK) {
                Util.showExceptionAlert(getActivity(), "Connect", new Exception("Connection failed: result=" + result.toString()));
            }
            refreshControls();
        }
    }

    //
    // Handle connect/disconnect requests.
    //
    private OnClickListener mButtonConnectClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            if (Model.getInstance().isConnected()) {
                try {
                    Model.getInstance().getClient().disconnect().await(2, TimeUnit.SECONDS);
                    refreshControls();
                } catch (Exception ex) {
                    Util.showExceptionAlert(getActivity(), "Disconnect", ex);
                }
            } else {
                // Always recreate our BandClient since the selection might
                // have changed. This is safe since we aren't connected.
                BandClient client = BandClientManager.getInstance().create(getActivity(), mPairedBands[mSelectedBandIndex]);
                Model.getInstance().setClient(client);

                mButtonConnect.setEnabled(false);

                // Connect must be called on a background thread.
                new ConnectTask().execute(Model.getInstance().getClient());
            }
        }
    };

    //
    // If there are multiple bands, the "choose band" button is enabled and
    // launches a dialog where we can select the band to use.
    //
    private OnClickListener mButtonChooseBandClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            String[] names = new String[mPairedBands.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = mPairedBands[i].getName();
            }

            builder.setItems(names, null);
            builder.setItems(names, new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mSelectedBandIndex = which;
                    dialog.dismiss();
                    refreshControls();
                }
            });

            builder.setTitle("Select band:");
            builder.show();
        }
    };

    private OnClickListener mButtonGetFwVersionClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                mTextFwVersion.setText("");
                BandPendingResult<String> result = Model.getInstance().getClient().getFirmwareVersion();
                String fwVersion = result.await(2, TimeUnit.SECONDS);
                mTextFwVersion.setText(fwVersion);
            } catch (TimeoutException t) {
                mTextFwVersion.setText("timeout");
            } catch (Exception e) {
                Util.showExceptionAlert(getActivity(), "Get firmware version", e);
            }
        }
    };

    private OnClickListener mButtonGetHwVersionClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                mTextHwVersion.setText("");
                BandPendingResult<String> result = Model.getInstance().getClient().getHardwareVersion();
                String hwVersion = result.await(2, TimeUnit.SECONDS);
                mTextHwVersion.setText(hwVersion);
            } catch (TimeoutException t) {
                mTextHwVersion.setText("timeout");
            } catch (Exception e) {
                Util.showExceptionAlert(getActivity(), "Get hardware version", e);
            }
        }
    };

    private OnClickListener mButtonVibrateClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            try {
                Model.getInstance()
                    .getClient()
                    .getNotificationManager()
                    .vibrate(mSelectedVibrationType)
                    .await();
            } catch (Exception e) {
                Util.showExceptionAlert(getActivity(), "Vibrate band", e);
            }
        }
    };

    private OnClickListener mButtonVibratePatternClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            VibrationType[] values = VibrationType.values();
            String[] names = new String[VibrationType.values().length];
            for (int i = 0; i < names.length; i++) {
                names[i] = values[i].toString();
            }

            builder.setItems(names, null);
            builder.setItems(names, new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mSelectedVibrationType = VibrationType.values()[which];
                    dialog.dismiss();
                    refreshControls();
                }
            });

            builder.setTitle("Select vibration type:");
            builder.show();
        }
    };

    private void refreshControls() {
        switch (mPairedBands.length) {
        case 0:
            mButtonChooseBand.setText("No paired bands");
            mButtonChooseBand.setEnabled(false);
            mButtonConnect.setEnabled(false);
            break;

        case 1:
            mButtonChooseBand.setText(mPairedBands[mSelectedBandIndex].getName());
            mButtonChooseBand.setEnabled(false);
            mButtonConnect.setEnabled(true);
            break;

        default:
            mButtonChooseBand.setText(mPairedBands[mSelectedBandIndex].getName());
            mButtonChooseBand.setEnabled(true);
            mButtonConnect.setEnabled(true);
            break;
        }

        boolean connected = Model.getInstance().isConnected();

        if (connected) {
            mButtonConnect.setText(R.string.disconnect_label);

            // must disconnect before changing the band selection
            mButtonChooseBand.setEnabled(false);
        } else {
            mButtonConnect.setText(R.string.connect_label);
            mTextFwVersion.setText("");
            mTextHwVersion.setText("");
        }

        mButtonVibratePattern.setText(mSelectedVibrationType.toString());
        mButtonGetFwVersion.setEnabled(connected);
        mButtonGetHwVersion.setEnabled(connected);
        mButtonVibrate.setEnabled(connected);
        mButtonVibratePattern.setEnabled(connected);
    }
}
