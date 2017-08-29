package me.relex.viewpagerheaderscrolldemo.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import me.relex.viewpagerheaderscrolldemo.R;
import me.relex.viewpagerheaderscrolldemo.utils.ActivityManager;

/**
 * @author kevin.
 */
public class BaseActivity extends AppCompatActivity {

  private LinearLayout rootLayout;
  private ActivityManager mActivityManager = ActivityManager.getActivityManager(this);


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 这句很关键，注意是调用父类的方法
    super.setContentView(R.layout.activity_base);
    // 经测试在代码里直接声明透明状态栏更有效
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
      localLayoutParams.flags =
          (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
    }
    initToolbar();
    mActivityManager.putActivity(this);

  }


  private void initToolbar() {

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }


  @Override
  public void setContentView(int layoutId) {

    setContentView(View.inflate(this, layoutId, null));
  }


  @Override
  public void setContentView(View view) {

    rootLayout = (LinearLayout) findViewById(R.id.root_layout);
    if (rootLayout == null) return;
    rootLayout.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    initToolbar();
  }
  @Override protected void onDestroy() {
    super.onDestroy();
    mActivityManager.removeActivity(this);
  }
  public void exit() {
    mActivityManager.exit();
  }


  @Override protected void onSaveInstanceState(Bundle outState) {
    // super.onSaveInstanceState(outState); //将这一行注释掉，阻止activity保存fragment的状态
  }
}
