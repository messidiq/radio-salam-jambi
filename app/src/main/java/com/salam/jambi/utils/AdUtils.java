package com.salam.jambi.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.salam.jambi.R;

public class AdUtils {

    private static AdUtils adUtils;

    private InterstitialAd mInterstitialAd;

    private boolean disableBannerAd = false, disableInterstitialAd = false;

    private AdUtils(Context context) {
        MobileAds.initialize(context, context.getResources().getString(R.string.app_ad_id));
    }

    public static AdUtils getInstance(Context context) {
        if(adUtils == null) {
            adUtils = new AdUtils(context);
        }
        return adUtils;
    }

    public void showBannerAd(final AdView mAdView) {
        if (disableBannerAd) {
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    mAdView.setVisibility(View.GONE);
                }
            });
        }
    }

    public void loadFullScreenAd(Activity activity) {
        mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId(activity.getResources().getString(R.string.interstitial_ad_unit_id));

        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
        Log.d("AdTesting", "Showing Ad 1 ");
    }

    public boolean showFullScreenAd() {
        Log.d("AdTesting", "Showing Ad 2");
        if(!disableInterstitialAd) {
            if (mInterstitialAd.isLoaded()) {
                Log.d("AdTesting", "Showing Ad");
                mInterstitialAd.show();
                return true;
            }
        }
        return false;
    }

    public InterstitialAd getInterstitialAd() {
        return mInterstitialAd;
    }

    public void disableBannerAd() {
        this.disableBannerAd = true;
    }

    public void disableInterstitialAd() {
        this.disableInterstitialAd = true;
    }


}
