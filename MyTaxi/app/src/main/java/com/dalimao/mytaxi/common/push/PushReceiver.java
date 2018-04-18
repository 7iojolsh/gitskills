package com.dalimao.mytaxi.common.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dalimao.mytaxi.common.Lbs.LocationInfo;
import com.dalimao.mytaxi.common.databus.RxBus;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;
import com.dalimao.mytaxi.common.utils.LogUtil;
import com.dalimao.mytaxi.main.bean.Order;
import com.dalimao.mytaxi.main.model.response.OrderStateOptResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.push.PushConstants;
import cn.bmob.v3.BmobInstallation;

/**
 * Created by liuguangli on 17/6/1.
 */

public class PushReceiver extends BroadcastReceiver {

    private static final int MSG_TYEP_LOCATION = 1;

    private static final int MSG_TYPE_PRDER = 2 ;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
            String msg = intent.getStringExtra("msg");
            Log.d("bmob", "客户端收到推送内容：" + msg);

            //  通知业务或UI
            // {"data":
            //  {"key":"4913c896-2686-4230-86e5-5d9ae0f76c89",
            //  "latitude":23.135379999999998,
            //  "longitude":113.38665700000003,
            //  "rotation":-137.4028},
            // "type":1}
            try {
                JSONObject jsonObject = new JSONObject(msg);
                int type = jsonObject.optInt("type");
                if (type == MSG_TYEP_LOCATION) {
                    //位置变化
                    LocationInfo locationInfo = new Gson().fromJson(jsonObject.optString("data")
                            , LocationInfo.class);
                    RxBus.getInstance().send(locationInfo);
                }else if(type == MSG_TYPE_PRDER){
                    //订单变化
                    //解析数据
                    Order order =
                            new Gson().fromJson(jsonObject.optString("data") , Order.class);
                    OrderStateOptResponse orderStateOptResponse = new OrderStateOptResponse();
                    orderStateOptResponse.setData(order);
                    orderStateOptResponse.setState(order.getState());
                    orderStateOptResponse.setCode(BaseBizResponse.STATE_OK);
                    //通知ui
                    RxBus.getInstance().send(orderStateOptResponse);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
