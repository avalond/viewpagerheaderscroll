package me.relex.viewpagerheaderscrolldemo.tools;

public interface ScrollableFragmentListener {

    void onFragmentAttached(ScrollableListener fragment, int position);

    void onFragmentDetached(ScrollableListener fragment, int position);
}
