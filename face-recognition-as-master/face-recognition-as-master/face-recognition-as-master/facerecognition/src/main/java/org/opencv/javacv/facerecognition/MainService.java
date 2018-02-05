package org.opencv.javacv.facerecognition;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.javacv.facerecognition.Database.Person;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by gaurav on 3/2/18.
 */
public class MainService extends Service{

    Thread main_thread;
    public int                    mAbsoluteFaceSize   = 96;
    private String TAG = "Main_Service";
    private static int MAIN_THREAD_SLEEP_TIME = 20000;
    private boolean running;

    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=https;AccountName=eagleeyestorag;AccountKey=gR1z5K5oiU0ka08/yOP8oDKtxMBEj+Ec3wpqvahlwFnFu0gjQPtqSujmIfBco0g6jKlUnIJ2XX6GKfEsFGe9OQ==;EndpointSuffix=core.windows.net";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
                    Log.d(TAG, "loop called");


                    try {
                        List<MissingPeople> l = FdActivity.mTable.execute().get();
                        List<Person> all_person = select_all_person();
                        List<Person> to_insert = new ArrayList<>();
                        List<Person> to_delete = new ArrayList<>();
                        List<MissingPeople> to_insert_azure = new ArrayList<>();

                        for (Person p : all_person) {
                            boolean found = false;
                            for (MissingPeople m : l) {
                                if (p.pid.equals(m.id)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                to_delete.add(p);
                            }
                        }
                        delete_person(to_delete);
                        for (Person p : to_delete) {
                            delete_person_model(p.pid);
                        }

                        for (MissingPeople m : l) {
                            Double d = location_distance(Double.parseDouble(m.latitude), Double.parseDouble(m.longitude));
                            if (d < Double.parseDouble(m.range)) {
                                // check
                                boolean found = false;
                                for (Person p : all_person) {
                                    if (p.pid.equals(m.id)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    Person newp = new Person();
                                    newp.pid = m.id;
                                    to_insert.add(newp);
                                    to_insert_azure.add(m);
                                }
                            }
                        }
                        insert_person(to_insert);

                        for(MissingPeople p:to_insert_azure){
                            Bitmap bmp = download_image(p.image);
                            add_person_model(bmp, p.id);
                            Log.d(TAG, "person with id " + p.id + " was added");
                        }

                        if(to_delete.size() > 0 || to_insert.size() > 0)
                            FdActivity.fr.train();

                        List<Person> t = select_all_person();
                        for (Person p : t) {
                            Log.d(TAG, p.pid);
                        }


                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    } catch (ExecutionException e1) {
                        e1.printStackTrace();
                    } catch (MobileServiceException e1) {
                        e1.printStackTrace();
                    }


                    Thread.sleep(MAIN_THREAD_SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Thread interrupted");
        }
    };

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String file_path,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file_path, options);
    }

    private Bitmap download_image(String image_name){
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            CloudBlobContainer container = serviceClient.getContainerReference("eagleeye-container");
            container.createIfNotExists();
            CloudBlockBlob blob = container.getBlockBlobReference(image_name);
            File destinationFile = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
            blob.downloadToFile(destinationFile.getAbsolutePath());
            Log.d(TAG, "image downloaded " + image_name);

            return decodeSampledBitmapFromFile(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg",
                    500, 500);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private double location_distance(double lat2, double lon2) {
        double lat1 = FdActivity.latitude;
        double lon1 = FdActivity.longitude;
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344 / 1000; // WRONG!!!!! *1000
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public void add_person_model(Bitmap image, String person_id) {
        Log.d(TAG, "add person called");
        Mat mrgb = new Mat();
        Mat mgray = new Mat();
        Utils.bitmapToMat(image, mrgb);
        mrgb.copyTo(mgray);
        Log.d(TAG, "my bitmap size "+image.getHeight()+" "+image.getWidth());
        Imgproc.cvtColor(mgray, mgray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mgray, mgray, Imgproc.COLOR_GRAY2RGBA, 4);

        int height = mgray.rows();
        if (Math.round(height * FdActivity.mRelativeFaceSize) > 0) {
            mAbsoluteFaceSize = Math.round(height * FdActivity.mRelativeFaceSize);
        }

        MatOfRect faces = new MatOfRect();
        if (FdActivity.mDetectorType == FdActivity.JAVA_DETECTOR) {
            if (FdActivity.mJavaDetector != null)
                FdActivity.mJavaDetector.detectMultiScale(mgray, faces, 1.1, 6, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            //Log.d(TAG, "java detector");
        }

        Rect[] facesArray = faces.toArray();
        Log.d(TAG, "facearray len " + Integer.toString(facesArray.length));
        if ((facesArray.length>0))
        {
            Mat m=new Mat();
            Rect r=facesArray[0];
            m = mrgb.submat(r);
            Log.d(TAG, "my photo face size "+m.rows() +" "+m.cols()+" "+m.channels());
            Log.d(TAG, "adding face");
            FdActivity.fr.add(m, person_id);
        }
    }

    public void delete_person_model(final String person_id) {
        File root = new File(FdActivity.mPath);
        FilenameFilter pngFilter = new FilenameFilter() {
            public boolean accept(File dir, String n) {
                String s=person_id;
                return n.toLowerCase().startsWith(s.toLowerCase()+"_");

            };
        };
        File[] imageFiles = root.listFiles(pngFilter);
        for (File image : imageFiles) {
            image.delete();
        }
    }


    public void insert_person(List<Person> p) {
        FdActivity.dao.insertPersons(p.toArray(new Person[p.size()]));
    }

    public void delete_person(List<Person> p) {
        FdActivity.dao.deletePersons(p.toArray(new Person[p.size()]));
    }

    public void update_person(List<Person> p) {
        FdActivity.dao.updatePersons(p.toArray(new Person[p.size()]));
    }

    public List<Person> select_person(String pid) {
        return FdActivity.dao.getPerson(pid);

    }

    public List<Person> select_all_person() {
        return FdActivity.dao.getAllPersons();
    }

}
