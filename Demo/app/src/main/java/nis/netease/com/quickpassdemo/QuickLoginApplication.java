package nis.netease.com.quickpassdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Created by hzhuqi on 2019/10/24
 */
public class QuickLoginApplication extends Application {
    private boolean isUseDialogMode = false; // 是否使用对话框模式

    @Override
    public void onCreate() {
        super.onCreate();
        if (isUseDialogMode) {
            registerActivity();
        }
    }

    private void registerActivity() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                // 这里以移动运营商的登录界面为例
                if (activity.getClass().getName().contains("LoginAuthActivity")) {
                    DisplayMetrics dm = new DisplayMetrics();
                    activity.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
                    WindowManager.LayoutParams p = activity.getWindow().getAttributes();
                    //设置window大小
                    p.height = (int) (dm.heightPixels * 0.4);
                    p.width = (int) (dm.widthPixels);
                    //设置window位置
                    p.gravity = Gravity.BOTTOM;
                    activity.getWindow().setAttributes(p);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}
