package neutrinos.eagleeyeserver.adapters;

/**
 * Created by Prakhar on 03-02-2018.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

import neutrinos.eagleeyeserver.R;


public class MissingAdapter extends RecyclerView.Adapter<MissingAdapter.ViewHolder>  {
    private ArrayList<String> mmissing_id,mmissing_name,mmissing_age,mmissing_sex,mmissing_address,mmissing_latitude,mmissing_longitude,mmissing_range,mmissing_reporter_contact,mmissing_image,mmissing_status;

    Context context;



    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View mView;
        TextView tv_name,tv_agesex,tv_address,tv_range,tv_last_location,tv_reporter_contact;
        LinearLayout card_missing;
        ImageView iv_image;

        public ViewHolder(View v){
            super(v);
            tv_name = (TextView)v.findViewById(R.id.tv_name);
            tv_agesex = (TextView)v.findViewById(R.id.tv_agesex);
            tv_address = (TextView)v.findViewById(R.id.tv_address);
            tv_range = (TextView)v.findViewById(R.id.tv_range);
            tv_last_location = (TextView)v.findViewById(R.id.tv_last_location);
            tv_reporter_contact = (TextView)v.findViewById(R.id.tv_reporter_contact);
            card_missing = (LinearLayout)v.findViewById(R.id.card_missing);
            iv_image = (ImageView)v.findViewById(R.id.iv_image);
            mView=v;
        }
    }

    public MissingAdapter(Context context, ArrayList<String> missing_id, ArrayList<String> missing_name, ArrayList<String> missing_age, ArrayList<String> missing_sex,
                          ArrayList<String> missing_address,ArrayList<String> missing_latitude, ArrayList<String> missing_longitude, ArrayList<String> missing_range, ArrayList<String> missing_reporter_contact, ArrayList<String> missing_image,ArrayList<String> missing_status)
    {
           mmissing_id = missing_id;
           mmissing_name = missing_name;
           mmissing_age = missing_age;
           mmissing_sex = missing_sex;
           mmissing_address = missing_address;
           mmissing_latitude = missing_latitude;
           mmissing_longitude = missing_longitude;
           mmissing_range = missing_range;
           mmissing_reporter_contact = missing_reporter_contact;
           mmissing_image = missing_image;
           mmissing_status = missing_status;


        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_missing_person, parent, false);
        ViewHolder vh=new ViewHolder(v);
        return vh;
    }

    @Override
    public int getItemCount() {
        return mmissing_id.size();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.tv_reporter_contact.setText("Contact no.: "+mmissing_reporter_contact.get(position));
        holder.tv_last_location.setText(mmissing_latitude.get(position)+","+mmissing_longitude.get(position));
        holder.tv_address.setText(mmissing_address.get(position));
        holder.tv_agesex.setText(mmissing_age.get(position)+","+mmissing_sex.get(position));
        holder.tv_name.setText(mmissing_name.get(position));
        holder.tv_range.setText(mmissing_range.get(position)+" km");


    }

}

