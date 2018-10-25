package com.example.nestedcallback;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
*@description  Rxjava创建操作符的实际开发需求场景：网络请求嵌套回调 需求
 *              需要进行嵌套网络请求：即在第1个网络请求成功后，继续再进行一次网络请求
 *              如 先进行 用户注册 的网络请求, 待注册成功后回再继续发送 用户登录 的网络请求
 *
 *
 *              实现功能：结合 Retrofit 与RxJava 实现网络请求嵌套
 *                        通过 公共的金山词霸API 来模拟 “注册 - 登录”嵌套网络请求
 *                        即先翻译 Register（注册），再翻译 Login（登录）
 *
 *
 *              步骤实现：
 *                       1、添加依赖
 *                       2、创建 接收服务器返回数据 的类
 *                       3、创建 用于描述网络请求 的接口（区别于Retrofit传统形式）
 *                       4、创建 Retrofit 实例
 *                       5、创建 网络请求接口实例 并 配置网络请求参数（区别于Retrofit传统形式）
 *                       6、发送网络请求（区别于Retrofit传统形式）
 *                       7、对返回的数据进行处理
 *
 *             为了演示是2个网络请求，所以对应设置2个接收服务器的数据类:Translation1.java、Translation2.java
*
*@author zjf
*@date 2018/10/25 11:06
*/
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // 定义Observable接口类型的网络请求对象
    Observable<Translation1> observable1;
    Observable<Translation2> observable2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 步骤1：创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://fy.iciba.com/")// 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create())//设置使用Gson解析(记得加入依赖)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 支持RxJava
                .build();
        // 步骤2：创建 网络请求接口 的实例
        GetRequest_Interface request = retrofit.create(GetRequest_Interface.class);
        // 步骤3：采用Observable<...>形式 对 2个网络请求 进行封装
        observable1 = request.getCall();
        observable2 = request.getCall_2();

        observable1.subscribeOn(Schedulers.io()) // （初始被观察者）切换到IO线程进行网络请求1
                .observeOn(AndroidSchedulers.mainThread())// （新观察者）切换到主线程 处理网络请求1的结果
                .doOnNext(new Consumer<Translation1>() {
                    @Override
                    public void accept(Translation1 translation1) throws Exception {
                        Log.d(TAG, "第1次网络请求成功");
                        translation1.show();// 对第1次网络请求返回的结果进行操作 = 显示翻译结果
                    }
                })
                .observeOn(Schedulers.io()) // （新被观察者，同时也是新观察者）切换到IO线程去发起登录请求
                                            // 特别注意：因为flatMap是对初始被观察者作变换，所以对于旧被观察者，它是新观察者，
                                            // 所以通过observeOn切换线程
                                            // 但对于初始观察者，它则是新的被观察者
                .flatMap(new Function<Translation1, ObservableSource<Translation2>>() {
                    @Override
                    public ObservableSource<Translation2> apply(Translation1 translation1) throws Exception {
                        // 将网络请求1转换成网络请求2，即发送网络请求2
                        return observable2;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Translation2>() {
                    @Override
                    public void accept(Translation2 translation2) throws Exception {
                        Log.d(TAG, "第2次网络请求成功");
                        translation2.show();
                        // 对第2次网络请求返回的结果进行操作 = 显示翻译结果
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "登录失败");
                    }
                });
    }
}
