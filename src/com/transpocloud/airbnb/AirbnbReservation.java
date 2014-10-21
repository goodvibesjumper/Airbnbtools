package com.transpocloud.airbnb;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.time.DateUtils;

public class AirbnbReservation {
	private Date 	startDate;
	private int		numberOfNights;
	private String	guestName;
	private String	listingName;		// which property is this res for
	private String	confirmationCode;
	private float	amount;
	private float	hostFee;
	private float	cleaningFee;
	private String	csvReservationLine;		// the line used to create this object
	private String	csvHeaderLine;
	
	HashMap<String, Integer> reservationNightsByMonthMap;	// generated upon creation
	// stores number of nights this reservation spans in the format
	// {"MM/YYYY",n} where "MM/YYYY" is a key representing a unique month/year combo
	//  and n is the number of nights this reservation 
	
	//----------- basic get/setters ----------------//
	public Date 	getStartDate() 			{ return startDate; }
	public int		getNumberOfNights() 	{ return numberOfNights; }
	public String 	getGuestName()			{ return guestName; }
	public float	getAmount()				{ return amount; }
	public float	getHostFee()			{ return hostFee; }
	public float	getCleaningFee()		{ return cleaningFee; }
	public String	getCsvReservationLine()	{ return csvReservationLine; }
	public String	getCsvHeaderLine() 		{ return csvHeaderLine; }
	public String	getListingName()		{ return listingName; }
	public String	getConfirmationCode()	{ return confirmationCode; }
	
	public HashMap<String, Integer> getReservationNightsByMonthMap() { return reservationNightsByMonthMap; }
	public AirbnbReservation(String aCsvColumnHeaderLine, String aCsvReservationLine) throws InvalidHeaderLineException, InvalidReservationLineException, ParseException {
		List<String> 	headerDescriptionList;
		List<String> 	reservationLineList;
		
		// First confirm that the data is a-okay
		if (!isValidHeaderLine(aCsvColumnHeaderLine)) { 
			throw new InvalidHeaderLineException();
		}
		
		if (!isValidReservationLine(aCsvColumnHeaderLine, aCsvReservationLine)) {
			throw new InvalidReservationLineException();
		}
		
		// If we got this far we should be able to create an AirbnbReservation
		//  object successfully
		headerDescriptionList 	= Arrays.asList(aCsvColumnHeaderLine.split(","));
		reservationLineList 	= Arrays.asList(aCsvReservationLine.split(","));
		
		String startDateString = reservationLineList.get(headerDescriptionList.indexOf("Start Date"));
		String numberOfNightsString = reservationLineList.get(headerDescriptionList.indexOf("Nights"));
		String amountString = reservationLineList.get(headerDescriptionList.indexOf("Amount"));
		String hostFeeString = reservationLineList.get(headerDescriptionList.indexOf("Host Fee"));
		String cleaningFeeString = reservationLineList.get(headerDescriptionList.indexOf("Cleaning Fee"));
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

		guestName 			= reservationLineList.get(headerDescriptionList.indexOf("Guest"));
		listingName 		= reservationLineList.get(headerDescriptionList.indexOf("Listing"));		
		startDate 			= formatter.parse(startDateString);
		confirmationCode 	= reservationLineList.get(headerDescriptionList.indexOf("Confirmation Code"));
		numberOfNights		= Integer.parseInt(numberOfNightsString);
		amount 				= Float.parseFloat(amountString);
		hostFee 			= Float.parseFloat(hostFeeString);
		cleaningFee 		= Float.parseFloat(cleaningFeeString);
		
		reservationNightsByMonthMap = getMonthlyReservationNights(); 
		return;
	}
	
	// get the scheduled checkout date of a reservation
	public static Date getCheckoutDate(String aCsvColumnHeaderLine, String aCsvReservationLine) {
		Date checkoutDate = null;
		String d = AirbnbReservation.getField("Date", aCsvColumnHeaderLine, aCsvReservationLine);
		String n = AirbnbReservation.getField("Nights", aCsvColumnHeaderLine, aCsvReservationLine);
		int nights = Integer.parseInt(n);
		
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		String dateInString = d;
		
		try {
			checkoutDate = formatter.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		DateUtils.addDays(checkoutDate, nights);
		
		return checkoutDate;
	}
	
	// determines if a line is a valid Airbnb header line for airbnb reservations
	public static boolean isValidHeaderLine(String aCsvColumnHeaderLine) {
		if (aCsvColumnHeaderLine.contains("Type"))
			return true;
		return false;
	}
	
	// determines if a string is a valid, parse-able Airbnb Reservation Line
	public static boolean isValidReservationLine(String aCsvColumnHeaderLine, String aCsvReservationLine) {
		List<String> 	headerDescriptionList;
		List<String> 	reservationLineList;
		
		// 1) place reservation line in an object that we can traverse through
		headerDescriptionList 	= Arrays.asList(aCsvColumnHeaderLine.split(","));
		reservationLineList 	= Arrays.asList(aCsvReservationLine.split(","));
		
		// need to loop through all comma delimited values in reservation line
		//  and determine that all fields are parsable
		String reservationDateString = reservationLineList.get(headerDescriptionList.indexOf("Date")).trim();
		String typeString = reservationLineList.get(headerDescriptionList.indexOf("Type")).trim();
		if (!typeString.toUpperCase().contains("RESERVATION")) return false;
		String confirmationCodeString = reservationLineList.get(headerDescriptionList.indexOf("Confirmation Code")).trim();
		String startDateString = reservationLineList.get(headerDescriptionList.indexOf("Start Date")).trim();
		String numberOfNightsString = reservationLineList.get(headerDescriptionList.indexOf("Nights")).trim();
		String guestString = reservationLineList.get(headerDescriptionList.indexOf("Guest")).trim();
		String listingString = reservationLineList.get(headerDescriptionList.indexOf("Listing")).trim();
		String amountString = reservationLineList.get(headerDescriptionList.indexOf("Amount")).trim();
		String hostFeeString = reservationLineList.get(headerDescriptionList.indexOf("Host Fee")).trim();
		String cleaningFeeString = reservationLineList.get(headerDescriptionList.indexOf("Cleaning Fee")).trim();
				
		if (confirmationCodeString.isEmpty()) return false;
		
		try {
			AirbnbReservation.getCheckoutDate(aCsvColumnHeaderLine, aCsvReservationLine);
		} catch (Exception e) {
			return false;
		}

		// reservationDateString
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		try {formatter.parse(reservationDateString);}
		catch (ParseException pe) { return false; }
		
		//confirmationCodeString
		if (confirmationCodeString.isEmpty()) return false;
		
		//startDateString
		try {formatter.parse(startDateString);}
		catch (ParseException pe) { return false; }
		
		//numberOfNightsString
		Integer.parseInt(numberOfNightsString);
		
		//guestString
		if (guestString.isEmpty()) return false;
		
		//listingString
		if (listingString.isEmpty()) return false;
		
		//amountString
		Float.parseFloat(amountString);
		
		//hostFeeString
		Float.parseFloat(hostFeeString);
		
		//cleaningFeeString
		Float.parseFloat(cleaningFeeString);
		
		return true;
	}
	
	public static String getField(String aField, String aCsvColumnHeaderLine, String aCsvReservationLine) {
		String fieldValue = "";
		
		List<String> 	headerDescriptionList;
		List<String> 	reservationLineList;
		
		headerDescriptionList 	= Arrays.asList(aCsvColumnHeaderLine.split(","));
		reservationLineList 	= Arrays.asList(aCsvReservationLine.split(","));
		fieldValue = reservationLineList.get(headerDescriptionList.indexOf(aField));
		
		return fieldValue;
	}
	
	
	public HashMap<String, Integer> getMonthlyReservationNights() {
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		
		Date d = (Date) startDate.clone();
		
		Calendar cal = Calendar.getInstance();
		
		for (int i = 0; i < numberOfNights; i++)
		{
			Date d2 = DateUtils.addDays(d, i);
			
			cal.setTime(d2);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);
			//System.out.println(month + "/" + year);
			String key = month + "/" + year;
			if (hm.containsKey(key)) {
				hm.replace(key, (hm.get(key))+1);
			} else {
				hm.put(key, 1);
			}
		}
		
		System.out.println(this.guestName + " " + this.listingName + " " + hm.toString());
		
		return hm;
	}
}

