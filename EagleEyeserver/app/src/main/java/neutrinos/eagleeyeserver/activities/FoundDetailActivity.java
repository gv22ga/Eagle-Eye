package neutrinos.eagleeyeserver.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import neutrinos.eagleeyeserver.R;
import neutrinos.eagleeyeserver.adapters.FoundAdapter;
import neutrinos.eagleeyeserver.adapters.FoundDetailAdapter;
import neutrinos.eagleeyeserver.data.Found;


public class FoundDetailActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=https;AccountName=eagleeyestorag;AccountKey=gR1z5K5oiU0ka08/yOP8oDKtxMBEj+Ec3wpqvahlwFnFu0gjQPtqSujmIfBco0g6jKlUnIJ2XX6GKfEsFGe9OQ==;EndpointSuffix=core.windows.net";

    MobileServiceClient mClient;
    private MobileServiceTable<Found> mTable;
    List<Found> found_detail_list;
    String id = "";
    private ProgressDialog progress;
    Button btn_map;

    ArrayList<String> found_time = new ArrayList<>();
    ArrayList<String> found_latitude = new ArrayList<>();
    ArrayList<String> found_longitude = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_detail);
        ActivityCompat.requestPermissions(FoundDetailActivity.this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        mRecyclerView=(RecyclerView)findViewById(R.id.recycler_found_detail_list);
        mLayoutManager = new LinearLayoutManager(FoundDetailActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        btn_map = (Button)findViewById(R.id.btn_map);
        Intent in = getIntent();
        id = in.getStringExtra("id");
        Log.d("person_id",id);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String sfound_time[] = found_time.toArray(new String[found_time.size()]);
                final String sfound_latitude[] = found_latitude.toArray(new String[found_latitude.size()]);
                final String sfound_longitude[] = found_longitude.toArray(new String[found_longitude.size()]);
                Intent i = new Intent(FoundDetailActivity.this,MapsActivity.class);
                i.putExtra("time",sfound_time);
                i.putExtra("latitude",sfound_latitude);
                i.putExtra("longitude",sfound_longitude);
                startActivity(i);
            }
        });

        progress = new ProgressDialog(FoundDetailActivity.this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Loading Please Wait...");
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.show();

        try {
            mClient = new MobileServiceClient("https://eagleeyes.azurewebsites.net", FoundDetailActivity.this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mTable = mClient.getTable(Found.class);

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // entity = addItemInTable(item);
                    //  e=mToDoTable.where().field("Text").eq("fuck item").execute().get();
                    found_detail_list =  mTable.where().field("person_id").eq(id).execute().get();
                    Log.d("person_id",found_detail_list.get(0).person_id);
                } catch (final Exception e) {
                    Log.d("ydoijd", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //Toast.makeText(MainActivity.this, entity.Text, Toast.LENGTH_SHORT).show();


                if (found_detail_list.size() > 0) {

                    final ArrayList<String> screenshot = new ArrayList<>();
                    found_time = new ArrayList<>();
                    found_latitude = new ArrayList<>();
                    found_longitude = new ArrayList<>();
                    //final ArrayList<File> screenshot_file = new ArrayList<>();
                    final File[] sscreenshot_file = new File[found_detail_list.size()];

                    for (int i = 0; i < found_detail_list.size(); i++) {
                        screenshot.add(found_detail_list.get(i).found_screenshot);
                        found_time.add(found_detail_list.get(i).found_time);
                        found_latitude.add(found_detail_list.get(i).found_lat);
                        found_longitude.add(found_detail_list.get(i).found_long);
                    }
                    final File[] destinationFile = new File[screenshot.size()];
                    final Boolean[] flag = new Boolean[screenshot.size()];
                    final Boolean[] check = new Boolean[screenshot.size()];
                    Arrays.fill(check, Boolean.FALSE);
                    for(int i=0;i<screenshot.size();i++){

                        final int finalI = i;
                        flag[finalI] = false;
                        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task1 = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
                                    CloudBlobClient serviceClient = account.createCloudBlobClient();

                                    // Container name must be lower case.
                                    CloudBlobContainer container = serviceClient.getContainerReference("eagleeye-container");
                                    container.createIfNotExists();
                                    //  Toast.makeText(BlobTesting.this, "container ban gya", Toast.LENGTH_SHORT).show();

                                    // Upload an image file.
                                    CloudBlockBlob blob = container.getBlockBlobReference(screenshot.get(finalI));
                                    //   Toast.makeText(BlobTesting.this, "blob reference", Toast.LENGTH_SHORT).show();

                                    // File sourceFile = new File(selectedImagePath);
                                    //     Toast.makeText(BlobTesting.this, "file", Toast.LENGTH_SHORT).show();
                                    // blob.upload(new FileInputStream(sourceFile), sourceFile.length());

                                    //   Toast.makeText(BlobTesting.this, "uploaded", Toast.LENGTH_SHORT).show();
                                    // Download the image file.
                                    destinationFile[finalI] = new File(Environment.getExternalStorageDirectory()+File.separator+"screenshot_"+finalI+".jpg");
                                    blob.downloadToFile(destinationFile[finalI].getAbsolutePath());
                                } catch (FileNotFoundException fileNotFoundException) {
                                    Log.d("FileNotFoundE", fileNotFoundException.getMessage());
                                    flag[finalI] =true;

                                } catch (StorageException storageException) {
                                    Log.d("StorageException", storageException.getMessage());
                                    flag[finalI] =true;
                                } catch (Exception e) {

                                    Log.d("Exception encountered: ", e.getMessage());
                                    flag[finalI] =true;
                                }

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                //Toast.makeText(MainActivity.this, entity.Text, Toast.LENGTH_SHORT).show();
                                if(!flag[finalI]){
                                   // Toast.makeText(FoundDetailActivity.this, "Screenshot download ho gya "+ finalI, Toast.LENGTH_SHORT).show();
                                   // screenshot_file.add(destinationFile[finalI]);
                                    sscreenshot_file[finalI] = destinationFile[finalI];
                                }
                                else{
                                   // screenshot_file.add(new File(Environment.getExternalStorageDirectory()+File.separator+"baby.jpg"));
                                    sscreenshot_file[finalI] = new File(Environment.getExternalStorageDirectory()+File.separator+"baby.jpg");
                                }
                                check[finalI] = true;
                                Boolean result = true;
                                for(int  j=0;j<screenshot.size();j++){
                                    result = result&check[j];
                                }
                                if(result){
                                    progress.dismiss();
                                   // Log.d("check",screenshot_file.size()+"  "+ screenshot.size());
                                    mAdapter = new FoundDetailAdapter(FoundDetailActivity.this, sscreenshot_file, found_time, found_latitude, found_longitude);
                                    mRecyclerView.setAdapter(mAdapter);
                                }

                            }

                        };

                        runAsyncTask(task1);

                    }



                } else {
                    Toast.makeText(FoundDetailActivity.this, "Couldn't find Details :(", Toast.LENGTH_SHORT).show();
                }
            }
        };

        runAsyncTask(task);


    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                return;
            }
        }
    }

}
