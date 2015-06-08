package com.payoff.weather.test.bvt;

import com.payoff.weather.client.BadUrlTypes;
import com.payoff.weather.client.ParsePayoffWeatherData;
import com.payoff.weather.client.PayoffWeatherServiceClient;
import org.testng.Assert;

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
    int highF,lowF,highC,lowC;
    @DataProvider(name = "good-locations-tz")
    public Object [][] testLocationsData(){
        return new Object [][]{
                {"GET",200,"Los Angeles",-7,7},
                {"GET",200,"New York",-4,7},
                {"GET",200,"92677",-7,7},
                {"GET",200,"Rawaki",13,7},
                {"GET",200,"Carlsbad, CA",-7,7},
                {"GET",200,"Carlsbad, NM",-6,7},
                {"GET",200,"  Oakland",-7,7},
                {"GET",200,"Oakland  ",-7,7}
        };
    }

    @DataProvider(name = "error-message-tests")
    public Object[][] errorTestsData() {
        return new Object[][]{
                {"POST",404, "Los Angeles", 1006, "The method requested was not found: \"\""},
                {"PUT", 404, "Los Angeles", 1006, "The method requested was not found: \"\""},
                {"DELETE", 404, "Los Angeles", 1006, "The method requested was not found: \"\""}
        };
    }

    @DataProvider(name = "bad-location-tests")
    public Object[][] badLocationData() {
        return new Object[][]{
                {"GET", 200, "", 1, "Location not Found"},
                {"GET", 200, "Validivostok_CA", 1, "Location not Found"},
                {"GET", 200, "-92129", 1, "Location not Found"},
                {"GET", 200, "*", 1, "Location not Found"}
         };
    }

    @Test(dataProvider = "good-locations-tz")
    public void checkValidWeather(String method,int expectedStatus, String city, int timeZoneOffset, int numberOfDays){
       // String jsonResult = "";
        log.info("Setting up REST client for checkValid Weather...");
        serviceClient = new PayoffWeatherServiceClient();
        log.info("Service client ready.  Starting test.");
        log.info("Starting weather check for location = "+city);
        System.out.println("Starting weather check for location = "+city);

        serviceClient.callWeatherService(method, city);
        String jsonWeatherResult = serviceClient.getResponseBody();
        Assert.assertEquals(200,serviceClient.getResponseStatusCode());
        Assert.assertEquals(7,ParsePayoffWeatherData.getResultSetSize(jsonWeatherResult),"For "+city+":  We expected to see 7 elements in the result set, but only, "+ParsePayoffWeatherData.getResultSetSize(jsonWeatherResult)+", were available.");
        for (int x=1;x<=7;x++)
        {

            if (x==1)
            {
                Assert.assertEquals(ParsePayoffWeatherData.getDayOfWeek(x,jsonWeatherResult).toLowerCase(),getThreeCharDayOfWeek(timeZoneOffset).toLowerCase(), "For "+city+":  We expected the first day of the week to be:  "+getThreeCharDayOfWeek(timeZoneOffset)+".  But we got:  "+ParsePayoffWeatherData.getDayOfWeek(x,jsonWeatherResult)+", instead.");
            }

            Assert.assertNotEquals("",ParsePayoffWeatherData.getDailyHigh(x,jsonWeatherResult),"For "+city+":  We have empty data for the fahrenheit high temp!");
            Assert.assertNotEquals("",ParsePayoffWeatherData.getDailyLow(x, jsonWeatherResult),"For "+city+":  We have blank data for the fahrenheit low temp!");
            Assert.assertNotEquals("",ParsePayoffWeatherData.getDailyHighC(x,jsonWeatherResult),"For "+city+":  We have blank data for the celsius high temp!");
            Assert.assertNotEquals("",ParsePayoffWeatherData.getDailyLowC(x, jsonWeatherResult),"For "+city+":  We have blank data for the celsius low temp!");

            highF = Integer.parseInt(ParsePayoffWeatherData.getDailyHigh(x,jsonWeatherResult));
            lowF = Integer.parseInt(ParsePayoffWeatherData.getDailyLow(x, jsonWeatherResult));
            highC = Integer.parseInt(ParsePayoffWeatherData.getDailyHighC(x, jsonWeatherResult));
            lowF = Integer.parseInt(ParsePayoffWeatherData.getDailyLowC(x, jsonWeatherResult));
                    //Make sure that if we have values in our temps that they are valid values and high>=low
            Assert.assertTrue(highF>=lowF, "The high temperature of the day can be no lower than the low for F.");
            Assert.assertTrue(highC>=lowC, "The high temperature of the day can be no lower than the low for C.");
            Assert.assertNotEquals(highC,highF, "Fahrenheit high and Celsius High should be different values.");
            Assert.assertNotEquals(lowC,lowF, "Fahrenheit low and Celsius low should be different values.");
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
        System.out.println("JSON String = "+jsonWeatherResult);
        System.out.println();

        int statusCode = serviceClient.getResponseStatusCode();

        Assert.assertEquals(statusCode,expectedStatus,"For bad locations, we should have get a "+expectedStatus+" http status code.  Instead we got:  "+statusCode+".");
        //shoulda just made this method return the object and let the test do it
        int errCode = ParsePayoffWeatherData.getErrorMessageCode(jsonWeatherResult);
        Assert.assertEquals(errCode,errorCode,"The error code returned from the service for a bad location should be:  "+errorCode+". We instead got:  "+errCode);
        String errMessage = ParsePayoffWeatherData.getErrorMessage(jsonWeatherResult);
        Assert.assertEquals(errMessage,errorMessage,"The error code returned from the service for a bad location should be:  "+errorMessage+". We instead got:  "+errMessage);

        log.info("BadlocationTests passed for bad location = "+city);
        System.out.println("BadlocationTests passed for bad location = "+city);
    }

    /**
     * Test - checkRestMethodCompliance will attempt to validate proper handling of non-compliant
     * http requests against a get service such as the getWeather service.
     * @param method
     * @param expectedStatus
     * @param city
     * @param errorCode
     * @param errorMessage
     */
    @Test(dataProvider = "error-message-tests")
    public void checkRestMethodCompliance(String method, int expectedStatus, String city, int errorCode, String errorMessage){
            System.out.println("Only GET should be used for this method, we'll use:  "+method+", for city:  "+city+".");
            log.info("Only GET should be used for this method, we'll use:  "+method+", for city:  "+city+".");
            log.info("Setting up REST client for checkRestMethodCompliance...");
            serviceClient = new PayoffWeatherServiceClient();
            log.info("Service client ready.  Starting test.");

            serviceClient.callWeatherService(method,city);

            String jsonWeatherResult = serviceClient.getResponseBody();

            System.out.println("JSON String = "+jsonWeatherResult);
            System.out.println();
            int statusCode = serviceClient.getResponseStatusCode();

            Assert.assertEquals(statusCode,expectedStatus,"For http method:  "+method+ ", we should have gotten a "+expectedStatus+" http status code.  Instead we got:  "+statusCode+".");
            //shoulda just made this method return the object and let the test do it
            int errCode = ParsePayoffWeatherData.getErrorMessageCode(jsonWeatherResult);
            Assert.assertEquals(errCode,errorCode,"The error code returned from the service for an incorrect method should be:  "+errorCode+". We instead got:  "+errCode);
            String errMessage = ParsePayoffWeatherData.getErrorMessage(jsonWeatherResult);
            Assert.assertEquals(errMessage,errorMessage,"The error code returned from the service for an incorrect method should be:  "+errorMessage+". We instead got:  "+errMessage);

            log.info("CheckRestMethodCompliance passed for method = "+method);
            System.out.println("CheckRestMethodCompliance passed for method = "+method);
    }

    /**
     * The brokenUrlTest will use a url which is missing the required "location=" parameter.
     * This should result in an error and message which will be verified in the response.
     */
    @Test
    public void brokenUrlTest(){
        System.out.println("Testing with missing location query parameter, should get a 200 status but an error message.");
        log.info("Testing with missing location query parameter, should get a 200 status but an error message.");
        serviceClient = new PayoffWeatherServiceClient();
        serviceClient.callWeatherService(BadUrlTypes.NO_LOCATION);

        String jsonWeatherResult = serviceClient.getResponseBody();
        int statusCode = serviceClient.getResponseStatusCode();

        System.out.println("JSON String = "+jsonWeatherResult);
        System.out.println();

        Assert.assertEquals(statusCode,200,"A missing URL should have resulted in a 200 http status code.  Instead we got:  "+statusCode+".");
        //shoulda just made this method return the object and let the test do it
        int errCode = ParsePayoffWeatherData.getErrorMessageCode(jsonWeatherResult);
        Assert.assertEquals(errCode,1008,"The error code returned from the service for an incorrect method should be:  1008"+" We instead got:  "+errCode);
        String errMessage = ParsePayoffWeatherData.getErrorMessage(jsonWeatherResult);
        Assert.assertEquals("Some parameters required by the method are missing",errCode,"The error code returned from the service for an incorrect method should be:  'Some parameters required by the method are missing'. We instead got:  "+errMessage);
        log.info("BrokenUrlTest passed for missing location parameter.");
        System.out.println("BrokenUrlTest passed for missing location parameter.");
    }

    /**
     * This service requires an authentication header.  We remove the entire header for this test.
     */
    @Test
    public void noAuthHeader(){
        System.out.println("Testing with missing auth header, should get a 401 status");
        log.info("Testing with missing auth header, should get a 401 status");
        serviceClient = new PayoffWeatherServiceClient();
        serviceClient.callWeatherService(BadUrlTypes.NO_AUTH);

        String jsonWeatherResult = serviceClient.getResponseBody();
        int statusCode = serviceClient.getResponseStatusCode();

        System.out.println("JSON String = "+jsonWeatherResult);
        System.out.println();

        Assert.assertEquals(statusCode,401, "A missing auth header should have resulted in a 401 http status code.  Instead we got:  "+statusCode+".");

        log.info("NoAuthHeader test passed.");
        System.out.println("NoAuthHeader test passed.");
    }

    /**
     * This service requires an authentication header.  We remove the entire key for this test.
     */
    @Test
    public void noAuthValue(){
        System.out.println("Testing with missing auth value, should get a 400 status");
        log.info("Testing with missing auth value, should get a 400 status");
        serviceClient = new PayoffWeatherServiceClient();
        serviceClient.callWeatherService(BadUrlTypes.NO_AUTH);

        String jsonWeatherResult = serviceClient.getResponseBody();
        int statusCode = serviceClient.getResponseStatusCode();

        System.out.println("JSON String = "+jsonWeatherResult);
        System.out.println();

        Assert.assertEquals(statusCode,401, "A missing auth value should have resulted in a 401 http status code.  Instead we got:  "+statusCode+".");

        log.info("NoAuthValue test passed.");
        System.out.println("NoAuthValue test passed.");
    }

    /**
     * This service requires an authentication header.  We use an invalid auth key for this test.
     */
    @Test
    public void badAuthValue(){
        System.out.println("Testing with missing auth value, should get a 400 status");
        log.info("Testing with missing auth value, should get a 400 status");
        serviceClient = new PayoffWeatherServiceClient();
        serviceClient.callWeatherService(BadUrlTypes.BAD_AUTH);

        String jsonWeatherResult = serviceClient.getResponseBody();
        int statusCode = serviceClient.getResponseStatusCode();

        System.out.println("JSON String = "+jsonWeatherResult);
        System.out.println();

        Assert.assertEquals(statusCode,403,"A bad auth value should have resulted in a 403 http status code.  Instead we got:  "+statusCode+".");

        log.info("BadAuthValue test passed.");
        System.out.println("BadAuthValue test passed.");
    }

    /**
     * The goal of this method is to "attempt" to get the current time in GMT and adjust it by the
     * specified hour offset using the TimeZone object to set the zone and then extract witht he SDF.
     * @param offset
     * @return
     */
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
