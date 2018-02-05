package neutrinos.eagleeyeserver.adapters;

/**
 * Created by Prakhar on 03-02-2018.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import neutrinos.eagleeyeserver.R;
import neutrinos.eagleeyeserver.activities.MainActivity;


public class FoundDetailAdapter extends RecyclerView.Adapter<FoundDetailAdapter.ViewHolder>  {
    private ArrayList<String> mfound_time,mfound_latitude,mfound_longitude;
    private File[] mscreenshot_file;
    private ProgressDialog progress;
    Context context;
    Boolean flag = false;
    File destinationFile;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View mView;
        TextView tv_found_location,tv_found_time,tv_found_date;
        ImageView iv_screenshot;

        public ViewHolder(View v){
            super(v);
            tv_found_location = (TextView)v.findViewById(R.id.tv_found_location);
            tv_found_date = (TextView)v.findViewById(R.id.tv_found_date);
            tv_found_time = (TextView)v.findViewById(R.id.tv_found_time);
            iv_screenshot = (ImageView)v.findViewById(R.id.iv_screenshot);
            mView=v;
        }
    }

    public FoundDetailAdapter(Context context, File[] screenshot_file, ArrayList<String> found_time, ArrayList<String> found_latitude, ArrayList<String> found_longitude)
    {
        mscreenshot_file = screenshot_file;
        mfound_time = found_time;
        mfound_latitude = found_latitude;
        mfound_longitude = found_longitude;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_found_details, parent, false);
        ViewHolder vh=new ViewHolder(v);
        return vh;
    }

    @Override
    public int getItemCount() {
        return mscreenshot_file.length;
    }


    @SuppressLint("SetTextI18n")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
       // Log.d("size",mscreenshot.size()+"");
        holder.tv_found_location.setText(mfound_latitude.get(0)+","+mfound_longitude.get(0));

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(mfound_time.get(position));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String sdate = new SimpleDateFormat("dd-MM-yyyy").format(date);
        String stime = new SimpleDateFormat("HH:mm:ss").format(date);
        holder.tv_found_time.setText(stime);
        holder.tv_found_date.setText(sdate);
        holder.iv_screenshot.setImageURI(Uri.fromFile(mscreenshot_file[position]));

    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

}

