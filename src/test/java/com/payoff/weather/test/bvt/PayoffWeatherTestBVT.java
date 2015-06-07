package com.payoff.weather.test.bvt;

import com.payoff.weather.client.ParsePayoffWeatherData;
import com.payoff.weather.client.PayoffWeatherServiceClient;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eric on 6/5/2015.
 */



public class PayoffWeatherTestBVT {
    private static final Logger log = Logger.getLogger(PayoffWeatherTestBVT.class);
    PayoffWeatherServiceClient serviceClient = null;
    @DataProvider(name = "good-locations-tz")
    public Object [][] testLocationsData(){
        return new Object [][]{
                {"GET",200,"Los Angeles",-7,7},
                {"GET",200,"New York",-4,7},
                {"GET",200,"92677",-7,7},
                {"GET",200,"Rawaki",13,7},
                {"GET",200,"Carlsbad, CA",-7,7},
                {"GET",200,"Carlsbad, NM",-6,7}
        };
    }

    @DataProvider(name = "error-message-tests")
    public Object[][] errorTestsData() {
        return new Object[][]{
                {"GET", 200, "", 1, "Location Not Found"},
                {"GET", 200, "Validivostok,CA", 1, "Location Not Found"},
                {"GET", 200, "-92129", 1, "Location Not Found"},
                {"GET", 200, "Los Angeles", 1008, "Some parameters required by the method are missing"},
                {"POST",405, "Los Angeles", 1002, "The requested method doesn't support this HTTP Method provided"},
                {"PUT", 405, "Los Angeles", 1002, "The requested method doesn't support this HTTP Method provided"},
                {"DELETE", 405, "Los Angeles", 1002, "The requested method doesn't support this HTTP Method provided"}

        };
    }
  /*  @BeforeClass
    public void setUp()
    {
        log = Logger.getLogger(PayoffWeatherTestBVT.class);
        log.info("Starting test...");
    }*/
    @BeforeTest
    public void setClient(){
        log.info("Setting up REST client for checkValid Weather...");
        serviceClient = new PayoffWeatherServiceClient();
        log.info("Service client ready.  Starting test.");
    }
    @Test(dataProvider = "good-locations-tz")
    public void checkValidWeather(String method,int expectedStatus, String city, int timeZoneOffset, int numberOfDays){
       // String jsonResult = "";
       // log.info("Starting weather check for location = "+city);
        if (method.toLowerCase().equals("get")){
            serviceClient.get(city);
            String jsonWeatherResult = serviceClient.getResponseBody();
            Assert.assertEquals(serviceClient.getResponseStatusCode(),200);
            Assert.assertEquals(ParsePayoffWeatherData.getResultSetSize(jsonWeatherResult),7, "We expected to see 7 elements in the result set, but only, "+ParsePayoffWeatherData.getResultSetSize(jsonWeatherResult)+", were available.");
            for (int x=1;x<=7;x++)
            {

                if (x==1)
                {
                    Assert.assertEquals(ParsePayoffWeatherData.getDayOfWeek(x,jsonWeatherResult).toLowerCase(),getThreeCharDayOfWeek().toLowerCase(), "We expected the first day of the week to be:  "+getThreeCharDayOfWeek()+".  Be we got:  "+ParsePayoffWeatherData.getDayOfWeek(x,jsonWeatherResult)+", instead.");
                }
                Assert.assertNotEquals(ParsePayoffWeatherData.getDailyHigh(x,jsonWeatherResult),"", "We have blank data for the fahrenheit high temp!");
                Assert.assertNotEquals(ParsePayoffWeatherData.getDailyLow(x, jsonWeatherResult),"", "We have blank data for the fahrenheit low temp!");
                Assert.assertNotEquals(ParsePayoffWeatherData.getDailyHighC(x,jsonWeatherResult),"", "We have blank data for the celsius high temp!");
                Assert.assertNotEquals(ParsePayoffWeatherData.getDailyLowC(x, jsonWeatherResult),"", "We have blank data for the celsius low temp!");
            }
        }
        //log.info("CheckValidWeather for location "+city+", has passed.");
    }

   /* @Test(dataProvider = "error-message-tests")
    public void checkErrorHandling(String method, int expectedStatus, String city, int errorCode, String errorMessage){

    }*/

    public String getThreeCharDayOfWeek()
    {
        return new SimpleDateFormat("EEE").format(new Date());
    }

}
