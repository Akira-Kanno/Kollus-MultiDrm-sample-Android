package kollus.test.media.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.util.Utils;

import kollus.test.media.MainActivity;
import kollus.test.media.player.KollusConstant;
import kollus.test.media.utils.LogUtil;

public class BaseFragment extends Fragment implements MainActivity.OnBackPressedListener {

    private static final String TAG = BaseFragment.class.getSimpleName();

    protected KollusStorage mStorage = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        initStorage();
        super.onCreate(savedInstanceState);
    }

    public void initStorage() {
        LogUtil.d(TAG, "initStorage");
        mStorage = KollusStorage.getInstance(getContext());
        LogUtil.d(TAG, mStorage.getVersion());

        if (!mStorage.isReady()) {
            LogUtil.d(TAG, "mStorage is not ready");
            int errorCode = mStorage.initialize(KollusConstant.KEY, KollusConstant.EXPIRE_DATE, getContext().getPackageName());
            int nRet = mStorage.setDevice(Utils.getStoragePath(getContext()), Utils.getPlayerId(getContext()),
                    Utils.getPlayerIdMd5(getContext()), Utils.isTablet(getContext()));


            LogUtil.d(TAG, "KollusStorage version : " + mStorage.getVersion());

            //public static final int ERROR_EXPIRED_KEY = -2;
            //public static final int ERROR_INVALID_KEY = -1;
            //public static final int ERROR_OK = 0;

            LogUtil.d(TAG, "KollusStorage init errorCode : " + errorCode);
            LogUtil.d(TAG, "KollusStorage setDevice : " + nRet);
        }

        mStorage.setNetworkTimeout(KollusConstant.NETWORK_TIMEOUT_SEC, KollusConstant.NETWORK_RETRY_COUNT);

        LogUtil.d(TAG, "mStorage ready : " + mStorage.isReady());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

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
    }

    @Override
    public void onDestroyView() {
        LogUtil.d(TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
