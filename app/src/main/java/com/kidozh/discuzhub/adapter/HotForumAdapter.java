package com.kidozh.discuzhub.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ForumActivity;
import com.kidozh.discuzhub.activities.ViewThreadActivity;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.HotForum;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HotForumAdapter extends RecyclerView.Adapter<HotForumAdapter.HotForumViewHolder> {
    private final static String TAG = HotForumAdapter.class.getSimpleName();
    List<HotForum> hotForumList;
    Context context;

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;

    public HotForumAdapter(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
    }

    public void setHotForumList(List<HotForum> hotForumList) {
        this.hotForumList = hotForumList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new HotForumViewHolder(LayoutInflater.from(context).inflate(R.layout.item_hot_forum,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HotForumViewHolder holder, int position) {
        HotForum hotForum = hotForumList.get(position);
        holder.hotForumName.setText(hotForum.name);
        if(hotForum.todayPosts >= 100){
            holder.hotForumTodayPosts.setText(R.string.forum_today_posts_over_much);
            holder.hotForumTodayPosts.setTextColor(context.getColor(R.color.colorAlizarin));
        }
        else {
            holder.hotForumTodayPosts.setText(String.valueOf(hotForum.todayPosts));
            holder.hotForumTodayPosts.setTextColor(context.getColor(R.color.colorPrimary));
        }
        holder.lastPostTime.setText(hotForum.lastpost);
        if(hotForum.lastPostSubject.length() == 0){
            holder.lastPostSubject.setText(R.string.unset);
        }
        else {
            holder.lastPostSubject.setText(hotForum.lastPostSubject);
        }

        holder.hotForumCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForumInfo forumInfo = new ForumInfo();
                forumInfo.fid = hotForum.fid;
                forumInfo.todayPosts = hotForum.todayPosts;
                forumInfo.name = hotForum.name;
                Intent intent = new Intent(context, ForumActivity.class);
                intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,forumInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                Log.d(TAG,"put base url "+bbsInfo.base_url);
                VibrateUtils.vibrateForClick(context);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        if(hotForumList == null){
            return 0;
        }
        else {
            return hotForumList.size();
        }

    }

    public static class HotForumViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.hot_forum_cardview)
        CardView hotForumCardview;
        @BindView(R.id.item_hot_forum_name)
        TextView hotForumName;
        @BindView(R.id.item_hot_forum_today_posts)
        TextView hotForumTodayPosts;

        @BindView(R.id.hot_forum_last_post_time)
        TextView lastPostTime;
        @BindView(R.id.hot_forum_last_post_subject)
        TextView lastPostSubject;

        public HotForumViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
