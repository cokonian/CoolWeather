package com.coolweather.app;

import android.text.TextUtils;

public class Utility 
{
    //�����ʹ�����������ص�province����
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allProvinces=response.split(",");
			if(allProvinces!=null&&allProvinces.length>0)
			{
				for(String p:allProvinces)
				{
					String[] array=p.split("\\|");
					Province province=new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	//�����ʹ�����������ص�city������
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId)
	{
	    if(!TextUtils.isEmpty(response))
	    {
	    	String[] allCities=response.split(",");
	    	if(allCities!=null&&allCities.length>0)
	    	{
	    		for(String p:allCities)
	    		{
	    			String[] array=p.split("\\|");
	    			City city=new City();
	    			city.setCityCode(array[0]);
	    			city.setCityName(array[1]);
	    			city.setProvinceId(provinceId);
	    			coolWeatherDB.saveCities(city);
	    		}
	    		return true;
	    	}	    		
	    }
		return false;	
	}
	
	//�����ʹ�����������ص�county������
	public synchronized static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allCounties=response.split(",");
			if(allCounties!=null&&allCounties.length>0)
			{
				for(String p:allCounties)
				{
					String[] array=p.split("\\|");
					County county=new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					coolWeatherDB.saveCounties(county);
				}
				return true;
			}
		}
		return false;
	}
}
