Repository PayoffWeatherTestBVT -

The purpose of this repository is to host the service level BVT tests that run against the Ultimate Web Service.

The test is built using Java 1.8 and uses TestNG 2.91 as the test framework.

The main test class is PayoffWeatherTestBVT.java and it contains 6 main tests:

The Ultimate Weather Service is REST-based web service that is located at.
It requires an authentication key X-Mashape-Key that is used to authorize the requests to the service.
There is also a require query parameter, location, which must provide an international location from which to pull our 7 day forecast.

The JSON response object is an array of json objects (a seven element array, one per each forecast day).
Each JSON object contains a three char day string, a low temp (int) and a high temp (int) for both Fahrenheit and Celsius.
Finally each object contains a "condition" string that describes the forecast state of the weather in that location.

This class is located in the public repository cirej2000/PayoffWeatherTestBVT in GitHub all required dependencies for building and running the tests are specified in the Maven pom.xml.
Outside of third party dependencies, this class requires some REST and JSON parsing methods that reside in the PayoffWeatherTestLibs-1.0-SNAPSHOT.jar.
This jar is built from classes which reside in the public repository cirej2000/PayoffWeatherTestLibs.  As this jar is not in the public Maven-repository, nor was
an Artifactory setup for this project, we use github maven plugin to utilize a mvn-repo branch to the PayoffWeatherTestLibs repo in order to host the PayoffWeatherTestLibs-1.0-SNAPSHOT.jar, for 
PayoffWeatherTestBVT to download as a dependency pointing to the repo using a <repository> url attribute.

The order of build should be 

PayoffWeatherTestLibs home folder (the one with the pom.xml):
mvn -DskiptTests=true clean install

PayoffWeatherTestBVT home folder
mvn clean test -Dtest=PayoffWeatherTestBVT to build execute all of the tests within the class.

The tests are broken up into the following test methods

1.  checkValidWeather(String method,int expectedStatus, String city, int timeZoneOffset, int numberOfDays)
 This is a data driven test which does a forecast request for valid location and is expected to be given a json array containing
 7 objects each representing the 7 days of the forecast.
 The next check is for the first day of the forecast to be today (whatever day that might be).  We add an offset to the parameters
 to adjust the date by whatever the offset for that location is from GMT.  Thus, if the location at which we're looking is already a day ahead
 we should expect tomorrow's date (relative to our location) to be the first day in the forecast.
 Next we validate that all the remaining fields are not empty (hi/low temps for F and C; conditions).
 We then validate that high and low temps are in the correct order (high>=low).
 We use City names, City and State and Zip Codes for the valid locations in this test.

2.  badLocationsTests(String method, int expectedStatus, String city, int errorCode, String errorMessage)
	Another data-driven test in which we do a request for an invalid location.
	We expect an HTTP 200 status, be we also expect a JSON response that includes an error code (int) and an error
	message (string).  We validate that these assertions are met and pass the test for each row in our dataProvider.
	
3.  checkRestMethodCompliance(String method, int expectedStatus, String city, int errorCode, String errorMessage)
	Data-driven - As this is a RESTful service it should comply to the standard (although not necessarily strict) that for requests that are the R in CRUD,
	that READ and requests are implemented using the HTTP GET method.  All other methods should be blocked either using a 504 (unsupported comman) or a
	404.  In this test we're validating the POST (update), PUT (create) and Delete (delete) are not supported by the location method of this service.
	
4.  brokenUrlTest() - This test validates that if we are missing a query parameter, that we get a 200 status, but an errorMessage.

5.  noAuthHeader() - This test validates that if the X-Mashape-Key header is missing that we receive a 401 not authorized status when attempting to
send requests to the service.

6.  noAuthValue() - This test validates that if we send the X-Mashape-Key header with no key value that we get a 400 status.

7.  badAuthValue() - This test will validate that we get a 403 status if we try to put an invalid Key in the X-Mashape-Key header.

8.  cityToZip() - This test will compare the weather results for a small town that has only one zip code they should have equal values 
for all 7 elements in the result set for both the city and zip requests.


In order to run the tests individually you would issue the following 
	mvn clean test -Dtest=PayoffWeatherTestBVT#<testmethodName>
	ex 
	mvn clean test -Dtest=PayoffWeatherTestBVT#noAuthHeader (will run only the noAuthHeader test).
	
Test Results are stored under the project folder's target/surefire-reports folder.

Checkin/Push combos to cirej2000/PayoffWeatherTestBVT on GitHub will trigger a build and test run of the full BVT
on the travisci project as specified in the .travis.yaml.  We will first bump the jdk level to 1.8 and then run the 
full mvn test command to run the BVT.  

There were some issues with the java build version and the org.json libraries, so instead of playing around with different
versions of JDK 7, I just went up to 1.8 since it is supported on Travis.

