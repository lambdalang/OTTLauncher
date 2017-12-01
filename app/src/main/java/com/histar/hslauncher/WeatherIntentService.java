// 2014.12.17 Zhao Huabing create Weather Layout
// 2015.07.20 Zhao Huabing change to Intent Service

package com.histar.hslauncher;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class WeatherIntentService extends IntentService {
    public static final String ACTION_REPORTWEATHER = "com.histar.hslauncher.action.ReportWeather";
    //    private static final String ACTION_QUERYYAHOOWEATHER = "com.histar.hslauncher.action.QueryYahooWeather";
    private static final String TAG = "WeatherService";

    public static void startQueryWeather(Context context) {
        Intent intent = new Intent(context, WeatherIntentService.class);
//        intent.setAction(ACTION_QUERYYAHOOWEATHER);
        context.startService(intent);
    }

    public WeatherIntentService() {
        super("WeatherIntentService");
    }

    private boolean isRunning = false;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (isRunning)
                return;
            isRunning = true;
//            final String action = intent.getAction();
//            if (ACTION_QUERYYAHOOWEATHER.equals(action)) {
            Log.i(TAG, "Loading location infomation ...");
            String ipJson = getHttpEntity("http://ip-api.com/json");
            if (ipJson == null)
                return;
            String location = getLocationFromJson(ipJson);
            if (location == null)
                return;

            Log.i(TAG, "Querying weather infomation ...");
            String weatherJson = getHttpEntity(buildQueryUrl(location));
            if (weatherJson == null)
                return;

            Log.i(TAG, "Report weather infomation ...");
            String weatherInfo = getWeatherInfo(weatherJson);
            Intent intentWeather = new Intent(ACTION_REPORTWEATHER);
            // info "city;temp;text;code"
            intentWeather.putExtra("info", weatherInfo);
            sendBroadcast(intentWeather);
//            }
            isRunning = false;
        }
    }

    private String getHttpEntity(String url) {
        HttpGet getMethod = new HttpGet(url);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response;
        try {
            response = httpClient.execute(getMethod);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                return EntityUtils.toString(response.getEntity(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private String getLocationFromJson(String json) {
        try {
            JSONObject jObject = new JSONObject(json);
            return jObject.getString("city") + ", " + jObject.getString("countryCode");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String buildQueryUrl(String location) {
        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("q", "select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"" + location + "\")"));
        params.add(new BasicNameValuePair("format", "json"));
        String param = URLEncodedUtils.format(params, "UTF-8");
       // Log.i("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$", param);
        String baseUrl = "https://query.yahooapis.com/v1/public/yql";
        //Log.i("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@", baseUrl + "?" + param);
       // Log.i baseUrl + "?" + param);
        return baseUrl + "?" + param;
    }

    private String getWeatherInfo(String json) {
        Log.i(TAG, "getWeatherInfo");
        try {
            JSONObject jObjectYahoo = new JSONObject(json);
            JSONObject channelObject = jObjectYahoo
                    .getJSONObject("query")
                    .getJSONObject("results")
                    .getJSONObject("channel");
            JSONObject locationObject = channelObject.getJSONObject("location");
            String text = locationObject.getString("city");
            JSONObject conditionObject = channelObject.getJSONObject("item").getJSONObject("condition");
            // 摄氏度(℃)=（华氏度(℉)-32）÷1.8
            text += ";" + String.valueOf((int) ((conditionObject.getInt("temp") - 32) / 1.8));
            text += ";" + conditionObject.getString("text");
            text += ";" + conditionObject.getString("code");
            Log.i(TAG, "weather info (city;temp;text;code) : " + text);
            return text;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }




    public static String iconSelect(int i) {
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 23:
            case 24:
                return "wip_bk_na";
            case 19:
                return "wip_bk_dust";
            case 3:
            case 4:
            case 37:
            case 38:
            case 39:
            case 45:
            case 47:
                return "wip_bk_thunderstorm";
            case 5:
            case 8:
            case 18:
                return "wip_bk_snow_rain";
            case 6:
            case 7:
            case 17:
            case 35:
                return "wip_bk_icy";
            case 9:
            case 10:
            case 11:
            case 12:
            case 40:
                return "wip_bk_rain";
            case 13:
            case 14:
            case 42:
            case 46:
                return "wip_bk_light_snow";
            case 15:
            case 16:
            case 25:
            case 41:
            case 43:
                return "wip_bk_snow";
            case 20:
            case 21:
            case 22:
                return "wip_bk_fog";
            case 26:
                return "wip_bk_overcast";
            case 27:
            case 28:
            case 29:
            case 30:
            case 44:
                return "wip_bk_cloudy";
            case 31:
            case 32:
            case 33:
            case 34:
            case 36:
                return "wip_bk_sunny";
            default:
                return "wip_bk_na";

        }
    }

    /*String code2char(int code) {
        switch (code) {
            case 0:
                return "龙卷风";
            case 1:
                return "热带风暴";
            case 2:
                return "暴风";
            case 3:
                return "大雷雨";
            case 4:
                return "雷阵雨";
            case 5:
                return "雨夹雪";
            case 6:
                return "雨夹雹";
            case 7:
                return "雪夹雹";
            case 8:
                return "冻雾雨";
            case 9:
                return "细雨";
            case 10:
                return "冻雨";
            case 11:
                return "阵雨";
            case 12:
                return "阵雨";
            case 13:
                return "阵雪";
            case 14:
                return "小阵雪";
            case 15:
                return "高吹雪";
            case 16:
                return "雪";
            case 17:
                return "冰雹";
            case 18:
                return "雨淞";
            case 19:
                return "粉尘";
            case 20:
                return "雾";
            case 21:
                return "薄雾";
            case 22:
                return "烟雾";
            case 23:
                return "大风";
            case 24:
                return "风";
            case 25:
                return "冷";
            case 26:
                return "阴";
            case 27:
                return "多云";
            case 28:
                return "多云";
            case 29:
                return "局部多云";
            case 30:
                return "局部多云";
            case 31:
                return "晴";
            case 32:
                return "晴";
            case 33:
                return "转晴";
            case 34:
                return "转晴";
            case 35:
                return "雨夹冰雹";
            case 36:
                return "热";
            case 37:
                return "局部雷雨";
            case 38:
                return "偶有雷雨";
            case 39:
                return "偶有雷雨";
            case 40:
                return "偶有阵雨";
            case 41:
                return "大雪";
            case 42:
                return "零星阵雪";
            case 43:
                return "大雪";
            case 44:
                return "局部多云";
            case 45:
                return "雷阵雨";
            case 46:
                return "阵雪";
            case 47:
                return "局部雷阵雨";
            default:
                return "水深火热";
        }
    }*/
/*
    private Bitmap getWeatherIcon(String json) {
        Log.i(TAG, "getWeatherIcon");
        try {
            JSONObject jObjectYahoo = new JSONObject(json);
            JSONObject itemObject = jObjectYahoo
                    .getJSONObject("query")
                    .getJSONObject("results")
                    .getJSONObject("channel")
                    .getJSONObject("item");
            String iconUrl = getIconFromDescription(itemObject.getString("description"));
            if(iconUrl == null)
                return null;
            return getBitmap(iconUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getIconFromDescription(String in) {
        Pattern p = Pattern.compile("src=\"(.+?)\"");
        Matcher m = p.matcher(in);
        if (m.find())
            return m.group(1);
        else
            return null;
    }

    public Bitmap getBitmap(String strUrl) {
        try {
            URL url = new URL(strUrl);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            return BitmapFactory.decodeStream(is);
        } catch ( e) {
            e.printStaIOExceptionckTrace();
            return null;
        }
    }
*/

}


/*
{"as":"AS4134 Chinanet","city":"Shenzhen","country":"China","countryCode":"CN","isp":"China Telecom","lat":22.5333,"lon":114.1333,"org":"China Telecom","query":"113.87.125.40","region":"44","regionName":"Guangdong","status":"success","timezone":"Asia/Shanghai","zip":""}

{
 "query": {
  "count": 1,
  "created": "2015-07-20T09:59:44Z",
  "lang": "zh-CN",
  "results": {
   "channel": {
    "title": "Yahoo! Weather - Shenzhen, CN",
    "link": "http://us.rd.yahoo.com/dailynews/rss/weather/Shenzhen__CN/*http://weather.yahoo.com/forecast/CHXX0120_f.html",
    "description": "Yahoo! Weather for Shenzhen, CN",
    "language": "en-us",
    "lastBuildDate": "Mon, 20 Jul 2015 5:00 pm CST",
    "ttl": "60",
    "location": {
     "city": "Shenzhen",
     "country": "China",
     "region": ""
    },
    "units": {
     "distance": "mi",
     "pressure": "in",
     "speed": "mph",
     "temperature": "F"
    },
    "wind": {
     "chill": "82",
     "direction": "100",
     "speed": "7"
    },
    "atmosphere": {
     "humidity": "84",
     "pressure": "29.53",
     "rising": "0",
     "visibility": "6.21"
    },
    "astronomy": {
     "sunrise": "5:50 am",
     "sunset": "7:10 pm"
    },
    "image": {
     "title": "Yahoo! Weather",
     "width": "142",
     "height": "18",
     "link": "http://weather.yahoo.com",
     "url": "http://l.yimg.com/a/i/brand/purplelogo//uh/us/news-wea.gif"
    },
    "item": {
     "title": "Conditions for Shenzhen, CN at 5:00 pm CST",
     "lat": "22.55",
     "long": "114.11",
     "link": "http://us.rd.yahoo.com/dailynews/rss/weather/Shenzhen__CN/*http://weather.yahoo.com/forecast/CHXX0120_f.html",
     "pubDate": "Mon, 20 Jul 2015 5:00 pm CST",
     "condition": {
      "code": "30",
      "date": "Mon, 20 Jul 2015 5:00 pm CST",
      "temp": "82",
      "text": "Partly Cloudy"
     },
     "description": "\n<img src=\"http://l.yimg.com/a/i/us/we/52/30.gif\"/><br />\n<b>Current Conditions:</b><br />\nPartly Cloudy, 82 F<BR />\n<BR /><b>Forecast:</b><BR />\nMon - Heavy Thunderstorms. High: 86 Low: 79<br />\nTue - Thunderstorms. High: 85 Low: 81<br />\nWed - Thunderstorms. High: 87 Low: 81<br />\nThu - Thunderstorms. High: 86 Low: 81<br />\nFri - Thunderstorms. High: 87 Low: 80<br />\n<br />\n<a href=\"http://us.rd.yahoo.com/dailynews/rss/weather/Shenzhen__CN/*http://weather.yahoo.com/forecast/CHXX0120_f.html\">Full Forecast at Yahoo! Weather</a><BR/><BR/>\n(provided by <a href=\"http://www.weather.com\" >The Weather Channel</a>)<br/>\n",
     "forecast": [
      {
       "code": "4",
       "date": "20 Jul 2015",
       "day": "Mon",
       "high": "86",
       "low": "79",
       "text": "Heavy Thunderstorms"
      },
      {
       "code": "4",
       "date": "21 Jul 2015",
       "day": "Tue",
       "high": "85",
       "low": "81",
       "text": "Thunderstorms"
      },
      {
       "code": "4",
       "date": "22 Jul 2015",
       "day": "Wed",
       "high": "87",
       "low": "81",
       "text": "Thunderstorms"
      },
      {
       "code": "4",
       "date": "23 Jul 2015",
       "day": "Thu",
       "high": "86",
       "low": "81",
       "text": "Thunderstorms"
      },
      {
       "code": "4",
       "date": "24 Jul 2015",
       "day": "Fri",
       "high": "87",
       "low": "80",
       "text": "Thunderstorms"
      }
     ],
     "guid": {
      "isPermaLink": "false",
      "content": "CHXX0120_2015_07_24_7_00_CST"
     }
    }
   }
  }
 }
}
 */

/*
Code 	Description
0 	tornado
1 	tropical storm
2 	hurricane
3 	severe thunderstorms
4 	thunderstorms
5 	mixed rain and snow
6 	mixed rain and sleet
7 	mixed snow and sleet
8 	freezing drizzle
9 	drizzle
10 	freezing rain
11 	showers
12 	showers
13 	snow flurries
14 	light snow showers
15 	blowing snow
16 	snow
17 	hail
18 	sleet
19 	dust
20 	foggy
21 	haze
22 	smoky
23 	blustery
24 	windy
25 	cold
26 	cloudy
27 	mostly cloudy (night)
28 	mostly cloudy (day)
29 	partly cloudy (night)
30 	partly cloudy (day)
31 	clear (night)
32 	sunny
33 	fair (night)
34 	fair (day)
35 	mixed rain and hail
36 	hot
37 	isolated thunderstorms
38 	scattered thunderstorms
39 	scattered thunderstorms
40 	scattered showers
41 	heavy snow
42 	scattered snow showers
43 	heavy snow
44 	partly cloudy
45 	thundershowers
46 	snow showers
47 	isolated thundershowers
3200 	not available
 */