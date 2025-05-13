package com.train.member.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class WeatherUtils {
    private static final  Map<String, String> cityAndCodeMap = new HashMap<>();
    static{
        try (InputStream inputStream = WeatherUtils.class.getClassLoader().getResourceAsStream("AMap_adcode_citycode.xlsx")) {
            if (inputStream == null){
                throw new RuntimeException("读取城市编码excel出错");
            }
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
            int numberOfSheets = xssfWorkbook.getNumberOfSheets();
            if(numberOfSheets < 1){
                throw new RuntimeException("城市编码excel表格出错");
            }
            XSSFSheet sheetAt = xssfWorkbook.getSheetAt(0);
            int lastRowNum = sheetAt.getLastRowNum();//最后一行
            for(int row = 2; row <= lastRowNum ; row ++){
                cityAndCodeMap.put(sheetAt.getRow(row).getCell(0).toString(),sheetAt.getRow(row).getCell(1).toString());
            }
            System.out.println("数据读取完毕");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args){
        System.out.println(JSON.toJSONString(getWeather("上海虹桥")));
    }
    private static final String KEY = "45c0706bdf4d576eb48393e964ae4517";
    public static Map<String,WeatherResult> getWeather(String cityName){
        cityName = cityName.replaceAll("[东西南北站]$","");
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes.getRequest();
//        String localAddr = request.getLocalAddr(); //获取请求的本地地址
//        int serverPort = request.getServerPort(); //获取发出请求的端口
        String[] suffixes = {"","区", "县", "市", "省"};
        String code = null;
        for(String su : suffixes){
            code = cityAndCodeMap.get(cityName + su);
            if(code != null && !code.isEmpty()) break;
        }
        if(code == null || code.isEmpty()){
            return new HashMap<>();
        }
        Map<String,String> map = new HashMap<>();
        map.put("key",KEY);
        map.put("output","json");
        map.put("city", code);
        map.put("extensions","all");
//		map.put("ip","172.27.159.254");
        Map<String,WeatherResult> resultMap = new HashMap<>();

        //创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //
        String result = "";
        CloseableHttpResponse response = null;

        try {
            String weatherUrl  = "https://restapi.amap.com/v3/weather/weatherInfo?parameters";
            URIBuilder builder2 = new URIBuilder(weatherUrl);
            for(String s : map.keySet()){
                builder2.addParameter(s,map.get(s));
            }
            URI uri2 = builder2.build();

            HttpGet get2 = new HttpGet(uri2);
            response = httpClient.execute(get2);

            if(response.getStatusLine().getStatusCode() == 200){
                result = EntityUtils.toString(response.getEntity(), "utf-8");
            }
//            System.out.println(result);
            JSONObject jsonObject2 = JSON.parseObject(result);
            JSONArray forecasts = jsonObject2.getJSONArray("forecasts");
            JSONObject jsonObject = forecasts.getJSONObject(0);
            String province = jsonObject.getString("province");
            JSONArray castArray = jsonObject.getJSONArray("casts");
            int i = 0;
            JSONObject ob = null;
            while(i <= castArray.size() - 1){
                ob  = castArray.getJSONObject(i++);
                if(ob == null){
                    break;
                }
                WeatherResult w = new WeatherResult();
                String date = ob.getString("date");
                w.setDate(date);
                w.setProvince(province);
                w.setDayweather(ob.getString("dayweather"));
                w.setDaywind(ob.getString("daywind"));
                w.setWeek(ob.getString("week"));
                w.setDaypower(ob.getString("daypower"));
                w.setDaytemp(ob.getString("daytemp"));
                w.setNightwind(ob.getString("nightwind"));
                w.setNighttemp(ob.getString("nighttemp"));
                w.setDayTempFloat( ob.getString("daytemp_float"));
                w.setNightTempFloat(ob.getString("nighttemp_float"));
                w.setNightWeather(ob.getString("nightweather"));
                w.setNightPower(ob.getString("nightpower"));
                resultMap.put(date, w);
            }

            return resultMap;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }
    private static String findCodeForCityName(String cityName) {
        // 创建正则表达式模式
        Pattern pattern = Pattern.compile(Pattern.quote(cityName), Pattern.CASE_INSENSITIVE);

        // 遍历 cityAndCodeMap 的键
        for (String key : cityAndCodeMap.keySet()) {
            if (pattern.matcher(key).find()) {
                return cityAndCodeMap.get(key);
            }
        }

        // 如果没有找到匹配项，返回 null
        return null;
    }
}
