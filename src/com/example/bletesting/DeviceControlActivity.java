package com.example.bletesting;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

public class DeviceControlActivity extends Activity{
	
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mPersonalField,mUpperField,mLowerField,mPulseField;
    private String mDeviceName;
    private String mDeviceAddress;
    //private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private String mup,mlow,mpluse,mtime;

     
    Handler mHandler;
    int counttime = 0;
	TextToSpeech texttospeech;
    String sendmessage;
    
    
    
    
    final Runnable runnable = new Runnable()
    {
        public void run() 
        {
 	        //scanLeDevice(true);
	        //mHandler.postDelayed(this, 5000);
        	counttime++;
            if (counttime == 1){
            	mHandler.removeCallbacks(this);
            
 
            	Intent updateIntent = new Intent();
            updateIntent.setAction("resume");
                    
           // updateIntent.putExtra("text", "This is the string to show");
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceControlActivity.this);
            manager.sendBroadcast(updateIntent);
            
        	DeviceControlActivity.this.finish();}
         	

        }
        };
    
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	//Toast.makeText(DeviceControlActivity.this, "initalize mBluetoothLeService", Toast.LENGTH_SHORT).show();


            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    
    
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	//Toast.makeText(DeviceControlActivity.this, "DisplayData", Toast.LENGTH_SHORT).show();
//	            Toast.makeText(DeviceControlActivity.this, "Data sent to server", Toast.LENGTH_SHORT ).show();
      
	           // DeviceControlActivity.this.finish();
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                	//Toast.makeText(DeviceControlActivity.this, "childhihhihihihi", Toast.LENGTH_SHORT).show();
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        	//Toast.makeText(DeviceControlActivity.this, "g"+groupPosition+" c"+childPosition, Toast.LENGTH_SHORT).show();
                        
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            
                            //Toast.makeText(DeviceControlActivity.this, "perm:"+characteristic.getPermissions(), Toast.LENGTH_SHORT).show();
                         //   Toast.makeText(DeviceControlActivity.this, "datadata", Toast.LENGTH_SHORT).show();
                          //  mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mUpperField.setText(R.string.no_data);
    }
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mHandler = new Handler();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mPersonalField = (TextView)findViewById(R.id.Personaldata);
        //mPersonalField.setText("姓名: 何志安   年齡: 21  性別: 男    ");
 
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
       // mGattServicesList.setOnChildClickListener(servicesListClickListner);
        //mConnectionState = (TextView) findViewById(R.id.connection_state);
        
         mUpperField = (TextView) findViewById(R.id.upper);
         mLowerField = (TextView) findViewById(R.id.lower);
         mPulseField = (TextView )findViewById(R.id.pulse);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        //mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
        
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }
    
 
	
	@Override
    protected void onResume() {
        super.onResume();
       // texttospeech = new TextToSpeech(this,ttsInitListener);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (texttospeech != null)
          texttospeech.shutdown();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
     
    		  
    	  
    	   
        super.onDestroy();
        unbindService(mServiceConnection);
         mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    
    
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
              //  mConnectionState.setText(resourceId);
            }
            
        });
    }

    private void displayData(String data) {
    	
    	
    	
        if (data != null) {
        	String displaydata [] = data.split(",");
         	 ;
 
         	 
	         SharedPreferences settings = getSharedPreferences ("user", 0);
	         String name = settings.getString("username", "");
	         int age = settings.getInt("userage", 0);
	         Boolean isman = settings.getBoolean("isman", true); 
	         String telephone = settings.getString("userphone", "");
	         
	       Toast.makeText(this,"display data", Toast.LENGTH_LONG).show();

	         String sex = "";
	           if (isman)
	        	 sex = "男";
	           else
	        	 sex = "女";
	         
          	if (name.length() !=0) 	
	           mPersonalField.setText("姓名: "+name+" 年齡: "+age + " 性別: "+sex  );		
	         
          	
          	 
        	if (displaydata[0].length()!=0){       		        	
              mUpperField.setText("上壓: "+ Integer.parseInt(displaydata[0]) );
              mup = displaydata[0].toString();}

        	if (displaydata[1].length()!=0){ 
        	  mLowerField.setText("下壓: "+Integer.parseInt(displaydata[1]));
              mlow = displaydata[1].toString();}

        	if (displaydata[2].length()!=0){         	
        	mPulseField.setText("脈搏: "+Integer.parseInt(displaydata[2]));
        	mpluse = displaydata[2].toString();
        	}
        	 
          	//mUpperField.setText(data);
         
        	
  
            new FetchTask().execute();
            
        	
        	Calendar cal = Calendar.getInstance(); 
      		 
	        int day = cal.get(Calendar.DAY_OF_MONTH) ;
        	int month = cal.get(Calendar.MONTH )+1;
	        int year = cal.get(Calendar.YEAR);
 	        int minute = cal.get(Calendar.MINUTE);
	        int hourofday = cal.get(Calendar.HOUR);
	        
	        String time = "";
	          if (hourofday>=12)
	        	  time = "  下午"+hourofday+"時"+minute+"分";
	          else
	        	  time = "  上午"+hourofday+"時"+minute+"分";
	        	
	        
	           sendmessage = name+ "  " + year + "年" + " " + month+"月  "+ day + "日  " +time+ "的血壓是:    " +    mUpperField.getText().toString()  +"   "+
	         mLowerField.getText().toString()  + "   " +  mPulseField.getText().toString() ;
	        		
	           texttospeech = new TextToSpeech(this, ttsInitListener);

 
              	//Toast.makeText(DeviceControlActivity.this, "hihihihi", Toast.LENGTH_SHORT).show();
         //    
 
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
              //      + "51716369")));
              
	          //  if (telephone.length()>0) 
                //  sendSMS( telephone ,  sendmessage);
             
          //   Toast.makeText(DeviceControlActivity.this, "sms發送成功", Toast.LENGTH_LONG ).show();} 
             

 	         //SharedPreferences settings = getSharedPreferences ("user", 0);
 	         SharedPreferences.Editor PE = settings.edit();
 	         PE.putInt("usertaken", 1);
 	         PE.commit();

             mHandler.postDelayed(runnable, 25000);

        }
    }
    
    
    private void sendSMS(String phoneNumber, String message)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
     }
    
    
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
           // Toast.makeText(this, "uuid:"+uuid+"", Toast.LENGTH_SHORT).show();

            String temp = SampleGattAttributes.lookup(uuid, unknownServiceString);

            if(temp.equals("Blood Pressure Service")){
           // Toast.makeText(this, "addlist|" +temp+"|", Toast.LENGTH_SHORT).show();

            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                String temp2 = SampleGattAttributes.lookup(uuid, unknownCharaString);
                if(temp2.equals("Blood Pressure measurement"))
                {
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
   //             Toast.makeText(this, "listuu|" +LIST_UUID+"|", Toast.LENGTH_SHORT).show();
 //               Toast.makeText(this, "uu|" +uuid+"|", Toast.LENGTH_SHORT).show();

                gattCharacteristicGroupData.add(currentCharaData);
                
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
         
        }
            
            
        }

        
        
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
       // mGattServicesList.setAdapter(gattServiceAdapter);
        
//auto display reading
        final BluetoothGattCharacteristic characteristic =mGattCharacteristics.get(0).get(0);
        mBluetoothLeService.readCharacteristic(characteristic);
        mNotifyCharacteristic = characteristic;
        mBluetoothLeService.setCharacteristicNotification(
                characteristic, true);
        
//        Toast.makeText(this, "reading!!!", Toast.LENGTH_SHORT).show();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
    
    
    private TextToSpeech.OnInitListener  ttsInitListener  = new TextToSpeech.OnInitListener()
    {

      @Override
      public void onInit(int status)
      {
        // TODO Auto-generated method stub
        /* 使用美國時�?�目�?�?支�?�中文 */
        //Locale loc = new Locale( Locale.TRADITIONAL_CHINESE, "", "");
        /* 檢查是�?�支�?�輸入的時�?� */
        if (texttospeech.isLanguageAvailable(Locale.TRADITIONAL_CHINESE) == TextToSpeech.LANG_AVAILABLE)
        {
          /* 設定語言 */
        	texttospeech.setLanguage(Locale.TRADITIONAL_CHINESE);
        	//Toast.makeText(MainActivity.this, "setus", Toast.LENGTH_LONG).show();

	        }
        
        
        
        texttospeech.setOnUtteranceCompletedListener(ttsUtteranceCompletedListener);
        
     
        if (status == TextToSpeech.SUCCESS )   {
	         texttospeech.speak(sendmessage, TextToSpeech.QUEUE_ADD,null);
	        // texttospeech.a
	         texttospeech.speak( "你今天的血壓資料已經傳到網上     sms已經送出到你的聯絡人",TextToSpeech.QUEUE_ADD,null);
          Log.i("TAG", "TextToSpeech.OnInitListener");

        }
       
       }

    };





private TextToSpeech.OnUtteranceCompletedListener ttsUtteranceCompletedListener = new TextToSpeech.OnUtteranceCompletedListener()
{
  @Override
  public void onUtteranceCompleted(String utteranceId)
  {
    // TODO Auto-generated method stub
    Log.i("TAG", "TextToSpeech.OnUtteranceCompletedListener");
  }
};


 

public class FetchTask extends AsyncTask<Void, Void, JSONArray> {
    @Override
    protected JSONArray doInBackground(Void... params) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://54.254.179.218/fyp2.php");

            Calendar mCalendar = Calendar.getInstance();
            SimpleDateFormat mdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mtime = mdf.format(mCalendar.getTime());
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("date",mtime));
            nameValuePairs.add(new BasicNameValuePair("up",mup));
            nameValuePairs.add(new BasicNameValuePair("low",mlow));
            nameValuePairs.add(new BasicNameValuePair("pulse",mpluse));
            
          //  Toast.makeText(DeviceControlActivity.this, "cccc", Toast.LENGTH_LONG).show();
            
          //  nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            String result11 = sb.toString();

            // parsing data
	            return new JSONArray(result11);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally{
        	
        	//Toast.makeText(DeviceControlActivity.this, "你的血壓已經成功傳到網上", Toast.LENGTH_LONG).show();
        }
    }
     

    @Override
    protected void onPostExecute(JSONArray result) {
        if (result != null) {
            // do something
        } else {
            // error occured
        }
    }
}
 
	
	
}
