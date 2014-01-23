/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package io.cordova.hellocordova;

import org.apache.cordova.CordovaActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

public class HelloCordova extends CordovaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.loadUrl("file:///android_asset/www/index.html");
		
		if(checkNetWorkStatus()){
			new UpdateManager(this,"auto").updateVersion();
			//updateVersion();// 启动时就检查 或者 在菜单中调用
		}
	}
	
	
	private boolean checkNetWorkStatus() {
	 if(isConnect()==false){
		 Toast.makeText(this, "网络已断开，请检查网络设置", Toast.LENGTH_SHORT).show();
		 new AlertDialog.Builder(this).setMessage("检查到没有可用的网络连接,请打开网络连接").setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ComponentName  cm  = new ComponentName("com.android.settings",
							"com.android.settings.Settings");
					Intent  intent = new Intent();
					intent.setComponent(cm);
					intent.setAction("android.intent.action.VIEW");
					startActivity(intent);
					finish();
				}
			}).show();
		 return  false;
	}
	 	return  true;
}
	
	 private boolean isConnect() {
		 ConnectivityManager  cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
		 NetworkInfo  netinfo= cm.getActiveNetworkInfo();
		if(netinfo !=null && netinfo.isConnected()){
			 if (netinfo.getState() == NetworkInfo.State.CONNECTED) { 
                 return true; 
             } 
		 }
		 return  false;

	}

}
