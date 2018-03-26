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
import com.baidu.mapapi.utils.CoordinateConverter;
import com.bigsing.fakemap.adapter.EasyRecyclerViewAdapter;
import com.bigsing.fakemap.adapter.ThemeColorAdapter;
import com.bigsing.fakemap.utils.ActivityCollector;
import com.bigsing.fakemap.utils.MapConvert;
import com.bigsing.fakemap.utils.ThemeColor;
import com.bigsing.fakemap.utils.ThemeUtils;
import com.bigsing.fakemap.utils.Utils;

import java.math.BigDecimal;
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


public class MainActivity extends BaseActivity implements OnGetPoiSearchResultListener {
    public static final String TAG = "MainActivity";

    private final static int INTERVEL = 200;
    //private GeoCoder mSearch;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private LocationManager mLocationManager;
    private String mMockProviderName = LocationManager.GPS_PROVIDER;
    private SearchView searchView;
    private ImageView btn_search;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private PoiSearch mPoiSearch = null;

    private String mLastCity = "";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ArrayList<ThemeColor> themeColorList = new ArrayList<>();
    private ThemeColorAdapter themeColorAdapter = new ThemeColorAdapter();

    @Override
    public String setActName() {
        return TAG;
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        // 应用用户选择语言
        config.locale = Locale.getDefault();
        resources.updateConfiguration(config, dm);

        initNavigationView();
        initView();


        ///////////////////////////////////////////

        initMap();
        initLocationAndOption();

        initChangeTheme();
        isXposedActived();
    }

    private void initNavigationView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.id_navigator_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setBackgroundColor(ThemeUtils.getToolBarColor());
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(Color.WHITE); //设置标题颜色
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //创建返回键，并实现打开关/闭监听
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        setupDrawerContent(mNavigationView);
        mNavigationView.setItemIconTintList(ThemeUtils.getNaviItemIconTinkList());
        View headerView = mNavigationView.getHeaderView(0);
        headerView.setBackgroundColor(ThemeUtils.getToolBarColor());
        CircleImageView sdvHeader = (CircleImageView) headerView.findViewById(R.id.sdv_avatar);
        sdvHeader.setImageResource(R.drawable.ic_avtar);
        TextView appnameTextView = (TextView) headerView.findViewById(R.id.appnameTextView);
        appnameTextView.setText(getString(R.string.header_name) + Utils.getVersionInfo(this));

        // 自己写的方法，设置NavigationView中menu的item被选中后要执行的操作
        onNavgationViewMenuItemSelected(mNavigationView);
    }

    private void initView() {
        searchView = (SearchView) findViewById(R.id.searchView);
        btn_search = (ImageView) findViewById(R.id.btn_search);
        //iv_mypos = (ImageView) findViewById(R.id.iv_mypos);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        //getGeo();


        //打开收藏列表
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //关闭收藏列表
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

        // 设置搜索文本监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null) {
                    query = "";
                }
                if (mLastCity == null) {
                    mLastCity = "";
                }
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city(mLastCity)
                        .keyword(query)
                        .pageNum(1));
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
//                    mListView.setFilterText(newText);
//                }else{
//                    mListView.clearTextFilter();
                }
                return false;
            }


        });

        //点击开始搜索
        btn_search.setTag(true);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((boolean) btn_search.getTag()== true) {
                    btn_search.setTag(false);
                    btn_search.setImageResource(R.drawable.ic_navigation_white_24dp);
                } else {
                    btn_search.setTag(true);
                    btn_search.setImageResource(R.drawable.ic_near_me_white_24dp);
                }
                //自动定位
                mLocationClient.registerLocationListener(myListener);    //注册监听函数
                mLocationClient.start();
                mLocationClient.requestLocation();
            }
        });

//        iv_mypos.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //自动定位
//                mLocationClient.registerLocationListener(myListener);    //注册监听函数
//                mLocationClient.start();
//                mLocationClient.requestLocation();
//            }
//        });
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

    private void showPositionInfo(final LatLng latLng, String posName) {
        updatePosition(latLng, false);

        //保存地图选点并返回
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyThemeGray);
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
                //MainActivity.this.finish();
                Utils.toast("地图位置已刷新~");
            }
        });
        builder.create().show();
    }


    private void updatePosition(LatLng latLng, boolean reCenter) {
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

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    /**
     * 设置NavigationView中menu的item被选中后要执行的操作
     *
     * @param mNav
     */
    private void onNavgationViewMenuItemSelected(NavigationView mNav) {
        mNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_menu_home:
                    case R.id.nav_menu_recommend:
                    case R.id.nav_menu_help:
                    case R.id.nav_menu_about:
                        Utils.openUrl(MainActivity.this, getString(R.string.url_home));
                        break;
                    case R.id.nav_menu_theme:
                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_theme_color, null, false);
                        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.theme_recycler_view);
                        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
                        recyclerView.setAdapter(themeColorAdapter);
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.title_select_theme)
                                .setView(view)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ThemeUtils.setThemeColor(getResources().getColor(themeColorList.get(themeColorAdapter.getPosition()).getColor()));// 不要变换位置
                                        ThemeUtils.setThemePosition(themeColorAdapter.getPosition());
                                        // finish();
                                        new Handler().postDelayed(new Runnable() {
                                            public void run() {
                                                ActivityCollector.getInstance().refreshAllActivity();
                                                // closeHandler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
                                            }
                                        }, 100);
                                    }
                                })
                                .show();

                        break;
                }

                // Menu item点击后选中，并关闭Drawerlayout
                menuItem.setChecked(true);
                //drawerlayoutHome.closeDrawers();
                // Toast.makeText(MainActivity.this,msgString,Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void initChangeTheme() {
        themeColorAdapter = new ThemeColorAdapter();
        themeColorList.add(new ThemeColor(R.color.theme_red_base));
        themeColorList.add(new ThemeColor(R.color.theme_blue));
        themeColorList.add(new ThemeColor(R.color.theme_blue_light));
        themeColorList.add(new ThemeColor(R.color.theme_balck));
        themeColorList.add(new ThemeColor(R.color.theme_teal));
        themeColorList.add(new ThemeColor(R.color.theme_brown));
        themeColorList.add(new ThemeColor(R.color.theme_green));
        themeColorList.add(new ThemeColor(R.color.theme_red));
        themeColorAdapter.setDatas(themeColorList);
        themeColorAdapter.setOnItemClickListener(new EasyRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, Object data) {
                for (ThemeColor themeColor : themeColorList) {
                    themeColor.setChosen(false);
                }
                themeColorList.get(position).setChosen(true);
                themeColorAdapter.notifyDataSetChanged();

            }
        });
    }

    private void isXposedActived() {
        if (XposedActive.isActive() == false) {
            Utils.toast(this, getString(R.string.xpose_not_actived));
        } else {
            Utils.toast(this, getString(R.string.xpose_actived));
        }

        //这里不弹框提示了，这样本APP还可以作为普通查看设备信息的工具使用
        if (false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyThemeGray);
            builder.setTitle(R.string.xpose_not_actived)
                    .setMessage(R.string.xpose_please_active)
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }

    }
}

