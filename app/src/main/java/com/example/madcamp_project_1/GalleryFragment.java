package com.example.madcamp_project_1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private View view;
    private ImageView bigImg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;

        bigImg = view.findViewById(R.id.bigView);

        bigImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                setRecyclerView();
            }
            else {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            }
        }
        else {

            setRecyclerView();
        }
    }

    private void setRecyclerView() {
        final ArrayList<String> list = getPathOfAllImages();

        RecyclerView rcView = view.findViewById(R.id.recyclerView);
        rcView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        GalleryAdapter adapter = new GalleryAdapter(list);
        adapter.setOnImgClickListener(new GalleryAdapter.OnImgClickListener() {
            @Override
            public void onImgClick(String imgPath) {
                Log.w("yo", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                bigImg.setImageURI(Uri.parse(imgPath));
                bigImg.setVisibility(View.VISIBLE);
            }
        });
        rcView.setAdapter(adapter);
    }

    private ArrayList<String> getPathOfAllImages() {

        ArrayList<String> result = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};

        Cursor imgCursor = getContext().getContentResolver().query(uri, projection, null, null, null);

        while (imgCursor.moveToNext()) {
            String absPath = imgCursor.getString(1);
            if (!TextUtils.isEmpty(absPath)) {
                result.add(absPath);
            }
        }

        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setRecyclerView();
            }
            else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
