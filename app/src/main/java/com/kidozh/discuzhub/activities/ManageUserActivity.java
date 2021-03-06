package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.bbsDetailedInformation.bbsShowInformationActivity;
import com.kidozh.discuzhub.activities.ui.bbsDetailedInformation.bbsShowInformationViewModel;
import com.kidozh.discuzhub.adapter.forumUsersAdapter;
import com.kidozh.discuzhub.callback.RecyclerViewItemTouchCallback;
import com.kidozh.discuzhub.callback.forumSwipeToDeleteUserCallback;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.dialogs.ManageAdapterHelpDialogFragment;
import com.kidozh.discuzhub.dialogs.ManageUserAdapterHelpDialogFragment;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManageUserActivity extends BaseStatusActivity
        implements RecyclerViewItemTouchCallback.onInteraction{
    final static String TAG = ManageUserActivity.class.getSimpleName();

    @BindView(R.id.bbs_user_recyclerview)
    RecyclerView usersRecyclerview;
    @BindView(R.id.add_a_user)
    Button addUserBtn;
    @BindView(R.id.empty_user_view)
    View emptyUserView;
    @BindView(R.id.manage_user_coordinatorLayout)
    CoordinatorLayout manageUserCoordinatorLayout;

    bbsShowInformationViewModel viewModel;


    forumUsersAdapter userAdapter;

    private LiveData<List<forumUserBriefInfo>> bbsUserInfoLiveDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);
        ButterKnife.bind(this);
        viewModel = new ViewModelProvider(this).get(bbsShowInformationViewModel.class);
        configureIntent();
        configureActionBar();
        configureRecyclerView();
        fetchUserList();
        configureAddUserBtn();
        showHelpDialog();
    }

    private void configureIntent(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        if(bbsInfo == null){
            finishAfterTransition();
        }
        else {
            URLUtils.setBBS(bbsInfo);
        }
    }

    void configureActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(bbsInfo.site_name);
            getSupportActionBar().setSubtitle(bbsInfo.base_url);
        }

    }

    void configureRecyclerView(){
        usersRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new forumUsersAdapter(this, bbsInfo);
        usersRecyclerview.setAdapter(userAdapter);
        // swipe to delete
        // swipe to delete support
        RecyclerViewItemTouchCallback callback = new RecyclerViewItemTouchCallback(this);
        //forumSwipeToDeleteUserCallback swipeToDeleteUserCallback = new forumSwipeToDeleteUserCallback(userAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(usersRecyclerview);

        usersRecyclerview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    void fetchUserList(){
        viewModel.loadUserList(bbsInfo.getId());
        viewModel.bbsUserInfoLiveDataList.observe(this, new Observer<List<forumUserBriefInfo>>() {
            @Override
            public void onChanged(List<forumUserBriefInfo> forumUserBriefInfos) {
                userAdapter.setUserList(forumUserBriefInfos);
                if(forumUserBriefInfos == null || forumUserBriefInfos.size() == 0){
                    emptyUserView.setVisibility(View.VISIBLE);
                }
                else {
                    emptyUserView.setVisibility(View.GONE);
                }
            }
        });

    }

    void configureAddUserBtn(){
        Activity activity =this;
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, LoginActivity.class);

                Log.d(TAG,"ADD A account "+bbsInfo);
                if(bbsInfo !=null){
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, (forumUserBriefInfo) null);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);

                    Bundle bundle = options.toBundle();
                    activity.startActivity(intent,bundle);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_info, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                this.finishAfterTransition();
                return false;
            case R.id.show_help_info:
                showHelpDialog();
                return false;
            case R.id.add_item:
                Intent intent = new Intent(this, LoginActivity.class);

                if(bbsInfo !=null){
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, (forumUserBriefInfo) null);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);

                    Bundle bundle = options.toBundle();
                    startActivity(intent,bundle);
                }
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showHelpDialog(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        ManageUserAdapterHelpDialogFragment dialogFragment = new ManageUserAdapterHelpDialogFragment();
        dialogFragment.show(fragmentManager,ManageAdapterHelpDialogFragment.class.getSimpleName());
    }


    @Override
    public void onRecyclerViewSwiped(int position, int direction) {
        List<forumUserBriefInfo> userBriefInfos = userAdapter.getUserList();
        if(userBriefInfos!=null){
            forumUserBriefInfo userBriefInfo = userBriefInfos.get(position);
            Log.d(TAG,"Get direction "+direction);
            if(direction == ItemTouchHelper.START){
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                startActivity(intent);
                VibrateUtils.vibrateForNotice(this);
                userAdapter.notifyDataSetChanged();
            }
            else {
                userAdapter.getUserList().remove(position);
                userAdapter.notifyDataSetChanged();
                showUndoSnackbar(userBriefInfo,position);
            }


        }
    }

    @Override
    public void onRecyclerViewMoved(int fromPosition, int toPosition) {
        List<forumUserBriefInfo> userBriefInfos = userAdapter.getUserList();
        if(userBriefInfos!=null){
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(userBriefInfos, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(userBriefInfos, i, i - 1);
                }
            }
            for(int i=0;i<userBriefInfos.size(); i++){
                userBriefInfos.get(i).position = i;
            }
            new UpdateBBSUserTask(userBriefInfos).execute();

        }
    }

    public class removeBBSUserTask extends AsyncTask<Void, Void, Void> {
        private forumUserBriefInfo userBriefInfo;
        private Context context;
        public removeBBSUserTask(forumUserBriefInfo userBriefInfo, Context context){
            this.userBriefInfo = userBriefInfo;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            forumUserBriefInfoDatabase.getInstance(context).getforumUserBriefInfoDao().delete(userBriefInfo);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public class UpdateBBSUserTask extends AsyncTask<Void, Void, Void> {
        private List<forumUserBriefInfo> userBriefInfos;
        private Context context;
        public UpdateBBSUserTask(List<forumUserBriefInfo> userBriefInfos){
            this.userBriefInfos = userBriefInfos;

        }
        @Override
        protected Void doInBackground(Void... voids) {

            forumUserBriefInfoDatabase.getInstance(context).getforumUserBriefInfoDao().update(userBriefInfos);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public void showUndoSnackbar(final forumUserBriefInfo userBriefInfo, final int position) {
        Log.d(TAG,"SHOW REMOVED POS "+position);
        new removeBBSUserTask(userBriefInfo,this).execute();
        Snackbar snackbar = Snackbar.make(manageUserCoordinatorLayout, getString(R.string.bbs_delete_user_info_template,userBriefInfo.username,bbsInfo.site_name),
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.bbs_undo_delete, new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                undoDelete(userBriefInfo,position);
            }
        });
        snackbar.show();
    }

    public void undoDelete(forumUserBriefInfo userBriefInfo, int position) {
        // insert to database
        userAdapter.getUserList().add(position,userBriefInfo);
        userAdapter.notifyDataSetChanged();
        new addBBSUserTask(userBriefInfo, this).execute();
    }

    public class addBBSUserTask extends AsyncTask<Void, Void, Void> {
        private forumUserBriefInfo userBriefInfo;
        private Context context;
        public addBBSUserTask(forumUserBriefInfo userBriefInfo, Context context){
            this.userBriefInfo = userBriefInfo;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            forumUserBriefInfoDatabase.getInstance(context).getforumUserBriefInfoDao().insert(userBriefInfo);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }
}