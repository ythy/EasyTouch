package com.mx.easytouch.adapter;

import java.util.List;

import com.mx.easytouch.R;
import com.mx.easytouch.vo.InstallPackage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class FavAppAdapter extends BaseAdapter {

	private Context mcontext;
	private LayoutInflater layoutInflator;
	private List<InstallPackage> list;
	private FavAppTouchListener mListener;

	public FavAppAdapter() {
	}

	public FavAppAdapter(Context context, List<InstallPackage> items) {
		mcontext = context;
		layoutInflator = LayoutInflater.from(mcontext);
		list = items;
	}

	public void setFavAppTouchListener( FavAppTouchListener listener) {
		mListener = listener;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		Component component = null;

		if (convertView == null) {
			convertView = layoutInflator.inflate(
					R.layout.adapter_favapp, null);
			component= new Component();
			component.tvName = (TextView) convertView
					.findViewById(R.id.tvAppName);
			component.btnDel = (Button) convertView
					.findViewById(R.id.btnFavDel);
			convertView.setTag(component);
		}
		else
			component = (Component) convertView.getTag();

		final Component currentComponent = component;
		final int position = arg0;
		try {
			component.tvName.setText(list.get(arg0).getAppName());
			component.btnDel.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String name = currentComponent.tvName.getText().toString();
					final int id = list.get(position).getId();
					InstallPackage fav = new InstallPackage();
					fav.setId(id);
					mListener.onDelBtnClickListener(fav);
				}
			});

			component.tvName.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
		                 //   index = position;
	                }
	                return false;
				}
	        });

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}



	public interface FavAppTouchListener {
		public void onDelBtnClickListener(InstallPackage info);
	}

	private static class Component{
		 public TextView tvName;
		 public Button btnDel;
	}
}
