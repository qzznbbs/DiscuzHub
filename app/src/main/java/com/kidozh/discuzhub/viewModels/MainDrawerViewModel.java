package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.kidozh.discuzhub.daos.forumInformationDao;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.ArrayList;
import java.util.List;

public class MainDrawerViewModel extends AndroidViewModel {
    private static final String TAG = MainDrawerViewModel.class.getSimpleName();
    public MutableLiveData<bbsInformation> currentBBSInformationMutableLiveData =
            new MutableLiveData<>(null);
    public LiveData<List<bbsInformation>> allBBSInformationMutableLiveData;
    public MutableLiveData<forumUserBriefInfo> currentForumUserBriefInfoMutableLiveData =
            new MutableLiveData<>(null);
    public MutableLiveData<List<forumUserBriefInfo>> forumUserListMutableLiveData =
            new MutableLiveData<>(null);

    private forumInformationDao forumInformationDao;

    public MainDrawerViewModel(@NonNull Application application) {
        super(application);
        forumInformationDao = BBSInformationDatabase.getInstance(getApplication()).getForumInformationDao();
        allBBSInformationMutableLiveData = forumInformationDao.getAllForumInformations();

    }

    public void setCurrentBBSById(LifecycleOwner lifecycleOwner, int bbsId){
        LiveData<bbsInformation> selectedBBSInfo= forumInformationDao.getForumInformationLiveDataById(bbsId);
        selectedBBSInfo.observe(lifecycleOwner, new Observer<bbsInformation>() {
            @Override
            public void onChanged(bbsInformation bbsInformation) {
                currentBBSInformationMutableLiveData.postValue(bbsInformation);
            }
        });


    }

}
