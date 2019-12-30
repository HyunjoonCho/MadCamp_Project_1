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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
    private RelativeLayout bigView;
    private ImageView bigImg;
    private Button backButton;
    private Button infoButton;
    private TextView infoText;
    private LinearLayout buttonLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;

        bigView = view.findViewById(R.id.bigView);
        bigImg = view.findViewById(R.id.imgView);
        buttonLayout = view.findViewById(R.id.ButtonLayout);
        backButton = view.findViewById(R.id.Back);
        infoButton = view.findViewById(R.id.Info);
        infoText = view.findViewById(R.id.Infotext);

        bigImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonLayout.getVisibility() == View.VISIBLE)
                    buttonLayout.setVisibility(View.GONE);
                else
                    buttonLayout.setVisibility(View.VISIBLE);
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoText.getVisibility() == View.VISIBLE)
                    infoText.setVisibility(View.GONE);
                else
                    infoText.setVisibility(View.VISIBLE);
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bigView.setVisibility(View.GONE);
                infoText.setVisibility(View.GONE);
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
        final ArrayList<ImageData> list = getPathOfAllImages();

        RecyclerView rcView = view.findViewById(R.id.recyclerView);
        rcView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        GalleryAdapter adapter = new GalleryAdapter(list);
        adapter.setOnImgClickListener(new GalleryAdapter.OnImgClickListener() {
            @Override
            public void onImgClick(ImageData image) {
                bigImg.setImageURI(Uri.parse(image.imgPath));
                infoText.setText(new String().concat("Image Name: ")
                        .concat(image.imgName)
                        .concat("\n\nImage Path: ")
                        .concat(image.imgPath)
                        .concat("\n\nImage Size: ")
                        .concat(image.imgSize));
                bigView.setVisibility(View.VISIBLE);
            }
        });
        rcView.setAdapter(adapter);
    }

    private ArrayList<ImageData> getPathOfAllImages() {

        ArrayList<ImageData> result = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};

        Cursor imgCursor = getContext().getContentResolver().query(uri, projection, null, null, null);

        while (imgCursor.moveToNext()) {
            String absPath = imgCursor.getString(1);
            String imgName = imgCursor.getString(2);
            String imgSize = imgCursor.getString(3);
            if (!TextUtils.isEmpty(absPath)) {
                result.add(new ImageData(absPath, imgName, imgSize));
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