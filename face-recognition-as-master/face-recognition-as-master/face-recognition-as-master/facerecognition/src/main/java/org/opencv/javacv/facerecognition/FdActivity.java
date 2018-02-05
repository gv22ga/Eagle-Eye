package org.opencv.javacv.facerecognition;

import java.io.ByteArrayOutputStream;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
//import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
//import org.opencv.javacv.facerecognition.Database.AppDatabase;
import org.opencv.imgproc.Imgproc;
import org.opencv.javacv.facerecognition.Database.AppDatabase;
import org.opencv.javacv.facerecognition.Database.Person;
import org.opencv.javacv.facerecognition.Database.PersonDao;
import org.opencv.javacv.facerecognition.R;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import com.googlecode.javacv.cpp.opencv_imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;





public class FdActivity extends Activity implements CvCameraViewListener2 {

    MobileServiceClient mClient;
    public static MobileServiceTable<MissingPeople> mTable;
    public static MobileServiceTable<Found> mTable2;

    MissingPeople item, entity;

    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=https;AccountName=eagleeyestorag;AccountKey=gR1z5K5oiU0ka08/yOP8oDKtxMBEj+Ec3wpqvahlwFnFu0gjQPtqSujmIfBco0g6jKlUnIJ2XX6GKfEsFGe9OQ==;EndpointSuffix=core.windows.net";


    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int TRAINING= 0;
    public static final int SEARCHING= 1;
    public static final int IDLE= 2;

    private static final int frontCam =1;
    private static final int backCam =2;


    private int faceState=IDLE;
//    private int countTrain=0;

//    private MenuItem               mItemFace50;
//    private MenuItem               mItemFace40;
//    private MenuItem               mItemFace30;
//    private MenuItem               mItemFace20;
//    private MenuItem               mItemType;
//
    private MenuItem               nBackCam;
    private MenuItem               mFrontCam;
    private MenuItem               mEigen;


    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    public static CascadeClassifier      mJavaDetector;
 //   private DetectionBasedTracker  mNativeDetector;

    public static int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    public static float                  mRelativeFaceSize   = 0.1f;
    public int                    mAbsoluteFaceSize   = 96;
    private int mLikely=999;

    public static String mPath="";

    private Tutorial3View   mOpenCvCameraView;
    private int mChooseCamera = backCam;

    EditText text;
    TextView textresult;
    private  ImageView Iv;
    Bitmap mBitmap;


    public static PersonRecognizer fr;
    ToggleButton toggleButtonGrabar,toggleButtonTrain,buttonSearch;
    Button buttonCatalog;
    ImageView ivGreen,ivYellow,ivRed;
    ImageButton imCamera;

    TextView textState;
    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;

    public static Double latitude, longitude;
    public static final String MY_PREFS_NAME = "EagleEyePrefFile";


    static final long MAXIMG = 1;
    static long FETCH_INTERVAL=20000;

    AsyncTask<Void, Void, Void> task1;
    Handler mHandler1, mHandler;
    Runnable mHandlerTask1;

    private String date_format = "yyyy-MM-dd HH:mm:ss";
    private long min_time_diff = 1;

    ArrayList<Mat> alimgs = new ArrayList<Mat>();


    int[] labels = new int[(int)MAXIMG];
    int countImages=0;

    labels labelsFile;
	static {
		OpenCVLoader.initDebug();
    	System.loadLibrary("opencv_java");
	}

    AppDatabase db;
    public static PersonDao dao;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                 //   System.loadLibrary("detection_based_tracker");



                    fr=new PersonRecognizer(mPath);
                    String s = getResources().getString(R.string.Straininig);
//                    Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

       //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        // database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "person_db").allowMainThreadQueries().build();
        dao = db.getPersonDao();

        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setCvCameraViewListener(this);


        mPath=getFilesDir()+"/facerecogOCV/";

        labelsFile= new labels(mPath);

        Iv=(ImageView)findViewById(R.id.imageView1);
        textresult = (TextView) findViewById(R.id.textView1);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	if (msg.obj=="IMG")
            	{
            	 Canvas canvas = new Canvas();
                 canvas.setBitmap(mBitmap);
                 Iv.setImageBitmap(mBitmap);
                 if (countImages>=MAXIMG)
                 {
                	 toggleButtonGrabar.setChecked(false);
                 	 grabarOnclick();
                 }
            	}
            	else
            	{
            		textresult.setText(msg.obj.toString());
            		 ivGreen.setVisibility(View.INVISIBLE);
            	     ivYellow.setVisibility(View.INVISIBLE);
            	     ivRed.setVisibility(View.INVISIBLE);

            	     if (mLikely<0);
            	     else if (mLikely<50)
            			ivGreen.setVisibility(View.VISIBLE);
            		else if (mLikely<80)
            			ivYellow.setVisibility(View.VISIBLE);
            		else
            			ivRed.setVisibility(View.VISIBLE);
            	}
            }
        };
        text=(EditText)findViewById(R.id.editText1);
        buttonCatalog=(Button)findViewById(R.id.buttonCat);
        toggleButtonGrabar=(ToggleButton)findViewById(R.id.toggleButtonGrabar);
        buttonSearch=(ToggleButton)findViewById(R.id.buttonBuscar);
        toggleButtonTrain=(ToggleButton)findViewById(R.id.toggleButton1);
        textState= (TextView)findViewById(R.id.textViewState);
        ivGreen=(ImageView)findViewById(R.id.imageView3);
        ivYellow=(ImageView)findViewById(R.id.imageView4);
        ivRed=(ImageView)findViewById(R.id.imageView2);
        imCamera=(ImageButton)findViewById(R.id.imageButton1);

        ivGreen.setVisibility(View.INVISIBLE);
        ivYellow.setVisibility(View.INVISIBLE);
        ivRed.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        textresult.setVisibility(View.INVISIBLE);



        toggleButtonGrabar.setVisibility(View.INVISIBLE);

        buttonCatalog.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Intent i = new Intent(org.opencv.javacv.facerecognition.FdActivity.this,
        				org.opencv.javacv.facerecognition.ImageGallery.class);
        		i.putExtra("path", mPath);
        		startActivity(i);
        	};
        	});


        text.setOnKeyListener(new View.OnKeyListener() {
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
        		if ((text.getText().toString().length()>0)&&(toggleButtonTrain.isChecked()))
        			toggleButtonGrabar.setVisibility(View.VISIBLE);
        		else
        			toggleButtonGrabar.setVisibility(View.INVISIBLE);

                return false;
        	}
        });



		toggleButtonTrain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (toggleButtonTrain.isChecked()) {
					textState.setText(getResources().getString(R.string.SEnter));
					buttonSearch.setVisibility(View.INVISIBLE);
					textresult.setVisibility(View.VISIBLE);
					text.setVisibility(View.VISIBLE);
					textresult.setText(getResources().getString(R.string.SFaceName));
					if (text.getText().toString().length() > 0)
						toggleButtonGrabar.setVisibility(View.VISIBLE);


					ivGreen.setVisibility(View.INVISIBLE);
					ivYellow.setVisibility(View.INVISIBLE);
					ivRed.setVisibility(View.INVISIBLE);


				} else {
					textState.setText(R.string.Straininig);
					textresult.setText("");
					text.setVisibility(View.INVISIBLE);

					buttonSearch.setVisibility(View.VISIBLE);
					;
					textresult.setText("");
					{
						toggleButtonGrabar.setVisibility(View.INVISIBLE);
						text.setVisibility(View.INVISIBLE);
					}
//			        Toast.makeText(getApplicationContext(),getResources().getString(R.string.Straininig), Toast.LENGTH_LONG).show();
					fr.train();
					textState.setText(getResources().getString(R.string.SIdle));

				}
			}

		});



        toggleButtonGrabar.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				grabarOnclick();
			}
		});

        imCamera.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (mChooseCamera==frontCam)
				{
					mChooseCamera=backCam;
					mOpenCvCameraView.setCamBack();
				}
				else
				{
					mChooseCamera=frontCam;
					mOpenCvCameraView.setCamFront();

				}
			}
		});

        buttonSearch.setOnClickListener(new View.OnClickListener() {

     			public void onClick(View v) {
     				if (buttonSearch.isChecked())
     				{
     					if (!fr.canPredict())
     						{
     						buttonSearch.setChecked(false);
     			            Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
     			            return;
     						}
     					textState.setText(getResources().getString(R.string.SSearching));
     					toggleButtonGrabar.setVisibility(View.INVISIBLE);
     					toggleButtonTrain.setVisibility(View.INVISIBLE);
     					text.setVisibility(View.INVISIBLE);
     					faceState=SEARCHING;
     					textresult.setVisibility(View.VISIBLE);
     				}
     				else
     				{
     					faceState=IDLE;
     					textState.setText(getResources().getString(R.string.SIdle));
     					toggleButtonGrabar.setVisibility(View.INVISIBLE);
     					toggleButtonTrain.setVisibility(View.VISIBLE);
     					text.setVisibility(View.INVISIBLE);
     					textresult.setVisibility(View.INVISIBLE);

     				}
     			}
     		});

        boolean success=(new File(mPath)).mkdirs();
        if (!success)
        {
        	Log.e("Error","Error creating directory");
        }


        // GPS
        latitude = Double.parseDouble(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("latitude", "25.26"));
        longitude = Double.parseDouble(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("longitude", "82.98"));
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            Log.d(TAG, "GPS enabled");
        } else {
            Toast.makeText(
                    getBaseContext(),
                    "Enable GPS and restart application", Toast.LENGTH_LONG).show();
            Log.d(TAG, "GPS not enabled");
        }

        //db
        try {
            mClient = new MobileServiceClient("https://eagleeyes.azurewebsites.net", FdActivity.this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mTable = mClient.getTable(MissingPeople.class);
        mTable2 = mClient.getTable(Found.class);

        Intent in = new Intent(FdActivity.this, MainService.class);
        startService(in);




    }

    void grabarOnclick()
    {
    	if (toggleButtonGrabar.isChecked())
			faceState=TRAINING;
			else
			{ if (faceState==TRAINING)	;
			 // train();
			  //fr.train();
			  countImages=0;
			  faceState=IDLE;
			}


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
       // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);



    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
//        stopRepeatingTask();
        Intent in = new Intent(FdActivity.this, MainService.class);
        stopService(in);
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        int height = mGray.rows();
        if (Math.round(height * mRelativeFaceSize) > 0) {
            mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
        }
      //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);


        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 6, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
//            if (mNativeDetector != null)
//                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length==1)&&(faceState==TRAINING)&&(countImages<MAXIMG)&&(!text.getText().toString().isEmpty()))
        {


        Mat m=new Mat();
        Rect r=facesArray[0];


        m=mRgba.submat(r);
        Log.d(TAG, "camera face size "+m.rows() +" "+m.cols()+" "+m.channels());
        mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);


        Utils.matToBitmap(m, mBitmap);
       // SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");


        if (countImages<MAXIMG)
        {
        	fr.add(m, text.getText().toString());
        	countImages++;
        }

            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

        }
        else
        	 if ((facesArray.length>0)&& (faceState==SEARCHING))
          {
              Mat m=new Mat();

              int f=0;
              for(f=0; f<facesArray.length; f++) {
                  m=mGray.submat(facesArray[f]);
                  mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
                  Utils.matToBitmap(m, mBitmap);

                  Message msg = new Message();
                  String textTochange = "IMG";
                  msg.obj = textTochange;
                  mHandler.sendMessage(msg);

                  textTochange=fr.predict(m);
                  mLikely=fr.getProb();

                  if(mLikely < 80) {
                      msg = new Message();
                      msg.obj = textTochange;
                      mHandler.sendMessage(msg);
                      if (!textTochange.equals("Unknown")) {
                          Bitmap bmp = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
                          Utils.matToBitmap(mRgba, bmp);

                          add_found_person(bmp, textTochange, mLikely);
                      }
                  }
                  else{
                      msg = new Message();
                      msg.obj = "Unknown";
                      mHandler.sendMessage(msg);
                  }
              }

          }
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }

    private Date string_to_date(String s){
        try {
            return new SimpleDateFormat(date_format).parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String date_to_string(Date d){
        return new SimpleDateFormat(date_format).format(d);
    }

    private long time_difference(Date d1, Date d2){
        return (d2.getTime() - d1.getTime())/(60*1000);
    }

    @SuppressLint("StaticFieldLeak")
    private void add_found_person(Bitmap screenshot, String person_id, int probability){
        Log.d(TAG, "adding found person");
        final Person p = select_person(person_id).get(0);
        Date curr_date = new Date();

        final String prev_found_time = p.found_time;

        if(p.found_time.isEmpty() || time_difference(string_to_date(p.found_time), curr_date) > min_time_diff){
            Log.d(TAG, "update azure");
            p.found_time = date_to_string(curr_date);
            List<Person> to_update = new ArrayList<>();
            to_update.add(p);
            update_person(to_update);

            //updating to azure
            final File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temp1.jpg");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            screenshot.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            final byte[] bitmap_data = bos.toByteArray();


            task1 = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {

                    try {
                        Log.d(TAG, "background called");
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmap_data);
                        fos.flush();
                        fos.close();

                        CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
                        CloudBlobClient serviceClient = account.createCloudBlobClient();

                        CloudBlobContainer container = serviceClient.getContainerReference("eagleeye-container");
                        container.createIfNotExists();
                        CloudBlockBlob blob = container.getBlockBlobReference(p.pid + "_" + p.found_time + ".jpg");
                        File source_file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp1.jpg");
                        blob.upload(new FileInputStream(source_file), source_file.length());

                        Log.d(TAG, "screenshot uploaded");

                        Found person_found = new Found();
                        person_found.person_id = p.pid;
                        person_found.found_time = p.found_time;
                        person_found.found_lat = String.valueOf(latitude);
                        person_found.found_long = String.valueOf(longitude);
                        person_found.found_screenshot = p.pid + "_" + p.found_time + ".jpg";

                        mTable2.insert(person_found);
                        Log.d(TAG, "updated found table");

                        if(prev_found_time.isEmpty()){
                            Log.d(TAG, "missing person status updated");

                            MissingPeople missing_person_to_update = mTable.where().field("id").eq(p.pid).execute().get().get(0);
                            missing_person_to_update.status = "1";
                            mTable.update(missing_person_to_update);

                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (StorageException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                };

            };

            task1.execute();
        }
    }


    public void add_person_model(Bitmap image, String person_id) {
        Log.d(TAG, "add_image_bitmap_name called");
        Mat mrgb = new Mat();
        Mat mgray = new Mat();
        Utils.bitmapToMat(image, mrgb);
        mrgb.copyTo(mgray);
        Imgproc.cvtColor(mgray, mgray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mgray, mgray, Imgproc.COLOR_GRAY2RGBA, 4);

        MatOfRect faces = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mgray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            //Log.d(TAG, "java detector");
        }

        Rect[] facesArray = faces.toArray();
        Log.d(TAG, "facearray len " + Integer.toString(facesArray.length));
        if ((facesArray.length==1))
        {
            Mat m=new Mat();
            Rect r=facesArray[0];
            m = mrgb.submat(r);
            Log.d(TAG, "adding face");
            fr.add(m, person_id);
            // re train
            fr.train();
        }
    }

    public void delete_person_model(final String person_id) {
        File root = new File(mPath);
        FilenameFilter pngFilter = new FilenameFilter() {
            public boolean accept(File dir, String n) {
                String s=person_id;
                return n.toLowerCase().startsWith(s.toLowerCase()+"-");

            };
        };
        File[] imageFiles = root.listFiles(pngFilter);
        for (File image : imageFiles) {
            image.delete();
        }
    }


    /*******************************************GPS + LOCATION*********************************/
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "Location Set: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            Log.v(TAG, "longitude "+ Double.toString(longitude));
            Log.v(TAG, "latitude "+Double.toString(latitude));
            Log.d(TAG, "sample distance "+location_distance(25.26, 82.98));
            getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit().putString("latitude", latitude.toString()).commit();
            getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit().putString("longitude", longitude.toString()).commit();
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    private double location_distance(double lat2, double lon2) {
        double lat1 = latitude;
        double lon1 = longitude;
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344 / 1000;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    /******************************TIMER THREAD**********************************************/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        if (mOpenCvCameraView.numberCameras()>1)
        {
        nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
        mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));
//        mEigen = menu.add("EigenFaces");
//        mLBPH.setChecked(true);
        }
        else
        {imCamera.setVisibility(View.INVISIBLE);

        }
        //mOpenCvCameraView.setAutofocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
//        if (item == mItemFace50)
//            setMinFaceSize(0.5f);
//        else if (item == mItemFace40)
//            setMinFaceSize(0.4f);
//        else if (item == mItemFace30)
//            setMinFaceSize(0.3f);
//        else if (item == mItemFace20)
//            setMinFaceSize(0.2f);
//        else if (item == mItemType) {
//            mDetectorType = (mDetectorType + 1) % mDetectorName.length;
//            item.setTitle(mDetectorName[mDetectorType]);
//            setDetectorType(mDetectorType);
//
//        }
        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);
      //  mEigen.setChecked(false);
        if (item == nBackCam)
        {
        	mOpenCvCameraView.setCamFront();
        	mChooseCamera=frontCam;
        }
        	//fr.changeRecognizer(0);
        else if (item==mFrontCam)
        {
        	mChooseCamera=backCam;
        	mOpenCvCameraView.setCamBack();

        }

        item.setChecked(true);

        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
//        if (mDetectorType != type) {
//            mDetectorType = type;
//
//            if (type == NATIVE_DETECTOR) {
//                Log.i(TAG, "Detection Based Tracker enabled");
//                mNativeDetector.start();
//            } else {
//                Log.i(TAG, "Cascade detector enabled");
//                mNativeDetector.stop();
//            }
//        }
   }

    public void insert_person(List<Person> p) {
        dao.insertPersons(p.toArray(new Person[p.size()]));
    }

    public void delete_person(List<Person> p) {
        dao.deletePersons(p.toArray(new Person[p.size()]));
    }

    public void update_person(List<Person> p) {
        dao.updatePersons(p.toArray(new Person[p.size()]));
    }

    public List<Person> select_person(String pid) {
        return dao.getPerson(pid);

    }

    public List<Person> select_all_person() {
        return dao.getAllPersons();
    }




}
