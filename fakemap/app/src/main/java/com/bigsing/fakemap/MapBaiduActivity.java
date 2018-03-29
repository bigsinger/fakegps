package com.bigsing.fakemap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
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
import com.bigsing.fakemap.adapter.EasyRecyclerViewAdapter;
import com.bigsing.fakemap.adapter.ThemeColorAdapter;
import com.bigsing.fakemap.utils.ActivityCollector;
import com.bigsing.fakemap.utils.MapConvert;
import com.bigsing.fakemap.utils.ThemeColor;
import com.bigsing.fakemap.utils.ThemeUtils;
import com.bigsing.fakemap.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/*
- 模拟定位虚拟坐标位置，使用百度地图SDK选择地图坐标。可以伪装任意位置，对微信,QQ,陌陌等众多软件有效。

使用方法：
- 安装APP
- 在Xposed框架中启用本模块
- 重启手机
- 随便选择一个地图POI坐标


- Fake My GPS allows you to select a custom GPS location that will be seen by apps of your choosing.

How to use :
- Install app.
- Enable module in xposed framework.
- Open app and select a new place.
- Reboot .
*/


public class MapBaiduActivity extends MyMapActivity implements OnGetPoiSearchResultListener {
    public static final String TAG = "MapBaiduActivity";

    private final static int INTERVEL = 200;
    //private GeoCoder mSearch;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private LocationManager mLocationManager;
    private String mMockProviderName = LocationManager.GPS_PROVIDER;

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private PoiSearch mPoiSearch = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        initMap();
        initLocationAndOption();
    }

    private void initLocationAndOption() {
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类

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

    @SuppressLint("NewApi")
    private void initMap() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        //获取屏幕高度宽度
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //去掉logo
        mMapView.removeViewAt(1);
        // 设置启用内置的缩放控件
        mMapView.showZoomControls(true);

        // 设置地图模式为交通地图
        mBaiduMap.setTrafficEnabled(true);

//        UiSettings uiSettings = mBaiduMap.getUiSettings();
//        uiSettings.setCompassEnabled(true);
//        uiSettings.setAllGesturesEnabled(true);
//        uiSettings.setOverlookingGesturesEnabled(true);
//        uiSettings.setRotateGesturesEnabled(true);
//        uiSettings.setScrollGesturesEnabled(true);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.ic_near_me_white_24dp);
//        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
//        mBaiduMap.setMyLocationConfigeration(config);
        //带方向的小圆点
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));


        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                // TODO Auto-generated method stub
                LatLng target = mBaiduMap.getMapStatus().target;
                System.out.println(target.toString());
                //mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(target));
            }

            @Override
            public void onMapStatusChange(MapStatus arg0) {
                // TODO Auto-generated method stub

            }
        });

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

    @Override
    protected void doSearchInCity(String cityName) {
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(mLastCity)
                .keyword(cityName)
                .pageNum(1));
    }

    @Override
    protected void doRequestLocation() {
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    protected void showPositionInfo(final LatLng latLng, String posName) {
        updatePosition(latLng, false);

        //保存地图选点并返回
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyThemeGray);
        builder.setTitle(posName);
        builder.setMessage(latLng.toString());
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LatLng desLatLng = MapConvert.convertBaiduToGPS(latLng);
                SharedPreferences preferences = getSharedPreferences(Constant.TAG, Context.MODE_WORLD_READABLE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("baidulatitude", latLng.latitude + "");
                editor.putString("baidulongitude", latLng.longitude + "");
                editor.putString("latitude", desLatLng.latitude + "");
                editor.putString("longitude", desLatLng.longitude + "");
                editor.commit();
                //MapBaiduActivity.this.finish();
                Utils.toast("地图位置已刷新~");
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

//        mBaiduMap.addOverlay(new MarkerOptions().position(latLng)
//                .icon(BitmapDescriptorFactory
//                .fromResource(R.drawable.icon_mark)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mLocationClient.stop();
        //mSearch.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

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
            Utils.toast(this, poiDetailResult.getName() + ": " + poiDetailResult.getAddress());
        } else {
            Utils.toast(this, "未找到结果");
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    /**
     * 停止模拟位置，以免启用模拟数据后无法还原使用系统位置
     * 若模拟位置未开启，则removeTestProvider将会抛出异常；
     * 若已addTestProvider后，关闭模拟位置，未removeTestProvider将导致系统GPS无数据更新；
     */
    public void stopMockLocation(Context context) {
        int mock_enable = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0);
        if (mock_enable == 0) return;
        try {
            mLocationManager.clearTestProviderEnabled(mMockProviderName);
            mLocationManager.removeTestProvider(mMockProviderName);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        try {
            //关闭 允许模拟地点
            Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0);
        } catch (Exception e) {
            Log.e(TAG, "write error", e);
        }
    }


    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                Toast.makeText(getApplicationContext(), "获取位置信息失败", Toast.LENGTH_LONG).show();
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
}

