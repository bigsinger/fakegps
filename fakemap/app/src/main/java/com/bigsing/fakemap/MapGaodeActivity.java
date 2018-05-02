package com.bigsing.fakemap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupWindow;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.bigsing.fakemap.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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


public class MapGaodeActivity extends MyMapActivity implements CompoundButton.OnCheckedChangeListener
        , LocateRecyclerAdapter.OnLocationItemClick, AMapLocationListener, PoiSearch.OnPoiSearchListener, LocationSource {
    public static final String TAG = "MapGaodeActivity";
    RecyclerView mLocateRecycler;
    private com.amap.api.maps2d.MapView mapView;
    private com.amap.api.maps2d.AMap aMap;
    //定位服务类。此类提供单次定位、持续定位、地理围栏、最后位置相关功能
    private AMapLocationClient aMapLocationClient;
    private LocationSource.OnLocationChangedListener listener;
    //定位参数设置
    private AMapLocationClientOption aMapLocationClientOption;
    private PopupWindow popupWindow;
    private List<LocationInfo> mPoiList;
    private LocateRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapView = (com.amap.api.maps2d.MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);  //必须写
        aMap = mapView.getMap();
        //设置地图类型
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);

        MyLocationStyle locationStyle = new MyLocationStyle();
        //locationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.start));
        locationStyle.strokeColor(Color.BLUE);
        locationStyle.strokeWidth(5);
        aMap.setMyLocationStyle(locationStyle);

        aMap.moveCamera(CameraUpdateFactory.zoomTo(12));

        // 设置定位监听
        aMap.setLocationSource(this);
        aMap.getUiSettings().setScaleControlsEnabled(true);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式，参见类AMap。
        //aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        // 设置为true表示系统定位按钮显示并响应点击，false表示隐藏，默认是false
        aMap.setMyLocationEnabled(true);

        // 点击地图的时候，地图选点
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // 设置当前地图显示为当前位置
//                aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("当前位置");
                markerOptions.visible(true);
                aMap.getMapScreenMarkers().clear();
                aMap.addMarker(markerOptions);


                PoiSearch.Query query = new PoiSearch.Query("", "", "");
                query.setPageSize(20);
                PoiSearch search = new PoiSearch(MapGaodeActivity.this, query);
                search.setBound(new PoiSearch.SearchBound(new LatLonPoint(latLng.latitude, latLng.longitude), 10000));
                search.setOnPoiSearchListener(MapGaodeActivity.this);
                search.searchPOIAsyn();
            }
        });

        // 长按选定
        aMap.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                showPositionInfo(latLng, "");
            }
        });

        aMapLocationClient = new AMapLocationClient(getApplicationContext());
        aMapLocationClient.setLocationListener(this);

        //初始化定位参数
        aMapLocationClientOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        aMapLocationClientOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        aMapLocationClientOption.setOnceLocation(true);
        //设置是否强制刷新WIFI，默认为强制刷新
        aMapLocationClientOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        aMapLocationClientOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        aMapLocationClientOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        aMapLocationClient.setLocationOption(aMapLocationClientOption);
        //启动定位
        aMapLocationClient.startLocation();
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void OnLocationClick(RecyclerView parent, View view, int position, LocationInfo info) {
        Log.d(TAG, String.format("选择了：%02d la: %f lo: %f %s", position, info.getLatitude(), info.getLonTitude(), info.getAddress()));
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        saveFakeLocation(MapGaodeActivity.this, info.getLatitude(), info.getLonTitude());
    }


    @Override
    protected void doSearchInCity(String cityName) {

    }

    @Override
    protected void doRequestLocation() {

    }

    @Override
    public void onPoiSearched(PoiResult result, int i) {
        mPoiList = new ArrayList<>();
//        PoiSearch.Query query = result.getQuery();
        ArrayList<PoiItem> pois = result.getPois();
        for (PoiItem poi : pois) {
            String name = poi.getCityName();
            String snippet = poi.getSnippet();
            LocationInfo info = new LocationInfo();
            info.setAddress(snippet);
            LatLonPoint point = poi.getLatLonPoint();

            info.setLatitude(point.getLatitude());
            info.setLonTitude(point.getLongitude());
            mPoiList.add(info);
            Log.d("onPoiSearched: ", snippet);
        }

        showPopupWindow();
//        mAdapter.notifyDataSetChanged();
    }

    private void showPopupWindow() {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.popupwindow, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.locate_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new LocateRecyclerAdapter(this, mPoiList);
        mAdapter.setLocationItemClick(this);
        recyclerView.setAdapter(mAdapter);
        popupWindow = new PopupWindow(mToolbar, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(btn_autoLocate);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
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
//                LatLng desLatLng = MapConvert.convertBaiduToGPS(latLng);
                SharedPreferences preferences = getSharedPreferences(Constant.TAG, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("baidulatitude", latLng.latitude + "");
                editor.putString("baidulongitude", latLng.longitude + "");
//                editor.putString("latitude", desLatLng.latitude + "");
//                editor.putString("longitude", desLatLng.longitude + "");
                editor.commit();
                //MapBaiduActivity.this.finish();
                Utils.toast("地图位置已刷新~");
            }
        });
        builder.create().show();
    }

    protected void updatePosition(LatLng latLng, boolean reCenter) {
    }

    /**
     * 定位回调监听，当定位完成后调用此方法
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (listener != null && aMapLocation != null) {
            listener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                aMapLocation.getLatitude();//获取经度
                aMapLocation.getLongitude();//获取纬度;
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
                aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果
                aMapLocation.getCountry();//国家信息
                aMapLocation.getProvince();//省信息
                aMapLocation.getCity();//城市信息
                aMapLocation.getDistrict();//城区信息
                aMapLocation.getRoad();//街道信息
                aMapLocation.getCityCode();//城市编码
                aMapLocation.getAdCode();//地区编码

//                lat = aMapLocation.getLatitude();
//                lon = aMapLocation.getLongitude();
//                Log.v("pcw", "lat : " + lat + " lon : " + lon);
//                Log.v("pcw", "Country : " + aMapLocation.getCountry() + " province : " + aMapLocation.getProvince() + " City : " + aMapLocation.getCity() + " District : " + aMapLocation.getDistrict());


            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("Tomato", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }

    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        listener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        aMapLocationClient.stopLocation();//停止定位
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        //销毁定位客户端
        if (aMapLocationClient != null) {
            aMapLocationClient.onDestroy();
            aMapLocationClient = null;
            aMapLocationClientOption = null;
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}

