package com.mesutgolcuk.accelerometer_hw;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Mesut GOLCUK
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // sensor tanimlamalari
    private Sensor accelerometer;
    private SensorManager sm;
    // widget tanimlamalari
    private EditText epsilon;
    private EditText threshold;
    private TextView activeText;
    private TextView passiveText;
    // sensor okunan deger tanimlamalari
    private long lastTime = 0;
    private double lastX, lastY, lastZ;
    // kacar saniye o durumda bulunduklari
    private int activeSecond;
    private int passiveSecond;
    // kullanicinin girebildigi threshold ve epsilon degerleri
    private double epsilonValue;
    private double thresholdValue;

    private final static double  SECOND = 1000;

    /**
     * Activity ilk olusturuldugunda calisir
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // arayuzdeki widget lari id lerine gore bulup eslestirir
        findWidgets();
        // active ve passive degerlerini 0 a esitler
        updateActive(0);
        updatePassive(0);
        // SensorManager objesi Servis olarak alınır
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Sensor Manager dan accelerometer sensoru alinir
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // accelerometer listener icin register edilir
        registerListener();

        getValues();

    }

    /**
     * Widget lari id leri ile eslestirip objeleri olusturur
     */
    public void findWidgets(){
        epsilon = (EditText) findViewById(R.id.epsilonText);
        threshold = (EditText) findViewById(R.id.thresholdText);
        activeText = (TextView) findViewById(R.id.activeText);
        passiveText = (TextView) findViewById(R.id.passiveText);
    }

    /**
     * Edit textlerin degerlerini okur
     */
    public void getValues(){
        epsilonValue = Double.parseDouble(epsilon.getText().toString());
        thresholdValue = Double.parseDouble(threshold.getText().toString());
    }

    /**
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        double x,y,z;
        long currentTime;
        // degeri okunan sensorun dogrulugu kontrol edilir
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Sistem saati alinir
            currentTime = System.currentTimeMillis();
            // son sensor okumasinin uzerinden 1 sn gectiyse
            if ( (currentTime - lastTime) > SECOND ) {
                // Accelerometre nin x y z degerleri okunur
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                lastTime = currentTime;
                // okunan degerler ile ondan onceki degerler arasi fark thresholddan yuksekse
                if (  isHigherXYZ(lastX-x,lastY-y,lastZ-z,thresholdValue) ) {
                    Log.i("active", String.valueOf(activeSecond));
                    // aktif olunan sureyi 1 arttirip guncelle
                    updateActive(activeSecond + 1);
                }
                // okunan degerler ile ondan onceki degerler arasi fark epsilondan kucukse
                else if( isLowerXYZ(lastX-x,lastY-y,lastZ-z,epsilonValue) ){
                    Log.i("passive", String.valueOf(passiveSecond));
                    // pasif olunan sureyi 1 artirip guncelle
                    updatePassive(passiveSecond + 1);

                }
                // bir once okunmus olan degerleri guncelle
                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    /**
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Start button tiklandi
     * @param v
     */
    public void startClicked(View v){
        getValues();
        registerListener();
    }

    /**
     * Stop button tiklandi
     * @param v
     */
    public void stopClicked(View v){
        unregisterListener();
    }

    /**
     * Sensoru dinlemek uzere kaydet
     */
    public void registerListener(){
        sm.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Sensorun kaydini sil
     */
    public void unregisterListener(){
        sm.unregisterListener(this,accelerometer);
    }

    /**
     * Verilen 1,2,3. parametrelerin mutlak degerinin 4. parametreden buyuklugu kontrol edilir
     * @param x 1. sayi
     * @param y 2. sayi
     * @param z 3. sayi
     * @param value buyuklugu kontrol edilen sayi
     * @return Hem 1 hem 2 hem 3 4 den buyukse true else false
     */
    public boolean isHigherXYZ(double x,double y, double z,double value){
        if( Math.abs(x) > value && Math.abs(y) > value && Math.abs(z) > value ){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Verilen 1,2,3. parametrelerin mutlak degerinin 4. parametreden kucuklugu kontrol edilir
     * @param x 1. sayi
     * @param y 2. sayi
     * @param z 3. sayi
     * @param value buyuklugu kontrol edilen sayi
     * @return Hem 1 hem 2 hem 3 4 den kucukse true else false
     */
    public boolean isLowerXYZ( double x,double y, double z,double value ){
        if(Math.abs(x) < value && Math.abs(y) < value && Math.abs(z) < value){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * aktif suresini temizleme butonu tiklandi
     * @param v
     */
    public void clearActiveClicked(View v){
        updateActive(0);
        lastTime =  System.currentTimeMillis();
    }
    /**
     * pasif suresini temizleme butonu tiklandi
     * @param v
     */
    public void clearPassiveClicked(View v){
        updatePassive(0);
        lastTime =  System.currentTimeMillis();
    }

    /**
     * pasif suresini guncelle
     * @param sec guncellenecek deger
     */
    public void updatePassive(int sec){
        passiveSecond = sec;
        activeText.setText("Passive: "+ sec);
    }

    /**
     * aktif suresini guncelle
     * @param sec guncellenecek deger
     */
    public void updateActive(int sec){
        activeSecond = sec;
        passiveText.setText("Active: "+ sec);
    }
}
