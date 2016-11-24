package com.aaronlife.callhelper;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

// 更進一步：
//   1. 記住字型大小
//   2. 去除重複的電話號碼
//   3. 背景和文字顏色的改變
//   4. 記住現在的顯示模式
public class MainActivity extends Activity 
{
	static final String KEY_TEXT_SIZE = "TextSize";   // 儲存文字大小的key名稱
	static final String KEY_COLUMN_NUM = "ColumnNum"; // 儲存每列數量的key名稱
	static final int CALL_MODE_NORMAL = 0; // 響鈴模式一般
	static final int CALL_MODE_NON_FRIEND_SILENT = 1; // 非好友靜音
	
	ContactAdapter ca = null;       // ListView的Adapter
	ContactAdapterGrid cag = null;  // GridView的Adapter
	
	ListView list = null;           // 聯絡人清單（ListView)物件
	GridView grid = null;           // 聯絡人格子（GridView)物件
	Button btnMode = null;          // 切換撥號/簡訊模式的按鈕
	ImageView btnBlockMode = null;     // 切換響鈴模式
	ImageView btnViewMode = null;   // 切換顯示模式的按鈕
	
	int textSize = -1;                          // 文字大小
	int currentLayout = R.layout.activity_main; // 目前使用的版面
	int columnNum = 3;                          // GridView每列顯示的數量
	int currentRingingMode = CALL_MODE_NORMAL;  // 目前響鈴模式
	
	private ArrayList<HashMap<String, Object>> listContact = null;
	SharedPreferences sp;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(currentLayout);
        
        // 取得SharedPreferences物件
        sp = getPreferences(MODE_PRIVATE);
        
        // 取得使用者設定的文字大小，如果未設定，就給預設值
        textSize = MyToolBox.getInt(this, KEY_TEXT_SIZE, -1);
        columnNum = MyToolBox.getInt(this, KEY_COLUMN_NUM, -1);
        
        // 尚未設定文字大小的值
        if(textSize == -1)
        {
        	textSize = 40; // 單位：sp
        	// 將文字大小存到手機上
        	MyToolBox.putInt(this, KEY_TEXT_SIZE, textSize);
        }
        
        if(columnNum == -1)
        {
        	columnNum = 3;
        	// 將每列要顯示的聯絡人數量存到手機上
        	MyToolBox.putInt(this, KEY_COLUMN_NUM, columnNum);
        }
        
        listContact = MyToolBox.initContactData(this); // 讀取手機聯絡人，必須在initView()之前將聯絡人資料準備好
        initView();        // 介面顯示初始化
        
        // 恢復響鈴模式
        currentRingingMode = sp.getInt("RingingMode", -1);
        
        switch(currentRingingMode)
        {
        case CALL_MODE_NON_FRIEND_SILENT:
        	btnBlockMode.setImageResource(R.drawable.phone_block);
	        startService(new Intent(this, BlockUnknownService.class));
	        break;
	        
        default: 
        	currentRingingMode = CALL_MODE_NORMAL;
	        btnBlockMode.setImageResource(R.drawable.phone_volume);
	        stopService(new Intent(this, BlockUnknownService.class));
	        break;
	    }
		
		
		
    }

    private void initView()
    {
    	// 關聯元件
    	btnMode = (Button)findViewById(R.id.btnMode);
    	btnViewMode = (ImageView)findViewById(R.id.viewMode);
    	btnBlockMode = (ImageView)findViewById(R.id.blockMode);
    	
    	// 根據目前的顯示模式決定要顯示的版面（ListView或GridView）
    	if(currentLayout == R.layout.activity_main)
    	{
    		btnViewMode.setImageResource(R.drawable.grid);
    		initListView(); // 初始化ListView顯示
    	}
    	else
    	{
    		btnViewMode.setImageResource(R.drawable.list);
    		initGridView(); // 初始化GridView顯示
    	}
    }
    
    private void initListView()
    {
        ca = new ContactAdapter(this, listContact, textSize);
        list = (ListView)findViewById(R.id.listContact);
        
        list.setAdapter(ca);
        
        // ListView使用者選擇選項的傾聽器
        list.setOnItemClickListener(new ListView.OnItemClickListener()
        {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				if(ca != null)
				{
					// 取得被選取的電話號碼
					final TextView name = (TextView)view.findViewById(R.id.name);
					
					// 判斷是電話模式還是簡訊模式
					if(btnMode.getText().toString().equals(getText(R.string.send_sms)))
					{
						// 開啟簡訊Activity
						Intent it = new Intent(MainActivity.this, SmsEditActivity.class);
						it.putExtra("name", name.getText().toString());
						it.putExtra("number", name.getTag().toString());
						startActivity(it);
					}
					else
					{
						MyToolBox.Dlalog(MainActivity.this, "" + getText(R.string.are_you_sure_call) + name.getText().toString() + "?", textSize, new OnClickListener() 
						{
							@Override
							public void onClick(DialogInterface dialog, int which) 
							{
								// 撥電話
								MyToolBox.callPhone(MainActivity.this, name.getTag().toString());	
							}
						});	
					}
				}
			}
        });
    }
    
    private void initGridView()
    {
    	grid = (GridView)findViewById(R.id.gridContact);
    	
    	// 設定GridView美航要顯示幾個元件
    	grid.setNumColumns(columnNum);
    	
    	// 根據要顯示幾個元件來決定每個元件的寬
    	grid.setColumnWidth(list.getWidth() / columnNum);
    	
    	// 因為剛setContentView, 整個view還沒被初始化完畢, 所以如果呼叫GridView.getWidth()很可能得到0, 
    	// 所以先拿ListView的getWidth()來用
    	cag = new ContactAdapterGrid(this, listContact, list.getWidth() / columnNum, new Button.OnClickListener()
    	{
    		// 使用者點擊了GridView上的某個按鈕
    		@Override
    		public void onClick(final View v) 
    		{
    			String name = ((Button)v).getText().toString();
    			
    			if(btnMode.getText().toString().equals(getText(R.string.send_sms)))
				{
					// 開啟簡訊Activity
					Intent it = new Intent(MainActivity.this, SmsEditActivity.class);
					it.putExtra("name", name);
					it.putExtra("number", v.getTag().toString());
					startActivity(it);
				}
				else
				{
					MyToolBox.Dlalog(MainActivity.this, "" + getText(R.string.are_you_sure_call) + name + "?", textSize, new OnClickListener() 
					{
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							// 撥電話
			    			MyToolBox.callPhone(MainActivity.this, v.getTag().toString());	
						}
					});
				}
    		}
    	});
    	
    	grid.setAdapter(cag);
    }
    
    // 切換顯示模式（ListView或GridView）
    public void viewMode(View v)
    {
    	if(currentLayout == R.layout.activity_main)
    		currentLayout = R.layout.activity_main_grid;
    	else
    		currentLayout = R.layout.activity_main;
    	
    	setContentView(currentLayout);
    	initView();	
    }
    
    // 切換響鈴模式
    public void blockMode(View v)
    {
    	if(currentRingingMode == CALL_MODE_NON_FRIEND_SILENT)
    	{
    		currentRingingMode = CALL_MODE_NORMAL;
    		btnBlockMode.setImageResource(R.drawable.phone_volume);
    		Intent intent = new Intent(this, BlockUnknownService.class);
    		stopService(intent);
    		
    		// 存下狀態
    		sp.edit().putInt("RingingMode", CALL_MODE_NORMAL).commit();
    	}
    	else
    	{
    		currentRingingMode = CALL_MODE_NON_FRIEND_SILENT;
    		btnBlockMode.setImageResource(R.drawable.phone_block);
    		Intent intent = new Intent(this, BlockUnknownService.class);
    		startService(intent);
    		
    		// 存下狀態
    		sp.edit().putInt("RingingMode", CALL_MODE_NON_FRIEND_SILENT).commit();
    	} 
    }
    
    // 切換撥電話或簡訊模式
    public void callMode(View v)
    {
    	if(btnMode.getText().toString().equals(getText(R.string.send_sms)))
    		btnMode.setText(R.string.call_phone);
    	else
    		btnMode.setText(R.string.send_sms);
    }
    
    // 放大按鈕被點擊了
    public void zoomIn(View v)
    {
    	if(currentLayout == R.layout.activity_main)
    	{
    		// 改變ListView上的文字大小（最大文字大小為200sp）
    		if(textSize < 200) textSize += 2;
	    	ca.setTextSize(textSize);
	    	list.setAdapter(ca);
	    	
	    	// 將新的文字大小存到手機上
	    	MyToolBox.putInt(this, KEY_TEXT_SIZE, textSize);
    	}
    	else
    	{
    		// 改變GridView每列顯示的元件數量（最少每列顯示1個）
    		if(columnNum > 1) columnNum--;
    		grid.setNumColumns(columnNum);
        	grid.setColumnWidth(list.getWidth() / columnNum);
        	cag.setGridWidth(grid.getWidth() / columnNum);  // 透過grid寬度來計算一個欄位要多寬
        	grid.setAdapter(cag);
        
        	// 將新的每列要顯示的聯絡人數量存到手機上
        	MyToolBox.putInt(this, KEY_COLUMN_NUM, columnNum);
    	}
    }
    
    // 縮小按鈕被點擊了
    public void zoomOut(View v)
    {
    	if(currentLayout == R.layout.activity_main)
    	{
    		// 最小文字大小為10sp
	    	if(textSize > 10) textSize -= 2;
	    	ca.setTextSize(textSize);
	    	list.setAdapter(ca);
	    	
	    	// 將新的文字大小存到手機上
	    	MyToolBox.putInt(this, KEY_TEXT_SIZE, textSize);
    	}
    	else
    	{
    		// 改變GridView每列顯示的元件數量（最多每列顯示20個）
    		if(columnNum < 20) columnNum++;
    		grid.setNumColumns(columnNum);
        	grid.setColumnWidth(list.getWidth() / columnNum);
        	cag.setGridWidth(grid.getWidth() / columnNum);
        	grid.setAdapter(cag);
        	
        	// 將新的每列要顯示的聯絡人數量存到手機上
        	MyToolBox.putInt(this, KEY_COLUMN_NUM, columnNum);
    	}
    }
}
