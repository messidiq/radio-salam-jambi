package com.salam.jambi.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.salam.jambi.R;

/**
 * Created by Admin on 29-Apr-18.
 */

public class VolumeController {

    public static void showVolumeBar(Context mContext, View anchorView, final AudioManager audioManager, final ImageView imageViewVolume) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View popupView = inflater.inflate(R.layout.volume_controller_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, 100);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        int location[] = new int[2];
        anchorView.getLocationOnScreen(location); // Get the View's(the one that was clicked in the Activity) location
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0], location[1] - 150);// Using location, the PopupWindow will be displayed right under anchorView

        SeekBar seekBarVolumeControl = popupView.findViewById(R.id.seek_bar_volume);
        seekBarVolumeControl.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolumeControl.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        seekBarVolumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                if (i == 0) {
                    imageViewVolume.setImageResource(R.drawable.img_volume_off_icon);
                } else {
                    imageViewVolume.setImageResource(R.drawable.img_volume_up_icon);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
