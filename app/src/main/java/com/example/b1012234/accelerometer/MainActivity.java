package com.example.b1012234.accelerometer;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.String.*;


public class MainActivity extends ActionBarActivity
                  implements SensorEventListener,LocationListener {

    //加速度測定
    private TextView AccX;
    private TextView AccY;
    private TextView AccZ;

    //角速度測定
    private TextView GyroX;
    private TextView GyroY;
    private TextView GyroZ;

    //緯度・経度・スピード
    private TextView latitude;
    private TextView longitude;
    private TextView speed;

    //回転行列
    private static final int MATRIX_SIZE = 16;
    float[] inR = new float[MATRIX_SIZE];
    float[] outR = new float[MATRIX_SIZE];
    float[] I = new float[MATRIX_SIZE];

    //傾きセンサの値
    float[] orValues = new float[3];
    float[] mgValues = new float[3];
    float[] acValues = new float[3];

    //GPS
    LocationManager mLocationManager = null;
    private static final String LOG_TAG ="Location";


    //SoundPool(効果音再生)
    private SoundPool mSoundPool;
    private int[] mSoundId = new int[2];//使う効果音の数だけ配列作成


    private String path;
    private SimpleDateFormat sdf;

    //ボタンの判定
    private int count = 0;

    //センサマネジャ
    private SensorManager mSensorManager;
    private Sensor mMagField;
    private Sensor mAccerometer;


    BufferedWriter bw;
    Date date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AccX = (TextView) findViewById(R.id.AccX);
        AccY = (TextView) findViewById(R.id.AccY);
        AccZ = (TextView) findViewById(R.id.AccZ);

        GyroX = (TextView) findViewById(R.id.GyroX);
        GyroY = (TextView) findViewById(R.id.GyroY);
        GyroZ = (TextView) findViewById(R.id.GyroZ);

        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        speed = (TextView) findViewById(R.id.speed);

        // クリックイベントを取得したいボタン
        Button Start_bt = (Button) findViewById(R.id.start);
        Button Stop_bt = (Button) findViewById(R.id.stop);

        // クリックリスナーを登録
        Start_bt.setOnClickListener(new View.OnClickListener() {

            //クリック時に呼ばれるメソッド
            @Override
            public void onClick(View v) {
                mSoundPool.play(mSoundId[0], 1.0F, 1.0F, 0, 0, 1.0F);
                Toast.makeText(getApplicationContext(), "計測開始", Toast.LENGTH_SHORT).show();
                System.out.println("startclick");
                count++;
                System.out.println("start:" + count);
                sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");//時刻の出力フォーマット作成
            }
        });
        Stop_bt.setOnClickListener(new View.OnClickListener() {

            //クリック時に呼ばれるメソッド
            @Override
            public void onClick(View v) {
                mSoundPool.play(mSoundId[1], 1.0F, 1.0F, 0, 0, 1.0F);
                Toast.makeText(getApplicationContext(), "計測終了", Toast.LENGTH_SHORT).show();
                System.out.println("stopclick");
                count++;
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("stop:" + count);
            }
        });


        // センサーマネージャのインスタンスを取得
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccerometer =
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagField =
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLocationManager =(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        date = new Date();//現在時刻の取得

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        //GPS
        Location lastLocation = mLocationManager.getLastKnownLocation
                (LocationManager.GPS_PROVIDER);
        updateDisplayedInfo(lastLocation);
        //位置情報更新要求(リスナの登録)
        mLocationManager.requestLocationUpdates
                (LocationManager.GPS_PROVIDER, 0, 0, this);
        //加速度(重力加速度を含まない)
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        //加速度(重力加速度を含む)
        //List<Sensor> sensors4 = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        //ジャイロセンサ
        List<Sensor> sensors2 = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        //磁気センサ
       // List<Sensor> sensors3 = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        //リスナー登録(100Hzはアプリが止まる)
        for (Sensor s : sensors) {
            mSensorManager.registerListener(this, s, 20000);
        }
        for (Sensor s2 : sensors2) {
            mSensorManager.registerListener(this, s2,20000);
        }
//        for (Sensor s3 : sensors3) {
//            mSensorManager.registerListener(this, s3, SensorManager.SENSOR_DELAY_UI);
//        }
//        for (Sensor s4 : sensors4) {
//            mSensorManager.registerListener(this, s4,SensorManager.SENSOR_DELAY_UI);
//        }

        //効果音を使えるように読み込み
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC,0);
        mSoundId[0] = mSoundPool.load(getApplicationContext(),R.raw.start,1);
        mSoundId[1] = mSoundPool.load(getApplicationContext(),R.raw.stop,1);

    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        Calendar time = Calendar.getInstance();
        int year = time.get(time.YEAR);
        int month = time.get(time.MONTH);
        int day = time.get(time.DAY_OF_MONTH);
        int hour = time.get(time.HOUR_OF_DAY);
        int minute = time.get(time.MINUTE);
        int second = time.get(time.SECOND);
        int ms = time.get(time.MILLISECOND);

        String nowtime = valueOf(year) + "/" +
                valueOf(month + 1) + "/" + valueOf(day) + "_" + valueOf(hour) + ":"
                + valueOf(minute) + ":" + valueOf(second) + ":" + valueOf(ms);
       // System.out.println(nowtime);



//        switch (e.sensor.getType()) {
//            case Sensor.TYPE_ACCELEROMETER:
//                acValues = e.values.clone();
//                System.out.println("GRAVITYx:" + acValues[0]);
//                System.out.println("GRAVITYy:" + acValues[1]);
//                System.out.println("GRAVITYz:" + acValues[2]);
//                break;
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                mgValues = e.values.clone();
//                System.out.println("MAGx:" + mgValues[0]);
//                System.out.println("MAGy:" + mgValues[1]);
//                System.out.println("MAGz:" + mgValues[2]);
//                break;
//        }
//
//            if (mgValues != null && acValues != null) {
//                //地磁気センサと加速度センサの値から、回転行列inR, Iを作成
//                SensorManager.getRotationMatrix(inR, I, acValues, mgValues);
//                //内部状態inRを元に、システムに合った座標軸系へ行列変換（outR）
//                SensorManager.remapCoordinateSystem(inR,
//                        SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
//                //メソッドでは傾き情報として、Z軸方向の方位、Ｘ軸方向のpitch、Y軸方向のrollを得る
//                SensorManager.getOrientation(outR, orValues);
//
//
//
//                if (count % 2 != 0) {
//                String fileName = sdf.format(date) + "Orientation" + ".csv";
//                path = Environment.getExternalStorageDirectory() + "/" + fileName;
//
//                File file = new File(path);
//                file.getParentFile().mkdir();
//
//                String write_int = nowtime + "," +
//                        rad2Deg(orValues[0]) + "," +
//                        rad2Deg(orValues[1]) + "," +
//                        rad2Deg(orValues[2]) + "\n";
//                FileOutputStream fos;
//                try {
//                    fos = new FileOutputStream(file, true);
//                    OutputStreamWriter writer = new OutputStreamWriter(fos);
//                    bw = new BufferedWriter(writer);
//                    bw.write(write_int);
//                    bw.flush();
//
//                    System.out.println("save3");
//                } catch (UnsupportedEncodingException k) {
//                    k.printStackTrace();
//                } catch (FileNotFoundException k) {
//                    String message = k.getMessage();
//                    k.printStackTrace();
//                } catch (IOException k) {
//                    String message = k.getMessage();
//                    k.printStackTrace();
//                }
//            }
//                azimuth.setText("方位角:" + rad2Deg(orValues[0]));
//                pitch.setText("傾斜角:" + rad2Deg(orValues[1]));
//                roll.setText("回転角:" + rad2Deg(orValues[2]));
//        }

        switch (e.sensor.getType()) {
            case Sensor.TYPE_PROXIMITY: {
                System.out.println("PROXIMITY:");
               // AccX.setText("近接センサ:" + e.values[0]);
                break;
            }
            //加速度
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                if (count % 2 != 0) {
                    String fileName = sdf.format(date) + "acceleration" + ".csv";
                    path = Environment.getExternalStorageDirectory() + "/" + fileName;
                    File file = new File(path);
                    file.getParentFile().mkdir();

                    String write_int = nowtime + "," +
                            e.values[mSensorManager.DATA_X] + "," +
                            e.values[mSensorManager.DATA_Y] + "," +
                            e.values[mSensorManager.DATA_Z] + "\n";
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(file, true);
                        OutputStreamWriter writer = new OutputStreamWriter(fos);
                        bw = new BufferedWriter(writer);
                        bw.write(write_int);
                        bw.flush();

                        System.out.println("save");
                    } catch (UnsupportedEncodingException k) {
                        k.printStackTrace();
                    } catch (FileNotFoundException k) {
                        String message = k.getMessage();
                        k.printStackTrace();
                    } catch (IOException k) {
                        String message = k.getMessage();
                        k.printStackTrace();
                    }
                }

           AccX.setText("x軸加速度:" + e.values[mSensorManager.DATA_X]);
            AccY.setText("y軸加速度:" + e.values[mSensorManager.DATA_Y]);
            AccZ.setText("z軸加速度:" + e.values[mSensorManager.DATA_Z]);

            break;
        }
    }

    switch(e.sensor.getType())
    {
        //ジャイロセンサ
        case Sensor.TYPE_GYROSCOPE: {
            if (count % 2 != 0) {
                String fileName = sdf.format(date) + "gyroscope" + ".csv";
                path = Environment.getExternalStorageDirectory() + "/" + fileName;
                File file = new File(path);
                file.getParentFile().mkdir();

                String write_int = nowtime + "," +
                        e.values[mSensorManager.DATA_X] + "," +
                        e.values[mSensorManager.DATA_Y] + "," +
                        e.values[mSensorManager.DATA_Z] + "\n";
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(file, true);
                    OutputStreamWriter writer = new OutputStreamWriter(fos);
                    bw = new BufferedWriter(writer);
                    bw.write(write_int);
                    bw.flush();

                    System.out.println("save2");
                } catch (UnsupportedEncodingException k) {
                    k.printStackTrace();
                } catch (FileNotFoundException k) {
                    String message = k.getMessage();
                    k.printStackTrace();
                } catch (IOException k) {
                    String message = k.getMessage();
                    k.printStackTrace();
                }
            }
            GyroX.setText("x軸角速度:" + e.values[mSensorManager.DATA_X]);
            GyroY.setText("y軸角速度:" + e.values[mSensorManager.DATA_Y]);
            GyroZ.setText("z軸角速度:" + e.values[mSensorManager.DATA_Z]);
            break;
        }
    }
    }

    private int rad2Deg(float rad){
        return (int) Math.floor(Math.toDegrees(rad));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause()
    {
        //Listnerの登録解除
        mSensorManager.unregisterListener(this);
        mLocationManager.removeUpdates(this);
        super.onPause();
    }

    protected void onStop()
    {
        super.onStop();
        //SoundPool 解放
        mSoundPool.unload(mSoundId[0]);
        mSoundPool.unload(mSoundId[1]);
        mSoundPool.release();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "onLocationChanged has been called.");
        //緯度、経度、標高の更新
        updateDisplayedInfo(location);
        Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Log.v("Speed", String.valueOf(location.getSpeed()));

        Calendar time = Calendar.getInstance();
        int year = time.get(time.YEAR);
        int month = time.get(time.MONTH);
        int day = time.get(time.DAY_OF_MONTH);
        int hour = time.get(time.HOUR_OF_DAY);
        int minute = time.get(time.MINUTE);
        int second = time.get(time.SECOND);
        int ms = time.get(time.MILLISECOND);

        String nowtime = valueOf(year) + "/" +
                valueOf(month + 1) + "/" + valueOf(day) + "_" + valueOf(hour) + ":"
                + valueOf(minute) + ":" + valueOf(second) + ":" + valueOf(ms);
        System.out.println(nowtime);

        if (count % 2 != 0) {
            String fileName = sdf.format(date) + "gps" + ".csv";
            path = Environment.getExternalStorageDirectory() + "/" + fileName;
            File file = new File(path);
            file.getParentFile().mkdir();

            String write_int = nowtime + "," +
                    String.valueOf(location.getLatitude()) + "," +
                    String.valueOf(location.getLongitude()) + "," +
                    String.valueOf(ChangeMStoKMH(location.getSpeed())) + "\n";
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file, true);
                OutputStreamWriter writer = new OutputStreamWriter(fos);
                bw = new BufferedWriter(writer);
                bw.write(write_int);
                bw.flush();

                System.out.println("save3");
            } catch (UnsupportedEncodingException k) {
                k.printStackTrace();
            } catch (FileNotFoundException k) {
                String message = k.getMessage();
                k.printStackTrace();
            } catch (IOException k) {
                String message = k.getMessage();
                k.printStackTrace();
            }
        }

    }
    public void updateDisplayedInfo(Location location){
        if (location == null){
            Log.e(LOG_TAG, "location is null.");
            return;
        }
        //緯度の表示更新
        TextView lat_value = (TextView)findViewById(R.id.latitude);
        lat_value.setText("latitude:"+Double.toString(location.getLatitude()));
        //経度の表示更新
        TextView lon_value = (TextView)findViewById(R.id.longitude);
        lon_value.setText("longitude:"+Double.toString(location.getLongitude()));
        //スピードの表示更新
        TextView spd_value = (TextView)findViewById(R.id.speed);
        spd_value.setText("speed(m/s):" + ChangeMStoKMH(location.getSpeed()));
    }

    private float ChangeMStoKMH(float sp){
        float kmh;
        kmh = sp * 3600 / 1000;

        return kmh;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}


