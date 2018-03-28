package com.bigsing.fakemap;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.model.MyLocationStyle;

import java.text.SimpleDateFormat;
import java.util.Date;

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


public class MapGaodeActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener
        , AMapLocationListener, LocationSource {
    public static final String TAG = "MapGaodeActivity";
    private com.amap.api.maps2d.MapView mapView;
    private com.amap.api.maps2d.AMap aMap;
    //定位服务类。此类提供单次定位、持续定位、地理围栏、最后位置相关功能
    private AMapLocationClient aMapLocationClient;
    private LocationSource.OnLocationChangedListener listener;
    //定位参数设置
    private AMapLocationClientOption aMapLocationClientOption;

    @Override
    public String setActName() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // 设置定位监听
        aMap.setLocationSource(this);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式，参见类AMap。
        //aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        // 设置为true表示系统定位按钮显示并响应点击，false表示隐藏，默认是false
        aMap.setMyLocationEnabled(true);

        aMapLocationClient = new AMapLocationClient(getApplicationContext());
        aMapLocationClient.setLocationListener(this);

        //初始化定位参数
        aMapLocationClientOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        aMapLocationClientOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        aMapLocationClientOption.setOnceLocation(false);
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

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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

}

