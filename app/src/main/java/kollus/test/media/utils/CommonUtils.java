package kollus.test.media.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import kollus.test.media.Config;
import kollus.test.media.download.DownloadService;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    public static int getDeviceWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.widthPixels;
    }

    public static int getDeviceHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.heightPixels;
    }

    public static String convertDownLoadStatus(int what) {
        switch (what) {
            case DownloadService.ADD_HANDLER:
                return "Download Handler init";
            case DownloadService.DOWNLOAD_START:
                return "Download start";
            case DownloadService.DOWNLOAD_LOADED:
                return "Download Loaded";
            case DownloadService.DOWNLOAD_ALREADY_LOADED:
                return "Download Already Loaded";
            case DownloadService.DOWNLOAD_STARTED:
                return "Download Started";
            case DownloadService.DOWNLOAD_LOAD_ERROR:
                return "Download Load Error";
            case DownloadService.DOWNLOAD_CANCEL:
                return "Download Cancel";
            case DownloadService.DOWNLOAD_ERROR:
                return "Download Error";
            case DownloadService.DOWNLOAD_PROCESS:
                return "Download Process";
            case DownloadService.DOWNLOAD_COMPLETE:
                return "Download Complete";
            case DownloadService.DOWNLOAD_DRM:
                return "Download DRM";
            case DownloadService.DOWNLOAD_DRM_INFO:
                return "Download DRM Info";
            default:
                return "";
        }
    }

    public static void setStreamVolume(Context context, boolean isVolumeUp) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (isVolumeUp) {
            manager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.RINGER_MODE_SILENT);
        } else {
            manager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.RINGER_MODE_SILENT);
        }
    }

    public static int getStreamVolume(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return manager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String createMultiDrmUrl(String mckey, String cuid) throws JSONException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, InvalidKeyException {

        // 1st - create license rule
        JSONObject playbackPolicy = new JSONObject();
        playbackPolicy.put("limit", true);
        playbackPolicy.put("persistent", false);
        playbackPolicy.put("duration", 86400);

        JSONObject token = new JSONObject();
        token.put("playback_policy", playbackPolicy);
        token.put("allow_mobile_abnormal_device", false);
        token.put("playready_security_level", 0);
        //LogUtil.d(TAG, "inkaToken: " + token.toString());

        // 2nd encryption license rule
        JwtUtil jwtUtil = new JwtUtil();
        String tokenStr = jwtUtil.AES_Encode(token.toString(), Config.SITE_KEY, Config.IV);
        LogUtil.d(TAG, "tokenStr: " + tokenStr);

        // 3nd create hash
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleClock = new SimpleDateFormat("hh:mm:ss");
        String currentTime = simpleDate.format(mDate) + "T" + simpleClock.format(mDate) + "Z";

        String hash_pre = Config.ACCESS_KEY + Config.DRM_TYPE + Config.SITE_ID + cuid
                + Config.CID + tokenStr + currentTime;
        String encoding_Hash = jwtUtil.baseEncodeToString(jwtUtil.createHash(hash_pre));
        LogUtil.d(TAG, "hash_pre: " + hash_pre);
        LogUtil.d(TAG, "create hash: " + encoding_Hash);

        // 4th - Generate License Token
        JSONObject inkaPayload = new JSONObject();
        inkaPayload.put("drm_type", Config.DRM_TYPE);
        inkaPayload.put("site_id", Config.SITE_ID);
        inkaPayload.put("user_id", cuid);
        inkaPayload.put("cid", Config.CID);
        inkaPayload.put("token", tokenStr);
        inkaPayload.put("timestamp", currentTime);
        inkaPayload.put("hash", encoding_Hash);

        String encoding_inkaPayload = jwtUtil.baseEncodeToString(inkaPayload.toString());
        //LogUtil.d(TAG, "encoding_inkaPayload : " + encoding_inkaPayload);

        // 5th Generate kollus jwt token
        JSONObject customHeader = new JSONObject();
        customHeader.put("key", "pallycon-customdata-v2");
        customHeader.put("value", encoding_inkaPayload);

        JSONObject data = new JSONObject();
        data.put("license_url", "https://license.pallycon.com/ri/licenseManager.do");
        data.put("certificate_url", "https://license.pallycon.com/ri/fpsKeyManager.do?siteId=" + Config.SITE_ID);
        data.put("custom_header", customHeader);

        JSONObject drmPolicy = new JSONObject();
        drmPolicy.put("kind", "inka");
        drmPolicy.put("streaming_type", Config.STREAMING_TYPE);
        drmPolicy.put("data", data);

        JSONObject mcObject = new JSONObject();
        mcObject.put("mckey", mckey);
        mcObject.put("drm_policy", drmPolicy);

        JSONArray mcArray = new JSONArray();
        mcArray.put(mcObject);

        JSONObject payload = new JSONObject();
        payload.put("expt", System.currentTimeMillis() + 3600);
        payload.put("cuid", cuid);
        payload.put("mc", mcArray);

        String strToken = jwtUtil.createJwt(payload.toString(), Config.DRM_SECURITY_KEY);

        LogUtil.d(TAG, "strToken : " + strToken);
        return String.format("https://v.jp.kollus.com/si?jwt=%s&custom_key=%s&purge_cache", strToken, Config.DRM_CUSTOM_KEY);
    }

    public static void startKollusApp(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.kollus.media");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startKollusApp(Context context, String url) {
        try {
            String scheme = "kollus://path" + "?url=" + url;
            LogUtil.d(TAG, scheme);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.kollus.media")));
        }
    }
}
