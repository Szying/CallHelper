package com.aaronlife.callhelper;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class BlockUnknownService extends Service 
{
	// 建立手機狀態傾聽器, 用來針對手機不同狀態想要做的事情
	public class MyPhoneCallListener extends PhoneStateListener // 必須繼承自PhoneStateListener
	{
		// 收機狀態改變時會被呼叫的方法
		@Override
		public void onCallStateChanged(int state, String incomingNumber) 
		{
			// 判讀手機狀態
			switch(state)
			{
			case TelephonyManager.CALL_STATE_IDLE:    // 待機
				//Toast.makeText(BlockUnknownService.this, R.string.idle, Toast.LENGTH_SHORT).show();
				audioManager.setRingerMode(ringingMode); // 恢復響鈴模式
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: // 通話中
				//Toast.makeText(BlockUnknownService.this, R.string.offhook, Toast.LENGTH_SHORT).show();
				break;
			case TelephonyManager.CALL_STATE_RINGING: // 響鈴中
				//Toast.makeText(BlockUnknownService.this, R.string.ringing, Toast.LENGTH_SHORT).show();
				
				// 紀錄舊的響鈴模式
				ringingMode = audioManager.getRingerMode();
				
				boolean isInContact = false;
				if(null != audioManager)
				{
					// 比對電話號碼, 如果不在聯絡人內
					for(HashMap<String, Object> data : listContact)
					{
						if(data.get("number").equals(incomingNumber))
						{
							isInContact = true;
							break;
						}
					}
					
					// 將響鈴模式設為靜音
					if(!isInContact)
						audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				}
				break;
				
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}
		
	private ArrayList<HashMap<String, Object>> listContact = null;
	private int ringingMode;
	AudioManager audioManager;
	MyPhoneCallListener ps = new MyPhoneCallListener();
	
	@Override
	public void onCreate() 
	{
		// TODO Auto-generated method stub
		super.onCreate();
		
		// 讀取手機聯絡人
		listContact = MyToolBox.initContactData(this); 
		
		// 準備好AudioManager用來改變手機鈴聲模式
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		// 紀錄舊的響鈴模式
		ringingMode = audioManager.getRingerMode();
		
		// 取得系統通話服務物件
		TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		
		// 將傾聽器設定給系統通話服務
		telMgr.listen(ps, PhoneStateListener.LISTEN_CALL_STATE);
		
		Toast.makeText(BlockUnknownService.this, R.string.block, Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() 
	{
		// 取得系統通話服務物件
		TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		
		// 將傾聽器設定給系統通話服務
		telMgr.listen(ps, PhoneStateListener.LISTEN_NONE);
		
		Toast.makeText(BlockUnknownService.this, R.string.unblock, Toast.LENGTH_SHORT).show();
		
		super.onDestroy();
	}
}
