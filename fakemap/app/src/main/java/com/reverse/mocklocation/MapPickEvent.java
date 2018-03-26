package com.reverse.mocklocation;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by sing on 2016/10/13.
 */

public class MapPickEvent {
    public LatLng latLng;

    public MapPickEvent(LatLng latLng) {
        this.latLng = latLng;
    }
}
