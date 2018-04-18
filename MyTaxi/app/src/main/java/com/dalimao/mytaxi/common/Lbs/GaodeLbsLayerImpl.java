package com.dalimao.mytaxi.common.Lbs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.dalimao.mytaxi.R;
import com.dalimao.mytaxi.common.utils.LogUtil;
import com.dalimao.mytaxi.common.utils.SensorEventHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Route;

import static android.R.attr.rotation;

/**
 * Created by lsh on 2018/4/10.
 */

public class GaodeLbsLayerImpl implements ILbsLayer {
    private static final String TAG = "GaodeLbsLayerImpl";
    private static final String KEY_MY_MARKER = "1000";
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
    private Map<String, Marker> markerMap = new HashMap<>();
    private Marker mLocMarker;
    //当前城市
    private String mCity;
    // 路径查询对象
    private RouteSearch mRouteSearch;

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

    /**
     * 添加或替换marker
     *
     * @param locationInfo 位置信息对象
     * @param bitmap       maker 图标
     */
    @Override
    public void addOrUpdateMarker(LocationInfo locationInfo, Bitmap bitmap) {
        if (markerMap == null) {
            markerMap = new HashMap<>();
        }
        Marker storedmarker = markerMap.get(locationInfo.getKey());
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
            markerMap.put(locationInfo.getKey(), marker);
            if (KEY_MY_MARKER.equals(locationInfo.getKey())) {
                // 传感器控制我的位置标记的旋转角度
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
                LogUtil.d(TAG, "activate");
            }

            @Override
            public void deactivate() {
                if (mlocationClient != null) {
                    mlocationClient.startLocation();
                    mlocationClient.onDestroy();
                }
                mlocationClient = null;
            }
        });
        //设置默认定位按钮是否显示 , 这里先不向业务使用方开放
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
    }

    public void setUpLocation() {
        //设置监听器
        mlocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                //定位位置变化
                if (mMapLocationChangeListener != null) {
                    mMapLocationChangeListener.onLocationChanged(aMapLocation);
                    //记录当前城市
                    mCity = aMapLocation.getCity();
                    LogUtil.d(TAG, "onLocationChanged");
                    LocationInfo locationInfo = new LocationInfo(aMapLocation.getLatitude()
                            , aMapLocation.getLongitude());
                    locationInfo.setName(aMapLocation.getPoiName());
                    locationInfo.setKey(KEY_MY_MARKER);
                    if (firstLocation) {
                        firstLocation = false;
                        moveCameraToPoint(locationInfo, 17);
                        if (mLocationChangeListener != null) {
                            mLocationChangeListener.onLocation(locationInfo);
                        }
                    }
                    if (mLocationChangeListener != null) {
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
        setUpLocation();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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

    @Override
    public void moveCameraToPoint(LocationInfo locationInfo, int scale) {
        LatLng latLng = new LatLng(locationInfo.getLatitude(),
                locationInfo.getLongitude());
        CameraUpdate up = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                latLng, scale, 30, 30));
        aMap.moveCamera(up);
    }

    @Override
    public void moveCamera(LocationInfo locationInfo1, LocationInfo locationInfo2) {
        try {
            LatLng latLng =
                    new LatLng(locationInfo1.getLatitude(),
                            locationInfo1.getLongitude());
            LatLng latLng1 =
                    new LatLng(locationInfo2.getLatitude(),
                            locationInfo2.getLongitude());
            LatLngBounds.Builder b = LatLngBounds.builder();
            b.include(latLng);
            b.include(latLng1);
            LatLngBounds latLngBounds = b.build();
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
        } catch (Exception e) {
            LogUtil.e(TAG, "moveCamera: " + e.getMessage());
        }
    }

    @Override
    public String getCity() {
        return mCity;
    }

    /**
     * 高德地图poi搜索接口
     *
     * @param key      关键字
     * @param listener 监听器
     */
    @Override
    public void poiSearch(String key, final OnSearchedListener listener) {
        if (!TextUtils.isEmpty(key)) {
            // 1, 组装关键字
            InputtipsQuery inputtipsQuery = new InputtipsQuery(key, "");
            Inputtips inputtips = new Inputtips(context, inputtipsQuery);
            // 2 , 开始异步搜索
            inputtips.requestInputtipsAsyn();
            // 3 : 监听处理搜索结果
            inputtips.setInputtipsListener(new Inputtips.InputtipsListener() {
                @Override
                public void onGetInputtips(List<Tip> list, int rCode) {
                    if (rCode == AMapException.CODE_AMAP_SUCCESS) {
                        List<LocationInfo> locationInfos = new ArrayList<LocationInfo>();
                        for (int i = 0; i < list.size(); i++) {
                            Tip tip = list.get(i);
                            LocationInfo locationInfo = new LocationInfo(tip.getPoint().getLatitude(),
                                    tip.getPoint().getLongitude());
                            locationInfo.setName(tip.getName());
                            locationInfo.setKey(tip.getPoiID());
                            locationInfos.add(locationInfo);
                        }
                        listener.onSearched(locationInfos);
                    } else {
                        listener.onError(rCode);
                    }
                }
            });
        }
    }

    @Override
    public void clearAllMarkers() {
        aMap.clear();
        markerMap.clear();
    }

    /**
     * 路径规划
     *
     * @param mStartLocation          起始位置
     * @param mEndLocation            结束位置
     * @param color                   线条颜色
     * @param onRouteCompleteListener 规划状态监听
     */
    @Override
    public void driverRoute(LocationInfo mStartLocation, LocationInfo mEndLocation,
                            final int color, final OnRouteCompleteListener onRouteCompleteListener) {

        // 1:组装起点和终点之间信息 LatLonPoint : 高德提供的几何点对象类
        LatLonPoint startPoint =
                new LatLonPoint(mStartLocation.getLatitude(), mStartLocation.getLongitude());
        LatLonPoint endPoint =
                new LatLonPoint(mEndLocation.getLatitude(), mEndLocation.getLongitude());
        //构造路径规划的起点和终点坐标
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        // 2:创建路径查询参数
        //第一个参数标识路径规划的起点和终点
        //第二个参数表示驾车模式
        //第三个参数途径点
        //第四个参数表示避让区域
        //第五个参数表示避让道路
        //此类定义了驾车路径查询规划
        final RouteSearch.DriveRouteQuery driveRouteQuery =
                new RouteSearch.DriveRouteQuery(
                        fromAndTo
                        , RouteSearch.DrivingDefault
                        , null
                        , null
                        , "");
        // 3:创建搜索对象 , 异步路径规划驾车模式查询
        if(mRouteSearch == null){
            mRouteSearch = new RouteSearch(context) ;
        }
        // 4:执行搜索,计算驾车线路
        mRouteSearch.calculateDriveRouteAsyn(driveRouteQuery);
        mRouteSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
                //获取一条路径
                DrivePath drivePath = driveRouteResult.getPaths().get(0);
                //获取这条路径上的所有点  ,使用polyline绘制路径
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(color);
                //起点
                LatLonPoint startPos = driveRouteResult.getStartPos();
                //返回驾车规划方案的路段列表。
                List<DriveStep> steps = drivePath.getSteps();
                //终点
                LatLonPoint targetPos = driveRouteResult.getTargetPos();
                //添加起点
                polylineOptions.add(new LatLng(startPos.getLatitude() , startPos.getLongitude()));
                //添加中间节点
                for (DriveStep step : steps) {
                    List<LatLonPoint> polyline = step.getPolyline();
                    for (LatLonPoint latLonPoint : polyline) {
                        polylineOptions.add(
                                new LatLng(latLonPoint.getLatitude() , latLonPoint.getLongitude()));
                    }
                }
                //添加终点
                polylineOptions.add(new LatLng(targetPos.getLatitude() , targetPos.getLongitude()));
                //行程绘制
                aMap.addPolyline(polylineOptions);
                /**
                 * 业务回掉
                 */
                if(onRouteCompleteListener != null){
                    RouteInfo routeInfo = new RouteInfo();
                    routeInfo.setTaxiCost(driveRouteResult.getTaxiCost());
                    routeInfo.setDistance(0.5f + drivePath.getDistance()/1000);
                    routeInfo.setDuration(10 + Long.valueOf(drivePath.getDuration()/1000*60).intValue());
                    onRouteCompleteListener.onComplete(routeInfo);
                }
            }


            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

            }
        });


    }
}
