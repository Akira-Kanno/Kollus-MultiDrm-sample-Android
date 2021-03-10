package kollus.test.media.player;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.kollus.sdk.media.KollusPlayerContentMode;
import com.kollus.sdk.media.KollusPlayerLMSListener;
import com.kollus.sdk.media.MediaPlayer;
import com.kollus.sdk.media.MediaPlayer.OnBufferingUpdateListener;
import com.kollus.sdk.media.content.BandwidthItem;
import com.kollus.sdk.media.content.KollusContent;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import kollus.test.media.utils.LogUtil;

public class CustomPlayer {
    private static final String TAG = CustomPlayer.class.getSimpleName();

    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private SurfaceView mSurfaceVew;
    private int mPlayType;
    private String mSourceUrl;
    private float mPlayingRate = 1.0f;

    public CustomPlayer(Context context, MediaPlayer mediaPlayer, SurfaceView surfaceView) {
        this.mContext = context;
        this.mMediaPlayer = mediaPlayer;
        this.mSurfaceVew = surfaceView;

        initListener();
    }

    private void initListener() {
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mMediaPlayer.setKollusPlayerLMSListener(kollusPlayerLMSListener);
        mMediaPlayer.setOnTimedTextDetectListener(onTimedTextDetectListener);
        mMediaPlayer.setOnTimedTextListener(onTimedTextListene);
        mMediaPlayer.setOnInfoListener(infoListener);
        mMediaPlayer.setOnBufferingUpdateListener(bufferingUpdateListener);
    }

    public void start() {
        LogUtil.d(TAG, "start()");
        if (mMediaPlayer == null) {
            return;
        }

        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void prepareAsync() {
        if (mMediaPlayer == null)
            return;

        if (TextUtils.isEmpty(mSourceUrl)) {
            LogUtil.d(TAG, "mSourceUrl is empty");
            return;
        }

        LogUtil.d(TAG, "mSourceUrl : " + mSourceUrl);

        if (mPlayType == 0) {
            mMediaPlayer.setDataSourceByUrl(mSourceUrl, "");
        } else {
            mMediaPlayer.setDataSourceByKey(mSourceUrl, "");
        }
        mMediaPlayer.prepareAsync();
    }

    public void pause() {
        LogUtil.d(TAG, "pause()");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public boolean setPlayingRate(int mode) {
        boolean result = false;

        switch (mode) {
            case -1:
                mPlayingRate -= 0.1f;
                break;
            case 1:
                mPlayingRate += 0.1f;
                break;
            default:
                mPlayingRate = 1;
                break;
        }

        if (mPlayingRate <= 0.5f) {
            mPlayingRate = 0.5f;
        }

        if (mPlayingRate >= 2.0f) {
            mPlayingRate = 2.0f;
        }

        if (mMediaPlayer != null) {
            LogUtil.d(TAG, "mPlayingRate : " + mPlayingRate);
            result = mMediaPlayer.setPlayingRate(mPlayingRate);
        }

        return result;
    }

    public void setVolumeLevel(int level) {
        if (mMediaPlayer != null)
            mMediaPlayer.setVolumeLevel(level);
    }

    public void setMute(boolean mute) {
        if (mMediaPlayer != null)
            mMediaPlayer.setMute(mute);
    }

    public void setFF() {
        if (mMediaPlayer != null) {
            LogUtil.d(TAG, "setFF() : getCurrentPosition : " + mMediaPlayer.getCurrentPosition());
            int sec = mMediaPlayer.getCurrentPosition() + 300000;
            mMediaPlayer.seekToExact(sec);
        }
    }

    public void setRW() {
        if (mMediaPlayer != null) {
            LogUtil.d(TAG, "setRW() : getCurrentPosition : " + mMediaPlayer.getCurrentPosition());
            int sec = mMediaPlayer.getCurrentPosition() - 300000;
            mMediaPlayer.seekToExact(sec);
        }
    }

    public int getPlayAt() {
        if (mMediaPlayer != null) {
            LogUtil.d(TAG, "getPlayAt() : getPlayAt : " + mMediaPlayer.getPlayAt());
            return mMediaPlayer.getPlayAt();
        }

        return -1;

    }

    public void release() {
        LogUtil.d(TAG, "release()");
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.release();
    }

    public void finish() {
        LogUtil.d(TAG, "finish()");
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public SurfaceView getSurfaceVew() {
        return mSurfaceVew;
    }

    public String getSourceUrl() {
        return mSourceUrl;
    }

    public float getPlayingRate() {
        return mPlayingRate;
    }

    public void setDataSource(int type, String url) {
        mPlayType = type;
        mSourceUrl = url;
    }

    private void setSizeSurfaceView(MediaPlayer mediaPlayer) {
        mSurfaceVew.getHolder().setFixedSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());

        LogUtil.d(TAG, "onPrepared() - getWidth : " + mSurfaceVew.getWidth());
        LogUtil.d(TAG, "onPrepared() - getHeight : " + mSurfaceVew.getHeight());
        LogUtil.d(TAG, "onPrepared() - left : " + mSurfaceVew.getLeft());
        LogUtil.d(TAG, "onPrepared() - right : " + mSurfaceVew.getRight());
        LogUtil.d(TAG, "onPrepared() - top : " + mSurfaceVew.getTop());
        LogUtil.d(TAG, "onPrepared() - bottom : " + mSurfaceVew.getBottom());

        int mVideoWidth = mediaPlayer.getVideoWidth();
        int mVideoHeight = mediaPlayer.getVideoHeight();
        int displayWidth = mSurfaceVew.getWidth();
        int displayHeight = mSurfaceVew.getHeight();

        int l = mSurfaceVew.getLeft();
        int r = mSurfaceVew.getRight();
        int t = mSurfaceVew.getTop();
        int b = mSurfaceVew.getBottom();

        if (mVideoWidth * displayHeight > displayWidth * mVideoHeight) {
            displayHeight = displayWidth * mVideoHeight / mVideoWidth;
        } else if (mVideoWidth * displayHeight < displayWidth * mVideoHeight) {
            displayWidth = displayHeight * mVideoWidth / mVideoHeight;
        }

        l = (r - l - displayWidth) / 2;
        r = l + displayWidth;
        t = (b - t - displayHeight) / 2;
        b = t + displayHeight;

        mSurfaceVew.layout(l, t, r, b);
    }

    public MediaPlayer.OnPreparedListener onPreparedListener = mediaPlayer -> {
        if (mediaPlayer != null) {
            setSizeSurfaceView(mediaPlayer);
            LogUtil.d(TAG, "onPrepared() - getPlayAt() : " + mediaPlayer.getPlayAt());
            mediaPlayer.start();
        }
    };

    public MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            LogUtil.d(TAG, "onCompletion()");

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
    };

    public MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            LogUtil.d(TAG, "onError() what : " + what + " extra : " + extra);

            if (mediaPlayer != null) {
                LogUtil.d(TAG, "errorMsg : " + mediaPlayer.getErrorString(extra));
            }

            return false;
        }
    };

    public MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {

        @Override
        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
            LogUtil.d(TAG, "onVideoSizeChanged() width : " + width + "/ height : " + height);

            LogUtil.d(TAG, String.format("onVideoSizeChanged (%d %d) dimension(%d %d)",
                    width, height, mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight()));
            if (mediaPlayer.getVideoWidth() != 0 && mediaPlayer.getVideoHeight() != 0) {
                mSurfaceVew.getHolder().setFixedSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
                mSurfaceVew.requestLayout();
            }
        }
    };

    private KollusPlayerLMSListener kollusPlayerLMSListener = new KollusPlayerLMSListener() {
        @Override
        public void onLMS(String request, String response) {
            LogUtil.d(TAG, "request : " + request + " / response : " + response);
        }
    };

    private MediaPlayer.OnTimedTextDetectListener onTimedTextDetectListener = new MediaPlayer.OnTimedTextDetectListener() {
        @Override
        public void onTimedTextDetect(MediaPlayer mediaPlayer, int i) {
            LogUtil.d(TAG, "onTimedTextDetect() : " + i);

        }
    };
    private MediaPlayer.OnTimedTextListener onTimedTextListene = new MediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(MediaPlayer mediaPlayer, String s) {
            LogUtil.d(TAG, "OnTimedTextListener() : " + s);

        }

        @Override
        public void onTimedImage(MediaPlayer mediaPlayer, byte[] bytes, int i, int i1) {

        }
    };

    private OnBufferingUpdateListener bufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
            LogUtil.d(TAG, "onBufferingUpdate() percent : " + percent);
        }

    };

    private MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
            LogUtil.d(TAG, "onInfo() i : " + what + " , i1 : " + extra);
            return false;
        }

        @Override
        public void onBufferingStart(MediaPlayer mediaPlayer) {
            LogUtil.d(TAG, "onBufferingStart()");

        }

        @Override
        public void onBufferingEnd(MediaPlayer mediaPlayer) {
            LogUtil.d(TAG, "onBufferingEnd()");

        }

        @Override
        public void onFrameDrop(MediaPlayer mediaPlayer) {
            LogUtil.d(TAG, "onFrameDrop()");

        }

        @Override
        public void onDownloadRate(MediaPlayer mediaPlayer, int i) {
            LogUtil.d(TAG, "onDownloadRate()");

        }

        @Override
        public void onDetectBandwidthList(MediaPlayer mediaPlayer, List<BandwidthItem> list) {
            LogUtil.d(TAG, "onDetectBandwidthList()");

        }

        @Override
        public void onChangedBandwidth(MediaPlayer mediaPlayer, BandwidthItem bandwidthItem) {
            LogUtil.d(TAG, "onChangedBandwidth()");

        }

        @Override
        public void onCodecInitFail(MediaPlayer mediaPlayer, String s) {

        }
    };

}
