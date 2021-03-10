package kollus.test.media.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.StoredLMSListener;
import com.kollus.sdk.media.content.KollusContent;

import java.util.ArrayList;

import kollus.test.media.MainActivity;
import kollus.test.media.R;
import kollus.test.media.ui.adapter.DownloadAdapter;
import kollus.test.media.utils.LogUtil;


public class ContentsListFragment extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = ContentsListFragment.class.getSimpleName();

    private KollusStorage mStorage = null;
    private ListView mListView = null;
    private DownloadAdapter mAdapter;
    private ArrayList<KollusContent> mDownLoadList = new ArrayList();


    public static ContentsListFragment newInstance() {
        return new ContentsListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreateView");

        View root = inflater.inflate(R.layout.fragment_contentlist, container, false);

        mStorage = KollusStorage.getInstance(getContext());
        mDownLoadList = mStorage.getDownloadContentList();

        mListView = (ListView) root.findViewById(R.id.contents_list);
        mAdapter = new DownloadAdapter(getContext(), mDownLoadList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        return root;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtil.d(TAG, "onItemClick() position : " + position);

        KollusContent content = mAdapter.getItem(position);

        if (content != null) {
            ((MainActivity) getActivity()).replaceFragment(PlayVideoFragment.newInstance(), 1, content.getMediaContentKey());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtil.d(TAG, "onItemLongClick() position : " + position);

        final KollusContent content = mAdapter.getItem(position);
        if (content != null) {
            LogUtil.d(TAG, "onItemClick() position : " + content.getDetailInfoUrl());
            LogUtil.d(TAG, "onItemClick() position : " + content.getMediaUrl());
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm").setMessage("Delete download content?");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int errorCode = mStorage.remove(content.getMediaContentKey());
                    LogUtil.d(TAG, "errorCode : " + errorCode);
                    mDownLoadList.remove(content);
                    mAdapter.notifyDataSetChanged();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;
        }
        return false;
    }
}
