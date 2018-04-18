package com.dalimao.mytaxi.main.model.response;

import com.dalimao.mytaxi.common.Lbs.LocationInfo;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;

import java.util.List;

/**
 * Created by lsh on 2018/4/11.
 */

public class NearDirversResponse extends BaseBizResponse {
    List<LocationInfo> data ;

    public List<LocationInfo> getData() {
        return data;
    }

    public void setData(List<LocationInfo> data) {
        this.data = data;
    }
}
