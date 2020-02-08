package joseph.at.androidconnection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanResult;


/**
 * Created by anupamchugh on 09/02/16.
 */
public class CustomAdapter extends BaseAdapter {

    private ArrayList<DataModel> dataSet= new ArrayList<>();
    List<ScanResult> results;
    Context mContext;

    OnAdapterItemListener mCallBack;



   public CustomAdapter(Context context){

       mCallBack = (OnAdapterItemListener) context;
   }

    /**
     * Updates the list of not bonded devices.
     * @param results list of results from the scanner
     */
    public void update(@NonNull final List<ScanResult> results) {

        dataSet.clear();

        this.results = results;

        for (final ScanResult result : results) {

            dataSet.add(new DataModel(result.getDevice().getName(),"dum","dem","dem"));

        }

        notifyDataSetChanged();

    }


    @Override
    public int getCount() {
        if(dataSet!=null)
        return dataSet.size();


      return 0;
    }

    @Override
    public ScanResult getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());


        Holder holder = new Holder();

        if(convertView==null){


            convertView = inflater.inflate(R.layout.row_item,parent,false);
            holder.name = convertView.findViewById(R.id.name);

            Log.d("adapter", "getView: "+dataSet.get(position).name);

            holder.name.setText(""+(String)dataSet.get(position).name);



        }else {

            holder.name = convertView.findViewById(R.id.name);
            holder.name.setText(""+(String)dataSet.get(position).name);

        }


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.click(getItem(position).getDevice());
            }
        });


        return convertView;
    }


    private class Holder{
        private TextView name;
    }


    interface OnAdapterItemListener{
        void click(BluetoothDevice device);}

}
