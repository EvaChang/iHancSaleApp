package com.example.hx.ihanc;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.hx.ihanc.dummy.DummyContent;
import com.example.hx.ihanc.dummy.DummyContent.DummyItem;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class creditFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private List<Credit> mCredits=new ArrayList<Credit>();
    private int page=1;
    private MycreditRecyclerViewAdapter adpter;
    private  RecyclerView recyclerView;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public creditFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static creditFragment newInstance(int columnCount) {
        creditFragment fragment = new creditFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
       // Log.d("credit","creditFragment newInstance");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Log.d("credit","onCreate");
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        mListener=new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(Credit item) {
              CreditDetailDialog creditDetailDialog=CreditDetailDialog.newInstance(item.getMemberId(),item.getName()+"--"+item.getCreditSum());
              creditDetailDialog.show(getFragmentManager(),"credit");
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_credit_list, container, false);
        context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.creditListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adpter=new MycreditRecyclerViewAdapter(mCredits, mListener);
        recyclerView.setAdapter(adpter);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page=1;
                initCreditData();
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        initCreditData();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState==RecyclerView.SCROLL_STATE_IDLE){
                    int lastVisiblePosition;
                    RecyclerView.LayoutManager layoutManager=recyclerView.getLayoutManager();
                    lastVisiblePosition= ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    if (layoutManager.getChildCount()>0             //当当前显示的item数量>0
                            &&lastVisiblePosition>=layoutManager.getItemCount()-1           //当当前屏幕最后一个加载项位置>=所有item的数量
                            &&layoutManager.getItemCount()>layoutManager.getChildCount()) { // 当当前总Item数大于可见Item数
                        swipeRefreshLayout.setRefreshing(true);
                        recyclerView.setNestedScrollingEnabled(false);
                        initCreditData();
                    }
                }
            }
        });
        return view;
    }

    public void initCreditData(){
        Log.d("credit","init");
        RequestParams params=new RequestParams();
        params.put("page",page);
        params.put("search","");
        params.put("order","");
        IhancHttpClient.get("/index/sale/credit", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(page==1){mCredits.clear();adpter.isLast=false;}
                String res=new String(responseBody);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject resJson=new JSONObject(res);
                    JSONArray list=resJson.getJSONArray("credit");
                    int size=resJson.getInt("total_count");
                    if (size-list.length()-mCredits.size()<=0) adpter.isLast=true;
                    for (int i = 0; i <list.length() ; i++) {
                        JSONObject itemJSON=list.getJSONObject(i);
                        Credit item=new Credit(
                                itemJSON.getString("member_name"),itemJSON.getInt("sum"),itemJSON.getInt("member_id"));
                        mCredits.add(item);
                    }
                    page++;
                    adpter.notifyDataSetChanged();
                    recyclerView.setNestedScrollingEnabled(true);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       // Log.d("credit","start");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Credit item);
    }


}
