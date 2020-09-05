package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.works.AutoClearHistoriesWork;
import com.kidozh.discuzhub.works.PushUserNotificationWork;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.notificationUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashScreenActivity extends BaseStatusActivity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    @BindView(R.id.splash_screen_agreement_content)
    TextView splashScreenNotification;
    @BindView(R.id.splash_screen_agree_to_continue_btn)
    Button agreeToContinueBtn;
    @BindView(R.id.splash_screen_terms_of_use_card)
    CardView privacyPolicyCardView;
    @BindView(R.id.splash_screen_privacy_policy_card)
    CardView termsOfUseCardView;
    @BindView(R.id.splash_screen_version)
    TextView versionTextView;

    String AGREEMENT_VERSION_PREFERENCE = "AGREEMENT_VERSION_PREFERENCE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);
        registerNotification();
        configureDarkMode();

        registerWorkManager();
        renderPage();
        //enterMainActivity();

    }

    // register notification
    private void registerNotification(){
        // splashScreenNotification.setText(R.string.action_register_notification_channel);
        notificationUtils.createUpdateProgressNotificationChannel(this);
        notificationUtils.createUsersUpdateChannel(this);
    }

    private void registerWorkManager(){
        Log.d(TAG,"Register work");

        // Create a Constraints object that defines when the task should run

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
//                .setRequiresDeviceIdle(true)
                .build();
        // update frequency
        // extracting all user...
        List<forumUserBriefInfo> allUsers = forumUserBriefInfoDatabase
                .getSyncDatabase(this)

                .getforumUserBriefInfoDao()
                .getAllUser();
        Log.d(TAG,"ALL USER "+allUsers.size());
        // sync frequency test
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this) ;
        String syncFreq = prefs.getString(getString(R.string.preference_key_sync_time),"30min");
        int periodicFreq = 30;
        switch (syncFreq){
            case "15min":
                periodicFreq = 15;
                break;
            case "30min":
                periodicFreq = 30;
                break;
            case "1h":
                periodicFreq = 60;
                break;
            case "2h":
                periodicFreq = 120;
                break;
            case "5h":
                periodicFreq = 500;
                break;
        }
        WorkManager.getInstance(this).cancelAllWork();

        // auto clear
        if(UserPreferenceUtils.autoClearViewHistories(this)){
            Constraints autoClearConstraints = new Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiresDeviceIdle(true)
                    .build();
            PeriodicWorkRequest autoClearRequest = new PeriodicWorkRequest.Builder(AutoClearHistoriesWork.class,1, TimeUnit.DAYS)
                    .setConstraints(autoClearConstraints)
                    .build();
            WorkManager.getInstance(this).enqueue(autoClearRequest);
        }



        for(int i=0; i<allUsers.size();i++){

            forumUserBriefInfo userBriefInfo = allUsers.get(i);

            Data userData = new Data.Builder()
                    .putInt(bbsConstUtils.WORK_MANAGER_PASS_USER_ID_KEY, userBriefInfo.getId())
                    .build();
            // start periodic work
            Log.d(TAG,"Register notification "+userBriefInfo.username);

            PeriodicWorkRequest saveRequest =
                    new PeriodicWorkRequest.Builder(PushUserNotificationWork.class, periodicFreq, TimeUnit.MINUTES)
                            .setInputData(userData)
                            .setConstraints(constraints)
                            .addTag(bbsConstUtils.WORK_MANAGER_UPDATE_USERS_TAG)
                            .build();

            WorkManager.getInstance(this)
                    .enqueue(saveRequest);

            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(PushUserNotificationWork.class)
                    .setConstraints(constraints)
                    .setInputData(userData)
                    .build();
            WorkManager.getInstance(this).enqueue(oneTimeWorkRequest);
        }

    }

    private void configureDarkMode(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this) ;
        String dark_mode_settings = prefs.getString(getString(R.string.preference_key_display_mode),"");
        switch (dark_mode_settings){
            case "MODE_NIGHT_NO":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return;
            case "MODE_NIGHT_YES":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return;
            case "MODE_NIGHT_FOLLOW_SYSTEM":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                return;
            case "MODE_NIGHT_AUTO_BATTERY":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                return;

                default:
                    ;

        }
    }



    private void enterMainActivity(){

        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
        finishAfterTransition();
    }

    private void renderPage(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String agreedVersion = prefs.getString(AGREEMENT_VERSION_PREFERENCE,"");
        String currentVersion = getVersionName();
        versionTextView.setText(getString(R.string.app_version_template,currentVersion));
        agreeToContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(AGREEMENT_VERSION_PREFERENCE,currentVersion);
                editor.apply();
                enterMainActivity();
            }
        });
        privacyPolicyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/privacy_policy/"));
                startActivity(intent);
            }
        });
        termsOfUseCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/term_of_use/"));
                startActivity(intent);
            }
        });



        if(agreedVersion.equals(currentVersion)){
            Log.d(TAG,"ARGEEMENT EQUALS "+agreedVersion);
            enterMainActivity();
        }

    }

    public String getVersionName() {
        try{
            // 获取packagemanager的实例
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
            String version = packInfo.versionName;
            return version;
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String agreedVersion = prefs.getString(AGREEMENT_VERSION_PREFERENCE,"");
        String currentVersion = getVersionName();
        if(agreedVersion.equals(currentVersion)){
            enterMainActivity();
        }
        // finish();
    }
}
