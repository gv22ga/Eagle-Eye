package neutrinos.eagleeyeserver.activities;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import neutrinos.eagleeyeserver.adapters.FoundAdapter;
import neutrinos.eagleeyeserver.adapters.MissingAdapter;
import neutrinos.eagleeyeserver.data.MissingPeople;
import neutrinos.eagleeyeserver.fragments.MissingFragment;

/**
 * Created by rishabh on 04/02/18.
 */

public class MainService extends Service {

    MobileServiceClient mClient;
    private MobileServiceTable<MissingPeople> mTable;
    List<MissingPeople> missing_list;

    RecyclerView.Adapter mAdapter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Thread main_thread;
    private String TAG = "Main_Service";
    private static int MAIN_THREAD_SLEEP_TIME = 20000;
    private boolean running;

    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=https;AccountName=eagleeyestorag;AccountKey=gR1z5K5oiU0ka08/yOP8oDKtxMBEj+Ec3wpqvahlwFnFu0gjQPtqSujmIfBco0g6jKlUnIJ2XX6GKfEsFGe9OQ==;EndpointSuffix=core.windows.net";


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        running = true;
        main_thread = new Thread(runnable);
        main_thread.start();
    }

    @Override
    public void onDestroy() {
//        main_thread.interrupt();
        running = false;
        Log.d(TAG, "Thread Destroyed");
        super.onDestroy();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Service thread started");
            while (running){
                try {
                    Thread.sleep(MAIN_THREAD_SLEEP_TIME);
                    Log.d(TAG, "loop called");

                    try {
                        mClient = new MobileServiceClient("https://eagleeyes.azurewebsites.net", getApplicationContext());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    mTable = mClient.getTable(MissingPeople.class);

                    try {
                        // entity = addItemInTable(item);
                        //  e=mToDoTable.where().field("Text").eq("fuck item").execute().get();
                        missing_list =  mTable.where().field("status").eq("0").execute().get();
                        Log.d(TAG, "missing list created");

                    } catch (final Exception e) {
                        Log.d("ydoijd", e.toString());
                    }

                    if (missing_list.size() > 0) {

                        ArrayList<String> missing_id = new ArrayList<>();
                        ArrayList<String> missing_name = new ArrayList<>();
                        ArrayList<String> missing_age = new ArrayList<>();
                        ArrayList<String> missing_sex = new ArrayList<>();
                        ArrayList<String> missing_address = new ArrayList<>();
                        ArrayList<String> missing_latitude = new ArrayList<>();
                        ArrayList<String> missing_longitude = new ArrayList<>();
                        ArrayList<String> missing_range = new ArrayList<>();
                        ArrayList<String> missing_reporter_contact = new ArrayList<>();
                        ArrayList<String> missing_image = new ArrayList<>();
                        ArrayList<String> missing_status = new ArrayList<>();

                        for (int i = 0; i < missing_list.size(); i++) {
                            missing_id.add(missing_list.get(i).id);
                            missing_name.add(missing_list.get(i).name);
                            missing_age.add(missing_list.get(i).age);
                            missing_sex.add(missing_list.get(i).sex);
                            missing_address.add(missing_list.get(i).address);
                            missing_latitude.add(missing_list.get(i).latitude);
                            missing_longitude.add(missing_list.get(i).longitude);
                            missing_range.add(missing_list.get(i).range);
                            missing_reporter_contact.add(missing_list.get(i).reporter_contact);
                            missing_image.add(missing_list.get(i).image);
                            missing_status.add(missing_list.get(i).status);

                        }
                        Log.d(TAG, "adapter parameters built");

                        mAdapter = new MissingAdapter(getApplicationContext(), missing_id, missing_name, missing_age, missing_sex, missing_address, missing_latitude, missing_longitude, missing_range, missing_reporter_contact, missing_image, missing_status);

                        Message completeMessage = MissingPersonActivity.mHandler.obtainMessage(0, mAdapter);
                        completeMessage.sendToTarget();

                        Log.d(TAG, "message sent");


                        try {
                            // entity = addItemInTable(item);
                            //  e=mToDoTable.where().field("Text").eq("fuck item").execute().get();
                            missing_list = mTable.where().field("status").eq("1").execute().get();

                        } catch (final Exception e) {
                            Log.d("ydoijd", e.toString());
                        }
                        if (missing_list.size() > 0) {

                            missing_id = new ArrayList<>();
                            missing_name = new ArrayList<>();
                            missing_age = new ArrayList<>();
                            missing_sex = new ArrayList<>();
                            missing_address = new ArrayList<>();
                            missing_latitude = new ArrayList<>();
                            missing_longitude = new ArrayList<>();
                            missing_range = new ArrayList<>();
                            missing_reporter_contact = new ArrayList<>();
                            missing_image = new ArrayList<>();
                            missing_status = new ArrayList<>();

                            for (int i = 0; i < missing_list.size(); i++) {
                                missing_id.add(missing_list.get(i).id);
                                missing_name.add(missing_list.get(i).name);
                                missing_age.add(missing_list.get(i).age);
                                missing_sex.add(missing_list.get(i).sex);
                                missing_address.add(missing_list.get(i).address);
                                missing_latitude.add(missing_list.get(i).latitude);
                                missing_longitude.add(missing_list.get(i).longitude);
                                missing_range.add(missing_list.get(i).range);
                                missing_reporter_contact.add(missing_list.get(i).reporter_contact);
                                missing_image.add(missing_list.get(i).image);
                                missing_status.add(missing_list.get(i).status);

                            }

                            mAdapter = new FoundAdapter(getApplicationContext(), missing_id, missing_name, missing_age, missing_sex, missing_address, missing_latitude, missing_longitude, missing_range, missing_reporter_contact, missing_image, missing_status);
                            completeMessage = MissingPersonActivity.mHandler.obtainMessage(1, mAdapter);
                            completeMessage.sendToTarget();

                        } else {
                            Log.d(TAG, "empty missing list");
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Thread interrupted");
        }
    };





}
