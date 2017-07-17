package lvge.com.myapp.modules.my_4s_management;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;

import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;

import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;

import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.Text;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.Date;
import java.util.Locale;

import lvge.com.myapp.R;
import lvge.com.myapp.model.AddressModel;

public class My4sAddressActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,PoiSearch.OnPoiSearchListener{

    private Double lat;
    private Double lng;
    private String address;
    private TextView currentPosition;

    private Marker attentionMark = null;

    //显示地图需要的变量
    private MapView mapView;//地图控件
    private AMap aMap;//地图对象
    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器

    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;

    private ProgressDialog progDialog = null;
    private PoiSearch.Query query;
    private String city = "";
    private int currentPage;
    private PoiSearch poiSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my4s_address);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_4s_address_management);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        TextView confirm = (TextView)findViewById(R.id.my_4s_address_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        lng = Double.parseDouble(intent.getStringExtra("lng"));
        lat = Double.parseDouble(intent.getStringExtra("lat"));
        address = intent.getStringExtra("address");

       final EditText current_position = (EditText)findViewById(R.id.current_position);

        ImageView my4s_address_search_ImageView = (ImageView)findViewById(R.id.my4s_address_search_ImageView);
        my4s_address_search_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearchQuery(current_position.getText().toString().trim());
            }
        });

        /**
         * 地图定位
         */
        //显示地图
        mapView = (MapView) findViewById(R.id.map);
        //必须要写
        mapView.onCreate(savedInstanceState);
        //获取地图对象
        aMap = mapView.getMap();

        //设置显示定位按钮 并且可以点击
        UiSettings settings = aMap.getUiSettings();
        aMap.setLocationSource(this);
        // 是否显示定位按钮
        //settings.setMyLocationButtonEnabled(true);
        // 是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);
        //定位的小图标 默认是蓝点 这里自定义一团火，其实就是一张图片
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.shop_manage_shop_address));
        myLocationStyle.radiusFillColor(android.R.color.holo_orange_dark);
        myLocationStyle.strokeColor(android.R.color.holo_orange_dark);
        aMap.setMyLocationStyle(myLocationStyle);
        //开始定位
        //设置定位监听

        initLoc();
    }

    private void initLoc() {
        if (mLocationClient != null) {
            mLocationClient.startLocation();
            return;
        }
        //初始化定位
        mLocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位回调监听
/*
        mLocationClient.setLocationListener(this);
*/
        if (address.equals("")) {
            mLocationClient.setLocationListener(this);
        } else {
            //添加图钉
            aMap.addMarker(getMarkerOptions(address));
            aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            //将地图移动到定位点
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(this.lat, this.lng)));
            currentPosition = (TextView) findViewById(R.id.current_position);
            currentPosition.setText(address);
        }
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
    /*    //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);*/

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    private MarkerOptions getMarkerOptions(String str) {
        //   AttentionModel attentionModel
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
        //  options.icon(options.getIcon());
        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.shop_manage_shop_address));
        options.title(str);
        options.position(new LatLng(lat, lng));
        //设置多少帧刷新一次图片资源
        options.period(60);
        return options;

    }

    //定位回调函数
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                lat = amapLocation.getLatitude();//获取纬度
                lng = amapLocation.getLongitude();//获取经度

                GeocodeSearch geocodeSearch = new GeocodeSearch(this);
                geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                    @Override
                    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                        String Province = regeocodeResult.getRegeocodeAddress().getProvince();
                        String City = regeocodeResult.getRegeocodeAddress().getCity();

                        String Crossroads = regeocodeResult.getRegeocodeAddress().getTowncode();
                        String Building = regeocodeResult.getRegeocodeAddress().getBuilding();
                        String FormatAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                        address = FormatAddress;

                        currentPosition = (TextView) findViewById(R.id.current_position);
                        currentPosition.setText(address);

                    }

                    @Override
                    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

                    }
                });

                LatLonPoint latLonPoint = new LatLonPoint(lat, lng);
                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);

                geocodeSearch.getFromLocationAsyn(query);


                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));

                    //获取定位信息
                }

                //点击定位按钮 能够将地图的中心移动到定位点
                mListener.onLocationChanged(amapLocation);

            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("地图错误", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }


    //激活定位
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        initLoc();
    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
        mListener = null;
        if (this.mLocationClient != null) {
            this.mLocationClient.stopLocation();
            this.mLocationClient.onDestroy();
        }
        this.mLocationClient = null;
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void doSearchQuery(String str){
        showProgressDialog();
        currentPage = 0;

        query = new PoiSearch.Query(str,"",city);
        query.setPageSize(10);
        query.setPageNum(currentPage);
        poiSearch = new PoiSearch(this,query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    private void showProgressDialog(){
        if(progDialog == null){
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索：。。。");
        progDialog.show();
    }

    private void dissmissProgressDialog(){
        if(progDialog != null){
            progDialog.dismiss();
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {

        dissmissProgressDialog();

        if(i == 1000){

        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
