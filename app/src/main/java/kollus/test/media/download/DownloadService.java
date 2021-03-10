package kollus.test.media.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kollus.sdk.media.KollusPlayerDRMListener;
import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.KollusStorage.OnKollusStorageListener;
import com.kollus.sdk.media.content.KollusContent;
import com.kollus.sdk.media.util.ErrorCodes;
import com.kollus.sdk.media.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kollus.test.media.utils.LogUtil;

public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();

    public static final int ADD_HANDLER = 0;
    public static final int DOWNLOAD_START = 10;
    public static final int DOWNLOAD_LOADED = 11;
    public static final int DOWNLOAD_ALREADY_LOADED = 12;
    public static final int DOWNLOAD_STARTED = 13;
    public static final int DOWNLOAD_LOAD_ERROR = 14;
    public static final int DOWNLOAD_CANCEL = 20;
    public static final int DOWNLOAD_CANCELED = 21;
    public static final int DOWNLOAD_ERROR = 30;
    public static final int DOWNLOAD_PROCESS = 40;
    public static final int DOWNLOAD_COMPLETE = 50;
    public static final int DOWNLOAD_DRM = 60;
    public static final int DOWNLOAD_DRM_INFO = 61;

    private KollusStorage mStorage;
    Messenger mMessenger = new Messenger(new LocalHandler());
    Messenger mClientMessenger;
    List<Handler> mHandlers;
    private ExecutorService mExecutor;
    List<DownloadInfo> mDownloadList;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlers = new ArrayList<Handler>();
        mDownloadList = new ArrayList<DownloadInfo>();
        mStorage = KollusStorage.getInstance(getApplicationContext());

//        mStorage.setOnKollusStorageListener(mKollusStorageListener);
        mStorage.registerKollusStorageListener(mKollusStorageListener);
        mStorage.registerKollusPlayerDRMListener(mKollusPlayerDRMListener);
        mExecutor = Executors.newFixedThreadPool(1);
        Log.d(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
//		return super.onStartCommand(intent, flags, startIdonComplete);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (mStorage != null) {
            mStorage.unregisterKollusStorageListener(mKollusStorageListener);
            mStorage.unregisterKollusPlayerDRMListener(mKollusPlayerDRMListener);
        }
        super.onDestroy();
    }

    /*
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        synchronized (mDownloadList) {
            for (DownloadInfo info : mDownloadList) {
                String mcKey = info.getKollusContent().getMediaContentKey();
                if (mcKey != null) {
                    Log.d(TAG, "mcKey : " + mcKey);
                    mStorage.unload(mcKey);
                }
            }
            mDownloadList.clear();
        }
        return super.onUnbind(intent);
    }
    */

    private class LocalHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_HANDLER: {
                    mClientMessenger = new Messenger((Handler) msg.obj);
                    try {
                        mClientMessenger.send(Message.obtain(null, ADD_HANDLER, "Registed messanger"));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;

                case DOWNLOAD_START: {
                    DownloadInfo info = (DownloadInfo) msg.obj;
                    LoadTask task = new LoadTask(info);
                    mExecutor.execute(task);
                }
                break;

                case DOWNLOAD_CANCEL: {
                    String mediaContentKey = (String) msg.obj;
                    LogUtil.d(TAG, "DOWNLOAD_CANCEL:" + mediaContentKey);
                    try {
                        mStorage.unload(mediaContentKey);

                        synchronized (mDownloadList) {

                            if (mDownloadList != null && !mDownloadList.isEmpty() && mDownloadList.get(0).getKollusContent().getMediaContentKey().equals(mediaContentKey))
                                nextDownload();
                            else
                                removeDownloadList(msg.arg1);
                        }

                        mClientMessenger.send(Message.obtain(null, DOWNLOAD_CANCELED, 0, 0, mediaContentKey));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private class LoadTask implements Runnable {
        private DownloadInfo mInfo;

        LoadTask(DownloadInfo info) {
            mInfo = info;
        }

        @Override
        public void run() {
            try {
                synchronized (mDownloadList) {
                    for (DownloadInfo info : mDownloadList) {
                        if (info.getUrl().equals(mInfo.getUrl())) {
                            Log.w(TAG, "Already exists in DownloadList");
                            mClientMessenger.send(Message.obtain(null, DOWNLOAD_ALREADY_LOADED));
                            return;
                        }
                    }
                }

                int nErrorCode = mStorage.load(mInfo.getUrl(), "1234Test", mInfo.getKollusContent());
                if (nErrorCode != ErrorCodes.ERROR_OK) {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_LOAD_ERROR, 0, nErrorCode));
                } else {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_LOADED, 0, 0, mInfo));
                    synchronized (mDownloadList) {
                        if (mDownloadList.isEmpty()) {
                            int nRet = mStorage.download(mInfo.getKollusContent().getMediaContentKey());
                            LogUtil.d(TAG, "Send Message Start index " + mInfo.getKollusContent().getUriIndex() + " return " + nRet);
                            if (nRet >= 0) {
                                mClientMessenger.send(Message.obtain(null, DOWNLOAD_STARTED, 0, 0, mInfo.getKollusContent()));
                            } else {
                                mClientMessenger.send(Message.obtain(null, DOWNLOAD_ERROR, 0, nRet, mInfo.getKollusContent()));
                            }

                        }

                        mDownloadList.add(mInfo);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    OnKollusStorageListener mKollusStorageListener = new OnKollusStorageListener() {
        @Override
        public void onComplete(KollusContent content) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_COMPLETE, 0, 0, content));
                    synchronized (mDownloadList) {
                        nextDownload();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onProgress(KollusContent content) {
            Log.w(TAG, "onProgress....");
            if (mClientMessenger != null) {
                try {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_PROCESS, 0, 0, content));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onError(KollusContent content, int errorCode) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_ERROR, 0, errorCode, content));
                    synchronized (mDownloadList) {
                        nextDownload();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void nextDownload() throws RemoteException {
        while (!mDownloadList.isEmpty()) {
            mDownloadList.remove(0);

            if (mDownloadList.isEmpty()) {
                break;
            } else {
                int nRet = mStorage.download(mDownloadList.get(0).getKollusContent().getMediaContentKey());
                if (nRet >= 0) {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_STARTED, 0, 0, mDownloadList.get(0).getKollusContent()));
                    break;
                } else {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_ERROR, 0, nRet, mDownloadList.get(0).getKollusContent()));
                }
            }
        }
    }

    private void removeDownloadList(int index) {
        for (DownloadInfo info : mDownloadList) {
            if (info.getKollusContent().getUriIndex() == index) {
                mDownloadList.remove(info);
                break;
            }
        }
    }

    KollusPlayerDRMListener mKollusPlayerDRMListener = new KollusPlayerDRMListener() {
        @Override
        public void onDRM(String request, String response) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_DRM, new DownloadDRM(request, response)));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDRMInfo(KollusContent content, int nInfoCode) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger.send(Message.obtain(null, DOWNLOAD_DRM_INFO, 0, nInfoCode, content));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}

