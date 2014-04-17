package org.teleal.cling.android.browser;

//import android.app.Service;
import android.net.wifi.WifiManager;
import android.os.Handler;

import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.meta.Service;
//import org.teleal.cling.model.meta.Service;
//import org.teleal.cling.model.meta.Service;
//import org.teleal.cling.binding.xml.Descriptor.Service;
//import org.teleal.cling.binding.xml.Descriptor.Service;
//import org.teleal.cling.model.types.ServiceType;
//import org.teleal.cling.binding.xml.Descriptor.Service;
//import org.teleal.cling.model.types.ServiceType;
//import org.teleal.cling.model.types.UDAServiceType;
//import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDAServiceType;

/**
 * @author Christian Bauer
 */
public class BrowserUpnpService extends AndroidUpnpServiceImpl {

	
 
	
    @Override
    protected AndroidUpnpServiceConfiguration createConfiguration(WifiManager wifiManager) {
        return new AndroidUpnpServiceConfiguration(wifiManager) {

             //The only purpose of this class is to show you how you'd
              //configure the AndroidUpnpServiceImpl in your application:

          // @Override
          // public int getRegistryMaintenanceIntervalMillis() {
            //   return 7000;
           //}

           
           @Override
           public ServiceType[] getExclusiveServiceTypes() {
               return new ServiceType[] {
                       new UDAServiceType("SwitchPower")
               };
           }
 
             

        };
    }

}
