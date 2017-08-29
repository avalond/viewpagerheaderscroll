package me.relex.viewpagerheaderscrolldemo.utils;

import android.app.Activity;
import android.content.Context;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 5/26/17.
 */

public class ActivityManager {
  private Context mContext;

  private static ActivityManager activityManager;


  public static ActivityManager getActivityManager(Context context) {
    if (activityManager == null) {
      activityManager = new ActivityManager(context);
    }
    return activityManager;
  }


  private ActivityManager(Context context) {
    this.mContext = context;
  }


  /**
   * task map, 用于记录Activity的栈,方便退出程序(为了不影响系统回收Activity,使用软引用)
   */
  private final HashMap<String, SoftReference<Activity>> mTaskMap = new HashMap<String, SoftReference<Activity>>();


  /**
   * 向运用task map里面添加activity
   *
   * @param act: activity
   */
  public final void putActivity(Activity act) {
    mTaskMap.put(act.toString(), new SoftReference<Activity>(act));
  }


  /**
   * 移除task栈里的某个activity
   *
   * @param act: activity
   */
  public final void removeActivity(Activity act) {
    mTaskMap.remove(act.toString());
  }


  /**
   * 清除运用的task栈,如果程序正常运行会导致运用退出桌面
   */
  public final void exit() {
    for (Map.Entry<String, SoftReference<Activity>> stringSoftReferenceEntry : mTaskMap.entrySet()) {
      SoftReference<Activity> activityReference = stringSoftReferenceEntry.getValue();
      Activity activity = activityReference.get();
      if (activity != null) {
        activity.finish();
      }
    }
    mTaskMap.clear();
  }
}
