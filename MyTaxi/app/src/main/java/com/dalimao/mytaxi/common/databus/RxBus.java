package com.dalimao.mytaxi.common.databus;

import android.util.Log;

import com.dalimao.mytaxi.common.Lbs.LocationInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lsh on 2018/4/9.
 */

@SuppressWarnings("unchecked")
public class RxBus {
    private static final String TAG = "RxBus";
    private static volatile RxBus insstance;
    //订阅者集合
    private Set<Object> subsrcibers;

    /**
     * 注册DataBusSubsrciber
     *
     * @param subsrciber
     */
    public synchronized void register(Object subsrciber) {
        subsrcibers.add(subsrciber);
    }

    /**
     * 注销DataBusSubsrciber
     *
     * @param subsrciber
     */
    public synchronized void unRegister(Object subsrciber) {
        subsrcibers.remove(subsrciber);
    }

    /**
     * 单例模式
     */
    private RxBus() {
        subsrcibers = new CopyOnWriteArraySet<>();
    }

    public static synchronized RxBus getInstance() {
        if (insstance == null) {
            synchronized (RxBus.class) {
                if (insstance == null) {
                    insstance = new RxBus();
                }
            }

        }
        return insstance;
    }

    /**
     * 包装处理过程
     *
     * @param func
     */
    public void chainProcess(Func1 func) {
        Observable.just("")
                .subscribeOn(Schedulers.io())  //指定处理过程在io线程
                .map(func)  //包装处理过程
                .observeOn(AndroidSchedulers.mainThread()) //指定消费事件在main 线程
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object data) {
                        Log.d(TAG, "chainProcess start");
                        if(data == null ){
                                return;
                        }
                        send(data);
                    }
                });
    }

    /**
     * 发送数据
     * @param data
     */
    public void send(Object data) {
        for (Object s : subsrcibers) {
            //扫描注解 , 将数据发送到注册的对象的标记方法
            callMethodByAnnotiation(s, data);
        }

    }
    /**
     *  反射获取去向方法列表 , 判断:
     * 1 是否被注解修饰
     * 2 参数类型是否和Data类型一致
     *
     * @param target
     * @param data
     */
    private void callMethodByAnnotiation(Object target, Object data) {
        //获取方法列表
        Method[] methodArray = target.getClass().getDeclaredMethods();
        for (int i = 0; i < methodArray.length; i++) {
            try {
                //判断是否被 RegisterBus 注解修饰
                if (methodArray[i].isAnnotationPresent(RegisterBus.class)) {
                    // 被@RegisterBus 修饰的方法 拿到其参数类型
                    Class paramType = methodArray[i].getParameterTypes()[0];
                    if (data.getClass().getName().equals(paramType.getName())) {
                        //参数类型和data 一样 , 调用此方法
                        methodArray[i].invoke(target, new Object[]{data});
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }


}
