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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
                {"POST",405, "Los Angeles", 1002, "The requested method doesn't support this HTTP Method provided"},
                {"PUT", 405, "Los Angeles", 1002, "The requested method doesn't support this HTTP Method provided"},
                {"DELETE", 405, "Los Angeles", 1002, "The requested method doesn't support this HTTP Method provided"}
        };
    }

    @DataProvider(name = "bad-location-tests")
    public Object[][] badLocationData() {
        return new Object[][]{
                {"GET", 200, "", 1, "Location Not Found"},
                {"GET", 200, "Validivostok,CA", 1, "Location Not Found"},
                {"GET", 200, "-92129", 1, "Location Not Found"}
         };
    }

    @Test(dataProvider = "good-locations-tz")
    public void checkValidWeather(String method,int expectedStatus, String city, int timeZoneOffset, int numberOfDays){
       // String jsonResult = "";
        log.info("Setting up REST client for checkValid Weather...");
        serviceClient = new PayoffWeatherServiceClient(false);
        log.info("Service client ready.  Starting test.");
        log.info("Starting weather check for location = "+city);
        System.out.println("Starting weather check for location = "+city);

        serviceClient.callWeatherService(method, city);
        String jsonWeatherResult = serviceClient.getResponseBody();
        Assert.assertEquals(serviceClient.getResponseStatusCode(),200);
        Assert.assertEquals(ParsePayoffWeatherData.getResultSetSize(jsonWeatherResult),7, "For "+city+":  We expected to see 7 elements in the result set, but only, "+ParsePayoffWeatherData.getResultSetSize(jsonWeatherResult)+", were available.");
        for (int x=1;x<=7;x++)
        {

            if (x==1)
            {
                Assert.assertEquals(ParsePayoffWeatherData.getDayOfWeek(x,jsonWeatherResult).toLowerCase(),getThreeCharDayOfWeek(timeZoneOffset).toLowerCase(), "For "+city+":  We expected the first day of the week to be:  "+getThreeCharDayOfWeek(timeZoneOffset)+".  But we got:  "+ParsePayoffWeatherData.getDayOfWeek(x,jsonWeatherResult)+", instead.");
            }
            Assert.assertNotEquals(ParsePayoffWeatherData.getDailyHigh(x,jsonWeatherResult),"", "For "+city+":  We have empty data for the fahrenheit high temp!");
            Assert.assertNotEquals(ParsePayoffWeatherData.getDailyLow(x, jsonWeatherResult),"", "For "+city+":  We have blank data for the fahrenheit low temp!");
            Assert.assertNotEquals(ParsePayoffWeatherData.getDailyHighC(x,jsonWeatherResult),"", "For "+city+":  We have blank data for the celsius high temp!");
            Assert.assertNotEquals(ParsePayoffWeatherData.getDailyLowC(x, jsonWeatherResult),"", "For "+city+":  We have blank data for the celsius low temp!");
        }
        log.info("Weather check for location = "+city+" Passed");
        System.out.println("Weather check for location = "+city+" Passed");
    }

    @Test(dataProvider = "bad-location-tests")
    public void badLocationTests(String method, int expectedStatus, String city, int errorCode, String errorMessage){
        System.out.println("Sending through bad locations and validating the correct error code/msg");
        log.info("Bad location attempted:  " + city + ".");
        log.info("Setting up REST client for badLocationTests...");
        serviceClient = new PayoffWeatherServiceClient();
        log.info("Service client ready.  Starting test.");

        serviceClient.callWeatherService(method,city);

        String jsonWeatherResult = serviceClient.getResponseBody();
        int statusCode = serviceClient.getResponseStatusCode();

        Assert.assertEquals(expectedStatus,statusCode, "For bad locations, we should have get a "+expectedStatus+" http status code.  Instead we got:  "+statusCode+".");
        //shoulda just made this method return the object and let the test do it
        int errCode = ParsePayoffWeatherData.getErrorMessageCode(jsonWeatherResult);
        Assert.assertEquals(errorCode,errCode,"The error code returned from the service for a bad location should be:  "+errorCode+". We instead got:  "+errCode);
        String errMessage = ParsePayoffWeatherData.getErrorMessage(jsonWeatherResult);
        Assert.assertEquals(errorMessage,errMessage,"The error code returned from the service for a bad location should be:  "+errorMessage+". We instead got:  "+errMessage);

        log.info("BadlocationTests passed for bad location = "+city);
        System.out.println("BadlocationTests passed for bad location = "+city);
    }
    @Test(dataProvider = "error-message-tests")
    public void checkRestMethodCompliance(String method, int expectedStatus, String city, int errorCode, String errorMessage){
            System.out.println("Only GET should be used for this method, we'll use:  "+method+", for city:  "+city+".");
            log.info("Only GET should be used for this method, we'll use:  "+method+", for city:  "+city+".");
            log.info("Setting up REST client for checkRestMethodCompliance...");
            serviceClient = new PayoffWeatherServiceClient();
            log.info("Service client ready.  Starting test.");

            serviceClient.callWeatherService(method,city);

            String jsonWeatherResult = serviceClient.getResponseBody();
            int statusCode = serviceClient.getResponseStatusCode();

            Assert.assertEquals(expectedStatus,statusCode, "For http method:  "+method+ ", we should have gotten a "+expectedStatus+" http status code.  Instead we got:  "+statusCode+".");
            //shoulda just made this method return the object and let the test do it
            int errCode = ParsePayoffWeatherData.getErrorMessageCode(jsonWeatherResult);
            Assert.assertEquals(errorCode,errCode,"The error code returned from the service for an incorrect method should be:  "+errorCode+". We instead got:  "+errCode);
            String errMessage = ParsePayoffWeatherData.getErrorMessage(jsonWeatherResult);
            Assert.assertEquals(errorMessage,errMessage,"The error code returned from the service for an incorrect method should be:  "+errorMessage+". We instead got:  "+errMessage);

            log.info("CheckRestMethodCompliance passed for method = "+method);
            System.out.println("CheckRestMethodCompliance passed for method = "+method);
    }

    @Test
    public void brokenUrlTest(){
        System.out.println("Testing with missing location query parameter, should get a 200 status but an error message.");
        log.info("Testing with missing location query parameter, should get a 200 status but an error message.");
        serviceClient = new PayoffWeatherServiceClient(true);
        serviceClient.callWeatherService("get","paris");

        String jsonWeatherResult = serviceClient.getResponseBody();
        int statusCode = serviceClient.getResponseStatusCode();

        Assert.assertEquals(200,statusCode, "A missing URL should have resulted in a 200 http status code.  Instead we got:  "+statusCode+".");
        //shoulda just made this method return the object and let the test do it
        int errCode = ParsePayoffWeatherData.getErrorMessageCode(jsonWeatherResult);
        Assert.assertEquals(1008,errCode,"The error code returned from the service for an incorrect method should be:  1008"+" We instead got:  "+errCode);
        String errMessage = ParsePayoffWeatherData.getErrorMessage(jsonWeatherResult);
        Assert.assertEquals("Some parameters required by the method are missing",errCode,"The error code returned from the service for an incorrect method should be:  'Some parameters required by the method are missing'. We instead got:  "+errMessage);
        log.info("BrokenUrlTest passed for missing location parameter.");
        System.out.println("BrokenUrlTest passed for missing location parameter.");
    }

    public String getThreeCharDayOfWeek(int offset)
    {
        TimeZone tz;
        if (offset < 0)
            tz = TimeZone.getTimeZone("GMT-"+Integer.toString(Math.abs(offset)));
        else
            tz = TimeZone.getTimeZone(("GMT+"+Integer.toString(offset)));
        Calendar rightNow = Calendar.getInstance(tz);
        return new SimpleDateFormat("EEE").format(rightNow.getTime());
    }

}
