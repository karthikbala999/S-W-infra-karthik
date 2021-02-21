package com.example.healthmonitor;
/*Manifest file importing*/
import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.SensorsApi;
import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.HealthDataTypes;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class HealthMonitorActivity extends AppCompatActivity
       {
    private BluetoothAdapter mBluetoothAdapter;
    public static final String TAG = "SensorApp";
    private static final int REQUEST_OAUTH = 1;
    private boolean authInProgress = false;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    private OnDataPointListener mListener;
    /***  to implement the App Indexing API.*/
    private GoogleApiClient client = null;
    // [END mListener_variable_reference]
    private static final DataType[] myDataTypes = {
/*
            DataType.TYPE_LOCATION_SAMPLE
*/
            DataType.TYPE_HEART_RATE_BPM
           /* DataType.TYPE_STEP_COUNT_CUMULATIVE,
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.TYPE_DISTANCE_DELTA*/
    };
   // private static OnDataPointListener mLocationListener;
    private static OnDataPointListener mHeartRateBpmListener;
   /* private static OnDataPointListener mStepCountCumulativeListener;
    private static OnDataPointListener mStepCountDeltaListener;
    private static OnDataPointListener mDistanceCumulativeListener;
    private static OnDataPointListener mDistanceDeltaListener;
    private static OnDataPointListener mActivitySampleListener;*/
    private static final OnDataPointListener[] mDataSourceListeners = {
           // mLocationListener,
            mHeartRateBpmListener
           /* mStepCountCumulativeListener,
            mStepCountDeltaListener,
            mDistanceCumulativeListener,
            mDistanceDeltaListener,
            mActivitySampleListener*/
    };
  //  private BlueToothDevicesManager mBleDevicesManager;
    private RecyclerView.Adapter mAdapter;
    private Button bleenable,scanble,hoverbtn;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    public HealthMonitorActivity() {
        //no args constructor
        //
        }
    public HealthMonitorActivity(RecyclerView.Adapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main2);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleenable = findViewById(R.id.ble_enable);
        scanble = findViewById(R.id.btn_scan);
        hoverbtn = findViewById(R.id.hover_btn);
        scanble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startDiscovery();

            }
        });
        bleenable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                } else {
                    /*bluetooth poppup*/
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 1000);
                }
            }
        });

        hoverbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HealthMonitorActivity.this, UploadFiles.class);
                startActivity(intent);
                finish();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        Log.i("Checking for Permission request", "Over here");
        if (!(checkPermissions(Manifest.permission.BODY_SENSORS) && checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermissions(Manifest.permission.BLUETOOTH) && checkPermissions(Manifest.permission.BLUETOOTH_ADMIN)))
            requestPermissions();
        //fitnessclient is called
        buildFitnessClient();
    }
    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * Authentication will occasionally fail intentionally known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        if (client == null && (checkPermissions(Manifest.permission.BODY_SENSORS) && checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermissions(Manifest.permission.BLUETOOTH) && checkPermissions(Manifest.permission.BLUETOOTH_ADMIN))) {
            client = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.SENSORS_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    String accountName = Plus.AccountApi.getAccountName(client).toString();
                                    Log.i(TAG, accountName);
                                    /* For Recording data  */
                                    final Button distance =  findViewById(R.id.dis_btn);
                                    distance.setOnClickListener(v -> {
                                        readHistoricData(HealthDataTypes.TYPE_BLOOD_PRESSURE, DataType.TYPE_ACTIVITY_SEGMENT);
                                    });
                                    final Button calories =  findViewById(R.id.cal_btn);
                                    calories.setOnClickListener(v -> {
                                        readHistoricData(HealthDataTypes.TYPE_BLOOD_GLUCOSE, DataType.TYPE_ACTIVITY_SEGMENT);
                                    });



                                    final DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                                            .setDataTypes(DataType.TYPE_HEART_RATE_BPM)
                                            .setDataSourceTypes(DataSource.TYPE_RAW)
                                            .build();

                                    Fitness.SensorsApi.findDataSources(client, dataSourceRequest)
                                            .setResultCallback(new ResultCallback<DataSourcesResult>() {
                                                @Override
                                                public void onResult(DataSourcesResult dataSourcesResult) {
                                                    for(DataSource dataSource : dataSourcesResult.getDataSources())
                                                    {
                                                        Log.i("DATASOURCE", "Data source found: " + dataSource.toString());
                                                        Log.i("DATASOURCE", "Data Source type: " + dataSource.getDataType().getName());
                                                        registerFitnessDataListener(dataSource, DataType.TYPE_HEART_RATE_BPM);
                                                    }
                                                }
                                            });
                                }


                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, (GoogleApiClient.OnConnectionFailedListener) connectionResult -> {
                        Log.i(TAG, "Google Play services connection failed. Cause: " + connectionResult.toString());
                        Snackbar.make(HealthMonitorActivity.this.findViewById(R.id.main_content_view),
                                connectionResult.getErrorCode() + ": Exception while connecting to Google Play services, might be because you didn't allow access",
                                Snackbar.LENGTH_INDEFINITE).show();
                        Intent restartActivity = new Intent(HealthMonitorActivity.this, HealthMonitorActivity.class);
                        startActivity(restartActivity);
                    }).build();
        }
    }

           private void registerFitnessDataListener(DataSource dataSource, DataType typeHeartRateBpm) {

               SensorRequest request = new SensorRequest.Builder()
                       .setDataSource(dataSource)
                       .setDataType(typeHeartRateBpm)
                       .setSamplingRate(2, TimeUnit.SECONDS)
                       .build();

               Fitness.SensorsApi.add(client, request, (OnDataPointListener) this)
                       .setResultCallback(new ResultCallback<Status>() {
                           @Override
                           public void onResult(Status status) {
                               if (!status.isSuccess()) {
                                   Log.e("DATASOURCES", "register " + dataSource.getStreamName() + " failed");
                               } else {
                                   Log.i("DATASOURCES", "register " + dataSource.getStreamName() + " succeed");
                               }
                           }
                       });
           }

           private void readHistoricData(DataType dataType, DataType aDataType) {
        // Begin by creating the query.
        DataReadRequest readRequest = queryFitnessData(dataType, aDataType);
        // [START read_dataset]
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        Fitness.HistoryApi.readData(client, readRequest).setResultCallback(new com.google.android.gms.common.api.ResultCallback<DataReadResult>() {
            @Override
            public void onResult(DataReadResult dataReadResult) {
                Status status = dataReadResult.getStatus();
                if (status.isSuccess()) {
                    printData(dataReadResult);
                }
            }
        });
    }
    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    public static DataReadRequest queryFitnessData(DataType dt, DataType adt) {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DATE, -1);
        long startTime = cal.getTimeInMillis();
        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));
        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
              /*  .aggregate(dt, adt)*/
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
               /* .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();*/


                .aggregate(HealthDataTypes.TYPE_BLOOD_PRESSURE, HealthDataTypes.AGGREGATE_BLOOD_PRESSURE_SUMMARY)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]
        return readRequest;
    }

    /**
     * Log a record of the query result. It's possible to get more constrained data sets by
     * specifying a data source or data type, but for demonstrative purposes here's how one would
     * dump all the data. In this sample, logging also prints to the device screen, so we can see
     * what the query returns, but your app should not log fitness information as a privacy
     * consideration. A better option would be to dump the data you receive to a local data
     * directory to avoid exposing it to other applications.
     */
    public void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
      /*  if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets are: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets are: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }*/
        Log.v(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
        if (dataReadResult.getBuckets().size() > 0) {

            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                Log.v(TAG, "Datasets: " + dataSets);

                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            System.out.print("Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        else {
            Log.i(TAG, "HAHA");
        }
        // [END parse_read_data_result]
    }
    // [START parse_dataset]
    private void dumpDataSet(DataSet dataSet) {

           Log.v(TAG, "Name: " + dataSet.getDataType().getName());
           DateFormat dateFormat = getTimeInstance();
           Log.v(TAG, "Fields: " + dataSet.getDataSource().getDataType().getFields());

           Log.v(TAG, "Data Point Values :" + dataSet.getDataPoints());
           for (DataPoint dp : dataSet.getDataPoints()) {
               Log.v(TAG, "Data Point:");
               Log.v(TAG, "Type: " + dataSet.getDataType().getName());
               Log.v(TAG, "Start: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
               Log.v(TAG, "End: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
               for (Field field : dp.getDataType().getFields()) {
                   Log.v(TAG, "Field: " + field.getName() + ", Value : " + dp.getValue(field).asFloat());
               }
           }
    }



    @Override
    protected void onResume() {
        super.onResume();
        // This ensures that if the user denies the permissions then uses Settings to re-enable
        // them, the app will start working.
        buildFitnessClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }


    private void createDataListener(DataSource dataSource, DataType dataType) {
        OnDataPointListener listener = dataPoint -> {

            for (Field field : dataPoint.getDataType().getFields()) {
                Value val = dataPoint.getValue(field);
                Log.i(TAG, "Detected DataPoint field contents: " + field.describeContents());
                Log.i(TAG, "Detected DataPoint field: " + field.getName());
                Log.i(TAG, "Detected DataPoint value: " + val);
                Log.i(TAG, "Detected DataPoint source: " + dataPoint.getDataSource());
                Log.i(TAG, "Detected DataPoint original source: " + dataPoint.getOriginalDataSource());
                Log.i(TAG, "Timestamp" + dataPoint.getTimestamp(TimeUnit.DAYS));
            }
        };
        registerFitnessDataListener(dataSource, dataType, listener);
    }
    private void unRegisterFitnessDataListeners() {
        for (OnDataPointListener listener : mDataSourceListeners) {
            if (listener != null) {
                unregisterFitnessDataListener();
            }
        }
    }
    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
    private void registerFitnessDataListener(DataSource dataSource, DataType dataType, final OnDataPointListener listener) {

        // [START register_data_listener]
        mListener = dataPoint -> {
            for (Field field : dataPoint.getDataType().getFields()) {
                Value val = dataPoint.getValue(field);
                Log.i(TAG, "Detected DataPoint field: " + field.getName());
                Log.i(TAG, "Detected DataPoint value: " + val);
            }
        };

        Fitness.SensorsApi.add(
                client,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                listener)
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        Log.i(TAG, "Listener registered!" + status.getStatusMessage());


                    } else {
                        Log.i(TAG, "Listener not registered.");
                    }
                });

        // [END register_data_listener]
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.


    }

    /**
     * Unregister the listener with the Sensors API.
     */
    private void unregisterFitnessDataListener() {
        if (mListener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.SensorsApi.remove(
                client,
                mListener)
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        Log.i(TAG, "Listener was removed!");
                    } else {
                        Log.i(TAG, "Listener was not removed.");
                    }
                });
        // [END unregister_data_listener]
    }


    /*** Return the current state of the permissions needed.*/
    private boolean checkPermissions(String permission) {
        int permissionState = ActivityCompat.checkSelfPermission(this, permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        boolean shouldProvideRationale1 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BODY_SENSORS);
        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BLUETOOTH);
        boolean shouldProvideRationale3 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BLUETOOTH_ADMIN);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale && shouldProvideRationale1 && shouldProvideRationale2 && shouldProvideRationale3) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");


        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(HealthMonitorActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                if (!client.isConnecting() && !client.isConnected()) {
                    client.connect();
                }
            }
        }
    }
           private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
               public void onReceive(Context context, Intent intent) {
                   String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                       BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                       mDeviceList.add(device);
                   }
               }
           };
}