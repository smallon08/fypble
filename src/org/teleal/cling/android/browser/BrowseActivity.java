/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.teleal.cling.android.browser;
import android.R.integer;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

 import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.InvalidValueException;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.SwitchableRouter;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.message.UpnpResponse;

 import com.example.bletesting.R;

import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author Christian Bauer
 */
public class BrowseActivity extends ListActivity {
    // private static final Logger log = Logger.getLogger(BrowseActivity.class.getName());
    private ArrayAdapter<DeviceDisplay> listAdapter;
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private AndroidUpnpService upnpService;
    
    public int on = 1;
    private Timer mytimer ;
    
    int mytime = 0 , noofdevice = 0;
    
    private Handler handler=new Handler();
    
    final Runnable runnable = new Runnable()
    {
        public void run() 
        {
 
        	searchNetwork(); 
        	//Toast.makeText(BrowseActivity.this, "searching", Toast.LENGTH_SHORT).show();
            
        	if (noofdevice>0){
        	   	//Toast.makeText(BrowseActivity.this, "removehandler", Toast.LENGTH_SHORT).show();
        	     }
          handler.postDelayed(this,  10000);
        }
    };
    
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	
        	//Toast.makeText(BrowseActivity.this, "connected", Toast.LENGTH_LONG).show();
            upnpService = (AndroidUpnpService) service;
        	//Toast.makeText(BrowseActivity.this, , Toast.LENGTH_LONG).show();
            
            // Refresh the list with all known devices
            listAdapter.clear();
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }
            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);
            // Search asynchronously for all devices
            upnpService.getControlPoint().search();
        }
        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };
    
    
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	ServiceId serviceId = new UDAServiceId("SwitchPower");
    	RemoteService switchPower;
    	
    	if ((switchPower = (RemoteService)listAdapter.getItem(position).getDevice().findService(serviceId)) != null) {
    		  
    		                      //System.out.println("Service discovered: " + switchPower);
    		                     
    		                    for (int i = 0 ; i<=1; i++ ){
    		                    	on++;
    		                     
    		                      executeAction(upnpService, switchPower);}
    	}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        setListAdapter(listAdapter);
        //mytimer = new Timer ();
    
        
        //==========================================================================
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        
        
        //WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //wifiManager.setWifiEnabled(true);
        
        int ipAddress = wifiInfo.getIpAddress();
        
        String ipBinary = Integer.toBinaryString(ipAddress);

      //Leading zeroes are removed by toBinaryString, this will add them back.
         while(ipBinary.length() < 32) {
          ipBinary = "0" + ipBinary;
         }

      //get the four different parts
      String a=ipBinary.substring(0,8);
      String b=ipBinary.substring(8,16);
      String c=ipBinary.substring(16,24);
      String d=ipBinary.substring(24,32);

      //Convert to numbers
      String actualIpAddress =Integer.parseInt(d,2)+"."+Integer.parseInt(c,2)+"."+Integer.parseInt(b,2)+"."+Integer.parseInt(a,2);
     // Toast.makeText(this, actualIpAddress, Toast.LENGTH_SHORT).show();

  //==========================================================================================
       
      getApplicationContext().bindService(
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
        
          handler.postDelayed(runnable, 10000);
        
        
    }
    
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	 
        	
        }
        return super.onKeyDown(keyCode, event);
    }

    
    
    public void goback(){
    	Intent updateIntent = new Intent();
        updateIntent.setAction("resume2");
                
       // updateIntent.putExtra("text", "This is the string to show");
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(updateIntent);
        
    	 // onBackPressed();
           BrowseActivity.this.finish();  
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        getApplicationContext().unbindService(serviceConnection);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	 
	      getMenuInflater().inflate(R.menu.upnpmenu, menu);
 
	      Toast.makeText(this, "created", Toast.LENGTH_SHORT ).show();
    	   return true;
    	   
       
     }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                searchNetwork();
                break;
            case R.id.network:
                if (upnpService != null) {
                    SwitchableRouter router = (SwitchableRouter) upnpService.get().getRouter();
                    if (router.isEnabled()) {
                        Toast.makeText(this, R.string.disabling_router, Toast.LENGTH_SHORT).show();
                        router.disable();
                    } else {
                        Toast.makeText(this, R.string.enabling_router, Toast.LENGTH_SHORT).show();
                        router.enable();
                    }
                }
                break;
            case R.id.debug:
                Logger logger = Logger.getLogger("org.teleal.cling");
                if (logger.getLevel().equals(Level.FINEST)) {
                    Toast.makeText(this, R.string.disabling_debug_logging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.INFO);
                } else {
                    Toast.makeText(this, R.string.enabling_debug_logging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.FINEST);
                }
                break;
        }
        return false;
    }
    protected void searchNetwork() {
        if (upnpService == null) return;
         Toast.makeText(this, "searchingLAN" , Toast.LENGTH_SHORT).show();
        
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
    }
    
    
  
    protected class BrowseRegistryListener extends DefaultRegistryListener {
        /* Discovery performance optimization for very slow Android devices! */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }
        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            showToast(
                    "Discovery failed of '" + device.getDisplayString() + "': " +
                            (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
                    true
            );
            deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */
        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
             
        	  
        	deviceAdded(device);
 
        	ServiceId serviceId = new UDAServiceId("SwitchPower");
        	RemoteService switchPower;
        	
        	
        	if ((switchPower = device.findService(serviceId)) != null) {
        		noofdevice ++ ;
        		handler.removeCallbacks(runnable);
        		final RemoteService switchPower2 = switchPower;
                for (int i = 0 ; i<=1; i++ ){
                	on++;
                 
                  executeAction(upnpService, switchPower2);
 
                
                
                } 
                
                goback();
        	
        	}
        		
     
        }
        
        
        
        
        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }
        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
        	Toast.makeText(BrowseActivity.this, "localdevice", Toast.LENGTH_SHORT).show();
            deviceAdded(device);
        }
        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }
        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    DeviceDisplay d = new DeviceDisplay(device);
                    int position = listAdapter.getPosition(d);
                    if (position >= 0) {
                        // Device already in the list, re-set new value at same position
                        listAdapter.remove(d);
                        listAdapter.insert(d, position);
                    } else {
                        listAdapter.add(d);
                    }
                    // Sort it?
                    // listAdapter.sort(DISPLAY_COMPARATOR);
                    // listAdapter.notifyDataSetChanged();
                }
            });
        }
        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    listAdapter.remove(new DeviceDisplay(device));
                }
            });
        }
    }
    protected void showToast(final String msg, final boolean longLength) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(
                        BrowseActivity.this,
                        msg,
                        longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
    protected class DeviceDisplay {
        Device device;
        public DeviceDisplay(Device device) {
            this.device = device;
        }
        public Device getDevice() {
            return device;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceDisplay that = (DeviceDisplay) o;
            return device.equals(that.device);
        }
        @Override
        public int hashCode() {
            return device.hashCode();
        }
        @Override
        public String toString() {
            String name =
                    device.getDetails() != null && device.getDetails().getFriendlyName() != null
                            ? device.getDetails().getFriendlyName()
                            : device.getDisplayString();
            // Display a little star while the device is being loaded (see performance optimization earlier)
            return device.isFullyHydrated() ? name : name + " *";
        }
    }
    static final Comparator<DeviceDisplay> DISPLAY_COMPARATOR =
            new Comparator<DeviceDisplay>() {
                public int compare(DeviceDisplay a, DeviceDisplay b) {
                    return a.toString().compareTo(b.toString());
                }
            };
            
            
            
            
            
          
           
            void executeAction(AndroidUpnpService upnpService, RemoteService switchPowerService) {
            	  
            	              ActionInvocation setTargetInvocation =
            	                     new SetTargetActionInvocation(switchPowerService);
            	
                          // Executes asynchronous in the background
            	              upnpService.getControlPoint().execute(
            	                      new ActionCallback(setTargetInvocation) {
            	  
            	                          @Override
            	                          public void success(ActionInvocation invocation) {
            	                              assert invocation.getOutput().length == 0;
            	                              //System.out.println("Successfully called action!");
            	                         }
            	 
            	                         @Override
            	                          public void failure(ActionInvocation invocation,
            	                                              UpnpResponse operation,
            	                                              String defaultMsg) {
            	                              //System.err.println(defaultMsg);
            	                          }
            	                      }
            	              );
            	  
            	      }
           
            
            
            class SetTargetActionInvocation extends ActionInvocation {
            	 
            	         SetTargetActionInvocation(RemoteService service) {
            	              super(service.getAction("SetTarget"));
            	             //super(service.getAction("setstring"));
            	             
            	             try {
            	            	 //.i("done","done");
            	                // Throws InvalidValueException if the value is of wrong type
            	            	if (on %2==1){
            	                   setInput("NewTargetValue", true);
            	                  //setInput("NewTargetValue2", "abc");
            	                 // Log.i("done1","done1");  
            	            	}
            	            	else {
            	                   setInput("NewTargetValue", false);
            	                    //setInput("NewTargetValue2", "abcdd");
            	                // Log.i("done2","done2");  
            	            	}
            	             } catch (InvalidValueException ex) {
            	                 System.err.println(ex.getMessage());
            	                 System.exit(1);
            	             }
            	         }
            	     }
            	     
}