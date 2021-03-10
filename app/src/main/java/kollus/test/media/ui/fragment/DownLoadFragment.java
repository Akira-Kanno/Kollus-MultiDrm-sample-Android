package kollus.test.media.ui.fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kollus.sdk.media.content.FileManager;
import com.kollus.sdk.media.content.KollusContent;
import com.kollus.sdk.media.util.KollusUri;
import com.kollus.sdk.media.util.Log;
import com.kollus.sdk.media.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Set;

import kollus.test.media.Config;
import kollus.test.media.R;
import kollus.test.media.download.DownloadInfo;
import kollus.test.media.download.DownloadService;
import kollus.test.media.utils.CommonUtils;
import kollus.test.media.utils.LogUtil;

import static kollus.test.media.Config.MODE_MAKE_JWT;


public class DownLoadFragment extends BaseFragment {

    private static final String TAG = DownLoadFragment.class.getSimpleName();

    private static final int DATA_DOWNLOAD = 101;
    private static final int DATA_DOWNLOAD_CANCEL = 102;
    private static final int CHECK_EXIT = 103;

    private FileManager mFileManager;
    private ArrayList<KollusContent> mContentsList;
    private Messenger mMessenger;
    private boolean mBounded;
    private TextView mLogContenTitle;
    private TextView mLogView;
    private TextView contentTitle;
    private TextView contentProgress;
    private PowerManager.WakeLock wakeLock = null;


    public String jwtUrl = "";

    public static DownLoadFragment newInstance() {
        return new DownLoadFragment();
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreateView");

        View root = inflater.inflate(R.layout.fragment_download, container, false);
        mLogView = (TextView) root.findViewById(R.id.log_tv);
        contentProgress = (TextView) root.findViewById(R.id.log_progress);
        contentTitle = (TextView) root.findViewById(R.id.log_title);

        mContentsList = mStorage.getDownloadContentList();
        mFileManager = new FileManager(FileManager.DIRECTORY);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        if (!mBounded) {
            Intent intent = new Intent(getActivity(), DownloadService.class);
            getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onResume() {
        LogUtil.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        super.onDestroy();
        if (mBounded) {
            LogUtil.d(TAG, "unbindService");
            getContext().unbindService(mConnection);
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "onServiceDisconnected");
            mBounded = false;
            mMessenger = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "onServiceConnected");

            mMessenger = new Messenger(service);
            mBounded = true;

            try {
                mMessenger.send(Message.obtain(null, DownloadService.ADD_HANDLER, new ClientHandler()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_DOWNLOAD:
                    startDownload((String) msg.obj);
                    break;
//                case DATA_DOWNLOAD_CANCEL:
//                    alertCancelDownload(msg.arg1 == 1);
//                    break;
//                case CHECK_EXIT:
//                    mExit = false;
//                    break;
            }
            super.handleMessage(msg);
        }
    };

    public class ClientHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            LogUtil.d(TAG, "handleMessage msg.what : " + msg.what);
            mLogView.setText(mLogView.getText() + "\n" + CommonUtils.convertDownLoadStatus(msg.what));
            switch (msg.what) {
                case DownloadService.ADD_HANDLER:
                    if (MODE_MAKE_JWT) {
                        String mckey = "LwjBx2pY";
                        String cuid = "catenoidtest";
                        try {
                            jwtUrl = CommonUtils.createMultiDrmUrl(mckey, cuid);
                            jwtUrl += "&download";
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    startDownload(jwtUrl);
                    break;
                case DownloadService.DOWNLOAD_LOADED: {
                    DownloadInfo info = (DownloadInfo) msg.obj;
                    KollusContent content = info.getKollusContent();

                    boolean exist = false;
                    for (KollusContent iter : mContentsList) {
                        if (iter.getMediaContentKey().equals(content.getMediaContentKey())) {
                            exist = true;
                            break;
                        }
                    }

                    if (!exist) {
                        FileManager downloadLocation = mFileManager;
                        if (info.getFolder() != null) {
                            String[] items = info.getFolder().split("/");
                            for (String item : items) {
                                downloadLocation = downloadLocation.addDirectory(item);
                            }
                        }

                        downloadLocation.addFile(0, content.getMediaContentKeyMD5());
                        saveFileManager();

                        mContentsList.add(content);
                    }
                    content.setDownloading(true);
                }
                break;
                case DownloadService.DOWNLOAD_ALREADY_LOADED:
                    break;
                case DownloadService.DOWNLOAD_STARTED:
                    break;
                case DownloadService.DOWNLOAD_CANCELED: {
                    String mediaContentKey = (String) msg.obj;
                    for (KollusContent content : mContentsList) {
                        if (content.getMediaContentKey().equals(mediaContentKey)) {
                            LogUtil.d(TAG, "DOWNLOAD_CANCELED:" + content.getMediaContentKey());
                            content.setDownloadCanceled();
                            content.setDownloading(false);
                        }
                    }
                }
                break;
                case DownloadService.DOWNLOAD_LOAD_ERROR:
                    break;
                case DownloadService.DOWNLOAD_ERROR:
                    int errorCode = msg.arg2;
                    mLogView.setText(mLogView.getText() + " " + errorCode);
                    break;
                case DownloadService.DOWNLOAD_PROCESS: {
                    KollusContent content = (KollusContent) msg.obj;
                    if (content != null) {
                        int percent = (int) (content.getReceivingSize() * 100 / content.getFileSize());
                        contentTitle.setText("Title : " + content.getSubCourse() + " (" + content.getMediaContentKey() + ")");
                        contentProgress.setText("Process: " + content.getDownloadPercent() + "%");
                        content.setDownloading(true);
                        content.setReceivedSize(content.getReceivingSize());
                        content.setDownloadPercent(percent);
                    }
                }
                break;
                case DownloadService.DOWNLOAD_COMPLETE:
                    KollusContent content = (KollusContent) msg.obj;
                    mStorage.getKollusContent(content, content.getMediaContentKey());
                    content.setDownloadCompleted(true);
                    //int percent = (int) (content.getReceivingSize() * 100 / content.getFileSize());
                    //content.setReceivedSize(content.getReceivingSize());
                    //content.setDownloadPercent(percent);
                    break;
                case DownloadService.DOWNLOAD_DRM:
                    break;
                case DownloadService.DOWNLOAD_DRM_INFO:
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }

    private void startDownload(String url) {
        KollusUri uri = KollusUri.parse(url);
        String location = null;
        String path;
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            path = url.substring(0, queryIndex);
        } else {
            path = url;
        }

        Set<String> keySet = uri.getQueryParameterNames();
        boolean first = true;
        for (String key : keySet) {
            LogUtil.d(TAG, String.format("startDownload '%s' ==> '%s'", key, uri.getQueryParameter(key)));
            if (key.equalsIgnoreCase("folder")) {
                location = Uri.decode(uri.getQueryParameter(key));
            } else {
                if (first)
                    path += "?";
                else
                    path += "&";

                path += key;
                path += "=";
                path += uri.getQueryParameter(key);

                first = false;
            }
        }

        FileManager downloadLocation = mFileManager;
        if (location != null) {
            String[] items = location.split("/");
            for (String item : items) {
                downloadLocation = downloadLocation.addDirectory(item);
            }
        }

        LogUtil.d(TAG, "startDownload downStart --> folder [" + location + "] url [" + path + "]");
        DownloadInfo info = new DownloadInfo(location, path);
        try {
            mMessenger.send(Message.obtain(null, DownloadService.DOWNLOAD_START, info));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void cancelDownLoad() {
        try {
            mMessenger.send(Message.obtain(null, DownloadService.DOWNLOAD_CANCEL,
                    0, 0, "7vseZe0V"));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void saveFileManager() {
        JSONObject root = new JSONObject();
        try {
            FileManager.save(root, mFileManager);
            Utils.saveDirectoryJSON(getContext(), getStoragePath(getContext()), root.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getStoragePath(Context context) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + context.getPackageName();
        String storagePath = preference.getString("storage_location", defaultPath);
        return storagePath;
    }
}
