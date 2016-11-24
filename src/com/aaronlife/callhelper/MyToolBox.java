package com.aaronlife.callhelper;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MyToolBox 
{
	// 撥打電話（需要權限：android.permission.CALL_PHONE）
	public static void callPhone(Context context, String number)
	{
		Intent it = new Intent("android.intent.action.CALL", Uri.parse("tel:" + number));
		// 明確指定使用內建的撥號器，否則如有安裝其它撥號軟體(如skype)，教會跳出選擇畫面，老人家會不知所措
		it.setPackage("com.android.phone");
		context.startActivity(it);
	}
	
	// 傳送簡訊（需要權限：android.permission.SEND_SMS）
	public static void sendSms(Context context, String number, String message)
	{
		// 取得SmsManager
		SmsManager sm = SmsManager.getDefault();
		
		// 建立空的PendingIntent
		// 所謂PendingIntent即是不會立刻做動作的Intent, 會在事件發生或是條件滿足時才會執行該Intent
		PendingIntent pi = PendingIntent.getBroadcast
								(context,          // 發出該Intent的Activity 
								 0,             // Request Code, 此處給0即可 
								 new Intent(),  // 一個空的Intent 
								 0);            // 旗標
		
		try
		{
			sm.sendTextMessage(number, // 要送出簡訊的電話
							   null,    // 電話號碼位址
							   message, // 簡訊內容
							   pi,      // 發送簡訊用的PendingIntent
							   null);   // 接收回覆用的Intent(這裡不需要)
			
			Toast.makeText(context, context.getText(R.string.sms_sent) + number, Toast.LENGTH_SHORT).show();
		}
		catch(Exception e)  // 如果發送錯誤或其他錯誤發生
		{
			Toast.makeText(context, context.getText(R.string.send_to) + number + context.getText(R.string.fail), Toast.LENGTH_SHORT).show();
		}
	}
	
	// SharedPreferences檔名
	final static String SETTINGS = "Settings";
	
	// 從SharedPreferences存入字串
	public static void putInt(Context context, String name, int value)
	{
		SharedPreferences sp = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		sp.edit().putInt(name, value).commit();
	}
	
	// 從SharedPreferences取出整數
	public static int getInt(Context context, String name, int defValue)
	{
		SharedPreferences sp = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		return sp.getInt(name, defValue);
	}
	
	// 從SharedPreferences存入字串
	public static void putString(Context context, String name, String value)
	{
		SharedPreferences sp = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		sp.edit().putString(name, value).commit();
	}
	
	// 從SharedPreferences取出字串
	public static String getString(Context context, String name, String defValue)
	{
		SharedPreferences sp = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		return sp.getString(name, defValue);
	}
	
	// 建立自訂文字大小的AlertDialog
	public static void Dlalog(Context context, String message, int textSize, OnClickListener listener)
	{
		AlertDialog.Builder dlg = new AlertDialog.Builder(context);
		dlg.setMessage(message);
		
		dlg.setPositiveButton(R.string.ok, listener);
		dlg.setNegativeButton(R.string.cancel, null);
		
		// 改變AlertDialog的預設文字大小
		AlertDialog dialog = dlg.show();
		TextView textView = (TextView) dialog.findViewById(android.R.id.message);
	    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
	    ((Button)dialog.findViewById(android.R.id.button1)).setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
	    ((Button)dialog.findViewById(android.R.id.button2)).setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
	    ((Button)dialog.findViewById(android.R.id.button3)).setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
	}
	
	// 可以改變文字大小的Toast
	public static void toast(Context context, String message, int textSize)
    {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);

        TextView textView = new TextView(context);
        textView.setBackgroundColor(Color.DKGRAY);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        Typeface typeface = Typeface.create("serif", Typeface.BOLD);
        textView.setTypeface(typeface);
        textView.setPadding(10, 10, 10, 10);
        textView.setText(message);

        toast.setView(textView);
        toast.show();
    }
	
	///////////////////////////////////////////////////////////////////////////////////////
	//
	// 取得聯絡人資料
	//
	public static Bitmap openPhoto(Context context, long photoId) 
	{
		Cursor cursor = context.getContentResolver().query(
											ContactsContract.Data.CONTENT_URI, 
											new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO}, 
											ContactsContract.Data._ID + "=?", 
											new String[] {Integer.toString((int)photoId)}, 
											null);
		
		if (cursor == null) 
		{
			return null;
		}
		try 
		{
			if(cursor.moveToFirst()) 
			{
				byte[] data = cursor.getBlob(0); // 照片為byte陣列資料格式
				if (data != null) 
				return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
			}
		} 
		finally 
		{
			cursor.close(); // 關閉Cursor
		}
		
		return null;  // 該聯絡人沒有照片
	}
	
	public static ArrayList<HashMap<String,Object>> initContactData(Context context)
	{
		/////////////////////////////////////////////////////////////////////////////////
		//
		// 取得手機聯絡人
		//
		ArrayList<HashMap<String,Object>> listContact = new ArrayList<HashMap<String,Object>>();
		
		// 定義要讀取的聯絡人欄位
		final String[] wantedData = new String[]
		{
			ContactsContract.Contacts._ID,                   // 讀取聯絡人ID(流水編號)
			ContactsContract.CommonDataKinds.Phone.NUMBER,   // 電話號碼
			ContactsContract.CommonDataKinds.Phone.TYPE,     // 電話類型（家裡、公司等等）
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, // 姓名
			ContactsContract.CommonDataKinds.Phone.PHOTO_ID,     // 照片ID（用來取得照片用）
		};
		
		// 透過ContentResolver物件讀取聯絡人資料
		ContentResolver cr = context.getContentResolver();
		
		// 聯絡人資料URI
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		
		// 取得聯絡人資料並存入Cursor物件
		Cursor c = cr.query(uri, // 聯絡人資料存放路徑 
		wantedData,  // 要讀出的欄位
		null,        // 選擇條件
		null,        // 選擇條件參數
		null);       // 排序
		
		// 透過Cursor物件將聯絡人資料一筆一筆讀出來後放到ArrayList裏頭
		while(c.moveToNext())  // 將資料讀到最後一筆時會回傳false
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			
			// 取得聯絡人名字欄位索引值
			int indexName = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
			map.put("name", c.getString(indexName)); // 透過索引取得名字
			
			// 取得聯絡人照片
			int indexPhoto = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID); // 取得ID欄位索引值
			map.put("photo", openPhoto(context, c.getInt(indexPhoto))); // 透過索引取得照片
			
			// 取得電話欄位索引值
			int indexNum = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			map.put("number", c.getString(indexNum)); // 透過索引取得電話號碼
			
			// 存入ArrayList
			listContact.add(map);
		}
		
		// 關閉Cursor
		c.close();
		
		return listContact;
	}
}
