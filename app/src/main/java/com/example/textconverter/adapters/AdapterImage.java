package com.example.textconverter.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.textconverter.activities.ImageViewActivity;
import com.example.textconverter.R;
import com.example.textconverter.models.ModelImage;

import java.util.ArrayList;

public class AdapterImage extends RecyclerView.Adapter<AdapterImage.HolderImage> {

    private Context context;
    private ArrayList<ModelImage> imageArrayList;

    public AdapterImage(Context context, ArrayList<ModelImage> imageArrayList) {
        this.context = context;
        this.imageArrayList = imageArrayList;
    }

    @NonNull
    @Override
    public HolderImage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_image,parent,false);

        return new HolderImage(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImage holder, int position) {

        ModelImage modelImage=imageArrayList.get(position);
        Uri imageUri=modelImage.getImageUri();

        Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.baseline_image_24)
                .into(holder.imageiv);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ImageViewActivity.class);
                intent.putExtra("imageUri",""+imageUri);
                context.startActivity(intent);
            }
        });

        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                modelImage.setChecked(isChecked);
            }
        });

    }

    @Override
    public int getItemCount() {
        return imageArrayList.size();
    }

    class HolderImage extends RecyclerView.ViewHolder{

        ImageView imageiv;
        CheckBox checkbox;


        public HolderImage(@NonNull View itemView) {
            super(itemView);

            imageiv=itemView.findViewById(R.id.imageiv);
            checkbox=itemView.findViewById(R.id.checkbox);

        }
    }

}
