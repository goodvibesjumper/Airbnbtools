package com.transpocloud.airbnb;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

public class AirbnbReservationTest {

	String testFile = "Date,Type,Confirmation Code,Start Date,Nights,Guest,Listing,Details,Reference,Currency,Amount,Paid Out,Host Fee,Cleaning Fee\n"+
			"10/15/2014,Payout,,,,,,Transfer to ACH: *****4879,,USD,,92,,\n" +
			"10/15/2014,Reservation,CKQ94D,10/14/2014,1,Jia Qi Shen,Great Location Great Price! 2BR SLC,,,USD,92,,3,0\n" +
			"10/14/2014,Payout,,,,,,Transfer to ACH: *****4879,,USD,,291,,\n" +
			"10/14/2014,Reservation,J5MWQH,10/13/2014,6,Shane Rathbun,Huge 2br King Beds Great Location!,,,USD,291,,9,0\n" +
			"10/14/2014,Payout,,,,,,Transfer to ACH: *****4879,,USD,,92,,\n" +
			"10/13/2014,Reservation,CN4HMY,10/12/2014,1,Fanfei Gong,Great Location Great Price! 2BR SLC,,,USD,92,,3,0\n" +
			"10/14/2014,Payout,,,,,,Transfer to ACH: *****4879,,USD,,92,,\n" +
			"10/12/2014,Reservation,YHZDJZ,10/11/2014,1,Roosan Islam,Great Location Great Price! 2BR SLC,,,USD,92,,3,0\n" +
			"10/14/2014,Payout,,,,,,Transfer to ACH: *****4879,,USD,,92,,\n" +
			"10/11/2014,Reservation,3Z9EFX,10/10/2014,1,Troy Funk,Great Location Great Price! 2BR SLC,,,USD,92,,3,0\n" +
			"10/10/2014,Payout,,,,,,Transfer to ACH: *****4879,,USD,,92,,\n";

	String testHeader 			= "Date,Type,Confirmation Code,Start Date,Nights,Guest,Listing,Details,Reference,Currency,Amount,Paid Out,Host Fee,Cleaning Fee";
	String testPayoutLine 		= "10/15/2014,Payout,,,,,,Transfer to ACH: *****4879,,USD,,92,,\n";
	String testReservationLine 	= "10/15/2014,Reservation,CKQ94D,10/14/2014,1,Jia Qi Shen,Great Location Great Price! 2BR SLC,,,USD,92,,3,0\n";

	
	@Test
	public void testPrintRevenuesByListingAndMonthReport() {
		AirbnbReservationCollection resCollection = null;
		List<String> fileList;
		fileList = Arrays.asList("C:/temp/airbnb_2013.csv,C:/temp/airbnb_2014.csv,C:/temp/airbnb_pending.csv".split(","));
		try {
			resCollection = new AirbnbReservationCollection(fileList);
			resCollection.printRevenuesByListingAndMonthReport();
			assert(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAirbnbReservation_getMonthlyRevenueByPropertyMap() throws InvalidHeaderLineException, InvalidReservationLineException, ParseException {
		String testResLine = "12/20/2013,Reservation,ABCDEF,12/19/2014,15,Super Man,Great Location Great Price! 2BR SLC,,,USD,1380,,3,0\n";
		AirbnbReservation res = new AirbnbReservation(testHeader,testResLine);
		HashMap<String,Float> hm = res.getMonthlyRevenueByPropertyMap();
		System.out.println(hm);
	}
	
	
	
	
	@Test
	public void testPrintReservationNightTotals() {
		System.out.println("testPrintReservationNightTotals()----begin----");
		AirbnbReservationCollection resCollection = null;
		List<String> fileList;
		fileList = Arrays.asList("c:/temp/airbnb_2014.csv".split(","));
		try {
			resCollection = new AirbnbReservationCollection(fileList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		resCollection.getMonthlyReservationNightTotals();
		System.out.println("testPrintReservationNightTotals()----end----");
		assert(true);
	}
	
	@Test
	public void testGetMonthlyReservationNights() throws InvalidHeaderLineException, InvalidReservationLineException, ParseException {
		String testResString = "10/15/2014,Reservation,AB123C,09/29/2014,100,Jon Doe,Great Location Great Price! 2BR SLC,,,USD,920,,30,0\n";
		AirbnbReservation res = new AirbnbReservation(testHeader,testResString);
		HashMap<String, Integer> hm = res.getMonthlyReservationNights();
		assert(hm.toString().contains("{8/2014=2, 9/2014=8}"));
	}
	
	@Test
	public void testAirbnbReservationCollection() throws IOException, InvalidHeaderLineException, InvalidReservationLineException, ParseException {
		AirbnbReservationCollection resCollection;
		List<String> fileList;
		fileList = Arrays.asList("c:/temp/airbnb_2014.csv".split(","));
		try {
			resCollection = new AirbnbReservationCollection(fileList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert(true);
	}

	@Test
	public void testConstructorAndGetters() {
		try {
			AirbnbReservation airbnbReservation = new AirbnbReservation(testHeader,testReservationLine);
			airbnbReservation.getConfirmationCode();
			
		} catch (InvalidHeaderLineException | InvalidReservationLineException | ParseException e) {
			assert(false);
		}
		
		assert(true);
	}
	
	@Test
	public void testGetCheckoutDate() {
		Date d1 = AirbnbReservation.getCheckoutDate(testHeader, testReservationLine);
		Date d2 = null;
		
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		
		try {
			d2 = formatter.parse("10/16/2014");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assert(d1.equals(d2));
	}
	
	@Test
	public void testGetField() {
		String s = AirbnbReservation.getField("Confirmation Code", testHeader, testReservationLine);
		assertTrue(s.contains("CKQ94D"));
	}

	@Test
	public void testIsValidHeaderLine() {		
		boolean result = AirbnbReservation.isValidHeaderLine(testHeader);
		assertTrue(result);
	}

	@Test
	public void testIsValidReservationLine() {
		boolean result = AirbnbReservation.isValidReservationLine(testHeader, testReservationLine);
		assertTrue(result);
	}
	
	@Test
	public void testPrintPropertyList() {
		System.out.println("testPrintPropertyList()");
		AirbnbReservationCollection resCollection = null;
		List<String> fileList;
		fileList = Arrays.asList("c:/temp/airbnb_2014.csv".split(","));
		try {
			resCollection = new AirbnbReservationCollection(fileList);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		System.out.println("Property Listings : ");
		resCollection.printPropertyList();
	}
}
