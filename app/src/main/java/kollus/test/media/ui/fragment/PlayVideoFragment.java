package kollus.test.media.ui.fragment;

import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;

import com.kollus.sdk.media.MediaPlayer;
import com.kollus.sdk.media.content.KollusContent;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import kollus.test.media.R;
import kollus.test.media.player.CustomPlayer;
import kollus.test.media.player.KollusConstant;
import kollus.test.media.utils.CommonUtils;
import kollus.test.media.utils.LogUtil;

import static com.kollus.sdk.media.KollusStorage.TYPE_CACHE;
import static kollus.test.media.Config.MODE_MAKE_JWT;


public class PlayVideoFragment extends BaseFragment implements View.OnClickListener, MediaController.MediaPlayerControl {

    private static final String TAG = PlayVideoFragment.class.getSimpleName();

    private AudioManager mAudioManager = null;
    private MediaPlayer mMediaPlayer = null;
    private SurfaceView mSurfaceView = null;
    private CustomPlayer mPlayer = null;
    private TextView mLogTextView = null;

    public int playType = TYPE_CACHE;
    public String jwtUrl = "";

    public static PlayVideoFragment newInstance() {
        return new PlayVideoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        LogUtil.d(TAG, "onCreateView");

        if (getArguments() != null) {
            playType = getArguments().getInt("playType");
            jwtUrl = getArguments().getString("urlOrMcKey");

        } else {
            if (MODE_MAKE_JWT) {
                String mckey = "LwjBx2pY";
                String cuid = "catenoidtest";
                try {
                    jwtUrl = CommonUtils.createMultiDrmUrl(mckey, cuid);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        View root = inflater.inflate(R.layout.fragment_playvideo, container, false);

        mSurfaceView = (SurfaceView) root.findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(surfaceCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mSurfaceView.setSecure(true);
        }
        mMediaPlayer = new MediaPlayer(getContext(), mStorage, KollusConstant.PORT);
        mPlayer = new CustomPlayer(getContext(), mMediaPlayer, mSurfaceView);

        mLogTextView = (TextView) root.findViewById(R.id.control_log);

        root.findViewById(R.id.play).setOnClickListener(this);
        root.findViewById(R.id.pause).setOnClickListener(this);
        root.findViewById(R.id.rate_up).setOnClickListener(this);
        root.findViewById(R.id.rate_down).setOnClickListener(this);
        root.findViewById(R.id.volume_up).setOnClickListener(this);
        root.findViewById(R.id.volume_down).setOnClickListener(this);
        root.findViewById(R.id.mute).setOnClickListener(this);
        root.findViewById(R.id.un_mute).setOnClickListener(this);
        root.findViewById(R.id.ff).setOnClickListener(this);
        root.findViewById(R.id.rw).setOnClickListener(this);
        root.findViewById(R.id.restart).setOnClickListener(this);
        root.findViewById(R.id.callApp).setOnClickListener(this);

        mMediaPlayer = new MediaPlayer(getContext(), mStorage, 7740);
        mPlayer = new CustomPlayer(getContext(), mMediaPlayer, mSurfaceView);

        return root;
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceCreated()");

            if (mPlayer != null && holder != null) {
                mPlayer.getMediaPlayer().setDisplay(holder);
                mPlayer.setDataSource(playType, jwtUrl);
                mPlayer.prepareAsync();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            LogUtil.d(TAG, "surfaceChanged() width : " + width + "  height : " + height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceDestroyed()");
            if (mMediaPlayer != null) {
                mMediaPlayer.destroyDisplay();
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        LogUtil.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtil.d(TAG, "onPause");
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        if (mPlayer != null) {
            mPlayer.finish();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                setLogText("play");
                mPlayer.start();
                break;
            case R.id.pause:
                setLogText("pause");
                mPlayer.pause();
                break;
            case R.id.rate_up:
                mPlayer.setPlayingRate(1);
                setLogText("Rate Up : " + String.format("%.1f", mPlayer.getPlayingRate()));
                break;
            case R.id.rate_down:
                mPlayer.setPlayingRate(-1);
                setLogText("Rate Down : " + String.format("%.1f", mPlayer.getPlayingRate()));
                break;
            case R.id.volume_up:
                CommonUtils.setStreamVolume(getContext(), true);
                mPlayer.setVolumeLevel(CommonUtils.getStreamVolume(getContext()));
                setLogText("Volume up : " + CommonUtils.getStreamVolume(getContext()));
                break;
            case R.id.volume_down:
                CommonUtils.setStreamVolume(getContext(), false);
                mPlayer.setVolumeLevel(CommonUtils.getStreamVolume(getContext()));
                setLogText("Volume down : " + CommonUtils.getStreamVolume(getContext()));
                break;
            case R.id.ff:
                mPlayer.setFF();
                setLogText("setFF(10) : " + mPlayer.getMediaPlayer().getCurrentPosition() + "ms");
                break;
            case R.id.rw:
                mPlayer.setRW();
                setLogText("setRW(10) : " + mPlayer.getMediaPlayer().getCurrentPosition() + "ms");
                break;
            case R.id.mute:
                mPlayer.setMute(true);
                setLogText("set Mute");
                break;
            case R.id.un_mute:
                mPlayer.setMute(false);
                setLogText("set unMute");
                break;
            case R.id.restart:
                mPlayer.prepareAsync();
                setLogText("re start");
                break;
            case R.id.callApp:
                setLogText("call Kollus App");
                CommonUtils.startKollusApp(getContext(), jwtUrl);
                break;
            default:
                break;
        }
    }

    public void setLogText(String log) {
        if (log != null && mLogTextView != null) {
            mLogTextView.setText(log);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
