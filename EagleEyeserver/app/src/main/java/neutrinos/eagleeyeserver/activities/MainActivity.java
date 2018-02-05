package neutrinos.eagleeyeserver.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import neutrinos.eagleeyeserver.R;
import neutrinos.eagleeyeserver.data.MissingPeople;
import neutrinos.eagleeyeserver.data.TodoItem;

public class MainActivity extends AppCompatActivity {

    MobileServiceClient mClient;
    private MobileServiceTable<MissingPeople> mTable;
    MissingPeople item, entity;
    private ProgressDialog progress;
    Button btn_select_image,btn_upload;
    String selectedImagePath = "";
    Boolean flag = false;
    EditText et_name,et_age,et_sex,et_address,et_latitude,et_longitude,et_reporter_contact,et_image;
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=https;AccountName=eagleeyestorag;AccountKey=gR1z5K5oiU0ka08/yOP8oDKtxMBEj+Ec3wpqvahlwFnFu0gjQPtqSujmIfBco0g6jKlUnIJ2XX6GKfEsFGe9OQ==;EndpointSuffix=core.windows.net";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        btn_select_image = (Button)findViewById(R.id.btn_select_image);
        btn_upload = (Button)findViewById(R.id.btn_upload);
        et_name = (EditText)findViewById(R.id.et_name);
        et_age = (EditText)findViewById(R.id.et_age);
        et_sex = (EditText)findViewById(R.id.et_sex);
        et_address = (EditText)findViewById(R.id.et_address);
        et_latitude = (EditText)findViewById(R.id.et_latitude);
        et_longitude = (EditText)findViewById(R.id.et_longitude);
        et_reporter_contact = (EditText)findViewById(R.id.et_reporter_contact);
        et_image = (EditText)findViewById(R.id.et_image);


        btn_select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });


        progress = new ProgressDialog(MainActivity.this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Uploading Details...");
        progress.setCancelable(false);
        progress.setIndeterminate(true);

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                flag = false;
                try {
                    mClient = new MobileServiceClient("https://eagleeyes.azurewebsites.net", MainActivity.this);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                mTable = mClient.getTable(MissingPeople.class);
                progress.show();
                item = new MissingPeople();
                item.name = et_name.getText().toString();
                item.age = et_age.getText().toString();
                item.sex = et_sex.getText().toString();
                item.address = et_address.getText().toString();
                item.latitude = et_latitude.getText().toString();
                item.longitude = et_longitude.getText().toString();
                item.range = "10";
                item.reporter_contact = et_reporter_contact.getText().toString();
                item.image = item.name+item.reporter_contact+".jpg";
                item.status = "0";
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
                            CloudBlockBlob blob = container.getBlockBlobReference(item.image);
                            //   Toast.makeText(BlobTesting.this, "blob reference", Toast.LENGTH_SHORT).show();

                            File sourceFile = new File(selectedImagePath);
                            //     Toast.makeText(BlobTesting.this, "file", Toast.LENGTH_SHORT).show();
                            blob.upload(new FileInputStream(sourceFile), sourceFile.length());

                            //   Toast.makeText(BlobTesting.this, "uploaded", Toast.LENGTH_SHORT).show();
                            // Download the image file.
//                                        File destinationFile = new File(sourceFile.getParentFile(), "image1Download.jpg");
//                                        blob.downloadToFile(destinationFile.getAbsolutePath());
                        } catch (FileNotFoundException fileNotFoundException) {
                            Log.d("FileNotFoundE", fileNotFoundException.getMessage());
                            flag = true;

                        } catch (StorageException storageException) {
                            Log.d("StorageException", storageException.getMessage());
                            flag = true;
                        } catch (Exception e) {
                            flag = true;
                            Log.d("Exception encountered: ", e.getMessage());

                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        //Toast.makeText(MainActivity.this, entity.Text, Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        if (flag) {
                            Toast.makeText(MainActivity.this, "Uploading photo failed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
                            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    if (!progress.isShowing())
                                        progress.show();
                                }


                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {
                                        // entity = addItemInTable(item);
                                        //  e=mToDoTable.where().field("Text").eq("fuck item").execute().get();
                                        entity = mTable.insert(item).get();
                                        Log.d("lol",entity.name);

                                    } catch (final Exception e) {
                                        Log.d("lol", e.toString());
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    //Toast.makeText(MainActivity.this, entity.Text, Toast.LENGTH_SHORT).show();
                                    Toast.makeText(MainActivity.this, "insert done", Toast.LENGTH_SHORT).show();
                                    progress.dismiss();

                                }
                            };

                            runAsyncTask(task);

                        }
                    }
                };

                runAsyncTask(task1);

            }
        });



           }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                et_image.setText(selectedImagePath);
                Log.d("yooooo", selectedImagePath);
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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
