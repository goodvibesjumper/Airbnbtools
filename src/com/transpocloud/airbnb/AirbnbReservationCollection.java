package com.transpocloud.airbnb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.text.DateFormatSymbols;
import java.text.ParseException;

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
	 * 
	 */
	public void printRevenuesByMonthGuestReport() {
		HashMap<String,Integer> hm = getMonthlyReservationNightTotals();
		System.out.println("Reservation Collection Night Totals :\n" + hm);
		
		System.out.println("Monthly Revenues By Guest Report");
		System.out.println("------- -------- -- ----- ------\n");
		
		for (String listing : listingNames) {
			System.out.println(listing);
			
			
		}
		
	}
	
	public void printRevenuesByListingAndMonthReport() {
		System.out.println("Revenues By Listing and Month Report");
		System.out.println("-------- -- ------- --- ----- ------\n");
		
		float grandTotal = 0;
		float yearTotal = 0;
		int startYear = 2013;
		int endYear = 2015;
		int yearCount = endYear-startYear+1;
		int listingCount = listingNames.size();
		 
		float[][][] yearlyGrid = new float[yearCount+1][listingCount + 1][13]; // All properties
		
		for (int y = startYear; y <= endYear; y++) {
			yearTotal = 0;
			
			Float[] monthlyTotals = new Float[12];
						
			for (int x=0; x < 12; x++) { 
				monthlyTotals[x]=(float) 0;
			}
			
			for (String listing : listingNames) {
				int listingIdx = listingNames.indexOf(listing);
				
				float listingTotal = 0;
				System.out.println(listing + " " + y);
				for (int x=0; x < 12; x++) {
					String monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[x];
					monthName = monthName.substring(0, 3);

					float amt = 0;
				
					try {
						String key = listing + "|" + (x+1) + "/" + y;
						amt = monthlyRevenuesByPropertyMap.get(key);
						listingTotal+=amt;
						monthlyTotals[x] += amt;
						yearlyGrid[y-startYear][listingIdx][x] = amt;							
						
						yearTotal+=amt;
						grandTotal+=amt;
					} catch (Exception e) {}
	
					if (amt > 0)
						System.out.printf(monthName + "\t%8.2f\n",amt);
				}
				yearlyGrid[y-startYear][listingIdx][12] = listingTotal;
			
		    	System.out.println("------------------");
		    	System.out.printf("\t%8.2f\t" + y + " " + listing + "\n\n",listingTotal);
			}
			
			System.out.printf("Monthly Totals All Units " + y + " :\n");
			for (int x=0; x < 12; x++) {
				String monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[x];
				monthName = monthName.substring(0, 3);
				yearlyGrid[y-startYear][listingCount][x] = monthlyTotals[x];
				if (monthlyTotals[x] > 0)
					System.out.printf(monthName + "\t %8.2f\t\t\n",monthlyTotals[x]);
			}
			yearlyGrid[y-startYear][listingCount][12] = yearTotal;
			System.out.printf("\t --------\n");
			System.out.printf("\t%9.2f\n\n\n",yearTotal);
		}
		
		System.out.print("\nGRAND TOTAL\t" + grandTotal + "\n\n");
		
		// Print the yearly grid
    	System.out.printf("\n\n");
    	for (int iYear=0; iYear < yearCount; iYear++) {
    		System.out.print("\n" + (startYear + iYear));
    		for (int i=0; i< listingCount; i++) {
    			System.out.print("\t" + listingNames.get(i).substring(0, 9));
    		}
    		System.out.print("\tAll Units");
    		System.out.print("\n-----------------------------------------------------------------\n");
    		
    		
    		for (int iMonth=0; iMonth < 13; iMonth++) {	
    			
    			String monthName;
    			if (iMonth < 12){
    				monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[iMonth];
    				monthName = monthName.substring(0, 3);
    			} else {
    				System.out.println("-----------------------------------------------------------------");
    				monthName = "Tot";
    			}
    			
    			System.out.printf(monthName);
    			for (int iListing=0; iListing < listingCount+1; iListing++) {
    				float val = 0;
    				val = yearlyGrid[iYear][iListing][iMonth];
    				System.out.printf("\t%9.2f", val);
    			}
    			System.out.println();
    		}
    		System.out.println();
		}
			
	}
}
