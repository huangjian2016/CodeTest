package com.example.huangjian.okhttp3demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "OkHttp3";
    private OkHttpClient mOkHttpClient;
    private Button bt_send,bt_postsend,bt_sendfile,bt_downfile;
    private ImageView mImgView;

    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown;charset=utf-8");
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initOkHttpClient();
        bt_send =(Button)this.findViewById(R.id.bt_send);
        bt_postsend=(Button)this.findViewById(R.id.bt_postsend);
        bt_sendfile=(Button)this.findViewById(R.id.bt_sendfile);
        bt_downfile=(Button)this.findViewById(R.id.bt_downfile);
        mImgView=(ImageView)this.findViewById(R.id.imageView01);

        bt_send.setOnClickListener(this);
        bt_postsend.setOnClickListener(this);
        bt_sendfile.setOnClickListener(this);
        bt_downfile.setOnClickListener(this);
    }

    private void initOkHttpClient(){
        File sdcache = getExternalCacheDir();
        int cacheSize = 10*1024*1024; //10M

        OkHttpClient.Builder builder =new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20,TimeUnit.SECONDS)
                .readTimeout(20,TimeUnit.SECONDS)
                .cache(new Cache(sdcache.getAbsoluteFile(),cacheSize));

        mOkHttpClient = builder.build();
    }

    //通过implements关键字来实现View.OnClickListener()中onClick()实体
    public void onClick(View v){
        switch (v.getId()){
            case R.id.bt_send:
                getAysnHttp();
                break;
            case R.id.bt_postsend:
                postAysnHttp();
                break;
            case R.id.bt_sendfile:
                postAysnFile();
                break;
            case R.id.bt_downfile:
                downAysnFile();
                //sendMultipart();
                break;
        }
    }

    /**
     * 异步Get请求
     * @author huangjian
     * @time 2015-05-21
     * @param Null
     */
    private void getAysnHttp(){
        Request.Builder requestBuilder = new Request.Builder().url("http://www.baidu.com");
        requestBuilder.method("GET",null); //GET为大写，注意区分
        Request request = requestBuilder.build();
        Call mcall = mOkHttpClient.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(null != response.cacheResponse()){
                    String str = response.cacheResponse().toString();
                    Log.i(TAG,"cache 001 ----"+str);
                } else {
                    response.body().string();
                    String str = response.networkResponse().toString();
                    Log.i(TAG,"network 002 ----"+str);
                }

                //Android 更新UI的两种方法——handler和runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"请求成功 003 ---",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    /**
     * post异步请求
     * @author huangjian
     * @param null
     */
    private void postAysnHttp(){
        RequestBody formBody = new FormBody.Builder()
                .add("size", "10")
                .build();

        Request request = new Request.Builder()
                .url("http://www.cnblogs.com/")  //http://api.1-blog.com/biz/bizserver/article/list.do
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i(TAG,"post 004 ---str:\n"+str);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"请求成功 005",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    /**
     * 异步上传文件
     * @author huangjian
     * @param
     */
    private  void postAysnFile(){
        File file = new File("/storage/emulated/0/test1.txt"); ///sdcard/test123.txt
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN,file))
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "upload 006 ---:" + response.body().string());
            }
        });
    }

    /**
     * 异步下载文件
     * @author huangjian
     * @param
     */
    private void downAysnFile(){
        String url ="http://images.cnblogs.com/cnblogs_com/jxwolf/830432/o_11.jpg";
        Request request = new Request.Builder().url(url).build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                //显示下载的图片
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                try{
                    fileOutputStream = new FileOutputStream(new File("/storage/emulated/0/Download/test.jpg"));
                    byte[] buffer = new byte[2048];
                    int len =0;
                    while ((len = inputStream.read(buffer))!= -1){
                        fileOutputStream.write(buffer,0,len);
                    }
                    fileOutputStream.flush();
                }catch (IOException e){
                    Log.i(TAG,"error IOException\n");
                    e.printStackTrace();
                }
                Log.d(TAG, "恭喜，文件下载成功 ^_^!");

                //show the picture of the downloaded
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImgView.setImageBitmap(bitmap);
                        Log.i(TAG,"show the downloaded picture of the network 0009 ---\n");
                        Toast.makeText(getApplicationContext(),"显示下载的图片 008",Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    private void sendMultipart(){
        final RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title","hangjian")
                .addFormDataPart("image","test.jpg",
                        RequestBody.create(MEDIA_TYPE_PNG,new File("/sdcard/test.jpg")))
                .build();
        Request request = new Request.Builder()
                .header("Authorization","Client-ID"+"...")
                .url("https://api.imgur.com/3/image")
                .post(requestBody)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG,"0007 OK"+response.body().string());
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
