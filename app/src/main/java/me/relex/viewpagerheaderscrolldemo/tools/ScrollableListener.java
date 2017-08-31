package me.relex.viewpagerheaderscrolldemo.tools;

import android.view.MotionEvent;

public interface ScrollableListener {
    boolean isViewBeingDragged(MotionEvent event);

    boolean isLeftViewBeingDragged(MotionEvent event);
}
