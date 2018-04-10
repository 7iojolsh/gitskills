package yyl.diyapp.com.mytexi.common.Lbs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

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
    void addOrUpdateMarker(LocationInfo locationInfo , Bitmap bitmap) ;
    /**
     * 生命周期函数
     * @param state
     */
    void onCreate(Bundle state) ;
    void onResume();
    void onSavaInstanceState(Bundle outState) ;
    void onPause() ;
    void onDestory() ;


    interface  CommonLocationChangeListener{
        //当位置发生变得的时候调用
       void onLocationChange(LocationInfo locationInfo) ;
       //第一次定位到位置的时候调用该方法
       void onLocation(LocationInfo locationInfo) ;

    }
}
