package com.dalimao.mytaxi;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rx.functions.Func1;
import yyl.com.dalimao.mytaxi.common.databus.RegisterBus;
import yyl.com.dalimao.mytaxi.common.databus.RxBus;

/**
 * Created by lsh on 2018/4/9.
 */

public class RxBusTest {
    public static final String TAG = "RxBusTest";
    Presenter presenter;

    @Before
    public void setUp() throws Exception {
        /**
         * 初始化 presenter 并注册
         */
        presenter = new Presenter(new Manager());
        RxBus.getInstance().register(presenter);
    }

    @After
    public void tearDown() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RxBus.getInstance().unRegister(presenter);
    }

    @Test
    public void testGetUser() throws Exception {
        presenter.getUser();
    }

    @Test
    public void testGetOrder() throws Exception {
        presenter.getOrder();
    }
}

class Presenter {
    private Manager manager;

    public Presenter(Manager manager) {
        this.manager = manager;
    }

    public void getUser() {
        manager.getUser();
    }

    public void getOrder() {
        manager.getOrder();
    }


    @RegisterBus
    public void onUser(User user) {
        Log.d(RxBusTest.TAG, "receive User in thread : " +
                Thread.currentThread()) ;
    }

    @RegisterBus
    public void onUser(Order order) {
        Log.d(RxBusTest.TAG, "receive Order in thread : " +
                Thread.currentThread()) ;
    }
}

/**
 * 模拟model
 */
class Manager {
    public void getUser() {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                Log.d(RxBusTest.TAG, "chainProcess getUser start in thread = " +
                        Thread.currentThread().getName());
                User user = new User();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //把user 数据传到 Presenter
                return user;
            }
        });
    }

    public void getOrder() {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                Log.d(RxBusTest.TAG, "chainProcess getOrder start in thread = " +
                        Thread.currentThread().getName());
                Order order = new Order();
                //把order 数据传到 Presenter
                return order;
            }
        });
    }
}

/**
 * 要返回的数据类型
 */
class User {
}

class Order {
}