package neutrinos.eagleeyeserver.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import neutrinos.eagleeyeserver.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String[] time,latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent i= getIntent();
        time = i.getStringArrayExtra("time");
        latitude = i.getStringArrayExtra("latitude");
        longitude = i.getStringArrayExtra("longitude");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(20);
        mMap.setMaxZoomPreference(30);

        PolylineOptions plo =  new PolylineOptions();
        plo.color(Color.RED);
        plo.geodesic(true);
        plo.startCap(new RoundCap());
        plo.width(20);
        plo.jointType(JointType.BEVEL);
        for(int i=0;i<time.length;i++){
            LatLng loc = new LatLng(Double.parseDouble(latitude[i]),Double.parseDouble(longitude[i]));
            plo.add(loc);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = null;
            try {
                date = format.parse(time[i]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String sdate = new SimpleDateFormat("dd-MM-yyyy").format(date);
            String stime = new SimpleDateFormat("HH:mm:ss").format(date);

            mMap.addMarker(new MarkerOptions().position(loc).title("Time: "+ stime+"\nDate: "+ sdate));
            if(i == time.length-1){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            }
        }


        mMap.addPolyline(plo);

    }
}
