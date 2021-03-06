package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ForumActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kidozh.discuzhub.utilities.networkUtils.getPreferredClient;

public class ForumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String TAG = ForumAdapter.class.getSimpleName();
    private Context mContext;
    private List<ForumInfo> forumInfoList;
    private String jsonString;
    private bbsInformation bbsInfo;
    private forumUserBriefInfo curUser;

    ForumAdapter(Context context, String jsonObject, bbsInformation bbsInformation, forumUserBriefInfo curUser){
        this.mContext = context;
        this.jsonString = jsonObject;
        this.bbsInfo = bbsInformation;
        this.curUser = curUser;
    }

    public void setForumInfoList(List<ForumInfo> forumInfoList) {
        this.forumInfoList = forumInfoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;


        if(UserPreferenceUtils.conciseRecyclerView(context)){
            View view = inflater.inflate(R.layout.item_forum_concise, parent, shouldAttachToParentImmediately);
            return new ConciseForumViewHolder(view);
        }
        else {
            View view = inflater.inflate(R.layout.item_forum, parent, shouldAttachToParentImmediately);
            return new ForumViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderRaw, int position) {

        ForumInfo forum = forumInfoList.get(position);
        if(holderRaw instanceof ConciseForumViewHolder){
            // support rich text
            ConciseForumViewHolder holder = (ConciseForumViewHolder) holderRaw;
            Spanned sp = Html.fromHtml(forum.name,null,null);
            holder.mForumName.setText(sp, TextView.BufferType.SPANNABLE);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(this.mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

            Glide.with(mContext)
                    .load(forum.iconUrl)
                    .apply(RequestOptions
                            .placeholderOf(R.drawable.ic_forum_24px)
                            .error(R.drawable.ic_forum_24px))
                    .into(holder.mBBSForumImage);

            holder.mCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ForumActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,forum);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                    Log.d(TAG,"put base url "+bbsInfo.base_url);
                    VibrateUtils.vibrateForClick(mContext);
                    mContext.startActivity(intent);
                }
            });
            if(forum.todayPosts != 0){
                holder.mTodayPosts.setVisibility(View.VISIBLE);
                if(forum.todayPosts >= 100){
                    holder.mTodayPosts.setText(R.string.forum_today_posts_over_much);
                    holder.mTodayPosts.setBackgroundColor(mContext.getColor(R.color.colorAlizarin));
                }
                else {
                    holder.mTodayPosts.setText(String.valueOf(forum.todayPosts));
                    holder.mTodayPosts.setBackgroundColor(mContext.getColor(R.color.colorPrimary));
                }

            }
            else {
                holder.mTodayPosts.setVisibility(View.GONE);
            }
        }
        else {
            ForumViewHolder holder = (ForumViewHolder) holderRaw;
            Spanned sp = Html.fromHtml(forum.name,null,null);
            holder.mForumName.setText(sp, TextView.BufferType.SPANNABLE);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(this.mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

            Glide.with(mContext)
                    .load(forum.iconUrl)
                    .apply(RequestOptions
                            .placeholderOf(R.drawable.ic_forum_24px)
                            .error(R.drawable.ic_forum_24px))
                    .into(holder.mBBSForumImage);

            holder.mCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ForumActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,forum);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                    Log.d(TAG,"put base url "+bbsInfo.base_url);
                    VibrateUtils.vibrateForClick(mContext);
                    mContext.startActivity(intent);
                }
            });
            if(forum.todayPosts != 0){
                holder.mTodayPosts.setVisibility(View.VISIBLE);
                if(forum.todayPosts >= 100){
                    holder.mTodayPosts.setText(R.string.forum_today_posts_over_much);
                    holder.mTodayPosts.setTextColor(mContext.getColor(R.color.colorAlizarin));
                }
                else {
                    holder.mTodayPosts.setText(String.valueOf(forum.todayPosts));
                    holder.mTodayPosts.setTextColor(mContext.getColor(R.color.colorPrimary));
                }

            }
            else {
                holder.mTodayPosts.setVisibility(View.GONE);
            }
            // description
            sp = Html.fromHtml(forum.description);
            SpannableString spannableString = new SpannableString(sp);
            holder.mDescription.setText(spannableString, TextView.BufferType.SPANNABLE);

        }



    }

    @Override
    public int getItemCount() {
        if(forumInfoList == null){
            return 0;
        }
        else {
            return forumInfoList.size();
        }

    }
    
    public class ForumViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_forum_imageview)
        ImageView mBBSForumImage;
        @BindView(R.id.bbs_forum_name)
        TextView mForumName;
        @BindView(R.id.bbs_forum_cardview)
        CardView mCardview;
        @BindView(R.id.bbs_forum_today_posts)
        TextView mTodayPosts;
        @BindView(R.id.bbs_forum_description)
        TextView mDescription;
        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }



    public class ConciseForumViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_forum_imageview)
        ImageView mBBSForumImage;
        @BindView(R.id.bbs_forum_name)
        TextView mForumName;
        @BindView(R.id.bbs_forum_cardview)
        CardView mCardview;
        @BindView(R.id.bbs_forum_today_posts)
        TextView mTodayPosts;
        public ConciseForumViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
