package com.bigsing.fakemap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bigsing.fakemap.R;
import com.bigsing.fakemap.utils.MapConvert;
import com.bigsing.fakemap.utils.PermissionUtils;
import com.bigsing.fakemap.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by hzhuqi on 2018/5/29.
 */
public class GoogleMapFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, MyMapActivity.SearchAndLocationInterface {
    private MapView mMapView;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    private Context mContent;
    private AppCompatActivity mActivity;

    public GoogleMapFragment() {
    }

    @SuppressLint("ValidFragment")
    public GoogleMapFragment(Activity activity) {
        this.mActivity = (AppCompatActivity) activity;
        this.mContent = activity.getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_google_map, container, false);
        initView(rootView, savedInstanceState);
        return rootView;
    }

    private void initView(View rootView, Bundle mapViewBundle) {
//        Bundle mapViewBundle = null;
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
//        }
//        mMapView = (MapView) findViewById(R.id.map);
        mMapView = rootView.findViewById(R.id.google_map_view);
        mMapView.onCreate(mapViewBundle);
        mMapView.onResume();
        mMapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initClickEvent();
        enableMyLocation();
    }

    private void initClickEvent() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Toast.makeText(getApplicationContext(), "地图被点击", Toast.LENGTH_LONG).show();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in this position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Toast.makeText(getApplicationContext(), "地图被长按", Toast.LENGTH_LONG).show();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in this position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                showPositionInfo(latLng,"");
            }
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(mContent, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(mActivity, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, false);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            autoPositionToCurrentPosition();
        }
    }

    @SuppressLint("MissingPermission")
    //自动将标记移动到当前所在位置
    private void autoPositionToCurrentPosition() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //设置Criteria服务商的信息
        Criteria criteria = new Criteria();
        //经度要求
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        //取得效果最好的criteria
        String provider = locationManager.getBestProvider(criteria, true);
        //得到坐标的相关信息
        Location location = locationManager.getLastKnownLocation(provider);
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentPosition).title("My real position is here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
    }

    protected void showPositionInfo(final LatLng latLng, String posName) {
        //保存地图选点并返回
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.MyThemeGray);
        builder.setTitle(posName);
        builder.setMessage(latLng.toString());
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.saveFakeLocation(GoogleMapFragment.this, latLng.latitude, latLng.longitude);
            }
        });
        builder.create().show();
    }
    @Override
    public void doSearchInCity(String cityName) {

    }

    @Override
    public void doRequestLocation() {
        autoPositionToCurrentPosition();
    }
}
