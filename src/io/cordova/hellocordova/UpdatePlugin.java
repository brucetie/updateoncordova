package io.cordova.hellocordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;


public class UpdatePlugin extends CordovaPlugin {

	 private static final String SEND = "updatePlugin";
	    
	 @Override
	    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
			if (action.equals(SEND)) {
	        	HelloCordova myAct = (HelloCordova)this.cordova.getActivity();
	        	new UpdateManager(myAct.getContext(),"manual").updateVersion();
	            return true;
	        }
	        return false;
	    }

}
