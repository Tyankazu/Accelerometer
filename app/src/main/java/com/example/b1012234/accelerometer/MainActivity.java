package com.example.b1012234.accelerometer;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.String.*;


public class MainActivity extends ActionBarActivity
                  implements SensorEventListener {

    private TextView AccX;
    private TextView AccY;
    private TextView AccZ;

    private TextView GyroX;
    private TextView GyroY;
    private TextView GyroZ;

    private String path;

    //センサマネジャ
    private SensorManager mSensorManager;
    //private Sensor mAccelerometer;
    private float[] currentOrientationValues = {0.0f, 0.0f, 0.0f};
    private float[] currentAccelerationValues = {0.0f, 0.0f, 0.0f};

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


        // センサーマネージャのインスタンスを取得
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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

    public void onResume(){
        super.onResume();
        //加速度
        List<Sensor>sensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        //ジャイロセンサ
        List<Sensor>sensors2 = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        //リスナー登録
        for(Sensor s : sensors)
        {
            mSensorManager.registerListener(this, s,mSensorManager.SENSOR_DELAY_UI);
        }
        for(Sensor s2 : sensors2)
        {
            mSensorManager.registerListener(this, s2,mSensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent e)
    {
        Calendar time = Calendar.getInstance();
        int year = time.get(time.YEAR);
        int month = time.get(time.MONTH);
        int day = time.get(time.DAY_OF_MONTH);
        int hour = time.get(time.HOUR_OF_DAY);
        int minute = time.get(time.MINUTE);
        int second = time.get(time.SECOND);
        int ms = time.get(time.MILLISECOND);


        String nowtime = valueOf(year)+"/"+ valueOf(month+1)+"/"+ valueOf(day)+"_"+valueOf(hour)+":"
                + valueOf(minute)+":"+ valueOf(second)+":"+ valueOf(ms);
        System.out.println(nowtime);



        switch(e.sensor.getType())
        {
            //加速度
            case Sensor.TYPE_LINEAR_ACCELERATION:
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_mm_ss");//時刻の出力フォーマット作成
                String fileName = sdf.format(date)+"acceleration"+".csv";
                path = Environment.getExternalStorageDirectory()+"/"+ fileName;
                // System.out.println(path);
                File file = new File(path);
                file.getParentFile().mkdir();


                String write_int =nowtime+","+
                        e.values[mSensorManager.DATA_X]+","+
                        e.values[mSensorManager.DATA_Y] +","+
                        e.values[mSensorManager.DATA_Z]+"\n";
                FileOutputStream fos;
                try{
                    fos = new FileOutputStream(file,true);
                    OutputStreamWriter writer = new OutputStreamWriter(fos);
                    bw = new BufferedWriter(writer);
                    bw.write(write_int);
                    bw.flush();
                    bw.close();

                    System.out.println("save");
                } catch (UnsupportedEncodingException k){
                    k.printStackTrace();
                } catch (FileNotFoundException k){
                    String message = k.getMessage();
                    k.printStackTrace();
                } catch(IOException k){
                    String message = k.getMessage();
                    k.printStackTrace();
                }

                AccX.setText("x(redgrav):" +  e.values[mSensorManager.DATA_X]);
                AccY.setText("y(redgrav):" +  e.values[mSensorManager.DATA_Y]);
                AccZ.setText("z(redgrav):" +  e.values[mSensorManager.DATA_Z]);
                //System.out.println(currentAccelerationValues[0]);
                break;
            }

        }

        switch(e.sensor.getType())
        {
            //ジャイロセンサ
            case Sensor.TYPE_GYROSCOPE:
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_mm_ss");//時刻の出力フォーマット作成
                String fileName = sdf.format(date)+"gyroscope"+".csv";
                path = Environment.getExternalStorageDirectory()+"/"+ fileName;
                // System.out.println(path);
                File file = new File(path);
                file.getParentFile().mkdir();


                String write_int =nowtime+","+
                        e.values[mSensorManager.DATA_X]+","+
                        e.values[mSensorManager.DATA_Y] +","+
                        e.values[mSensorManager.DATA_Z]+"\n";
                FileOutputStream fos;
                try{
                    fos = new FileOutputStream(file,true);
                    OutputStreamWriter writer = new OutputStreamWriter(fos);
                    bw = new BufferedWriter(writer);
                    bw.write(write_int);
                    bw.flush();
                    bw.close();

                    System.out.println("save2");
                } catch (UnsupportedEncodingException k){
                    k.printStackTrace();
                } catch (FileNotFoundException k){
                    String message = k.getMessage();
                    k.printStackTrace();
                } catch(IOException k){
                    String message = k.getMessage();
                    k.printStackTrace();
                }

                GyroX.setText("x(gyro):" +  e.values[mSensorManager.DATA_X]);
                GyroY.setText("y(gyro):" +  e.values[mSensorManager.DATA_Y]);
                GyroZ.setText("z(gyro):" +  e.values[mSensorManager.DATA_Z]);
                //System.out.println(currentAccelerationValues[0]);
                break;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onStop()
    {
        super.onStop();
        //Listnerの登録解除
        mSensorManager.unregisterListener(this);


    }
}


