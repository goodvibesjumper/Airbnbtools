package com.transpocloud.airbnb;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public class AirbnbReports {
	
	public static void printRevenuesByListingAndMonthReport(AirbnbReservationCollection anAirbnbReservationCollection) {

		System.out.println("Revenues By Listing and Month Report");
		System.out.println("-------- -- ------- --- ----- ------");
		
		float grandTotal = 0;
		float yearTotal = 0;
		int startYear = 2013;
		int endYear = 2015;
		int yearCount = endYear-startYear+1;
		
		ArrayList<String> listingNames = anAirbnbReservationCollection.getListingNames();
		HashMap<String,Float> monthlyRevenuesByPropertyMap = anAirbnbReservationCollection.getMonthlyRevenuesByPropertyMap();
		
		int listingCount = anAirbnbReservationCollection.getListingNames().size();
		 
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
				//System.out.println(listing + " " + y);
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
	
					if (amt > 0) {
						//System.out.printf(monthName + "\t%8.2f\n",amt);
					}
				}
				yearlyGrid[y-startYear][listingIdx][12] = listingTotal;
			
		    	//System.out.println("------------------");
		    	//System.out.printf("\t%8.2f" + y + " " + listing + "\n\n",listingTotal);
			}
			
			//System.out.printf("Monthly Totals All Units " + y + " :\n");
			for (int x=0; x < 12; x++) {
				String monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[x];
				monthName = monthName.substring(0, 3);
				yearlyGrid[y-startYear][listingCount][x] = monthlyTotals[x];
				//if (monthlyTotals[x] > 0)
					//System.out.printf(monthName + "\t %8.2f\t\t\n",monthlyTotals[x]);
			}
			yearlyGrid[y-startYear][listingCount][12] = yearTotal;
			//System.out.printf("\t --------\n");
			//System.out.printf("\t%9.2f\n\n\n",yearTotal);
		}
		
		//System.out.print("\nGRAND TOTAL\t" + grandTotal + "\n\n");*/
		
		// Print the yearly grid
    	//System.out.printf("\n\n");
    	for (int iYear=0; iYear < yearCount; iYear++) {
    		System.out.print("\n" + (startYear + iYear));
    		for (int i=0; i< listingCount; i++) {
    			if (i==0) {
    				System.out.printf("%11s",AirbnbReservationCollection.getListingAlias(listingNames.get(i)));
    			} else {
    				System.out.printf("%12s",AirbnbReservationCollection.getListingAlias(listingNames.get(i)));
    			}
    		}
    		System.out.printf("%12s","All Units");
    		System.out.print("\n------------------------------------------------------\n");
    		
    		
    		for (int iMonth=0; iMonth < 13; iMonth++) {	
    			
    			String monthName;
    			if (iMonth < 12){
    				monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[iMonth];
    				monthName = monthName.substring(0, 3);
    			} else {
    				System.out.println("-----------------------------------------------------");
    				monthName = "Tot";
    			}
    			
    			System.out.printf("%-5s",monthName);
    			for (int iListing=0; iListing < listingCount+1; iListing++) {
    				float val = 0;
    				val = yearlyGrid[iYear][iListing][iMonth];
    				if (iListing == 0) {
    					System.out.printf("%10.2f", val);
    				} else {
    					System.out.printf("%12.2f", val);
    				}
    			}
    			System.out.println();
    		}
    		System.out.println();
		}
	}
    	
    public static void printCheckInByMonthReport(AirbnbReservationCollection anAirbnbReservationCollection) {
    	ArrayList<String> listingNames = anAirbnbReservationCollection.getListingNames();
    	
    	System.out.println("----------------------------");
    	System.out.println("Guest Check-In Counts Report");
    	System.out.println("----------------------------\n");
    	
    	int[][][] checkInArray = anAirbnbReservationCollection.getCheckInOrCheckOutByMonthArray(true);
    	
    	
    	for (int y = 0; y <= AirbnbGlobals.ENDYEAR-AirbnbGlobals.STARTYEAR; y++) {
    	
    		// Print out the header for this particular year grid
    		System.out.printf("%-5d", (AirbnbGlobals.STARTYEAR + y));
       		for (int i=0; i< listingNames.size(); i++) {
       			System.out.printf("%10s",AirbnbReservationCollection.getListingAlias(listingNames.get(i)));
       		}
      		System.out.printf("%8s","ALL");
       		System.out.print("\n---------------------------------------------\n");
       		
       		int grandTot = 0;
       		
       		for (int m = 0; m < 12; m++) {
       			String monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[m];
    			monthName = monthName.substring(0, 3);
    			System.out.printf(monthName);		// Each row starts by printing the month name
       		
    			int tot = 0;
    			
       			for (int l = 0; l < listingNames.size(); l++) {
    				int amt = checkInArray[y][l][m];
    				tot+=amt;
    				grandTot+=amt;
    				if (l==0) System.out.printf("%10d",amt);
    				else System.out.printf("%10d",amt);
    			}
       			System.out.printf("%10d\n",tot);
    		}
    		System.out.println("--------------------------------------------");
    		System.out.printf("Tot");
    		for (int l = 0; l < listingNames.size(); l++) {
    			System.out.printf("%10d",checkInArray[y][l][12]);
    		}
    		System.out.printf("%10d\n\n",grandTot);
    	}
    	
    }
    
    public static void printCheckOutByMonthReport(AirbnbReservationCollection anAirbnbReservationCollection) {
    	ArrayList<String> listingNames = anAirbnbReservationCollection.getListingNames();
    	
    	System.out.println("-----------------------------");
    	System.out.println("Guest Check-Out Counts Report");
    	System.out.println("-----------------------------\n");
    	
    	int[][][] checkOutArray = anAirbnbReservationCollection.getCheckInOrCheckOutByMonthArray(false);
    	
    	
    	for (int y = 0; y <= AirbnbGlobals.ENDYEAR-AirbnbGlobals.STARTYEAR; y++) {
    	
    		// Print out the header for this particular year grid
    		System.out.printf("%-5d", (AirbnbGlobals.STARTYEAR + y));
       		for (int i=0; i< listingNames.size(); i++) {
       			System.out.printf("%10s",AirbnbReservationCollection.getListingAlias(listingNames.get(i)));
       		}
      		System.out.printf("%8s","ALL");
       		System.out.print("\n----------------------------------------------\n");
       		
       		int grandTot = 0;
       		
       		for (int m = 0; m < 12; m++) {
       			String monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[m];
    			monthName = monthName.substring(0, 3);
    			System.out.printf(monthName);		// Each row starts by printing the month name
       		
    			int tot = 0;
    			
       			for (int l = 0; l < listingNames.size(); l++) {
    				int amt = checkOutArray[y][l][m];
    				tot+=amt;
    				grandTot+=amt;
    				if (l==0) System.out.printf("%10d",amt);
    				else System.out.printf("%10d",amt);
    			}
       			System.out.printf("%10d\n",tot);
    		}
    		System.out.println("---------------------------------------------");
    		System.out.printf("Tot");
    		for (int l = 0; l < listingNames.size(); l++) {
    			System.out.printf("%10d",checkOutArray[y][l][12]);
    		}
    		System.out.printf("%10d\n\n",grandTot);
    	}
    	
    }
			
	
	/**
	 * 
	 */
	public static void printRevenuesByMonthGuestReport(AirbnbReservationCollection anAirbnbReservationCollection) {
		HashMap<String,Integer> hm = anAirbnbReservationCollection.getMonthlyReservationNightTotals();

		System.out.println("Reservation Collection Night Totals :\n" + hm);
		
		System.out.println("Monthly Revenues By Guest Report");
		System.out.println("------- -------- -- ----- ------\n");
		
		for (String listing : anAirbnbReservationCollection.getListingNames()) {
			System.out.println(listing);
		}	
	}
	
	/**
	 * Allow us to see the number of occupied nights for each unit
	 */
	public static void printOccupancyReport(AirbnbReservationCollection anAirbnbReservationCollection) {
		System.out.println("\n");
		System.out.println("------------------- -- ------- --- ----- ----------------");
		System.out.println("          Occupancy By Listing and Month Report");
		System.out.println("------------------- -- ------- --- ----- ----------------");
		
		
		for (String listing : anAirbnbReservationCollection.getListingNames()) {
		//for (int y = startYear; y <= endYear; y++) {
			System.out.println(listing);
			int[] occupancyMonthlyTotals = new int[12];						
			for (int x=0; x < 12; x++) { 
				occupancyMonthlyTotals[x]=(int) 0;
			}
			
			//for (String listing : listingNames) {
			for (int y = AirbnbGlobals.STARTYEAR; y <= AirbnbGlobals.ENDYEAR; y++) {
				System.out.println("----" + y + "----");
				//System.out.println(listing + " " + y);
				for (int x=0; x < 12; x++) {
					String monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[x];
					monthName = monthName.substring(0, 3);

					int amt = 0;
					
					String s = (((x+1)<10)?"0":"") + Integer.toString(x+1) + "/01/" + Integer.toString(y);
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
					LocalDate d = null;
					try {
						d = LocalDate.parse(s,formatter);
					} catch (Exception e) {
						System.out.println(e); throw e;
					}
					
					int lastDayOfMonth = d.with(lastDayOfMonth()).getDayOfMonth();
					
					for (int z = 0; z < lastDayOfMonth; z++) {
						if (anAirbnbReservationCollection.isUnitVacant(listing, d.plusDays(z)) == false) amt++;
					}
				
					occupancyMonthlyTotals[x] = amt;		
					int occupancyRate = (int) ((100 * ((float)amt / (float)lastDayOfMonth)) + .5);
					
					if (occupancyRate > 0) {
						System.out.print(monthName + "\t" + amt + "/" + lastDayOfMonth + "\t" + (occupancyRate<100?" ":"") + occupancyRate + "%" + "  ");
						System.out.println(StringUtils.repeat("*", (int)((occupancyRate+.5)/2) ));
					}
				}
				
				//System.out.println();
			}
		}
	}

}
