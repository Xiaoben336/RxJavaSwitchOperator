package com.example.zjf.rxjavaswitchoperator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
*@description RxJava2的变换操作符:对事件序列中的事件 / 整个事件序列 进行加工处理（即变换），使得其转变成不同的事件 / 整个事件序列
 *                  1)、Map（）
 *                      作用：被观察者发送的每1个事件都通过 指定的函数 处理，从而变换成另外一种事件，
 *                            即将被观察者发送的事件转换为任意的类型事件。
 *                      应用场景：数据类型转换
 *                  2)、FlatMap（）
 *                      作用：将被观察者发送的事件序列进行 拆分 & 单独转换，再合并成一个新的事件序列，最后再进行发送
 *                      应用场景：无序的将被观察者发送的整个事件序列进行变换
 *                      原理：1、为事件序列中每个事件都创建一个 Observable 对象；
 *                            2、将对每个 原始事件 转换后的 新事件 都放入到对应 Observable对象；
 *                            3、将新建的每个Observable 都合并到一个 新建的、总的Observable 对象；
 *                            4、新建的、总的Observable 对象 将 新合并的事件序列 发送给观察者（Observer）
 *                  3)、ConcatMap（）
 *                      作用：类似FlatMap（）操作符，与FlatMap（）的 区别在于：拆分 & 重新合并生成的事件序列 的顺序 = 被观察者旧序列生产的顺序
 *                      应用场景：有序的将被观察者发送的整个事件序列进行变换
 *                  4)、Buffer（）
 *                      作用：定期从 被观察者（Obervable）需要发送的事件中 获取一定数量的事件 & 放到缓存区中，最终发送
 *                      应用场景：缓存被观察者发送的事件
*
*@author zjf
*@date 2018/10/24 16:32
*/
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*使用Map（） 将事件的参数从 整型 变换成 字符串类型 */
        Observable.create(new ObservableOnSubscribe<Integer>() {
            // 1. 被观察者发送事件 = 参数为整型 = 1、2、3
            @Override
            public void subscribe(ObservableEmitter<Integer> e) {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
            // 2. 使用Map变换操作符中的Function函数对被观察者发送的事件进行统一变换：整型变换成字符串类型
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) {
                return "使用 Map变换操作符 将事件" + integer +"的参数从 整型"+integer + " 变换成 字符串类型" + integer;
            }
        }).subscribe(new Consumer<String>() {
            // 3. 观察者接收事件时，是接收到变换后的事件 = 字符串类型
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, s);
            }
        });


        /*FlatMap（） 新合并生成的事件序列顺序是无序的，即 与旧序列发送事件的顺序无关*/
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                //通过flatMap中将被观察者生产的事件序列先进行拆分，再将每个事件转换为一个新的发送三个String事件
                // 最终合并，再发送给被观察者
                for (int i = 0;i < 3;i++){
                    list.add("我是flatMap事件 " + integer + "拆分后的子事件" + i);
                }
                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, s);
            }
        });

        /*concatMap():新合并生成的事件序列顺序是有序的，即 严格按照旧序列发送事件的顺序*/
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }

            // 采用concatMap（）变换操作符
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("我是concatMap事件 " + integer + "拆分后的子事件" + i);
                    // 通过concatMap中将被观察者生产的事件序列先进行拆分，再将每个事件转换为一个新的发送三个String事件
                    // 最终合并，再发送给被观察者
                }
                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, s);
            }
        });



        // 被观察者 需要发送5个数字
        Observable.just(1,2,3,4,5)
                .buffer(3,1)// 设置缓存区大小 & 步长
                                            // 缓存区大小 = 每次从被观察者中获取的事件数量
                                            // 步长 = 每次获取新事件的数量
                .subscribe(new Observer<List<Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Integer> integers) {
                        Log.d(TAG, " 缓存区里的事件数量 = " +  integers.size());
                        for (Integer value : integers) {
                            Log.d(TAG, " buffer事件 = " + value);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应" );
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }
                });

    }
}
