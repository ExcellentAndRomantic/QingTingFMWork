package com.lxqhmlwyh.qingtingfm.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lxqhmlwyh.qingtingfm.R;
import com.lxqhmlwyh.qingtingfm.adapter.SearchRecyclerViewAdapter;
import com.lxqhmlwyh.qingtingfm.databaseentities.ProvinceKeyTable;
import com.lxqhmlwyh.qingtingfm.entities.FMCardViewJson;
import com.lxqhmlwyh.qingtingfm.service.GetFMItemJsonService;
import com.lxqhmlwyh.qingtingfm.service.InitDataService;
import com.lxqhmlwyh.qingtingfm.utils.DataBaseUtil;
import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchFragment extends Fragment {

    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PopupWindow popupWindow;
    private View chooseDialog;
    private ListView provinceList;
    private TextView tvProvince;
    private RelativeLayout maskLayout;
    private RecyclerView fmRecyclerView;
    private JSONArray provinceData;
    private SearchRecyclerViewAdapter adapter;
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, null);
        this.view = view;
        initView();
        initDialog();
        showFM();
        return view;
    }

    private void initView() {
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        tvProvince = view.findViewById(R.id.tv_province);
        fmRecyclerView=view.findViewById(R.id.fm_recycler_view);
        maskLayout = view.findViewById(R.id.fragment_search_mask);
        searchView=view.findViewById(R.id.search_view);
        maskLayout.setOnClickListener(onClickListener);
        swipeRefreshLayout.setEnabled(false);
        /*swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });*/
        view.findViewById(R.id.choose_province).setOnClickListener(onClickListener);
    }

    /**
     * 初始化dialog
     */
    private void initDialog() {
        popupWindow = new PopupWindow();
        chooseDialog = View.inflate(getActivity(), R.layout.choose_province_dialog, null);
        popupWindow.setContentView(chooseDialog);
        //popupWindow.setFocusable(true);
        provinceList = chooseDialog.findViewById(R.id.dialog_list_view);
        final List<String> provinces = new ArrayList<>();
        provinceData = InitDataService.getDistrict();
        if(provinceData==null){
            Toast.makeText(getActivity(), "获取地区数据失败", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < provinceData.length(); i++) {
            try {
                JSONObject tempStr = provinceData.getJSONObject(i);
                provinces.add(tempStr.getString("title"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, provinces);
        provinceList.setAdapter(adapter);
        provinceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //列表点击事件
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                tvProvince.setText(provinces.get(i));
                Intent intent=new Intent(getActivity(),GetFMItemJsonService.class);
                try {
                    JSONObject district=provinceData.getJSONObject(i);
                    intent.putExtra("provinceId",district.getInt("id"));
                    updateRecord(district.getString("title"));
                    getActivity().startService(intent);
                    //showFM();
                    updateFM();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dismissDialog();
            }
        });
    }

    /**
     * 更新数据
     */
    private void updateRecord(String provinceName){
        //Toast.makeText(getActivity(), provinceName, Toast.LENGTH_SHORT).show();
        Iterator tables= ProvinceKeyTable.findAll(ProvinceKeyTable.class);
        long nowTimeStamp=System.currentTimeMillis();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(new Date(nowTimeStamp));

        while (tables.hasNext()){
            ProvinceKeyTable tableObj= (ProvinceKeyTable) tables.next();
            long thisTimeStamp=tableObj.getStamp();//获取数据库的时间戳
            Calendar thisCalendar=Calendar.getInstance();
            thisCalendar.setTime(new Date(thisTimeStamp));
            if (DataBaseUtil.isToday(calendar,thisCalendar)){//如果是同一天
                if (tableObj.getProvince().equals(provinceName)){
                    Log.e("updateRecord","更新今天访问"+provinceName+"的次数");
                    int count=tableObj.getCount()+1;
                    SugarRecord sugarRecord=tableObj;
                    long id=sugarRecord.getId();
                    SugarRecord.executeQuery("update PROVINCE_KEY_TABLE set count=? where id=?",count+"",id+"");
                    return;
                }

            }else{
                continue;
            }
        }

        ProvinceKeyTable table=new ProvinceKeyTable();
        table.setCount(1);
        table.setProvince(provinceName);
        table.setStamp(nowTimeStamp);
        table.save();
        Log.e("updateRecord","增加一条地区记录----"+table.toString());

    }


    /**
     * 显示FM
     */
    private void showFM(){
        JSONArray fmItemJson= GetFMItemJsonService.getLastGetJson();
        if (fmItemJson==null){
            Toast.makeText(getActivity(), "获取电台数据失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Gson gson=new Gson();
        List<FMCardViewJson> list=
                gson.fromJson(fmItemJson.toString(), new TypeToken<List<FMCardViewJson>>(){}.getType());
        adapter=new SearchRecyclerViewAdapter(getActivity(),list);
        GridLayoutManager manager=new GridLayoutManager(getActivity(),3);
        fmRecyclerView.setLayoutManager(manager);
        fmRecyclerView.setAdapter(adapter);
    }

    private void updateFM(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.upData();
                    }
                });
            }
        },1000);

    }

    /**
     * 显示dialog
     */
    private void showChooseProvinceWindow() {
        if (popupWindow.isShowing()) return;
        maskLayout.setVisibility(View.VISIBLE);
        maskLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.anim_choose_dialog_mask_after));
        popupWindow.setWidth(view.getLayoutParams().width);
        popupWindow.setHeight(view.getHeight() / 2);
        popupWindow.showAsDropDown(view);
        chooseDialog.findViewById(R.id.close_choose_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View btnView) {
            switch (btnView.getId()) {
                case R.id.fragment_search_mask:
                    dismissDialog();
                    break;
                case R.id.choose_province:
                    showChooseProvinceWindow();
                    break;
            }
        }
    };

    /**
     * 遮罩层
     */
    /*private void backgroundAlpha(float f) {
        maskLayout.setAlpha(f);
        maskLayout.setVisibility(View.VISIBLE);
    }*/

    /**
     * 关闭dialog
     */
    public void dismissDialog() {
        popupWindow.dismiss();
        //backgroundAlpha(1);
        maskLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.anim_choose_dialog_mask_before));
        maskLayout.setVisibility(View.GONE);
    }
}
