package com.bigsing.fakemap.utils;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

import java.math.BigDecimal;

/**
 * 百度坐标与GPS坐标转换
 * ref: http://xwangly.iteye.com/blog/2100982
 */
public class MapConvert {
    /**
     * 将GPS设备采集的原始GPS坐标转换成百度坐标
     */
    public static LatLng convertGPSToBaidu(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    /**
     * 百度坐标转换成GPS坐标
     */
    public static LatLng convertBaiduToGPS(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        double latitude = 2 * sourceLatLng.latitude - desLatLng.latitude;
        double longitude = 2 * sourceLatLng.longitude - desLatLng.longitude;
        BigDecimal bdLatitude = new BigDecimal(latitude);
        bdLatitude = bdLatitude.setScale(6, BigDecimal.ROUND_HALF_UP);
        BigDecimal bdLongitude = new BigDecimal(longitude);
        bdLongitude = bdLongitude.setScale(6, BigDecimal.ROUND_HALF_UP);
        return new LatLng(bdLatitude.doubleValue(), bdLongitude.doubleValue());
    }


}
