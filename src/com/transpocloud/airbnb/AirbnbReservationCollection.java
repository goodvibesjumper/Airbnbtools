package com.transpocloud.airbnb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.text.DateFormatSymbols;



/**
 * @author Crash
 *
 */
public class AirbnbReservationCollection {
	
	String csvHeaderLine = null;
	ArrayList<AirbnbReservation> airbnbReservationList = new ArrayList<AirbnbReservation>();
	ArrayList<String> listingNames = new ArrayList<String>();
	HashMap<String,Float> monthlyRevenuesByPropertyMap = new HashMap<String,Float>();
	
	public AirbnbReservationCollection() {}
	public ArrayList<String> getListingNames() { return listingNames; }
	
	
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
			//System.out.println("File Path : " + filePath);

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
					
				//System.out.println(line);
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
	 * 
	 */
	public void printRevenuesByMonthGuestReport() {
		HashMap<String,Integer> hm = getMonthlyReservationNightTotals();
		System.out.println("Reservation Collection Night Totals :\n" + hm);
		
	}
	
	public void printRevenuesByListingAndMonthReport() {
		System.out.println("Revenues By Listing and Month Report");
		//System.out.println(monthlyRevenuesByPropertyMap);
		
		float grandTotal = 0;
		for (String listing : listingNames) {
			float listingTotal = 0;
			System.out.println(listing);
			for (int x=0; x < 12; x++) {
				String monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[x];
				monthName = monthName.substring(0, 3);

				float amt = 0;
				
				try {
					String key = listing + "|" + x + "/2014";
					amt = monthlyRevenuesByPropertyMap.get(key);
					listingTotal+=amt;
					grandTotal+=amt;
				} catch (Exception e) {}
				
				//DecimalFormat myFormatter = new DecimalFormat("$##,###.##");
				DecimalFormat myFormatter = new DecimalFormat("$##,##0.00");
			    String amtString = myFormatter.format(amt);
				
				System.out.println(monthName + "\t" + amtString);
			}
			
			DecimalFormat myFormatter = new DecimalFormat("$##,###.##");
		    String amtString = myFormatter.format(listingTotal);
		    System.out.println("------------------");
			System.out.println("TOTAL\t" + amtString + "\t"+listing + "\n\n");
			
		}
		System.out.print("GRAND TOTAL\t" + grandTotal + "\n\n");
	}
}
