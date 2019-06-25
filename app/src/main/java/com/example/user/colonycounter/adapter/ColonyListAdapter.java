package com.example.user.colonycounter.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.colonycounter.R;
import com.example.user.colonycounter.model.Colony;

import java.util.ArrayList;

/**
 * Created by user on 24-Jun-19.
 */

public class ColonyListAdapter extends BaseAdapter {

    private Context context;
    private  int layout;
    private ArrayList<Colony> colonyList;

    public ColonyListAdapter(Context context, int layout, ArrayList<Colony> colonyList) {
        this.context = context;
        this.layout = layout;
        this.colonyList = colonyList;
    }

    @Override
    public int getCount() {
        return colonyList.size();
    }

    @Override
    public Object getItem(int position) {
        return colonyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView txtNumber;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);

            holder.txtNumber = (TextView) row.findViewById(R.id.txtColony);
            holder.imageView = (ImageView) row.findViewById(R.id.imgColony);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }

        Colony colony = colonyList.get(position);

        holder.txtNumber.setText("Colony : "+colony.getNumber());

        byte[] colonyImage = colony.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(colonyImage, 0, colonyImage.length);
        holder.imageView.setImageBitmap(bitmap);

        return row;
    }
}