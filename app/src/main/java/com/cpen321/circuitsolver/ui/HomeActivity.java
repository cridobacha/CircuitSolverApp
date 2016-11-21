package com.cpen321.circuitsolver.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.BaseActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import java.io.File;
import java.util.ArrayList;

public class HomeActivity extends BaseActivity {
    private static final String APP_NAME = "com.cpen321.circuitsolver";

    private static int RESULT_LOAD_IMAGE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private LinearLayout savedCircuitsScroll;
    private LinearLayout exampleCircuitScroll;

    private ArrayList<CircuitProject> circuitProjects = new ArrayList<>();
    private CircuitProject candidateProject;

    private static String selectedTag = null;

    private FloatingActionButton processingFab;
    private View cameraFab;
    private View loadFab;
    private View drawFab;
    private FloatingActionButton deleteFab;
    //private Button drawCircuitButton;


    private View.OnClickListener thumbnailListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LinearLayout parentView = (LinearLayout) view.getParent();

            for(int i=0; i < parentView.getChildCount(); i++) {
                ImageView imgView = (ImageView) parentView.getChildAt(i);
                imgView.setAlpha(1f);
            }

            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                if (imageView.getTag() == HomeActivity.selectedTag){
                    imageView.setAlpha(1f);
                    HomeActivity.this.setSelectedTag(null);
                } else {
                    imageView.setAlpha(0.65f);
                    HomeActivity.this.setSelectedTag((String) imageView.getTag());
                }

            }

        }
    };

    public void setSelectedTag(String selectedTag) {
        if (selectedTag == null){
            this.deleteFab.hide();
            this.processingFab.hide();
        } else {
            this.deleteFab.show();
            this.processingFab.show();
        }
        HomeActivity.selectedTag = selectedTag;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.cameraFab =  findViewById(R.id.capture_fab);
        this.loadFab =  findViewById(R.id.load_fab);
        this.drawFab =  findViewById(R.id.draw_fab);
        this.processingFab = (FloatingActionButton) findViewById(R.id.processing_fab);
        this.deleteFab = (FloatingActionButton) findViewById(R.id.delete_fab);
        //this.drawCircuitButton = (Button) findViewById(R.id.drawCircuitButton);

        this.savedCircuitsScroll = (LinearLayout) findViewById(R.id.saved_circuits_scroll);
        this.exampleCircuitScroll = (LinearLayout) findViewById(R.id.example_circuits_scroll);
        this.updateSavedCircuits();
        this.loadExamples();

        this.checkNecessaryPermissions();

//        final View actionB = findViewById(R.id.action_b);
//        actionB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
//            }
//        });

        this.cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.this.checkNecessaryPermissions();
                HomeActivity.this.dispatchTakePictureIntent();
            }
        });

        this.loadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.this.checkNecessaryPermissions();
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        this.drawFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent displayIntent = new Intent(HomeActivity.this, DrawActivity.class);
//                File circuitFolder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), HomeActivity.selectedTag);
//                displayIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, circuitFolder.getAbsolutePath());
                startActivity(displayIntent);
                finish();
            }
        });

        this.processingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HomeActivity.selectedTag == null)
                    return;
                Intent displayIntent = new Intent(HomeActivity.this, DrawActivity.class);
                File circuitFolder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), HomeActivity.selectedTag);
                displayIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, circuitFolder.getAbsolutePath());
                startActivity(displayIntent);
                finish();
            }
        });

        this.deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HomeActivity.selectedTag == null)
                    return;
                if (HomeActivity.selectedTag.contains("example"))
                    return;
                File circuitFolder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), HomeActivity.selectedTag);
                CircuitProject projToDelete = new CircuitProject(circuitFolder);
                projToDelete.deleteFileSystem();
                HomeActivity.this.updateSavedCircuits();
            }
        });
    }

    // TAKEN FROM OFFICIAL ANDROID DEVELOPERS PAGE (NOT MY OWN CODE)

    private void dispatchTakePictureIntent() {
        this.candidateProject = new CircuitProject(ImageUtils.getTimeStamp(),
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = this.candidateProject.generateOriginalImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        APP_NAME,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            else {
                System.out.println("photo file is null");
            }
        }
    }

    // END OF CODE TAKEN FROM OFFICIAL ANDROID DEVELOPERS PAGE

    private void dispatchLoadPictureIntent() {
        this.candidateProject = new CircuitProject(ImageUtils.getTimeStamp(),
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = this.candidateProject.generateOriginalImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        APP_NAME,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            else {
                System.out.println("photo file is null");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent analysisIntent = new Intent(getApplicationContext(), ProcessingActivity.class);
        analysisIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, this.candidateProject.getFolderPath());
        startActivity(analysisIntent);
        finish();
    }

    private void deleteFolderOrFile(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles())
                this.deleteFolderOrFile(child);
            file.delete();
        } else {
            file.delete();
        }
    }

    protected void updateSavedCircuits(){
        File imageStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] circuitProjects = imageStorageDir.listFiles();
        this.savedCircuitsScroll.removeAllViews();
        this.circuitProjects.clear();

        for (File project : circuitProjects) {
            if (project.isDirectory()){
                if (project.listFiles().length == 0)
                    this.deleteFolderOrFile(project);
                else {
                    this.circuitProjects.add(new CircuitProject(project));
                }
            }
        }

        for (CircuitProject circuitProject : this.circuitProjects) {
            ImageView newImage = new ImageView(getApplicationContext());
            newImage.setTag(circuitProject.getFolderID());
            newImage.setPadding(10, 10, 10, 10);
            newImage.setImageBitmap(circuitProject.getThumbnail());
            newImage.setOnClickListener(this.thumbnailListener);
            this.savedCircuitsScroll.addView(newImage);
        }
    }
    protected void loadExamples(){
        this.exampleCircuitScroll.removeAllViews();
        ImageView newImage = new ImageView(getApplicationContext());
        newImage.setTag("example_1");
        newImage.setPadding(10, 10, 10, 10);
        newImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.example_1));
        newImage.setOnClickListener(this.thumbnailListener);
        this.exampleCircuitScroll.addView(newImage);
    }


}
