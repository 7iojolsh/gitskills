package com.dalimao.mytaxi.common.Lbs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import java.util.List;

/**
 * Created by lsh on 2018/4/10.
 *  定义地图服务通用的抽象接口
 */

public interface ILbsLayer {
    /**
     * 获取地图
     */
    View getMapView();

    /**
     * 设置位置变化监听
     */
    void setLocationChangeListener(CommonLocationChangeListener changeListener);

    /**
     * 设置定位图标
     * @param res 图标资源文件
     */
    void setLocationRes(int res);

    /**
     * 添加,更新标记点 ,包括位置, 角度(通过id 识别)
     * @param locationInfo 位置信息对象
     * @param bitmap maker 图标
     */
    void addOrUpdateMarker(LocationInfo locationInfo, Bitmap bitmap) ;
    /**
     * 生命周期函数
     * @param state
     */
    void onCreate(Bundle state) ;
    void onResume();

    void onSaveInstanceState(Bundle outState) ;
    void onPause() ;
    void onDestory() ;


    /**
     *  移动动相机到某个点，
     * @param locationInfo
     * @param scale 缩放系数
     */
    void moveCameraToPoint(LocationInfo locationInfo, int scale);
    /**
     *  移动相机到两点之间的视野范围
     */
    void moveCamera(LocationInfo locationInfo1,
                    LocationInfo locationInfo2);
    /**
     * 获取当前城市
     */
    String getCity();


    /**
     * 联动搜索当前位置
     * @param key 关键字
     * @param listener 监听器
     */
    void poiSearch(String key , OnSearchedListener listener);

    /**
     * 清除所有的marker
     */
    void clearAllMarkers();

    /**
     * 绘制亮点之间线路
     * @param mStartLocation 起始位置
     * @param mEndLocation  结束位置
     * @param color 线条颜色
     * @param onRouteCompleteListener 规划状态监听
     */
    void driverRoute(LocationInfo mStartLocation, LocationInfo mEndLocation,
                     int color, OnRouteCompleteListener onRouteCompleteListener);

    interface  CommonLocationChangeListener{
        //当位置发生变得的时候调用
       void onLocationChange(LocationInfo locationInfo) ;
       //第一次定位到位置的时候调用该方法
       void onLocation(LocationInfo locationInfo) ;

    }


    /**
     * poi 搜索结果监听器
     */
     interface OnSearchedListener {
            void  onSearched(List<LocationInfo> resuits);
            void onError(int code );
    }
    /**
     * 路径规划完成监听
     */
    interface OnRouteCompleteListener {
        //规划完成
         void onComplete(RouteInfo routeInfo) ;
    }
}
