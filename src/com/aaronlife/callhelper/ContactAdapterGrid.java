package com.aaronlife.callhelper;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;


public class ContactAdapterGrid extends BaseAdapter 
{
	Context context = null;
	ArrayList<HashMap<String,Object>> listContact = null; // 存放聯絡人欄位
	
	int gridWidth = 0; // 每列顯示的聯絡人數量
	
	Button.OnClickListener btnCallPhone = null; // 聯絡人被點擊後要處理的事情傾聽器
	
	public ContactAdapterGrid(Context context, ArrayList<HashMap<String,Object>> listContact, int gridWidth, Button.OnClickListener btnCallPhone)
	{
		this.context = context;
		this.listContact = listContact;
		this.gridWidth = gridWidth;
		this.btnCallPhone = btnCallPhone;
	}

	@Override
	public int getCount() 
	{
		// 要顯示在ListView的資料數量
		return listContact.size();
	}

	@Override
	public Object getItem(int position) 
	{
		// 根據系統給的位置給予相對應位置的聯絡人資料
		return listContact.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		// 使用Button元件來當成GridView內的元件型態
		convertView = new Button(context);
		convertView.setLayoutParams(new GridView.LayoutParams(gridWidth - 10, gridWidth - 10));
		
		// 取得聯絡人照片並設定到按鈕上
		Bitmap bitmap = (Bitmap)listContact.get(position).get("photo");
		Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
		if(bitmap != null)
			((Button)convertView).setBackgroundDrawable(drawable);
		else
		{
			((Button)convertView).setText(listContact.get(position).get("name").toString());
			((Button)convertView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
			((Button)convertView).setBackgroundColor(Color.GRAY);
		}
			
		// 將該聯絡人電話號碼存放在Tag裡
		convertView.setTag(listContact.get(position).get("number").toString());
		
		// 設定傾聽器
		convertView.setOnClickListener(btnCallPhone);
		
		return convertView;
	}

	public void setGridWidth(int gridWidth)
	{
		this.gridWidth = gridWidth;
	}
}
