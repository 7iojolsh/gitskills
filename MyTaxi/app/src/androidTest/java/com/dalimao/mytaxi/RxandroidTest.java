package com.dalimao.mytaxi;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lsh on 2018/4/9.
 */
@RunWith(AndroidJUnit4.class)
public class RxandroidTest {

    // map
    @Test
    public void testMap() {
        String name = "dalimao";
        Observable.just(name)
                .subscribeOn(Schedulers.newThread()) // 指定下一个生成节点在新线程中处理
                .map(new Func1<String, User>() {
                    @Override
                    public User call(String name) {
                        User user = new User();
                        user.setName(name);
                        System.out.println("process User call in tread:"
                                + Thread.currentThread().getName());
                        return user;
                    }
                })

                .subscribeOn(Schedulers.newThread()) // 指定下一个生产节点在新线程中处理
                .map(new Func1<User, Object>() {
                    @Override
                    public Object call(User user) {
                        // 如果需要，我们在这里还可以对 User 进行加工
                        System.out.println("process User call in tread:"
                                + Thread.currentThread().getName());
                        return user;
                    }
                })

                .observeOn(AndroidSchedulers.mainThread()) // 指定消费节点在新线程中处理
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object data) {

                        System.out.println("receive User call in tread:"
                                + Thread.currentThread().getName());
                    }
                });
    }
    public static class User {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
