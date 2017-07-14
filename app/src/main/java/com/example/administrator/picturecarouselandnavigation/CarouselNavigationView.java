package com.example.administrator.picturecarouselandnavigation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/7/14
 * Author 郑少鹏
 */

public class CarouselNavigationView extends FrameLayout {
    // 使用universal-image-loader插件读取网络图片，需要工程导入universal-image-loader-1.8.6-with-sources.jar
    private ImageLoader imageLoader = ImageLoader.getInstance();
    // 轮播图片数量
    private final static int IMAGE_COUNT = 5;
    // 自动轮播时间间隔
    private final static int TIME_INTERVAL = 5;
    // 自动轮播开关标志
    private final static boolean isAutoPlay = true;
    // 自定义轮播图资源数组
    private String[] imageUrls;
    // 存放轮播图片ImageView的List集合
    private List<ImageView> imageViewsList;
    // 存放圆点的View的List集合
    private List<View> dotViewsList;
    // 声明ViewPager控件
    private ViewPager viewPager;
    // 当前轮播页
    private int currentItem = 0;
    // 线程池（支持周期性和定时处理任务）
    private ScheduledExecutorService scheduledExecutorService;
    // 声明上下文
    private Context context;

    // Handler
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            viewPager.setCurrentItem(currentItem);
        }
    };

    public CarouselNavigationView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public CarouselNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public CarouselNavigationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        // ImageLoader 图片组件初始化方法
        initImageLoader(context);
        // 初始化相关Data方法
        initData();
        if (isAutoPlay) {
            startPlay();
        }
    }

    /**
     * 开始轮播图切换
     */
    private void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 1, 4, TimeUnit.SECONDS);
    }

    /**
     * 停止轮播图切换
     */
    private void stopPlay() {
        scheduledExecutorService.shutdown();
    }

    /**
     * 初始化相关Data
     */
    private void initData() {
        imageViewsList = new ArrayList<>();
        dotViewsList = new ArrayList<>();
        // 异步任务获取图片
        new GetListTask().execute("");
    }

    /**
     * 初始化Views等UI
     */
    private void initUI(Context context) {
        if (imageUrls == null || imageUrls.length == 0)
            return;
        LayoutInflater.from(context).inflate(R.layout.carousel_navigation, this, true);
        // 初始化包含导航圆点控件
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        // 移除包含导航圆点布局中的views
        linearLayout.removeAllViews();
        // 导航圆点个数与图片资源数量相等
        for (int i = 0; i < imageUrls.length; i++) {
            ImageView view = new ImageView(context);
            view.setTag(imageUrls[i]);
            if (i == 0)// 位置为零处设置默认图
                view.setBackgroundResource(R.drawable.indicator1);
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageViewsList.add(view);

            ImageView dotView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = 4;
            params.rightMargin = 4;
            linearLayout.addView(dotView, params);
            dotViewsList.add(dotView);
        }
        viewPager = findViewById(R.id.viewPager);
        viewPager.setFocusable(true);
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
    }

    /**
     * 填充ViewPager的页面适配器
     */
    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(View container, int position, Object object) {
            // TODO Auto-generated method stub
            //((ViewPag.er)container).removeView((View)object);
            ((ViewPager) container).removeView(imageViewsList.get(position));
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ImageView imageView = imageViewsList.get(position);
            imageLoader.displayImage(imageView.getTag() + "", imageView);
            ((ViewPager) container).addView(imageViewsList.get(position));
            return imageViewsList.get(position);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return imageViewsList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            // TODO Auto-generated method stub
        }

        @Override
        public Parcelable saveState() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void finishUpdate(View arg0) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * ViewPager的监听器
     * 当ViewPager中页面的状态发生改变时调用
     */
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        boolean isAutoPlay = false;

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
            switch (arg0) {
                case 1:// 手势滑动，空闲中
                    isAutoPlay = false;
                    break;
                case 2:// 界面切换中
                    isAutoPlay = true;
                    break;
                case 0:// 滑动结束，即切换完毕或者加载完毕
                    // 当前为最后一张，此时从右向左滑，则切换到第一张
                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
                        viewPager.setCurrentItem(0);
                    }
                    // 当前为第一张，此时从左向右滑，则切换到最后一张
                    else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                    }
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageSelected(int pos) {
            // TODO Auto-generated method stub
            currentItem = pos;
            for (int i = 0; i < dotViewsList.size(); i++) {
                if (i == pos) {
                    dotViewsList.get(pos).setBackgroundResource(R.drawable.dot_select);
                } else {
                    dotViewsList.get(i).setBackgroundResource(R.drawable.dot_normal);
                }
            }
        }
    }

    /**
     * 执行轮播图切换任务
     */
    private class SlideShowTask implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            synchronized (viewPager) {
                currentItem = (currentItem + 1) % imageViewsList.size();
                handler.obtainMessage().sendToTarget();
            }
        }
    }

    /**
     * 销毁ImageView资源，回收内存
     */
    private void destoryBitmaps() {
        for (int i = 0; i < IMAGE_COUNT; i++) {
            ImageView imageView = imageViewsList.get(i);
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                // 解除drawable对view的引用
                drawable.setCallback(null);
            }
        }
    }

    /**
     * 异步任务,获取数据
     */
    class GetListTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                // 这里一般调用服务端接口获取一组轮播图，下面是从百度找的一组图片
                imageUrls = new String[]{
                        "http://img01.taopic.com/150330/240411-1503300Q33280.jpg",
                        "http://pic.58pic.com/58pic/13/27/83/45f58PICYkZ_1024.jpg",
                        "http://img4.duitang.com/uploads/blog/201310/19/20131019155006_WLKdZ.thumb.600_0.jpeg",
                        "http://pic1.win4000.com/wallpaper/6/5603c565201bb.jpg"
                };
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                initUI(context);
            }
        }
    }

    /**
     * ImageLoader 图片组件初始化
     *
     * @param context
     */
    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).
                threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove
                // for
                // release
                // app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }
}
