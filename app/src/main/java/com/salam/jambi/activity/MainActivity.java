package com.salam.jambi.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.salam.jambi.R;
import com.salam.jambi.adapter.ProgramListAdapter;
import com.salam.jambi.data.constant.AppConstants;
import com.salam.jambi.listeners.ListItemClickListener;
import com.salam.jambi.listeners.MediaRecorderStatusListener;
import com.salam.jambi.listeners.PermissionListener;
import com.salam.jambi.listeners.PlayerStatusListeners;
import com.salam.jambi.model.Program;
import com.salam.jambi.model.ProgramTime;
import com.salam.jambi.model.Programs;
import com.salam.jambi.model.RadioChannelData;
import com.salam.jambi.network.HttpParams;
import com.salam.jambi.network.RetrofitClient;
import com.salam.jambi.service.PlayerService;
import com.salam.jambi.utils.ActivityUtils;
import com.salam.jambi.utils.AdUtils;
import com.salam.jambi.utils.AppUtility;
import com.salam.jambi.utils.MyAnimation;
import com.salam.jambi.utils.MyDividerItemDecoration;
import com.salam.jambi.utils.NetworkUtils;
import com.salam.jambi.utils.ProgramNameDisplay;
import com.salam.jambi.utils.VolumeController;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.salam.jambi.utils.NetworkUtils.getConnectivityStatusInt;

public class MainActivity extends BaseActivity implements View.OnClickListener, PlayerStatusListeners, PermissionListener, MediaRecorderStatusListener {

    private Context mContext;
    private int volumeLevel;
    private boolean mBound = false, isPlayerPlaying = false;
    private PlayerService mService;
    private AudioManager audioManager;
    private Intent playerServiceIntent;
    private RecyclerView recyclerViewProgramList;
    private ArrayList<Program> programList = new ArrayList<>();
    private View playerBodyTransparentView;
    private ProgramListAdapter programListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView textViewRadioName, textViewCurrentProgram, textViewProgramHostName;
    private RelativeLayout relativeLayoutPlayerDiskHolder, relativeLayoutPlayerDisk;
    private LinearLayout linearLayoutPlayerHolder;
    private BroadcastReceiver myBroadCastReceiver, mInternetConnectivityChangeReceiver;
    private ProgressBar progressBarUrlFetching, progressBarProgramFetching;
    private ImageView imageViewPower, imageViewShare, imageViewProgress, imageViewPlayPause, imageViewPlayer, imageViewRecord, imageViewProgramListController, imageViewCollapseExpandArrow, imageViewVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable();
        initView();
        loadData();
        initListeners();
    }

    private void initVariable() {
        mContext = getApplicationContext();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        initializeBroadCastReceiver();
        netConnectionAvailabilityBroadCastReceiver();
        if (this.isPlayerServiceRunning()) {
            /**When activity is destroyed then there will be no reference of the running service.
             *  If the service is already running, then to get the reference of that service 'bindService' method will be called.
             */
            Intent intent = new Intent(MainActivity.this, PlayerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        registerReceivers(mInternetConnectivityChangeReceiver, myBroadCastReceiver);
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        imageViewPlayer = findViewById(R.id.img_player);
        imageViewRecord = findViewById(R.id.img_record);
        imageViewVolume = findViewById(R.id.img_volume);
        imageViewPlayPause = findViewById(R.id.img_play_pause);
        imageViewPower = findViewById(R.id.iv_power);
        imageViewShare = findViewById(R.id.iv_share);
        textViewRadioName = findViewById(R.id.tv_radio_name);
        textViewCurrentProgram = findViewById(R.id.tv_current_program);
        textViewProgramHostName = findViewById(R.id.tv_program_host_name);
        recyclerViewProgramList = findViewById(R.id.rv_program_list);
        linearLayoutPlayerHolder = findViewById(R.id.ll_player_holder);
        imageViewCollapseExpandArrow = findViewById(R.id.img_collapse_expand_arrow);
        imageViewProgramListController = findViewById(R.id.img_program_list_controller);
        playerBodyTransparentView = findViewById(R.id.player_body_transparent_view);
        relativeLayoutPlayerDiskHolder = findViewById(R.id.rl_player_disk_holder);
        relativeLayoutPlayerDisk = findViewById(R.id.rl_player_disk);
        imageViewProgress = findViewById(R.id.iv_progress);
        progressBarUrlFetching = findViewById(R.id.progress_bar_url_fetching);
        progressBarProgramFetching = findViewById(R.id.progress_bar_program_fetching);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewProgramList.setLayoutManager(mLayoutManager);
        recyclerViewProgramList.setItemAnimator(new DefaultItemAnimator());
        recyclerViewProgramList.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        programListAdapter = new ProgramListAdapter(this, programList);
        recyclerViewProgramList.setAdapter(programListAdapter);

        if (volumeLevel == 0) {
            imageViewVolume.setImageResource(R.drawable.img_volume_off_icon);
        } else {
            imageViewVolume.setImageResource(R.drawable.img_volume_up_icon);
        }
    }

    private void loadData() {
        loadChannelData();
        loadProgramList();
    }

    private void loadChannelData() {
        progressBarUrlFetching.setVisibility(View.VISIBLE);
        imageViewPlayPause.setEnabled(false);
        RetrofitClient.getClient().getChannelData(HttpParams.SHEET_ID, HttpParams.RADIO_URL_SHEET_NAME).enqueue(new Callback<RadioChannelData>() {
            @Override
            public void onResponse(Call<RadioChannelData> call, Response<RadioChannelData> response) {
                if (response.body() != null) {
                    progressBarUrlFetching.setVisibility(View.GONE);
                    imageViewPlayPause.setEnabled(true);
                    AppConstants.RADIO_CHANNEL_NAME = response.body().getRadioChannel().get(AppConstants.INDEX_ZERO).getChannelName();
                    textViewRadioName.setText(AppConstants.RADIO_CHANNEL_NAME);
                    AppConstants.RADIO_URL = response.body().getRadioChannel().get(AppConstants.INDEX_ZERO).getChannelUrl();
                }
            }

            @Override
            public void onFailure(Call<RadioChannelData> call, Throwable t) {
                progressBarUrlFetching.setVisibility(View.GONE);
            }
        });
    }

    private void loadProgramList() {
        progressBarProgramFetching.setVisibility(View.VISIBLE);
        RetrofitClient.getClient().getProgramList(HttpParams.SHEET_ID, HttpParams.PROGRAM_SHEET_NAME).enqueue(new Callback<Programs>() {
            @Override
            public void onResponse(Call<Programs> call, Response<Programs> response) {
                progressBarProgramFetching.setVisibility(View.GONE);
                if (!programList.isEmpty()) {
                    programList.clear();
                }
                programList.addAll(response.body().getPrograms());

                if (!programList.isEmpty()) {
                    showCurrentProgramNameBasedOnTime();
                    programListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Programs> call, Throwable t) {
                progressBarProgramFetching.setVisibility(View.GONE);
            }
        });
    }

    private void showCurrentProgramNameBasedOnTime() {
        ProgramNameDisplay programNameDisplay = new ProgramNameDisplay();
        programNameDisplay.setListener(new ProgramNameDisplay.ProgramNameDisplayListener() {
            @Override
            public void onProgramNameFound(String programName, String hostName) {
                textViewCurrentProgram.setText(programName);
                textViewProgramHostName.setText(hostName);
            }
        });
        programNameDisplay.execute(programList);
    }

    private void initListeners() {
        imageViewRecord.setOnClickListener(this);
        imageViewVolume.setOnClickListener(this);
        imageViewPlayPause.setOnClickListener(this);
        imageViewProgramListController.setOnClickListener(this);
        imageViewPower.setOnClickListener(this);
        imageViewShare.setOnClickListener(this);
        ActivityUtils.showAllPrograms(this);
        programListAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onAlarmIconClick(ImageView view, int position) {
                if (!ActivityUtils.isProgramAlarmAlreadySet(MainActivity.this, String.valueOf(programList.get(position).getProgramId()))) {
                    view.setImageResource(R.drawable.img_arlarm_active_icon);
                    ActivityUtils.setProgramAsFavorite(MainActivity.this, programList.get(position));

                    /** Set Alarm*/
                    ProgramTime programTime = AppUtility.getTime(programList.get(position).getProgramStartTime());
                    int alarmRequestCode = programList.get(position).getProgramId();
                    long alarmTimeStamp = AppUtility.getTimeInMillis(programTime.getAlarmTime());
                    if (alarmTimeStamp > AppConstants.INDEX_ZERO) {
                        AppUtility.setAlarm(getApplicationContext(), alarmTimeStamp, alarmRequestCode, programList.get(position));
                    }
                    // Show full screen ad // to disable ad remove this bellow line
                    AdUtils.getInstance(MainActivity.this).showFullScreenAd();
                } else {
                    view.setImageResource(R.drawable.img_alarm_inactive_icon);
                    ActivityUtils.removeProgramFromFavorite(MainActivity.this, String.valueOf(programList.get(position).getProgramId()));
                    /** Cancel Alarm*/
                    int alarmRequestCode = programList.get(position).getProgramId();
                    AppUtility.cancelAlarm(getApplicationContext(), alarmRequestCode);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdUtils.getInstance(this).loadFullScreenAd(this);
    }

    //Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            restorePlayerControllerView();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
        }
    };

    private void restorePlayerControllerView() {
        if (mService == null) {
            MyAnimation.showLoading(imageViewProgress);
            imageViewPlayPause.setImageResource(R.drawable.img_play_icon);
        } else {
            if (mService.isPlayerPlaying()) {
                imageViewPlayPause.setImageResource(R.drawable.img_pause_icon);
                isPlayerPlaying = true;
                if (!isProgramListControllerPressed) {
                    MyAnimation.rotationAnimator(imageViewPlayer);
                }
            } else {
                imageViewPlayPause.setImageResource(R.drawable.img_play_icon);
            }
            imageViewPlayPause.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        stopRecordingAudio(); // Recording will be stopped when app will be closed;
        isPlayerPlaying = false;
        unRegisterReceivers(mInternetConnectivityChangeReceiver, myBroadCastReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_play_pause:
                if (mService != null && mService.isPlayerPlaying()) {
                    pauseRadioPlayer();
                } else {
                    startRadioPlayer();
                }
                break;
            case R.id.img_program_list_controller:
                showOrHideProgramList(isPlayerPlaying, linearLayoutPlayerHolder, imageViewCollapseExpandArrow, recyclerViewProgramList, playerBodyTransparentView, relativeLayoutPlayerDiskHolder, relativeLayoutPlayerDisk, imageViewPlayer);
                break;
            case R.id.img_record:
                AppUtility.askAudioRecordPermission(MainActivity.this);
                break;
            case R.id.img_volume:
                VolumeController.showVolumeBar(MainActivity.this, imageViewVolume, audioManager, imageViewVolume);
                break;
            case R.id.iv_power:
                stopRadioPlayer();
                break;
            case R.id.iv_share:
                ActivityUtils.shareAppLink(this);
                break;
        }
    }

    /**
     * Start Player Controller Method
     */
    protected void startRadioPlayer() {
        if (getConnectivityStatusInt(this) == AppConstants.INDEX_ZERO) {
            showSnackBar();
        } else {
            MyAnimation.showLoading(imageViewProgress);
            imageViewPlayPause.setEnabled(false);
            if (mService == null && !mBound) {
                // when service is not created;
                playerServiceIntent = new Intent(MainActivity.this, PlayerService.class);
                startService(playerServiceIntent);
                bindService(playerServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
            } else if (mService != null && mBound) {
                // when service is already running;
                mService.startPlayer();
            }
        }
    }

    protected void pauseRadioPlayer() {
        if (mService != null) {
            mService.pausePlayer();
        }
    }

    protected void resumeRadioPlayer() {
        if (mService != null) {
            MyAnimation.showLoading(imageViewProgress);
            imageViewPlayPause.setEnabled(false);
            mService.resumePlayerWhenNetConnectionAvailable();
        }
    }

    protected void stopRadioPlayer() {
        if (mService != null) {
            mService.stopPlayer();
            finish();
        } else {
            finish();
        }
    }

    @Override
    public void onMyPlayerStartPlaying() {
        MyAnimation.stopLoading(imageViewProgress);
        isPlayerPlaying = true;
        imageViewPlayPause.setImageResource(R.drawable.img_pause_icon);
        imageViewPlayPause.setEnabled(true);
        if (!isProgramListControllerPressed) {
            MyAnimation.rotationAnimator(imageViewPlayer);
        }
    }

    @Override
    public void onMyPlayerPause() {
        stopRecordingAudio(); // Recording will be stopped when Player will be paused;
        imageViewPlayPause.setImageResource(R.drawable.img_play_icon);
        imageViewPlayPause.setEnabled(true);
        isPlayerPlaying = false;
        MyAnimation.stopRotationAnimator();
    }

    @Override
    public void onMyPlayerStop() {
        stopRecordingAudio(); // Recording will be stopped when Player will be stopped;
        imageViewPlayPause.setImageResource(R.drawable.img_play_icon);
        imageViewPlayPause.setEnabled(true);
        MyAnimation.stopRotationAnimator();
        MyAnimation.stopLoading(imageViewProgress);
        unbindService(mConnection);
        Intent playerServiceIntent = new Intent(MainActivity.this, PlayerService.class);
        stopService(playerServiceIntent);
        isPlayerPlaying = false;
        mService = null;
        mBound = false;
        Toast.makeText(this, "Radio stopped playing!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMyPlayerError() {
        Log.e("ExoPlayerTesting", "MainActivity -> onMyPlayerError");
    }

    @Override
    public void onPermissionGranted() {
        startRecordingAudio();
    }
    /** End Player Controller Method */


    /**
     * Start Recording Methods
     */
    private void startRecordingAudio() {
        if (mService != null) {
            //Log.d("MediaRecorder", "startRecordingAudio");
            if (mService.isPlayerPlaying()) {
                if (!mService.isRecorderOn()) {
                    String filePath = AppUtility.createImageFile(this);
                    if (filePath != null) {
                        mService.startRecording(filePath);
                    } else {
                        Toast.makeText(mService, "No External Storage Found!", Toast.LENGTH_SHORT).show();
                    }
                } else
                    mService.stopRecording();
            } else {
                Toast.makeText(this, "Start radio player first!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopRecordingAudio() {
        if (mService != null) {
            if (mService.isRecorderOn())
                mService.stopRecording();
        }
    }

    @Override
    public void onMyRecorderStart() {
        Toast.makeText(this, "Recording Started!", Toast.LENGTH_SHORT).show();
        imageViewRecord.setImageResource(R.drawable.img_record_inactive_icon);
    }

    @Override
    public void onMyRecorderStop() {
        Toast.makeText(this, "File saved in " + getString(R.string.recording_file_storage_directory), Toast.LENGTH_LONG).show();
        imageViewRecord.setImageResource(R.drawable.img_record_active_icon);
    }

    @Override
    public void onMyRecorderError() {
        Toast.makeText(this, "Recording Error!", Toast.LENGTH_SHORT).show();
        imageViewRecord.setImageResource(R.drawable.img_record_active_icon);
    }
    /**
     * End Recording Methods
     */


    /**
     * Start Broadcast Receiver Methods
     */
    private void initializeBroadCastReceiver() {
        myBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.d("ExoPlayerTesting", "MainActivity -> Broadcast Message  Received!");
                if (mBound) {
                    String status = intent.getStringExtra("STATUS");
                    switch (status) {
                        case AppConstants.ON_START_PLAYING:
                            MainActivity.this.onMyPlayerStartPlaying();
                            break;
                        case AppConstants.ON_PLAYER_PAUSE:
                            MainActivity.this.onMyPlayerPause();
                            break;
                        case AppConstants.ON_PLAYER_STOP:
                            MainActivity.this.onMyPlayerStop();
                            break;
                        case AppConstants.ON_PLAYER_ERROR:
                            //Log.d("ExoPlayerTesting", "MainActivity -> onPlayerError");
                            MainActivity.this.onMyPlayerError();
                            break;
                        case AppConstants.ON_START_RECORDING:
                            //Log.d("MediaRecorder", "MainActivity -> onStartRecording");
                            MainActivity.this.onMyRecorderStart();
                            break;
                        case AppConstants.ON_STOP_RECORDING:
                            //Log.d("MediaRecorder", "MainActivity -> onStopRecording");
                            MainActivity.this.onMyRecorderStop();
                            break;
                        case AppConstants.ON_RECORDING_ERROR:
                            //Log.d("MediaRecorder", "MainActivity -> onRecordingError");
                            MainActivity.this.onMyRecorderError();
                            break;
                    }
                }

            }
        };
    }

    protected void netConnectionAvailabilityBroadCastReceiver() {
        mInternetConnectivityChangeReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
            @Override
            public void onReceive(Context context, Intent intent) {

                int status = NetworkUtils.getConnectivityStatusInt(context);
                if (status == AppConstants.INDEX_ZERO) {
                    //Log.d("NetConnectivityStatus", "Not Available");
                    MainActivity.this.pauseRadioPlayer();
                    showSnackBar();
                } else {

                    if (isInitialStickyBroadcast()) {
                        // Do Nothing;
                    } else {
                        //Log.d("NetConnectivityStatus", "Available");
                        if(!AppConstants.RADIO_URL.equals("")){
                            MainActivity.this.resumeRadioPlayer();
                        }
                        loadData();
                        hideSnackBar();
                    }

                }
            }
        };
    }
    /** End Broadcast Receiver Methods*/
}
