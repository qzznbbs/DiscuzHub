package com.kidozh.discuzhub.activities.ui.smiley;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.SmileyAdapter;
import com.kidozh.discuzhub.utilities.bbsParseUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * interface
 * to handle interaction events.
 * Use the {@link SmileyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmileyFragment extends Fragment {
    private static final String TAG = SmileyFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SMILEY_PARAM = "SMILEY";

    SmileyAdapter adapter;

    // TODO: Rename and change types of parameters

    private List<bbsParseUtils.smileyInfo> curSmileyInfos = new ArrayList<>();

    private OnSmileyPressedInteraction mListener;

    public SmileyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SmileyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SmileyFragment newInstance(List<bbsParseUtils.smileyInfo> allSmileyInfos) {
        SmileyFragment fragment = new SmileyFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SMILEY_PARAM, (ArrayList<? extends Parcelable>) allSmileyInfos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            curSmileyInfos = getArguments().getParcelableArrayList(SMILEY_PARAM);
        }
    }

    @BindView(R.id.smiley_recyclerview)
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_smiley, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        configureRecyclerView();
    }

    void configureRecyclerView(){
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 6, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SmileyAdapter(getContext(), (v1, position) -> {
            ImageView img = (ImageView) v1;
            smileyClick(img.getDrawable(), position);
        });

        adapter.setSmileyInfos(curSmileyInfos);
        recyclerView.setAdapter(adapter);
    }

    private void smileyClick(Drawable d, int position) {

        if (position > adapter.getSmileyInfos().size()) {
            return;
        }


        String name = adapter.getSmileyInfos().get(position).code;
        Log.d(TAG,"get name "+name);
        onSmileyPressed(name,d);


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onSmileyPressed(String str, Drawable a) {
        if (mListener != null) {
            mListener.onSmileyPress(str, a);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSmileyPressedInteraction) {
            mListener = (OnSmileyPressedInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSmileyPressedInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSmileyPressedInteraction {
        // TODO: Update argument type and name
        void onSmileyPress(String str, Drawable a);
    }
}
