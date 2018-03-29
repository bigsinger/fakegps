package com.bigsing.fakemap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
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

/**
 * Created by sing on 2017/4/19.
 */

public abstract class MyMapActivity extends BaseActivity {
    public static final String TAG = "MyMapActivity";

    protected String mLastCity = "";

    private SearchView searchView;
    private ImageView btn_search;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private ArrayList<ThemeColor> themeColorList = new ArrayList<>();
    private ThemeColorAdapter themeColorAdapter = new ThemeColorAdapter();

//    protected abstract void updatePosition(LatLng latLng, boolean reCenter);

@Override
public String setActName() {
    return TAG;
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initView();

        updateConfig();
        initNavigationView();

        initChangeTheme();
        isXposedActived();
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

    protected abstract void doSearchInCity(String cityName);

    //自动定位
    protected abstract void doRequestLocation();

    protected void initView() {
        searchView = (SearchView) findViewById(R.id.searchView);
        btn_search = (ImageView) findViewById(R.id.btn_search);

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
                doSearchInCity(query);
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
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
                if ((boolean) btn_search.getTag() == true) {
                    btn_search.setTag(false);
                    btn_search.setImageResource(R.drawable.ic_navigation_white_24dp);
                } else {
                    btn_search.setTag(true);
                    btn_search.setImageResource(R.drawable.ic_near_me_white_24dp);
                }
                //自动定位
                doRequestLocation();
            }
        });

    }

    protected void updateConfig(){
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        // 应用用户选择语言
        config.locale = Locale.getDefault();
        resources.updateConfiguration(config, dm);
    }

    protected void initNavigationView() {
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


    protected void initChangeTheme() {
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

    protected void isXposedActived() {
        if (XposedActive.isActive() == false) {
            Utils.toast(this, getString(R.string.xpose_not_actived));
        } else {
            Utils.toast(this, getString(R.string.xpose_actived));
        }

        //这里不弹框提示了，这样本APP还可以作为普通查看设备信息的工具使用
        if (false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyThemeGray);
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
                        Utils.openUrl(MyMapActivity.this, getString(R.string.url_home));
                        break;
                    case R.id.nav_menu_theme:
                        View view = LayoutInflater.from(MyMapActivity.this).inflate(R.layout.dialog_theme_color, null, false);
                        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.theme_recycler_view);
                        recyclerView.setLayoutManager(new GridLayoutManager(MyMapActivity.this, 4));
                        recyclerView.setAdapter(themeColorAdapter);
                        AlertDialog.Builder builder = new AlertDialog.Builder(MyMapActivity.this);
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
                // Toast.makeText(MapBaiduActivity.this,msgString,Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

}
