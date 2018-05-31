package com.bigsing.fakemap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.bigsing.fakemap.utils.MapConvert;
import com.bigsing.fakemap.utils.Utils;

/**
 * Created by hzhuqi on 2018/5/29.
 */
public class BaiduMapFragment extends Fragment implements MyMapActivity.SearchAndLocationInterface {
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new BaiduMapFragment.MyLocationListener();
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private PoiSearch mPoiSearch = null;
    private Context mContent;
    private AppCompatActivity mActivity;
    private String mLastCity;

    public BaiduMapFragment() {
    }

    @SuppressLint("ValidFragment")
    public BaiduMapFragment(Activity activity) {
        this.mActivity = (AppCompatActivity) activity;
        this.mContent = activity.getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        SDKInitializer.initialize(mContent);
        View rootView = inflater.inflate(R.layout.fragment_baidu_map, container, false);
        initView(rootView, savedInstanceState);
        initData();
        return rootView;
    }

    private void initView(View rootView, Bundle mapViewBundle) {
        mMapView = rootView.findViewById(R.id.baidu_map_view);
        initMap();
    }

    private void initMap() {
        mBaiduMap = mMapView.getMap();
        mMapView.showZoomControls(true);
        mBaiduMap.setTrafficEnabled(true);// 设置地图模式为交通地图
        mBaiduMap.setMyLocationEnabled(true);
        initClickEvent();
        //搜索相关
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    Utils.toast("未找到结果");
                    return;
                } else if (poiResult.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
                    Utils.toast("关键词不明确，未找到位置");
                } else if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    PoiInfo pi = poiResult.getAllPoi().get(0);
                    updatePosition(pi.location, true);
                    return;
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                if (poiDetailResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    Utils.toast(poiDetailResult.getName() + ": " + poiDetailResult.getAddress());
                } else {
                    Utils.toast("未找到结果");
                }
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });
    }

    private void initClickEvent() {
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                updatePosition(latLng, false);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                showPositionInfo(mapPoi.getPosition(), mapPoi.getName());
                return false;
            }
        });

        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                showPositionInfo(latLng, "");
            }
        });
    }

    private void initData() {
        autoPositionToCurrentPosition();//自动定位到手机所在位置
    }


    private void autoPositionToCurrentPosition() {
        mLocationClient = new LocationClient(mContent);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        option.setOpenGps(true); //是否打开gps
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02

        //当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。调用requestLocation( )后，每隔设定的时间，定位SDK就会进行一次定位。
        //当不设此项，或者所设的整数值小于1000（ms）时，采用一次定位模式。
        option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        SharedPreferences preferences = MyApp.getSharedPreferences();
        String latitude = preferences.getString("baidulatitude", "");
        String longitude = preferences.getString("baidulongitude", "");
        if (TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) {
            //自动定位
            mLocationClient.registerLocationListener(myListener);    //注册监听函数
            mLocationClient.start();
            mLocationClient.requestLocation();
        } else {
            //定位到上次选择的位置
            updatePosition(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), true);
        }
    }

    protected void showPositionInfo(final LatLng latLng, String posName) {
        updatePosition(latLng, false);
        //保存地图选点并返回
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.MyThemeGray);
        builder.setTitle(posName);
        builder.setMessage(latLng.toString());
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LatLng desLatLng = MapConvert.convertBaiduToGPS(latLng);
                Utils.saveFakeLocation(BaiduMapFragment.this, desLatLng.latitude, desLatLng.longitude);
            }
        });
        builder.create().show();
    }

    protected void updatePosition(LatLng latLng, boolean reCenter) {
        mBaiduMap.clear();
        // 只是完成了定位
        MyLocationData locData = new MyLocationData.Builder().latitude(latLng.latitude)
                .longitude(latLng.longitude).build();
        //设置图标在地图上的位置
        mBaiduMap.setMyLocationData(locData);
        mLastCity="";
        if (reCenter == true) {
//            //获得百度地图状态
//            MapStatus.Builder builder = new MapStatus.Builder();
//            builder.target(latLng);
//            //设置缩放级别 16对应比例尺200米
//            builder.zoom(16);
//            MapStatus mapStatus = builder.build();
//            MapStatusUpdate m = MapStatusUpdateFactory.newMapStatus(mapStatus);
//            mBaiduMap.setMapStatus(m);
            // 开始移动百度地图的定位地点到中心位置
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(latLng, 16);
            mBaiduMap.animateMapStatus(u);
        }
    }

    @Override
    public void doSearchInCity(String cityName) {
        if(mLastCity==null||mLastCity.equals(""))
            mLastCity="杭州";
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(mLastCity)
                .keyword(cityName)
                .pageNum(1));
    }

    @Override
    public void doRequestLocation() {
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
        mLocationClient.requestLocation();
    }


    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                Toast.makeText(mContent, "获取位置信息失败", Toast.LENGTH_LONG).show();
                return;
            }
            mLastCity = location.getCity();
            // 只是完成了定位
            MyLocationData locData = new MyLocationData.Builder().latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            //设置图标在地图上的位置
            mBaiduMap.setMyLocationData(locData);
            // 开始移动百度地图的定位地点到中心位置
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16.0f);
            mBaiduMap.animateMapStatus(u);
            //定位成功后关闭定位
            mLocationClient.stop();
            //取消监听函数。
            mLocationClient.unRegisterLocationListener(myListener);
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }


}
