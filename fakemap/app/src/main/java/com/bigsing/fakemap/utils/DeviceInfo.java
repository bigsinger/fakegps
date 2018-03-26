package com.bigsing.fakemap.utils;

/**
 * Created by sing on 2017/4/18.
 */


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sing on 2016/9/7.
 * Android Build类获取系统信息: http://blog.csdn.net/ccpat/article/details/44776313
 */
public class DeviceInfo {
    private String mCPU_ABI = null;
    private String mBuildNum = null;
    private String mModel = null;
    private String mOSName = null;
    private String mOSVer = null;
    private int mSDKINT = 0;
    private String mAndroidID = null;
    private String mIMEI = null;
    private String mWifiMac = null;
    private String mWifiMacIP = null;
    private String mWifiMacFile = null;
    private String mWifiMacMi = null;
    private String mWifiSSID = null;
    private String mWifiBSSID = null;
    private String mPhoneNum = null;
    private String mSerialNum = null;
    private String mManufacture = null;
    private boolean mNetConnected = false;
    private String mNetworkType = null;
    private String mIMSI = null;
    private String mSimCarrier = null;
    private int mSDCardFreeSpace = 0;
    private boolean mhasSDCard = false;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    /**
     * 操作系统名称,如: MIUI, flymeos
     **/
    public static String getOSName() {
        return Build.DISPLAY;
    }

    public static boolean isConnected(Context context) {
        boolean isConnected = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);//获取系统的连接服务
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();//获取网络的连接情况
            if (activeNetInfo != null) {
                isConnected = activeNetInfo.isConnected();
            }
        } catch (Exception e) {

        }
        return isConnected;
    }

    /**
     * 计算sdcard上的剩余空间
     *
     * @return 剩余byte
     */
    public static int getSDCardFreeSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        int sdFreeByte = (stat.getAvailableBlocks() * stat.getBlockSize());
        return sdFreeByte;
    }

    public static boolean hasSDCard() {
        try {
            //可能会出现java.lang.ExceptionInInitializerError
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判断手机是否root
     * 根据存在特殊文件以及文件夹权限进行判断
     */
    public static boolean isRoot() {
        String binPath = "/system/bin/su";
        String xBinPath = "/system/xbin/su";
        if ((new File(binPath).exists() && isExecutable(binPath)) || (new File(xBinPath).exists() && isExecutable(xBinPath)))
            return true;
        return false;
    }

    public static boolean isRoot2() {
        File file = new File("/data/");
        return file.canWrite();
    }

    private static boolean isExecutable(String filePath) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("ls -l " + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String str = in.readLine();
            if (str != null && str.length() >= 4) {
                char flag = str.charAt(3);
                if (flag == 's' || flag == 'x')
                    return true;
            }

        } catch (IOException e) {

        } finally {
            if (process != null) {
                try {
                    process.destroy();

                } catch (Exception e) {

                }
            }
        }
        return false;
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;

    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;

    }

    public static String GetLocalMacAddress() {
        String mac = null;
        try {
            BluetoothAdapter btAda = BluetoothAdapter.getDefaultAdapter();
            mac = btAda.getAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac;
    }

    /*
    格式化机器信息
     */
    public Map<String, String> format(Context context) {
        String strResult = null;
        mCPU_ABI = getCPU_ABI();
        mBuildNum = getBuildNum();
        mModel = getModel();
        mOSName = getOSName();
        mOSVer = getOSVer();
        mSDKINT = getSDKINT();
        mAndroidID = getAndroidID(context).toUpperCase();
        mIMEI = getIMEI(context);
        mWifiMac = getWifiMac(context).toUpperCase();
        mWifiMacIP = getWifiMacIP(context);
        mWifiMacFile = getWifiMacFile();
        mWifiMacMi = getWifiMacMi();
        mWifiSSID = getWifiSSID();
        mWifiBSSID = getWifiBSSID();
        mPhoneNum = getPhoneNum(context);
        mSerialNum = getSerialNum();
        mManufacture = getManufacture();
        mNetConnected = isConnected(context);
        mNetworkType = getNetworkType(context);
        mIMSI = getIMSI(context);
        mSimCarrier = getSimCarrier(context);
        mSDCardFreeSpace = getSDCardFreeSpace();
        mhasSDCard = hasSDCard();
        mScreenWidth = getScreenWidth(context);
        mScreenHeight = getScreenHeight(context);

        LinkedHashMap<String, String> maps = new LinkedHashMap<String, String>();
        maps.put("CPU_ABI", mCPU_ABI);
        maps.put("BuildNum", mBuildNum);
        maps.put("Model", mModel);
        maps.put("OSName", mOSName);
        maps.put("OSVer", mOSVer);
        maps.put("SDK INT", mSDKINT + "");
        maps.put("AndroidID", mAndroidID);
        maps.put("IMEI", mIMEI);
        maps.put("WifiMac", mWifiMac);
        maps.put("WifiMacIP", mWifiMacIP);
        maps.put("WifiMacFile", mWifiMacFile);
        maps.put("WifiMacMi", mWifiMacMi);
        maps.put("WifiSSID", mWifiSSID);
        maps.put("WifiBSSID", mWifiBSSID);
        maps.put("PhoneNum", mPhoneNum);
        maps.put("SerialNum", mSerialNum);
        maps.put("Manufacture", mManufacture);
        maps.put("NetConnected", mNetConnected ? "true" : "false");
        maps.put("NetworkType", mNetworkType);
        maps.put("SimCarrier", mSimCarrier);
        maps.put("HasSDCard", hasSDCard() ? "true" : "false");
        maps.put("SDCardFreeSpace", mSDCardFreeSpace + "");
        maps.put("ScreenWidth", mScreenWidth + "");
        maps.put("ScreenHeigh", mScreenHeight + "");

//        JSONObject info = new JSONObject();
//        try {
//            info.put("CPU_ABI", mCPU_ABI);
//            info.put("BuildNum", mBuildNum);
//            info.put("Model", mModel);
//            info.put("OSName", mOSName);
//            info.put("OSVer", mOSVer);
//            info.put("SDK INT", mSDKINT);
//            info.put("AndroidID", mAndroidID);
//            info.put("IMEI", mIMEI);
//            info.put("WifiMac", mWifiMac);
//            info.put("WifiMacIP", mWifiMacIP);
//            info.put("WifiMacFile", mWifiMacFile);
//            info.put("WifiMacMi", mWifiMacMi);
//            info.put("WifiSSID", mWifiSSID);
//            info.put("WifiBSSID", mWifiBSSID);
//            info.put("PhoneNum", mPhoneNum);
//            info.put("SerialNum", mSerialNum);
//            info.put("Manufacture", mManufacture);
//            info.put("NetConnected", mNetConnected);
//            info.put("NetworkType", mNetworkType);
//            info.put("SimCarrier", mSimCarrier);
//            info.put("HasSDCard", hasSDCard());
//            info.put("SDCardFreeSpace", mSDCardFreeSpace);
//            info.put("ScreenWidth", mScreenWidth);
//            info.put("ScreenHeigh", mScreenHeight);
//
//            strResult = info.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return maps;
    }

    public String getCPU_ABI() {
        String CPU_ABI = Build.CPU_ABI + "  " + Build.CPU_ABI2;
        return CPU_ABI;
    }

    public String getBuildNum() {
        String buildNum = Build.VERSION.INCREMENTAL;
        return buildNum;
    }

    public String getModel() {
        String model = Build.MODEL;
        return model;
    }

    public String getOSVer() {
        String romVer = Build.VERSION.RELEASE;
        return romVer;
    }

    public int getSDKINT() {
        int sdkInt = Build.VERSION.SDK_INT;
        return sdkInt;
    }

    public String getAndroidID(Context context) {
        String androidID = null;
        try {
            androidID = Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            androidID = null;
        }
        return androidID;
    }

    public String getIMEI(Context context) {
        String imei = null;
        try {
            imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            imei = null;
        }
        return imei;
    }

    public String getWifiMac(Context context) {
        String wifiMac = "";
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiMac = wifiManager.getConnectionInfo().getMacAddress();
            if (wifiMac != null) {
                wifiMac = wifiMac.toLowerCase();
            }
        } catch (Exception e) {
            wifiMac = "";
        }
        return wifiMac;
    }

    /**
     * 获取当前ip地址
     */
    public String getWifiMacIP(Context context) {
        String wifiMacIP = null;

        try {
            // for (Enumeration<NetworkInterface> en = NetworkInterface
            // .getNetworkInterfaces(); en.hasMoreElements();) {
            // NetworkInterface intf = en.nextElement();
            // for (Enumeration<InetAddress> enumIpAddr = intf
            // .getInetAddresses(); enumIpAddr.hasMoreElements();) {
            // InetAddress inetAddress = enumIpAddr.nextElement();
            // if (!inetAddress.isLoopbackAddress()) {
            // return inetAddress.getHostAddress().toString();
            // }
            // }
            // }
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipInt = wifiInfo.getIpAddress();

            StringBuilder sb = new StringBuilder();
            sb.append(ipInt & 0xFF).append(".");
            sb.append((ipInt >> 8) & 0xFF).append(".");
            sb.append((ipInt >> 16) & 0xFF).append(".");
            sb.append((ipInt >> 24) & 0xFF);
            wifiMacIP = sb.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return wifiMacIP;
    }

    public String getWifiMacFile() {
        String wifiMacFile = null;

        return wifiMacFile;
    }

    public String getWifiMacMi() {
        String wifiMacMi = null;
        return wifiMacMi;
    }

    public String getWifiSSID() {
        String wifiSSID = null;
        return wifiSSID;
    }

    public String getWifiBSSID() {
        String wifiBSSID = null;
        return wifiBSSID;
    }

    public String getPhoneNum(Context context) {
        String phoneNum = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNum = telephonyManager.getLine1Number();
        return phoneNum;
    }

    public String getSerialNum() {
        String serialNum = Build.SERIAL;
        return serialNum;
    }

    public String getManufacture() {
        String facture = Build.MANUFACTURER;
        return facture;
    }

    /**
     * 获取当前网络
     * http://blog.csdn.net/hknock/article/details/37650917
     */
    public String getNetworkType(Context context) {
        String netType = "unknown";
        try {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (activeNetworkInfo == null) {
                return netType;
            }
            if (activeNetworkInfo.getType() == 1) {
                netType = "WIFI";
            } else if (activeNetworkInfo.getType() == 0) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int networkType = telephonyManager.getNetworkType();
                    switch (networkType) {
                        case 1: {
                            netType = "GPRS";
                            break;
                        }
                        case 2: {
                            netType = "EDGE";
                            break;
                        }
                        case 3: {
                            netType = "UMTS";
                            break;
                        }
                        case 8: {
                            netType = "HSDPA";
                            break;
                        }
                        case 9: {
                            netType = "HSUPA";
                            break;
                        }
                        case 10: {
                            netType = "HSPA";
                            break;
                        }
                        case 4: {
                            netType = "CDMA";
                            break;
                        }
                        case 5: {
                            netType = "EVDO_0";
                            break;
                        }
                        case 6: {
                            netType = "EVDO_A";
                            break;
                        }
                        case 7: {
                            netType = "1xRTT";
                            break;
                        }
                        case 11: {
                            netType = "iDen";
                            break;
                        }
                        case 12: {
                            netType = "EVDO_B";
                            break;
                        }
                        case 13: {
                            netType = "LTE";
                            break;
                        }
                        case 14: {
                            netType = "eHRPD";
                            break;
                        }
                        case 15: {
                            netType = "HSPA+";
                            break;
                        }
                        default: {
                            netType = "MOBILE(" + networkType + ")";
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            netType = "unknown";
        }
        return netType;
    }

    public String getIMSI(Context context) {
        String IMSI = null;
        try {
            TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            IMSI = tManager.getSubscriberId();
        } catch (Exception e) {
            IMSI = null;
        }
        return IMSI;
    }

    /**
     * 获取Sim卡运营商
     **/
    public String getSimCarrier(Context context) {
        String simCarrier = "unknown";
        try {
            TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String IMSI = tManager.getSubscriberId();
            if (IMSI.startsWith("46000")) {
                simCarrier = "中国移动";
            } else if (IMSI.startsWith("46002")) {
                simCarrier = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                simCarrier = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                simCarrier = "中国电信";
            } else {
                simCarrier = "unknown";
            }

        } catch (Exception e) {

        }
        return simCarrier;
    }

    /**
     * 获取屏幕分辨率-宽x高
     *
     * @param context
     * @return
     */
    public String getResolution(Context context) {
        StringBuffer resolution = new StringBuffer("");
        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metric = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metric);
            int width = metric.widthPixels;  // 屏幕宽度（像素）
            int height = metric.heightPixels;  // 屏幕高度（像素）
            resolution.append(Integer.toString(width)).append(" x ").append(Integer.toString(height));
        } catch (Exception e) {
        }
        return resolution.toString();
    }

    /**
     * 获得锁屏时间  毫秒
     */
    private int getScreenOffTime(Context context) {
        int screenOffTime = 0;
        try {
            screenOffTime = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception localException) {

        }
        return screenOffTime;
    }

    /**
     * 设置背光时间  毫秒
     */
    private void setScreenOffTime(Context context, int paramInt) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}

