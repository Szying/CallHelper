package com.aaronlife.callhelper;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter 
{
	private int textSize = 0;
	private LayoutInflater inflater = null; // 介面實體產生器
	private ArrayList<HashMap<String,Object>> listContact = null; // 存放聯絡人的欄位

	public ContactAdapter(Context context, ArrayList<HashMap<String,Object>> listContact, int textSize)
	{
		this.textSize = textSize;
		this.listContact = listContact;
		
		inflater = LayoutInflater.from(context);
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
		// 沒用到
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		// 實體化Layout
		convertView = inflater.inflate(R.layout.adapter, null);
		
		// 建立ImageView
		ImageView photo = (ImageView)convertView.findViewById(R.id.photo);
		Bitmap p = (Bitmap)listContact.get(position).get("photo");
		
		// 設定聯絡人照片
		if(p != null)
		{
			photo.setImageBitmap(p);
		}
		else
			photo.setImageResource(R.drawable.ic_launcher);
		
		// 建立TextView顯示聯絡人姓名
		TextView name = (TextView)convertView.findViewById(R.id.name);
		name.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        name.setText(listContact.get(position).get("name").toString());
        name.setTag(listContact.get(position).get("number").toString());
		
		return convertView;
	}

	public void setTextSize(int textSize)
	{
		this.textSize = textSize;
	}
}
