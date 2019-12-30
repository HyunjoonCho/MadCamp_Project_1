package com.example.madcamp_project_1;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private ArrayList<ImageData> imgList = null;
    private OnImgClickListener imgClkListener = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView ;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (imgClkListener != null && imgList != null) {

                            imgClkListener.onImgClick(imgList.get(pos));
                        }
                    }
                }
            });
            imgView = itemView.findViewById(R.id.image_view);
        }
    }

    GalleryAdapter (ArrayList<ImageData> list) {
        imgList = list ;
    }

    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.galleryview_item, parent, false) ;
        GalleryAdapter.ViewHolder vh = new GalleryAdapter.ViewHolder(view) ;

        return vh ;
    }


    @Override
    public void onBindViewHolder(GalleryAdapter.ViewHolder holder, int position) {
        ImageData data = imgList.get(position);
        holder.imgView.setImageURI(Uri.parse(data.imgPath));
    }

    @Override
    public int getItemCount() {
        return imgList.size() ;
    }

    public interface OnImgClickListener {
        void onImgClick(ImageData image);
    }

    public void setOnImgClickListener(OnImgClickListener listener) {
        this.imgClkListener = listener;
    }

}
