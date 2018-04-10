package yyl.diyapp.com.mytexi.common.Lbs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;

import java.util.HashMap;
import java.util.Map;

import yyl.diyapp.com.mytexi.common.Lbs.ILbsLayer;
import yyl.diyapp.com.mytexi.common.utils.LogUtil;
import yyl.diyapp.com.mytexi.common.utils.SensorEventHelper;
import yyl.diyapp.com.mytexi.main.view.MainActivity;

import static android.R.attr.rotation;

/**
 * Created by lsh on 2018/4/10.
 */

public class GaodeLbsLayerImpl implements ILbsLayer {
    private static final String TAG = "GaodeLbsLayerImpl";
    private static final int KEY_MY_MARKER = 1000;
    private Context context;
    //位置定位对象
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    //地图试对象
    private MapView mapView;
    //地图管理对象
    private AMap aMap;
    //地图位置变化回电对象
    private LocationSource.OnLocationChangedListener mMapLocationChangeListener;
    private boolean firstLocation = true;
    //方向传感器对象
    private SensorEventHelper mSensorHelper;
    //自定义位置变化监听对象
    private CommonLocationChangeListener mLocationChangeListener;
    //定位点样式
    private MyLocationStyle myLocationStyle;
    //管理地图标记集合
    private Map<Integer, Marker> markerMap = new HashMap<>();
    private Marker mLocMarker;


    public GaodeLbsLayerImpl(Context context) {
        //创建地图对象
        mapView = new MapView(context);
        //创建地图管理对象
        aMap = mapView.getMap();
        //创建定位对象
        mlocationClient = new AMapLocationClient(context);
        mLocationOption = new AMapLocationClientOption();
        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        //传感器对象
        mSensorHelper = new SensorEventHelper(context);
        mSensorHelper.registerSensorListener();
        this.context = context;
    }

    @Override
    public View getMapView() {
        return mapView;
    }

    @Override
    public void setLocationChangeListener(CommonLocationChangeListener changeListener) {
        mLocationChangeListener = changeListener;
    }

    @Override
    public void setLocationRes(int res) {
        myLocationStyle = new MyLocationStyle();
        //设置小蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(res));
        //设置原型的边框颜色
        myLocationStyle.strokeColor(Color.BLACK);
        //设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));
        myLocationStyle.strokeWidth(1.0f);


    }

    @Override
    public void addOrUpdateMarker(LocationInfo locationInfo, Bitmap bitmap) {
        Marker storedmarker = markerMap.get(locationInfo.getId());
        LatLng latLng = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());
        //判断集合中是否已经有该marker
        if (storedmarker != null) {
            //如果已经村在, 则更新位置 , 角度
            storedmarker.setPosition(latLng);
            storedmarker.setRotateAngle(locationInfo.getRotation());
        } else {
            //如果不存在则创建
            MarkerOptions options = new MarkerOptions();
            BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bitmap);
            options.icon(des);
            options.anchor(0.5f, 0.5f);
            options.position(latLng);
            Marker marker = aMap.addMarker(options);
            marker.setRotateAngle(rotation);
            markerMap.put(locationInfo.getId(), marker);
            if (locationInfo.getId() == KEY_MY_MARKER) {
                //使用传感器控制marker 方向
                mSensorHelper.setCurrentMarker(marker);
            }

        }
    }

    @Override
    public void onCreate(Bundle state) {
        mapView.onCreate(state);
        //地图初始化
        setUpMap();
    }

    private void setUpMap() {
        if (myLocationStyle != null) {
            aMap.setMyLocationStyle(myLocationStyle);
        }
        //设置地图激活(加载监听)
        aMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                mMapLocationChangeListener = onLocationChangedListener;
                LogUtil.d(TAG , "activate");
            }

            @Override
            public void deactivate() {
                if(mlocationClient != null){
                    mlocationClient.startLocation();
                    mlocationClient.onDestroy();
                }
                mlocationClient = null ;
            }
        });
        //设置默认定位按钮是否显示 , 这里先不向业务使用方开放
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
    }

    public void setUpLocation(){
        //设置监听器
        mlocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                //定位位置变化
                if(mMapLocationChangeListener  != null){
                    mMapLocationChangeListener.onLocationChanged(aMapLocation);
                    LogUtil.d(TAG , "onLocationChanged");
                    LocationInfo locationInfo = new LocationInfo(aMapLocation.getLatitude()
                            , aMapLocation.getLongitude());
                    locationInfo.setName(aMapLocation.getPoiName());
                    locationInfo.setId(KEY_MY_MARKER);
                    if(firstLocation ){
                        firstLocation = false ;
                        LatLng latLng = new LatLng(aMapLocation.getLatitude()
                                , aMapLocation.getLongitude());
                        CameraUpdate cameraUpdate =
                                CameraUpdateFactory.newCameraPosition(
                                        new CameraPosition(latLng, 18, 30, 30));
                        aMap.moveCamera(cameraUpdate);
                        if(mLocationChangeListener != null){
                            mLocationChangeListener.onLocation(locationInfo);
                        }
                    }
                    if(mLocationChangeListener != null){
                        mLocationChangeListener.onLocationChange(locationInfo);
                    }
                }
            }
        });
        mlocationClient.startLocation();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        setUpLocation() ;
    }

    @Override
    public void onSavaInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        mapView.onPause();
        mlocationClient.stopLocation();
    }

    @Override
    public void onDestory() {
        mapView.onDestroy();
        mlocationClient.onDestroy();
    }
}
