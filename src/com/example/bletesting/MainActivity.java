package com.example.bletesting;

 import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
 
import org.teleal.cling.android.browser.UpnpMainActivity;

import com.example.bletesting.R.id;
 
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;


 
public class MainActivity extends ListActivity {

	
	private boolean mScanning;
	private Handler mHandler;
	private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD =   25000; 
    public static final long delayperiod = 5;
    private boolean Got; 
    //private long mTime;
    ListView list;
 //   protected PowerManager.WakeLock mWakeLock;
	int hourtoalarm,mintoalarm,delaymin = 0,count = 0;
    private Handler handler=new Handler();
      
    
    final Runnable runnable = new Runnable()
    {
        public void run() 
        {
 	        scanLeDevice(true);
	        mHandler.postDelayed(this, SCAN_PERIOD);

        }
        };
        
    
      Runnable run2;  
      Calendar cal; 
      boolean setalarmed = false,setalarmed2 = false,setalarmed3 = false; 
      boolean done[] = new boolean [] {false,false,false};
      int back = 0;
       // boolean done = true
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Got = false;
		cal  = Calendar.getInstance(); 

		
        mHandler = new Handler();		
		list = (ListView)findViewById(android.R.id.list);
		 
				
  
        //Toast.makeText(this, hourofday+hour+minute,Toast.LENGTH_SHORT ).show();
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE does not support", Toast.LENGTH_SHORT).show();
            finish();
        }
		
		else
            Toast.makeText(this, "BLE support", Toast.LENGTH_SHORT).show();

		
		
		 final BluetoothManager bluetoothManager =
	                (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
	        mBluetoothAdapter = bluetoothManager.getAdapter();

	        // Checks if Bluetooth is supported on the device.
	        if (mBluetoothAdapter == null) {
	            Toast.makeText(this, "No bluetooth", Toast.LENGTH_SHORT).show();
	            finish();
	            return;
	        }
		
		
	        mLeDeviceListAdapter = new LeDeviceListAdapter();
	        list.setAdapter(mLeDeviceListAdapter);
 
	        mHandler.postDelayed(runnable, SCAN_PERIOD);
	        
	        
	        IntentFilter filter = new IntentFilter();
	        filter.addAction("resume");
	        filter.addAction("resume2");

	        
	        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
	        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
	            public void onReceive(Context context, Intent intent) {
	             
	            	
	            	
	            	if (intent.getAction().equals("resume") )
	            	  mHandler.postDelayed(runnable, SCAN_PERIOD);
	            	
	            	
	            	if (intent.getAction().equals("resume2")){
		            	  mHandler.postDelayed(runnable, SCAN_PERIOD);

	                Toast.makeText(MainActivity.this, "back", Toast.LENGTH_SHORT).show();
	            	  SharedPreferences settings = getSharedPreferences ("user", 0);
	 		         int usertaken = settings.getInt("usertaken", 0);
	 		         
	        		    Calendar cal = Calendar.getInstance(); 
	           		 
		    	        int minute = cal.get(Calendar.MINUTE);

		    	        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
		    	        
	 		    
	            	}
 	            	
 	            }
	        };
	        localBroadcastManager.registerReceiver(broadcastReceiver, filter);  
     
	        
	}
	
	
	public void setalarm(final int h , final int m, final boolean setted){
		
		
		handler.postDelayed(new Runnable(){
        	
        	@Override
        	public void run(){
        		
        		
        		    Calendar cal = Calendar.getInstance(); 
        		 
	    	        int minute = cal.get(Calendar.MINUTE);

	    	        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
	    	       
 	    	        
	    	         
	   	         SharedPreferences settings = getSharedPreferences ("user", 0);
		         int usertaken = settings.getInt("usertaken", 0);
		         
 	    	     // 
 	             Log.v("usertaken"+usertaken,"user"); 

 	             boolean done =  setted;
 	             
 	             
 	    
 	             
                 
  	    		     if (hourofday == hourtoalarm  && minute == mintoalarm+delaymin && !setalarmed ){
   			           setalarmed  = true;
   			           if (usertaken == 0)
                         toUPnP();
                       delaymin +=delayperiod;
                       handler.removeCallbacks(this);

  	    		     }
  	    		     
  	    		     if (hourofday == hourtoalarm  && minute == mintoalarm+delaymin && !setalarmed2 ){
     			         setalarmed2  = true;
     			           if (usertaken == 0)
     	                         toUPnP();
                          delaymin +=delayperiod;
                         handler.removeCallbacks(this);
   
    	    		     }
  	    		     
  	    		     
  	    		     if (hourofday == hourtoalarm  && minute == mintoalarm+delaymin && !setalarmed3 ){
     			         setalarmed3  = true;
     			           if (usertaken == 0)
     	                         toUPnP();
                  
                          
                         handler.removeCallbacks(this);
   
    	    		     }
  	    		     
  	    		     
  	     
                  
	    		else{
	    			
	    			
	    			setalarm(h,m,false);
	    		}
	    		
        	}
        	
        },1000);
		
	}
	
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   //Toast.makeText(MainActivity.this, device.getName()+" "+device.getAddress()+" ", Toast.LENGTH_SHORT).show();
       	           mLeDeviceListAdapter = new LeDeviceListAdapter();
                   mLeDeviceListAdapter.addDevice(device);
                   //list.setAdapter(mLeDeviceListAdapter);
                   Got = true;
                   
                   
                   //Toast.makeText(MainActivity.this, "added:" + Got, Toast.LENGTH_SHORT).show();
                   
                   
                   
                   if(device.getAddress().equals("78:C5:E5:99:F6:07")){
                	   
                        mHandler.removeCallbacks(runnable); 	   
                       final  Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
                       
                       intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                       intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                       
                       if (mScanning) {
                           mBluetoothAdapter.stopLeScan(mLeScanCallback);
                           mScanning = false;
                       }
                       //Toast.makeText(MainActivity.this, "our device!!!! ", Toast.LENGTH_SHORT).show();
                       
           	          // handler.removeCallbacks(runnable);
           	           startActivity(intent);}
                       
                       list.setAdapter(mLeDeviceListAdapter);
                     
                
                   
                   

                }
            });
        }
    };
	
    
    
    public void toUPnP(){
        mHandler.removeCallbacks(runnable); 	   

	 	Intent intent = new Intent();
	    intent.setClass(MainActivity.this,UpnpMainActivity.class);
     	startActivity(intent);
    	
    	
    }
    
    
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0:{
        	
        	
 
 
 	        int minute = cal.get(Calendar.MINUTE);
 	 
 	              //24 hour format
 	        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
 	       TimePickerDialog  timepickerdialog =  new TimePickerDialog(this,
                   mTimeSetListener, hourofday, minute , false);
 	       
 	      timepickerdialog.setTitle("設定Upnp響鬧發送時間");
 	       
            return timepickerdialog;  
         }
        }
        return null;
    }
    
    
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    hourtoalarm = hourOfDay;
                    mintoalarm = minute;
                    setalarmed = false; 
                    
              		
          		    Calendar cal = Calendar.getInstance(); 
          		 
  	    	        int m = cal.get(Calendar.MINUTE);

  	    	        int h = cal.get(Calendar.HOUR_OF_DAY);
  	    	        
                    setalarm(h,m,false);                    
                    
                    
                }
            };
    
    
    
    
    
    
    
    
    
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        
         //Toast.makeText(this, "ListItemClick!", Toast.LENGTH_LONG).show();

        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
        
    }
    
    
	private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
	
	
	public void checktime(){
		
	       Calendar cal = Calendar.getInstance(); 

	        int millisecond = cal.get(Calendar.MILLISECOND);
	        int second = cal.get(Calendar.SECOND);
	        int minute = cal.get(Calendar.MINUTE);
	              //12 hour format
	        int hour = cal.get(Calendar.HOUR);
	              //24 hour format
	        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
	        int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
	        int month = cal.get(Calendar.MONTH)+1;
	        Log.v("h:"+hourtoalarm+""+mintoalarm,"hourandalarm");
	        
 
	         SharedPreferences settings = getSharedPreferences ("user", 0);
	         int usertaken = settings.getInt("usertaken", 0);
	         
	         /* reset usertaken to 0 in the other day*/
	         if (hourofday == 0 && minute == 0){
	             SharedPreferences.Editor PE = settings.edit();
	 	         PE.putInt("usertaken", 0);
	 	         PE.commit();
	         }
	         
 
	        
 		if (hourofday == hourtoalarm && minute == mintoalarm && usertaken==0){
		 	Intent intent = new Intent();
    	    intent.setClass(this,UpnpMainActivity.class);
	     	startActivity(intent);
        	 
			//Toast.makeText(this, hourofday+" "+minute, Toast.LENGTH_SHORT ).show();
			handler.removeCallbacks(run2);
			
		} 
	}
	
	
    
    
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }
	

	
	
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case R.id.upnp:{
             	Intent intent = new Intent();
        	    intent.setClass(this,UpnpMainActivity.class);
    	     	startActivity(intent);
            	break;
            }
            case R.id.alarm:{
            	showDialog(0);
            	break;
            }
            
            case R.id.person:{
            	showdialog();
                break;
            }
            
            case R.id.reset:{
            	reset();
            	break;
            }
        }
        return true;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.	
	      getMenuInflater().inflate(R.menu.main, menu);
	        if (!mScanning) {
	            menu.findItem(R.id.menu_stop).setVisible(false);
	            menu.findItem(R.id.menu_scan).setVisible(true);
	            menu.findItem(R.id.menu_refresh).setActionView(null);
	            menu.findItem(R.id.upnp).setVisible(true);
	            menu.findItem(R.id.alarm).setVisible(true);
	            menu.findItem(R.id.person).setVisible(true);

	            
	        } else {
	            menu.findItem(R.id.menu_stop).setVisible(true);
	            menu.findItem(R.id.menu_scan).setVisible(false);
	            menu.findItem(R.id.menu_refresh).setActionView(
	                    R.layout.actionbar_indeterminate_progress);
	            menu.findItem(R.id.upnp).setVisible(true);
	            menu.findItem(R.id.alarm).setVisible(true);
	            menu.findItem(R.id.person).setVisible(true);


	        }
	        return true;
	        	        
	}
	
	
	protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }

        // Initializes list view adapter.
//        Toast.makeText(this, "setlistadapter", Toast.LENGTH_LONG).show();

        
        scanLeDevice(true);
    }
	
	
	
	public void reset(){
		
		delaymin = 0;
		setalarmed = false;
		setalarmed2 = false;
 		setalarmed3 = false;

		
        SharedPreferences settings = getSharedPreferences ("user", 0);
        SharedPreferences.Editor PE = settings.edit();

        PE.putInt("usertaken", 0);
        PE.commit();
          
		
	}
	
	
	public void showdialog (){
		
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("輸入資料");
		//alert.setMessage("o");

		// Set an EditText view to get user input 
	 
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.personallayout, null);
		
		final EditText nameinput = (EditText) textEntryView.findViewById(R.id.edittext1 );
		final EditText ageinput = (EditText) textEntryView.findViewById(R.id.edittext2 );
		final EditText phoneinput = (EditText) textEntryView.findViewById(R.id.edittext3 );

        final RadioButton manradio = (RadioButton) textEntryView.findViewById(R.id.rdo1);
        final RadioButton girlradio = (RadioButton) textEntryView.findViewById(R.id.rdo2);

        
        SharedPreferences settings = getSharedPreferences ("user", 0);
        String name = settings.getString("username", "");
        int age = settings.getInt("userage", 20);
        String phonenumber = settings.getString("userphone", "");
        Boolean isman = settings.getBoolean("isman", true);
        
        nameinput.setText(name);
        ageinput.setText(age+"");
        phoneinput.setText(phonenumber);
          if (isman)
        	  manradio.setChecked(true);
          else
        	  girlradio.setChecked(true);
          
        
        alert.setView(textEntryView) ;	 

        
        
 		alert.setPositiveButton("確定", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String name = nameinput.getText().toString();
		  int age = Integer.parseInt(ageinput.getText().toString()) ;
		  String phonenumber = phoneinput.getText().toString()  ;

		  //String phonenumber = phoneinput.getText().toString();
		  boolean isman = true;
		  
		  
		 // String message = name + age;
		  
		  if ( girlradio.isChecked())
			  isman = false;
		  
		  
	         SharedPreferences settings = getSharedPreferences ("user", 0);

             SharedPreferences.Editor PE = settings.edit();
	         PE.putString("username", name);
	         PE.putInt ("userage",age);
	         PE.putString ("userphone",phonenumber);
             PE.putBoolean("isman", isman);
	         PE.commit();
	         
	         
		  //Toast.makeText(MainActivity.this, name+" "+age+ " "+isman+" "+phonenumber, Toast.LENGTH_LONG).show();
			  
		  // Do something with value!
		  }
		});

		alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();	
		 
	}

	
	
	private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            //mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            mInflator = (LayoutInflater)MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // General ListView optimization code.
            if (view == null) {

                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0){
                viewHolder.deviceName.setText(deviceName);
                Log.i("done","done");
            }
            else{
                viewHolder.deviceName.setText("unknow");
                Log.i("done","done");}

            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
	}
        
        static class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
        
        
        
        
}
