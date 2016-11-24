package com.aaronlife.callhelper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SmsEditActivity extends Activity 
{
	int textSize; // 要顯示的文字大小
	EditText editSms; // 簡訊輸入元件
	String name;      // 聯絡人
	String number;    // 簡訊號碼
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_edit);
		
		textSize = MyToolBox.getInt(this, MainActivity.KEY_TEXT_SIZE, 40);
		
		// 設定文字大小
		TextView txtTitle = (TextView)findViewById(R.id.title);
		editSms = (EditText)findViewById(R.id.editSms);
		Button btnSend = (Button)findViewById(R.id.btnSend);
		Button btnCancel = (Button)findViewById(R.id.btnCancel);
		
		// 取得MainActivity傳過來的要發送的對方姓名和電話
		name = this.getIntent().getStringExtra("name");
		number = this.getIntent().getStringExtra("number");
		
		// 根據目前MainActivity文字大小來顯示文字
		txtTitle.setText(getText(R.string.send_to) + name);
		txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		editSms.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		btnSend.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
	}
	
	// 使用者點擊了傳送按鈕
	public void btnSend(View v)
	{
		final String message = editSms.getText().toString();
		
		if(message.length() <= 0)
		{
			// 沒有輸入文字，無法傳送
			MyToolBox.toast(this, getText(R.string.cannot_empty).toString(), textSize);
		}
		else
		{
			// 請使用者確認是否要傳送
			MyToolBox.Dlalog(this, getText(R.string.are_you_sure) + name + "?", textSize, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					// 傳送簡訊
					MyToolBox.sendSms(SmsEditActivity.this, number, message);
					finish();
				}
			});
		}
	}
	
	// 取消按鈕
	public void btnCancel(View v)
	{
		finish(); // 關閉Activity
	}
}
