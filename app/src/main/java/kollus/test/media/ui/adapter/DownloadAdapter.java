package kollus.test.media.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kollus.sdk.media.content.KollusContent;

import java.util.ArrayList;

import kollus.test.media.R;

public class DownloadAdapter extends ArrayAdapter<KollusContent> {
    private static final String TAG = DownloadAdapter.class.getSimpleName();

    private Resources mResources;
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<KollusContent> mContentsList;


    public DownloadAdapter(Context context, ArrayList<KollusContent> contentsList) {
        super(context, R.layout.file_list, contentsList);

        mContext = context;
        mResources = context.getResources();
        mInflater = LayoutInflater.from(context);
        mContentsList = contentsList;
    }

    public int getCount() {
        if (mContentsList == null)
            return 0;

        return mContentsList.size();
    }

    public KollusContent getItem(int position) {
        return mContentsList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ContentsViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_list, parent, false);

            holder = new ContentsViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.txtPercent = (TextView) convertView.findViewById(R.id.list_percent);
            holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            holder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            holder.timeBar = (ProgressBar) convertView.findViewById(R.id.download_progress);
            holder.btnDelete = (ImageView) convertView.findViewById(R.id.download_cancel);

            convertView.setTag(holder);
        } else {
            holder = (ContentsViewHolder) convertView.getTag();
        }

        KollusContent content = mContentsList.get(position);
        String thumbnail = content.getThumbnailPath();
        if (thumbnail != null && !thumbnail.startsWith("http://")) {
            Bitmap bm = BitmapFactory.decodeFile(thumbnail);
            if (bm != null)
                holder.icon.setImageBitmap(bm);
        }
        holder.btnDelete.setTag(content);

        String cource = content.getCourse();
        String subcource = content.getSubCourse();
        String title;
        if (cource != null && cource.length() > 0) {
            if (subcource != null && subcource.length() > 0)
                title = cource + "(" + subcource + ")";
            else
                title = cource;
        } else
            title = subcource;
        holder.fileName.setText(title);
        if (content.getDownloadError()) {
            holder.fileName.setTextColor(Color.RED);
            holder.fileName.setPaintFlags(holder.fileName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.fileName.setTextColor(Color.BLACK);
            holder.fileName.setPaintFlags(holder.fileName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        String strRecvFileSize = String.valueOf(content.getReceivedSize());
        String strFileSize = String.valueOf(content.getFileSize());
        holder.fileSize.setText(String.format("%s / %s", strRecvFileSize, strFileSize));
        holder.timeBar.setProgress(content.getDownloadPercent());
        holder.btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//				view.setVisibility(View.GONE);
            }
        });

        if (content.isCompleted()) {
            holder.txtPercent.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.txtPercent.setVisibility(View.VISIBLE);
            holder.txtPercent.setText(content.getDownloadPercent() + "%");
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public class ContentsViewHolder {
        public ImageView icon;
        public TextView fileName;
        public TextView fileSize;
        public ProgressBar timeBar;
        public ImageView icHang;
        public TextView txtPercent;
        public ImageView btnDelete;
    }
}

