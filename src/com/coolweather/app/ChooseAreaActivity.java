package com.coolweather.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity 
{
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    
    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList=new ArrayList<String>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
    	if(prefs.getBoolean("city_selected", false))
    	{
    		Intent i=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
    		startActivity(i);
    		finish();
    		return;
    	}
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.choose_area);    	
        listView=(ListView)findViewById(R.id.list_view);
        titleText=(TextView)findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB=CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(currentLevel==LEVEL_PROVINCE)
				{
					selectedProvince=provinceList.get(position);
					queryCities();
				}else if(currentLevel==LEVEL_CITY)
				{
					selectedCity=cityList.get(position);
					queryCounties();
				}else if(currentLevel==LEVEL_COUNTY)
				{
					String countyCode=countyList.get(position).getCountyCode();
					Intent i=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					i.putExtra("county_code", countyCode);
					startActivity(i);
					finish();
				}
			}
        	
        });
        queryProvinces();
    }
    
    //查询全国的省，先从DB查询，若无再去sever查询
    private void queryProvinces()
    {
    	provinceList=coolWeatherDB.loadProvinces();
    	if(provinceList.size()>0)
    	{
    		dataList.clear();
    		for(Province province:provinceList)
    		{
    			dataList.add(province.getProvinceName());
    		}
    		adapter.notifyDataSetChanged();
    		listView.setSelection(0);
    		titleText.setText("中国");
    		currentLevel=LEVEL_PROVINCE;
    	}else{
    		queryFromServer(null,"province");
    	}
    }
    
    private void queryCities()
    {
    	cityList=coolWeatherDB.loadCities(selectedProvince.getId());
    	if(cityList.size()>0)
    	{
    		dataList.clear();
    		for(City city:cityList)
    		{
    			dataList.add(city.getCityName());
    		}
    		adapter.notifyDataSetChanged();
    		listView.setSelection(0);
    		titleText.setText(selectedProvince.getProvinceName());
    		currentLevel=LEVEL_CITY;
    	}else{
    		queryFromServer(selectedProvince.getProvinceCode(),"city");
    	}
    }
    
    private void queryCounties()
    {
    	countyList=coolWeatherDB.loadCounties(selectedCity.getId());
    	if(countyList.size()>0)
    	{   
    		dataList.clear();
    		for(County county:countyList)
    		{
    			dataList.add(county.getCountyName());
    		}
    		adapter.notifyDataSetChanged();
    		listView.setSelection(0);
    		titleText.setText(selectedCity.getCityName());
    		currentLevel=LEVEL_COUNTY;
    	}else{
    		queryFromServer(selectedCity.getCityCode(),"county");
    	}
    }
    
    private void queryFromServer(final String code,final String type)
    {
    	String address;
    	if(!TextUtils.isEmpty(code))
    	{
    		address="http://www.weather.com.cn/data/list3/city"+code+".xml";
    	}else{
    		address="http://www.weather.com.cn/data/list3/city.xml";
    	}
    	showProgressDialog();
    	HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
    		@Override
    		public void onFinish(String response)
    		{
    			boolean result=false;
    			if("province".equals(type))
    			{
    				result=Utility.handleProvincesResponse(coolWeatherDB, response);
    			}else if("city".equals(type))
    			{
    				result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
    			}else if("county".equals(type))
    			{
    				result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
    			}
    			if(result)
    			{
    				runOnUiThread(new Runnable(){
    					@Override
    					public void run()
    					{
    						closeProgressDialog();
    						if("province".equals(type))
    						{
    							queryProvinces();
    						}else if("city".equals(type))
    						{
    							queryCities();
    						}else if("county".equals(type))
    						{
    							queryCounties();
    						}
    					}
    				});
    			}
    		}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "Loading failed", Toast.LENGTH_SHORT).show();
					}
					
				});
			}
    	});
    	
    }
    
    private void showProgressDialog(){
    	if(progressDialog==null)
    	{
    		progressDialog=new ProgressDialog(this);
    		progressDialog.setMessage("Loading");
    		progressDialog.setCanceledOnTouchOutside(false);
    	}
    	progressDialog.show();
    }
    
    private void closeProgressDialog(){
    	if(progressDialog!=null)
    	{
    		progressDialog.dismiss();
    	}
    }
    
    @Override
    public void onBackPressed()
    {
    	if(currentLevel==LEVEL_COUNTY)
    	{
    		queryCities();
    	}else if(currentLevel==LEVEL_CITY)
    	{
    		queryProvinces();
    	}else{
    		finish();
    	}
    }
}
