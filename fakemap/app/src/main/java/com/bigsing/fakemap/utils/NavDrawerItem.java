package com.bigsing.fakemap.utils;

/**
 * Created by sing on 2017/4/18.
 */

public class NavDrawerItem {
    private String count;
    private int icon;
    private boolean isCounterVisible;
    private String title;

    public NavDrawerItem() {
        super();
        this.count = "0";
        this.isCounterVisible = false;
    }

    public NavDrawerItem(final String title, final int icon) {
        super();
        this.count = "0";
        this.isCounterVisible = false;
        this.title = title;
        this.icon = icon;
    }

    public NavDrawerItem(final String title, final int icon, final boolean isCounterVisible, final String count) {
        super();
        this.count = "0";
        this.isCounterVisible = false;
        this.title = title;
        this.icon = icon;
        this.isCounterVisible = isCounterVisible;
        this.count = count;
    }

    public String getCount() {
        return this.count;
    }

    public void setCount(final String count) {
        this.count = count;
    }

    public boolean getCounterVisibility() {
        return this.isCounterVisible;
    }

    public int getIcon() {
        return this.icon;
    }

    public void setIcon(final int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setIsCounterVisibility(final boolean isCounterVisible) {
        this.isCounterVisible = isCounterVisible;
    }
}
