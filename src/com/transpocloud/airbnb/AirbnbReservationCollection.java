package com.transpocloud.airbnb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.text.ParseException;
import java.time.LocalDate;



/**
 * @author Crash
 * comment test 10/27/14 2:24pm
 */
public class AirbnbReservationCollection {

	String csvHeaderLine = null;
	ArrayList<AirbnbReservation> airbnbReservationList = new ArrayList<AirbnbReservation>();
	ArrayList<String> listingNames = new ArrayList<String>();
	HashMap<String,Float> monthlyRevenuesByPropertyMap = new HashMap<String,Float>();
	
	
	public AirbnbReservationCollection() {}
	public ArrayList<String> getListingNames() { return listingNames; }
	public HashMap<String,Float> getMonthlyRevenuesByPropertyMap() { return monthlyRevenuesByPropertyMap; }
	public ArrayList<AirbnbReservation> getAirbnbReservationList() {return airbnbReservationList;}
	
	public static String getListingAlias(String aListingString) {
		String ret = aListingString;
		
		if (aListingString.contains("Great Location Great Price!")) 		return "335 DN";
		if (aListingString.contains("Huge 2br King Beds Great Location!")) 	return "335 UP";
		if (aListingString.contains("1 Bedroom Half Duplex with Kitchen")) 	return "REGAL DN";
		
		return ret;
	}
	
	/**
	 * Test to see if a particular unit is vacant on a particular night.
	 * 
	 * @param aUnitName The text string that identifies the unit
	 * @param aDate		The date that we wish to check vacancy
	 * @return			true if vacant
	 */
	public boolean isUnitVacant(String aUnitName, LocalDate aDate) {
		//System.out.println("isUnitVacant() called, unit name: " + aUnitName + " Date:" + aDate);
		
		boolean isVacant = true;

		for(Iterator<AirbnbReservation> i = airbnbReservationList.iterator(); i.hasNext(); ) {
			
			AirbnbReservation res = i.next();
			
			if (res.getListingName().contains(aUnitName)) {
				LocalDate checkInDate = res.getCheckInDate();
				//System.out.println(res.getCsvReservationLine());
				//System.out.println(aDate.isEqual(checkInDate) + " " + aDate.isAfter(checkInDate) + aDate.isBefore(res.getCheckoutDate()));
				
				if (aDate.isEqual(checkInDate)) return false;
				if (aDate.isAfter(checkInDate) && aDate.isBefore(res.getCheckoutDate())) return false;
			}
		}
		
		return isVacant;
		
	}
	 
	
	/**
	 * Create a collection of AirbnbReservation objects from a .csv file
	 * @param aFileList A list of strings representing one or more .csv file
	 * @throws IOException
	 * @throws InvalidHeaderLineException
	 * @throws InvalidReservationLineException
	 * @throws ParseException
	 */
	public AirbnbReservationCollection(List<String> aFileList) throws IOException, InvalidHeaderLineException, InvalidReservationLineException, ParseException {

		for(Iterator<String> i = aFileList.iterator(); i.hasNext(); ) {
			
			String filePath = i.next();

			File csvData = new File(filePath);
						
			BufferedReader br = new BufferedReader(new FileReader(csvData));
			String line;
			int linesRead = 0;
			while ((line = br.readLine()) != null) {
			   // process the line.
				if ((linesRead == 0 && AirbnbReservation.isValidHeaderLine(line)))
					csvHeaderLine = line;
				
				if (AirbnbReservation.isValidReservationLine(csvHeaderLine, line)) {
					AirbnbReservation res = new AirbnbReservation(csvHeaderLine,line);
					String listingName = res.getListingName();
					airbnbReservationList.add(res);
					// keep an updated list of property names
					if (!listingNames.contains(listingName)) listingNames.add(listingName);
					
					// keep an updated HashMap that holds the property name + MM/YY
					//  along with total revenues.
					HashMap<String,Float> hm = res.getMonthlyRevenueByPropertyMap();
					for (HashMap.Entry<String, Float> cursor : hm.entrySet()) {
						String key = cursor.getKey();
						if (this.monthlyRevenuesByPropertyMap.containsKey(key)) {
							Float val = cursor.getValue() + monthlyRevenuesByPropertyMap.get(key);
							this.monthlyRevenuesByPropertyMap.replace(key, val);
						} else {
							this.monthlyRevenuesByPropertyMap.put(cursor.getKey(), cursor.getValue());							
						}
					} 
				}
				linesRead++;
			}
			br.close();
			//System.out.println(linesRead + " lines read from " + filePath);
			//System.out.println(airbnbReservationList.size() + " reservations.");
		}
		return;
	}

	public void printPropertyList() {
		System.out.println(listingNames.toString());
	}
	
	// for a reservation, get a list of "MMYYXX" values which represents
	//  the number of of nights (XX) that the reservation had in particular months
	//   so lets say a reservation starts on 10/30/14 and checks out on 11/3/14
	//   this should generate a list containing ["101402","111402"]
	public HashMap<String,Integer> getMonthlyReservationNightTotals() {
		HashMap<String,Integer> resTotalsMap = new HashMap<String,Integer>();
		for(Iterator<AirbnbReservation> i = airbnbReservationList.iterator(); i.hasNext(); ) {
			AirbnbReservation res = i.next();
			HashMap<String,Integer>resHashMap = res.getMonthlyReservationNights();
			
			for (HashMap.Entry<String, Integer> cursor : resHashMap.entrySet()) {
				String key = cursor.getKey();
				Integer value = cursor.getValue();
				
				if (resTotalsMap.containsKey(key)) {
					Integer oldValue = resTotalsMap.get(key);
					resTotalsMap.replace(key, oldValue+value);
				} else {
					resTotalsMap.put(key, value);
				}
			}
		}
		
		return resTotalsMap;
	}


	/**
	 * Create a 3 dimensional array that has the number of check-ins (or check-outs)done for a particular month and 
	 *  a particular listing.
	 *  
	 *  true - gets check in array
	 *  false - gets check out array
	 *  
	 *  The first dimension represents the year
	 *  The second dimension represents the listing
	 *  The third dimension represents each month
	 *  
	 * @return
	 */
	public int[][][] getCheckInOrCheckOutByMonthArray(boolean getCheckIn) {
		int yearCount = AirbnbGlobals.ENDYEAR-AirbnbGlobals.STARTYEAR+1;
		int listingCount = listingNames.size();
		
		int[][][] checkInArray = new int[yearCount][listingCount+1][13];
		
		for(Iterator<AirbnbReservation> i = airbnbReservationList.iterator(); i.hasNext(); ) {
			AirbnbReservation res = i.next();
			LocalDate d = getCheckIn ? res.getCheckInDate() : res.getCheckoutDate();
			int y = d.getYear();
			int m = d.getMonth().getValue() - 1;
			int l = this.listingNames.indexOf(res.getListingName());
			if ((y < 0) || (m < 0) || (l < 0)) {
				System.out.println("This should never happen");
			}
			checkInArray[y-AirbnbGlobals.STARTYEAR][l][m]++ ;			
		}
		
		for (int y=0; y <= (AirbnbGlobals.ENDYEAR-AirbnbGlobals.STARTYEAR); y++) {
			for (int l=0; l < listingCount; l++) {
				int tot = 0;
				for (int m=0; m <= 11; m++) {
					tot += checkInArray[y][l][m];
				}
				checkInArray[y][l][12] = tot;
			}
		}

		return checkInArray;
	}

}
