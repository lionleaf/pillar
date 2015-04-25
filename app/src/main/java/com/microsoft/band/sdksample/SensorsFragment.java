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

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.microsoft.band.BandException;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.SampleRate;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

public class SensorsFragment extends Fragment implements FragmentListener {

    // Accelerometer controls
    private Switch mSwitchAccelerometer;
    private TableLayout mTableAccelerometer;
    private RadioGroup mRadioGroupAccelerometer;
    private TextView mTextAccX;
    private TextView mTextAccY;
    private TextView mTextAccZ;
    private RadioButton mRadioAcc16;
    private RadioButton mRadioAcc32;

    // Gyroscope controls
    private Switch mSwitchGyro;
    private TableLayout mTableGyro;
    private RadioGroup mRadioGroupGyro;
    private TextView mTextGyroAccX;
    private TextView mTextGyroAccY;
    private TextView mTextGyroAccZ;
    private TextView mTextGyroAngX;
    private TextView mTextGyroAngY;
    private TextView mTextGyroAngZ;
    private RadioButton mRadioGyro16;
    private RadioButton mRadioGyro32;

    // Distance sensor controls
    private Switch mSwitchDistance;
    private TableLayout mTableDistance;
    private TextView mTextTotalDistance;
    private TextView mTextSpeed;
    private TextView mTextPace;
    private TextView mTextPedometerMode;

    // HR sensor controls
    private Switch mSwitchHeartRate;
    private TableLayout mTableHeartRate;
    private TextView mTextHeartRate;
    private TextView mTextHeartRateQuality;

    // Contact sensor controls
    private Switch mSwitchContact;
    private TableLayout mTableContact;
    private TextView mTextContact;

    // Skin temperature sensor controls
    private Switch mSwitchSkinTemperature;
    private TableLayout mTableSkinTemperature;
    private TextView mTextSkinTemperature;

    // UV sensor controls
    private Switch mSwitchUltraviolet;
    private TableLayout mTableUltraviolet;
    private TextView mTextUltraviolet;

    // Pedometer sensor controls
    private Switch mSwitchPedometer;
    private TableLayout mTablePedometer;
    private TextView mTextTotalSteps;

    // Each sensor switch has an associated TableLayout containing it's display controls.
    // The TableLayout remains hidden until the corresponding sensor switch is turned on.
    private HashMap<Switch, TableLayout> mSensorMap;

    //
    // For managing communication between the incoming sensor events and the UI thread
    //
    private volatile boolean mIsHandlerScheduled;
    private AtomicReference<BandAccelerometerEvent> mPendingAccelerometerEvent = new AtomicReference<BandAccelerometerEvent>();
    private AtomicReference<BandGyroscopeEvent> mPendingGyroscopeEvent = new AtomicReference<BandGyroscopeEvent>();
    private AtomicReference<BandDistanceEvent> mPendingDistanceEvent = new AtomicReference<BandDistanceEvent>();
    private AtomicReference<BandHeartRateEvent> mPendingHeartRateEvent = new AtomicReference<BandHeartRateEvent>();
    private AtomicReference<BandContactEvent> mPendingContactEvent = new AtomicReference<BandContactEvent>();
    private AtomicReference<BandSkinTemperatureEvent> mPendingSkinTemperatureEvent = new AtomicReference<BandSkinTemperatureEvent>();
    private AtomicReference<BandUVEvent> mPendingUVEvent = new AtomicReference<BandUVEvent>();
    private AtomicReference<BandPedometerEvent> mPendingPedometerEvent = new AtomicReference<BandPedometerEvent>();

    public SensorsFragment() {
    }

    public void onFragmentSelected() {
        if (isVisible()) {
            refreshControls();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sensors, container, false);

        mSensorMap = new HashMap<Switch, TableLayout>();

        //
        // Accelerometer setup
        //
        mSwitchAccelerometer = (Switch)rootView.findViewById(R.id.switchAccelerometer);
        mTableAccelerometer = (TableLayout)rootView.findViewById(R.id.tableAccelerometer);
        mRadioGroupAccelerometer = (RadioGroup)rootView.findViewById(R.id.rgAccelerometer);
        mSensorMap.put(mSwitchAccelerometer, mTableAccelerometer);
        mTableAccelerometer.setVisibility(View.GONE);
        mSwitchAccelerometer.setOnCheckedChangeListener(mToggleSensorSection);

        mTextAccX = (TextView)rootView.findViewById(R.id.textAccX);
        mTextAccY = (TextView)rootView.findViewById(R.id.textAccY);
        mTextAccZ = (TextView)rootView.findViewById(R.id.textAccZ);
        mRadioAcc16 = (RadioButton)rootView.findViewById(R.id.rbAccelerometerRate16ms);
        mRadioAcc32 = (RadioButton)rootView.findViewById(R.id.rbAccelerometerRate32ms);

        //
        // Gyro setup
        //
        mSwitchGyro = (Switch)rootView.findViewById(R.id.switchGyro);
        mTableGyro = (TableLayout)rootView.findViewById(R.id.tableGyro);
        mRadioGroupGyro = (RadioGroup)rootView.findViewById(R.id.rgGyro);
        mSensorMap.put(mSwitchGyro, mTableGyro);
        mTableGyro.setVisibility(View.GONE);
        mSwitchGyro.setOnCheckedChangeListener(mToggleSensorSection);

        mTextGyroAccX = (TextView)rootView.findViewById(R.id.textGyroAccX);
        mTextGyroAccY = (TextView)rootView.findViewById(R.id.textGyroAccY);
        mTextGyroAccZ = (TextView)rootView.findViewById(R.id.textGyroAccZ);
        mTextGyroAngX = (TextView)rootView.findViewById(R.id.textAngX);
        mTextGyroAngY = (TextView)rootView.findViewById(R.id.textAngY);
        mTextGyroAngZ = (TextView)rootView.findViewById(R.id.textAngZ);
        mRadioGyro16 = (RadioButton)rootView.findViewById(R.id.rbGyroRate16ms);
        mRadioGyro32 = (RadioButton)rootView.findViewById(R.id.rbGyroRate32ms);

        //
        // Distance setup
        //
        mSwitchDistance = (Switch)rootView.findViewById(R.id.switchDistance);
        mTableDistance = (TableLayout)rootView.findViewById(R.id.tableDistance);
        mSensorMap.put(mSwitchDistance, mTableDistance);
        mTableDistance.setVisibility(View.GONE);
        mSwitchDistance.setOnCheckedChangeListener(mToggleSensorSection);

        mTextTotalDistance = (TextView)rootView.findViewById(R.id.textTotalDistance);
        mTextSpeed = (TextView)rootView.findViewById(R.id.textSpeed);
        mTextPace = (TextView)rootView.findViewById(R.id.textPace);
        mTextPedometerMode = (TextView)rootView.findViewById(R.id.textPedometerMode);

        //
        // Heart rate setup
        //
        mSwitchHeartRate = (Switch)rootView.findViewById(R.id.switchHeartRate);
        mTableHeartRate = (TableLayout)rootView.findViewById(R.id.tableHeartRate);
        mSensorMap.put(mSwitchHeartRate, mTableHeartRate);
        mTableHeartRate.setVisibility(View.GONE);
        mSwitchHeartRate.setOnCheckedChangeListener(mToggleSensorSection);

        mTextHeartRate = (TextView)rootView.findViewById(R.id.textHeartRate);
        mTextHeartRateQuality = (TextView)rootView.findViewById(R.id.textHeartRateQuality);

        //
        // Contact setup
        //
        mSwitchContact = (Switch)rootView.findViewById(R.id.switchContact);
        mTableContact = (TableLayout)rootView.findViewById(R.id.tableContact);
        mSensorMap.put(mSwitchContact, mTableContact);
        mTableContact.setVisibility(View.GONE);
        mSwitchContact.setOnCheckedChangeListener(mToggleSensorSection);

        mTextContact = (TextView)rootView.findViewById(R.id.textContact);

        //
        // Skin temperature setup
        //
        mSwitchSkinTemperature = (Switch)rootView.findViewById(R.id.switchSkinTemperature);
        mTableSkinTemperature = (TableLayout)rootView.findViewById(R.id.tableSkinTemperature);
        mSensorMap.put(mSwitchSkinTemperature, mTableSkinTemperature);
        mTableSkinTemperature.setVisibility(View.GONE);
        mSwitchSkinTemperature.setOnCheckedChangeListener(mToggleSensorSection);

        mTextSkinTemperature = (TextView)rootView.findViewById(R.id.textSkinTemperature);

        //
        // Ultraviolet setup
        //
        mSwitchUltraviolet = (Switch)rootView.findViewById(R.id.switchUltraviolet);
        mTableUltraviolet = (TableLayout)rootView.findViewById(R.id.tableUltraviolet);
        mSensorMap.put(mSwitchUltraviolet, mTableUltraviolet);
        mTableUltraviolet.setVisibility(View.GONE);
        mSwitchUltraviolet.setOnCheckedChangeListener(mToggleSensorSection);

        mTextUltraviolet = (TextView)rootView.findViewById(R.id.textUltraviolet);

        //
        // Pedometer setup
        //
        mSwitchPedometer = (Switch)rootView.findViewById(R.id.switchPedometer);
        mTablePedometer = (TableLayout)rootView.findViewById(R.id.tablePedometer);
        mSensorMap.put(mSwitchPedometer, mTablePedometer);
        mTablePedometer.setVisibility(View.GONE);
        mSwitchPedometer.setOnCheckedChangeListener(mToggleSensorSection);

        mTextTotalSteps = (TextView)rootView.findViewById(R.id.textTotalSteps);

        return rootView;
    }

    //
    // When pausing, turn off any active sensors.
    //
    @Override
    public void onPause() {
        for (Switch sw : mSensorMap.keySet()) {
            if (sw.isChecked()) {
                sw.setChecked(false);
                mToggleSensorSection.onCheckedChanged(sw, false);
            }
        }

        super.onPause();
    }

    private OnCheckedChangeListener mToggleSensorSection = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!Model.getInstance().isConnected()) {
                return;
            }

            Switch sw = (Switch)buttonView;
            TableLayout table = mSensorMap.get(sw);
            BandSensorManager sensorMgr = Model.getInstance().getClient().getSensorManager();

            if (isChecked) {
                table.setVisibility(View.VISIBLE);

                if (table == mTableAccelerometer) {
                    mRadioGroupAccelerometer.setEnabled(false);
                    setChildrenEnabled(mRadioGroupAccelerometer, false);
                } else if (table == mTableGyro) {
                    mRadioGroupGyro.setEnabled(false);
                    setChildrenEnabled(mRadioGroupGyro, false);
                }

                // Turn on the appropriate sensor

                try {
                    if (sw == mSwitchAccelerometer) {
                        SampleRate rate;
                        if (mRadioAcc16.isChecked()) {
                            rate = SampleRate.MS16;
                        } else if (mRadioAcc32.isChecked()) {
                            rate = SampleRate.MS32;
                        } else {
                            rate = SampleRate.MS128;
                        }

                        mTextAccX.setText("");
                        mTextAccY.setText("");
                        mTextAccZ.setText("");
                        sensorMgr.registerAccelerometerEventListener(mAccelerometerEventListener, rate);
                    } else if (sw == mSwitchGyro) {
                        SampleRate rate;
                        if (mRadioGyro16.isChecked()) {
                            rate = SampleRate.MS16;
                        } else if (mRadioGyro32.isChecked()) {
                            rate = SampleRate.MS32;
                        } else {
                            rate = SampleRate.MS128;
                        }

                        mTextGyroAccX.setText("");
                        mTextGyroAccY.setText("");
                        mTextGyroAccZ.setText("");
                        mTextGyroAngX.setText("");
                        mTextGyroAngY.setText("");
                        mTextGyroAngZ.setText("");
                        sensorMgr.registerGyroscopeEventListener(mGyroEventListener, rate);
                    } else if (sw == mSwitchDistance) {
                        mTextTotalDistance.setText("");
                        mTextSpeed.setText("");
                        mTextPace.setText("");
                        mTextPedometerMode.setText("");
                        sensorMgr.registerDistanceEventListener(mDistanceEventListener);
                    } else if (sw == mSwitchHeartRate) {
                        mTextHeartRate.setText("");
                        mTextHeartRateQuality.setText("");
                        sensorMgr.registerHeartRateEventListener(mHeartRateEventListener);
                    } else if (sw == mSwitchContact) {
                        mTextContact.setText("");
                        sensorMgr.registerContactEventListener(mContactEventListener);
                    } else if (sw == mSwitchSkinTemperature) {
                        mTextSkinTemperature.setText("");
                        sensorMgr.registerSkinTemperatureEventListener(mSkinTemperatureEventListener);
                    } else if (sw == mSwitchUltraviolet) {
                        mTextUltraviolet.setText("");
                        sensorMgr.registerUVEventListener(mUltravioletEventListener);
                    } else if (sw == mSwitchPedometer) {
                        mTextTotalSteps.setText("");
                        sensorMgr.registerPedometerEventListener(mPedometerEventListener);
                    }
                } catch (BandException ex) {
                    Util.showExceptionAlert(getActivity(), "Register sensor listener", ex);
                }
            } else {
                table.setVisibility(View.GONE);

                if (table == mTableAccelerometer) {
                    mRadioGroupAccelerometer.setEnabled(true);
                    setChildrenEnabled(mRadioGroupAccelerometer, true);
                } else if (table == mTableGyro) {
                    mRadioGroupGyro.setEnabled(true);
                    setChildrenEnabled(mRadioGroupGyro, true);
                }

                // Turn off the appropriate sensor

                try {
                    if (sw == mSwitchAccelerometer) {
                        sensorMgr.unregisterAccelerometerEventListener(mAccelerometerEventListener);
                    } else if (sw == mSwitchGyro) {
                        sensorMgr.unregisterGyroscopeEventListener(mGyroEventListener);
                    } else if (sw == mSwitchDistance) {
                        sensorMgr.unregisterDistanceEventListener(mDistanceEventListener);
                    } else if (sw == mSwitchHeartRate) {
                        sensorMgr.unregisterHeartRateEventListener(mHeartRateEventListener);
                    } else if (sw == mSwitchContact) {
                        sensorMgr.unregisterContactEventListener(mContactEventListener);
                    } else if (sw == mSwitchSkinTemperature) {
                        sensorMgr.unregisterSkinTemperatureEventListener(mSkinTemperatureEventListener);
                    } else if (sw == mSwitchUltraviolet) {
                        sensorMgr.unregisterUVEventListener(mUltravioletEventListener);
                    } else if (sw == mSwitchPedometer) {
                        sensorMgr.unregisterPedometerEventListener(mPedometerEventListener);
                    }
                } catch (BandException ex) {
                    Util.showExceptionAlert(getActivity(), "Unregister sensor listener", ex);
                }
            }
        }
    };

    //
    // This method is scheduled to run on the UI thread after a sensor event has been received.
    // We clear our "is scheduled" flag and then update the UI controls for any new sensor
    // events (which we also clear).
    //
    private void handlePendingSensorReports() {
        // Because we clear this flag before reading the sensor events, it's possible that a
        // newly-generated event will schedule the handler to run unnecessarily. This is
        // harmless. If we update the flag after checking the sensors, we could fail to call
        // the handler at all.
        mIsHandlerScheduled = false;

        BandAccelerometerEvent accelerometerEvent = mPendingAccelerometerEvent.getAndSet(null);
        if (accelerometerEvent != null) {
            mTextAccX.setText(String.format("%.3f", accelerometerEvent.getAccelerationX()));
            mTextAccY.setText(String.format("%.3f", accelerometerEvent.getAccelerationY()));
            mTextAccZ.setText(String.format("%.3f", accelerometerEvent.getAccelerationZ()));
        }

        BandGyroscopeEvent gyroscopeEvent = mPendingGyroscopeEvent.getAndSet(null);
        if (gyroscopeEvent != null) {
            mTextGyroAccX.setText(String.format("%.3f", gyroscopeEvent.getAccelerationX()));
            mTextGyroAccY.setText(String.format("%.3f", gyroscopeEvent.getAccelerationY()));
            mTextGyroAccZ.setText(String.format("%.3f", gyroscopeEvent.getAccelerationZ()));
            mTextGyroAngX.setText(String.format("%.2f", gyroscopeEvent.getAngularVelocityX()));
            mTextGyroAngY.setText(String.format("%.2f", gyroscopeEvent.getAngularVelocityY()));
            mTextGyroAngZ.setText(String.format("%.2f", gyroscopeEvent.getAngularVelocityZ()));
        }

        BandDistanceEvent distanceEvent = mPendingDistanceEvent.getAndSet(null);
        if (distanceEvent != null) {
            mTextTotalDistance.setText(String.format("%d cm", distanceEvent.getTotalDistance()));
            mTextSpeed.setText(String.format("%.2f cm/s", distanceEvent.getSpeed()));
            mTextPace.setText(String.format("%.2f ms/m", distanceEvent.getPace()));
            mTextPedometerMode.setText(distanceEvent.getPedometerMode().toString());
        }

        BandHeartRateEvent heartRateEvent = mPendingHeartRateEvent.getAndSet(null);
        if (heartRateEvent != null) {
            mTextHeartRate.setText(String.valueOf(heartRateEvent.getHeartRate()));
            mTextHeartRateQuality.setText(heartRateEvent.getQuality().toString());
        }

        BandContactEvent contactEvent = mPendingContactEvent.getAndSet(null);
        if (contactEvent != null) {
            mTextContact.setText(contactEvent.getContactStatus().toString());
        }

        BandSkinTemperatureEvent skinTemperatureEvent = mPendingSkinTemperatureEvent.getAndSet(null);
        if (skinTemperatureEvent != null) {
            mTextSkinTemperature.setText(String.format("%.1f", skinTemperatureEvent.getTemperature()));
        }

        BandUVEvent uvEvent = mPendingUVEvent.getAndSet(null);
        if (uvEvent != null) {
            mTextUltraviolet.setText(uvEvent.getUVIndexLevel().toString());
        }

        BandPedometerEvent pedometerEvent = mPendingPedometerEvent.getAndSet(null);
        if (pedometerEvent != null) {
            mTextTotalSteps.setText(String.format("%d", pedometerEvent.getTotalSteps()));
        }
    }

    //
    // Queue an action to run on the UI thread to process sensor updates. Make sure
    // that we have at most one callback queued for the UI thread.
    //
    private synchronized void scheduleSensorHandler() {
        if (mIsHandlerScheduled) {
            return;
        }

        Activity activity = getActivity();

        if (activity != null) {
            mIsHandlerScheduled = true;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handlePendingSensorReports();
                }
            });
        }
    }

    //
    // Sensor event handlers - each handler just writes the new sample to an atomic
    // reference where it will be read by the UI thread. Samples that arrive faster
    // than they can be processed by the UI thread overwrite older samples. Each
    // handler calls scheduleSensorHandler() which makes sure that at most one call
    // is queued to the UI thread to update all of the sensor displays.
    //

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            mPendingAccelerometerEvent.set(event);
            scheduleSensorHandler();
        }
    };

    private BandGyroscopeEventListener mGyroEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            mPendingGyroscopeEvent.set(event);
            scheduleSensorHandler();
        }
    };

    private BandDistanceEventListener mDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(final BandDistanceEvent event) {
            mPendingDistanceEvent.set(event);
            scheduleSensorHandler();
        }
    };

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            mPendingHeartRateEvent.set(event);
            scheduleSensorHandler();
        }
    };

    private BandContactEventListener mContactEventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(final BandContactEvent event) {
            mPendingContactEvent.set(event);
            scheduleSensorHandler();
        }
    };

    private BandSkinTemperatureEventListener mSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent event) {
            mPendingSkinTemperatureEvent.set(event);
            scheduleSensorHandler();
        }
    };

    private BandUVEventListener mUltravioletEventListener = new BandUVEventListener() {
        @Override
        public void onBandUVChanged(final BandUVEvent event) {
            mPendingUVEvent.set(event);
            scheduleSensorHandler();
        }
    };

    private BandPedometerEventListener mPedometerEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(final BandPedometerEvent event) {
            mPendingPedometerEvent.set(event);
            scheduleSensorHandler();
        }
    };

    //
    // Other helpers
    //

    private static void setChildrenEnabled(RadioGroup radioGroup, boolean enabled) {
        for (int i = radioGroup.getChildCount() - 1; i >= 0; i--) {
            radioGroup.getChildAt(i).setEnabled(enabled);
        }
    }

    private void refreshControls() {
        boolean connected = Model.getInstance().isConnected();

        for (Switch sw : mSensorMap.keySet()) {
            sw.setEnabled(connected);
            if (!connected) {
                sw.setChecked(false);
                mToggleSensorSection.onCheckedChanged(sw, false);
            }
        }
    }
}
