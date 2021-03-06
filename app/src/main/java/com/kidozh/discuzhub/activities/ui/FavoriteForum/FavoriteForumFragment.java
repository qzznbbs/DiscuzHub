package com.kidozh.discuzhub.activities.ui.FavoriteForum;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.FavoriteForum.FavoriteForumViewModel;
import com.kidozh.discuzhub.adapter.FavoriteForumAdapter;
import com.kidozh.discuzhub.daos.FavoriteForumDao;
import com.kidozh.discuzhub.database.FavoriteForumDatabase;
import com.kidozh.discuzhub.entities.FavoriteForum;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.BaseResult;
import com.kidozh.discuzhub.results.VariableResults;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class FavoriteForumFragment extends Fragment {
    private static final String TAG = FavoriteForumFragment.class.getSimpleName();

    private FavoriteForumViewModel mViewModel;

    private static final String ARG_BBS = "ARG_BBS";
    private static final String ARG_USER = "ARG_USER";
    private static final String ARG_IDTYPE = "ARG_IDTYPE";

    private bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;

    public FavoriteForumFragment(){

    }

    public static FavoriteForumFragment newInstance(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        FavoriteForumFragment fragment = new FavoriteForumFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BBS, bbsInfo);
        args.putSerializable(ARG_USER,userBriefInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bbsInfo = (bbsInformation) getArguments().getSerializable(ARG_BBS);
            userBriefInfo = (forumUserBriefInfo) getArguments().getSerializable(ARG_USER);
        }
    }

    FavoriteForumAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorite_thread_fragment, container, false);
    }

    @BindView(R.id.blank_favorite_thread_view)
    View blankFavoriteThreadView;
    @BindView(R.id.favorite_thread_recyclerview)
    RecyclerView favoriteThreadRecyclerview;
    @BindView(R.id.favorite_thread_swipelayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.favorite_thread_sync_progressbar)
    ProgressBar syncFavoriteThreadProgressBar;
    @BindView(R.id.blank_favorite_thread_notice)
    TextView blankFavoriteNotice;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mViewModel = new ViewModelProvider(this).get(FavoriteForumViewModel.class);
        mViewModel.setInfo(bbsInfo,userBriefInfo);
        configureRecyclerview();
        bindViewModel();
        configureSwipeRefreshLayout();
        syncFavoriteThreadFromServer();
    }

    private void configureRecyclerview(){
        favoriteThreadRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteForumAdapter();
        adapter.setInformation(bbsInfo,userBriefInfo);
        mViewModel.getFavoriteItemListData().observe(getViewLifecycleOwner(),adapter::submitList);
        favoriteThreadRecyclerview.setAdapter(adapter);
        favoriteThreadRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
    }

    private void configureSwipeRefreshLayout(){
        if(userBriefInfo!=null){
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    syncFavoriteThreadProgressBar.setVisibility(View.GONE);
                    Toasty.info(getContext(),getString(R.string.sync_favorite_forum_start,bbsInfo.site_name), Toast.LENGTH_SHORT).show();
                    mViewModel.startSyncFavoriteForum();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        else {
            swipeRefreshLayout.setEnabled(false);
        }

    }

    private void bindViewModel(){


        mViewModel.getFavoriteItemListData().observe(getViewLifecycleOwner(),favoriteThreads -> {
            if(favoriteThreads.size() == 0){
                blankFavoriteThreadView.setVisibility(View.VISIBLE);
                blankFavoriteNotice.setText(R.string.favorite_forum_not_found);
            }
            else {
                blankFavoriteThreadView.setVisibility(View.GONE);
            }
        });
        mViewModel.errorMsgContent.observe(getViewLifecycleOwner(),error->{
            if(!TextUtils.isEmpty(error)){
                Toasty.error(getContext(),error,Toast.LENGTH_SHORT).show();
            }

        });
        mViewModel.resultMutableLiveData.observe(getViewLifecycleOwner(),favoriteForumResult -> {
            if(getContext() instanceof BaseStatusInteract){
                ((BaseStatusInteract) getContext()).setBaseResult(favoriteForumResult,favoriteForumResult!=null?favoriteForumResult.FavoriteForumVariable:null);
            }
        });
    }

    public void syncFavoriteThreadFromServer(){
        if(getContext() !=null && UserPreferenceUtils.syncFavorite(getContext()) && userBriefInfo!=null){

            bindSyncStatus();

            mViewModel.startSyncFavoriteForum();

        }

    }

    private void bindSyncStatus(){
        mViewModel.totalCount.observe(getViewLifecycleOwner(), count ->{
            if(count == -1){
                syncFavoriteThreadProgressBar.setVisibility(View.VISIBLE);
                syncFavoriteThreadProgressBar.setIndeterminate(true);
            }

        });

        mViewModel.FavoriteForumInServer.observe(getViewLifecycleOwner(),favoriteForums -> {
            if(mViewModel !=null){
                int count = mViewModel.totalCount.getValue();
                if(count == -1){

                }
                else if(count > favoriteForums.size()){
                    syncFavoriteThreadProgressBar.setVisibility(View.VISIBLE);
                    syncFavoriteThreadProgressBar.setMax(count);

                    syncFavoriteThreadProgressBar.setProgress(favoriteForums.size());

                }
                else {
                    syncFavoriteThreadProgressBar.setVisibility(View.GONE);
                    //Toasty.success(getContext(),getString(R.string.sync_favorite_thread_load_all),Toast.LENGTH_LONG).show();
                }
            }
            else {
                syncFavoriteThreadProgressBar.setVisibility(View.GONE);
            }

        });

        mViewModel.newFavoriteForum.observe(getViewLifecycleOwner(),newFavoriteForums ->{
            new SaveFavoriteItemAsyncTask(newFavoriteForums).execute();
        });
    }

    private class SaveFavoriteItemAsyncTask extends AsyncTask<Void,Void,Integer>{

        private List<FavoriteForum> favoriteForumList;

        public SaveFavoriteItemAsyncTask(List<FavoriteForum> favoriteForumList) {
            this.favoriteForumList = favoriteForumList;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            FavoriteForumDao dao = FavoriteForumDatabase.getInstance(getContext()).getDao();
            // query first
            List<Integer> insertTids = new ArrayList<>();
            for(int i = 0; i< favoriteForumList.size(); i++){
                insertTids.add(favoriteForumList.get(i).idKey);
                favoriteForumList.get(i).belongedBBSId = bbsInfo.getId();
                favoriteForumList.get(i).userId = userBriefInfo!=null?userBriefInfo.getUid():0;
                //Log.d(TAG,"fav id "+favoriteForumList.get(i).favid);
            }



            List<FavoriteForum> queryList = dao.queryFavoriteItemListByfids(bbsInfo.getId()
                    ,userBriefInfo!=null?userBriefInfo.getUid():0,
                    insertTids
                    );

            for(int i=0;i<queryList.size();i++){
                int tid = queryList.get(i).idKey;
                FavoriteForum queryForum = queryList.get(i);
                for(int j = 0; j< favoriteForumList.size(); j++){
                    FavoriteForum favoriteForum = favoriteForumList.get(j);
                    if(favoriteForum.idKey == tid){
                        favoriteForum.id = queryForum.id;
                        break;
                    }
                }
            }
            // remove all synced information

            //dao.clearSyncedFavoriteItemByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0);

            dao.insert(favoriteForumList);
            return favoriteForumList.size();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }


}