package neutrinos.eagleeyeserver.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import neutrinos.eagleeyeserver.R;
import neutrinos.eagleeyeserver.adapters.FoundAdapter;
import neutrinos.eagleeyeserver.adapters.MissingAdapter;
import neutrinos.eagleeyeserver.data.MissingPeople;

/**
 * Created by Prakhar on 02-02-2018.
 */

public class FoundFragment extends Fragment {

    public static RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    MobileServiceClient mClient;
    private MobileServiceTable<MissingPeople> mTable;
    List<MissingPeople> missing_list;

    private ProgressDialog progress;


    public FoundFragment() {
    }

    public static FoundFragment newInstance() {
        FoundFragment fragment = new FoundFragment();
        return fragment;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_found, container, false);
        mRecyclerView=(RecyclerView)rootView.findViewById(R.id.recycler_found_list);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);


        progress = new ProgressDialog(getActivity());
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Loading Please Wait...");
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.show();

        try {
            mClient = new MobileServiceClient("https://eagleeyes.azurewebsites.net", getActivity());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mTable = mClient.getTable(MissingPeople.class);

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // entity = addItemInTable(item);
                    //  e=mToDoTable.where().field("Text").eq("fuck item").execute().get();
                    missing_list =  mTable.where().field("status").eq("1").execute().get();

                } catch (final Exception e) {
                    Log.d("ydoijd", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //Toast.makeText(MainActivity.this, entity.Text, Toast.LENGTH_SHORT).show();
                progress.dismiss();

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

                    mAdapter = new FoundAdapter(getActivity(), missing_id,missing_name,missing_age,missing_sex,missing_address,missing_latitude,missing_longitude,missing_range,missing_reporter_contact,missing_image,missing_status);

                    mRecyclerView.setAdapter(mAdapter);

                } else {
                    Toast.makeText(getActivity(), "No Found Persons :(", Toast.LENGTH_SHORT).show();
                }
            }
        };

        runAsyncTask(task);

        return rootView;
    }
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }


}
