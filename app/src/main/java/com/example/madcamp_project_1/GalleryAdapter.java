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

    private ArrayList<String> imgList = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView ;

        ViewHolder(View itemView) {
            super(itemView) ;
            imgView = itemView.findViewById(R.id.image_view) ;
        }
    }

    GalleryAdapter (ArrayList<String> list) {
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

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(GalleryAdapter.ViewHolder holder, int position) {
        String text = imgList.get(position);
        holder.imgView.setImageURI(Uri.parse(text));
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return imgList.size() ;
    }


}
