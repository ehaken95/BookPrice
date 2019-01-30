package org.androidtown.bookprice;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_IMAGE_CAPTURE = 1001;

    ImageView imageView;
    private String imageFilePath;
    private Uri photoUri;
    private int isimageFileValid = 0;//이미지가 있으면 1, 없으면 0 초기는 0
    Bitmap btm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        Button btn = this.findViewById(R.id.button);
        Button btn1= this.findViewById(R.id.button1);
        Button btn2 = this.findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_openCam(view);
            }
        });
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_checkPic(view);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_checkPicGC(view);
            }
        });

    }

    public void button_openCam(View v){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//권한체크부분 오류
        {                                                 //버튼 두번눌러야 실행됨->아예 어플실행할시 권한묻게 변경할것
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA);
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                                    PackageManager.PERMISSION_GRANTED || permissionCheck== PackageManager.PERMISSION_DENIED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                        0);
            }
            else{
                ;
            }
            //권한 있음
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        //Error occurred while creating the File
                    }

                    if (photoFile != null) {
                        Log.d("imageis Valid", "image");
                        isimageFileValid=1;//이미지가 있으니 1로 변환
                        Log.d("imageis Valid  :  ", String.valueOf(isimageFileValid));
                        photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        }
        else
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager())!=null) {
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch (IOException ex){
                    //Error occurred while creating the File
                }

                if(photoFile !=null){
                    Log.d("imageis Valid", "image");
                    isimageFileValid=1;//이미지가 있으니 1로 변환
                    Log.d("imageis Valid  :  ", String.valueOf(isimageFileValid));
                    photoUri = FileProvider.getUriForFile(this,getPackageName(),photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    public void button_checkPic(View v){
        //이미지 판별 java로 이동해서
        //이미지 확인 후, book이면 책이 맞다고 판별
        //(추후)만약 책이 맞으면 이미지 추출까지 한번에 진행.로딩바 구현해야함
        //책이 아니면 아니라고 얼러트뷰
        //이미지가 없으면 없다고 에러얼러트
        if(isimageFileValid==0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("오류");
            builder.setMessage("판별할 사진이 없습니다. 사진을 촬영해 주세요.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }else{//사진판별해주는 자바로 넘어가야함. 비트맵파일도 같이 전송
            //Intent it2 = new Intent(getApplicationContext(),ImageLabelML.class);
            //it2.putExtra("photoUri",photoUri.toString());

            try{
                btm = MediaStore.Images.Media.getBitmap(getContentResolver(),photoUri);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            //파이어베이스 코드 임시 비활성화//
            /*
            // [START image_from_bitmap]
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(btm);
            // [END image_from_bitmap]
            ImageLabelML labelML = new ImageLabelML(image);
            labelML.runDetector();
            String resML = labelML.getResultML();
            */
            //파이어베이스 코드 임시 비활성화

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("결과");
            builder.setMessage("이것은 " + /*resML*/  "입니다 ");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();




        }



    }

    public void button_checkPicGC(View v) {
        if (isimageFileValid == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("오류");
            builder.setMessage("판별할 사진이 없습니다. 사진을 촬영해 주세요.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {//사진판별해주는 자바로 넘어가야함. 비트맵파일도 같이 전송
            Intent it2 = new Intent(getApplicationContext(),ImageLabelGC.class);
            it2.putExtra("photoUri",photoUri.toString());
            startActivity(it2);


        }
    }
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /*prefix */
                ".jpg", /* suffix */
                storageDir      /*directory*/
        );
        imageFilePath = image.getAbsolutePath();

        return image;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ((ImageView)findViewById(R.id.imageView)).setImageURI(photoUri);

        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
//        if(requestCode == 0){
//            if(grantResults[0] == 0){
//                Toast.makeText(this,"카메라 권한이 승인됨",Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(this,"카메라 권한이 거절 되었습니다.",Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

}
