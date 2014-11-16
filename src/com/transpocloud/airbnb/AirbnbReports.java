package com.transpocloud.airbnb;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class AirbnbReports {
	
	public static void printReservationLengthReport(AirbnbReservationCollection anAirbnbReservationCollection) {
		System.out.println("---------------------------------");
		System.out.println("Reservation Length Summary Report");
		System.out.println("---------------------------------\n");
		
		int startYear = AirbnbGlobals.STARTYEAR;
		int endYear = AirbnbGlobals.ENDYEAR;
		int yearCount = endYear-startYear+1;
		int listingCount = anAirbnbReservationCollection.getListingNames().size();
		int maxReservationLength = AirbnbGlobals.MAXRESERVATIONLENGTH;
		float grandTotAllRevenues = 0;
		
		ArrayList<AirbnbReservation> reservations = anAirbnbReservationCollection.getAirbnbReservationList();
		int[][][] reservationLengthsArray = new int[yearCount][listingCount+1][maxReservationLength];
		float[][][] reservationRevenuesArray = new float[yearCount][listingCount+1][maxReservationLength];
		
		for(Iterator<AirbnbReservation> i = reservations.iterator(); i.hasNext(); ) {
			AirbnbReservation res = i.next();
			int listingIdx = anAirbnbReservationCollection.getListingNames().indexOf(res.getListingName());
			int yearIdx = res.getCheckInDate().getYear()-startYear;
			int resLength = res.getNumberOfNights();
			reservationLengthsArray[yearIdx][listingIdx][resLength]++;
			reservationLengthsArray[yearIdx][listingCount][resLength]++;					// running total
			reservationRevenuesArray[yearIdx][listingIdx][resLength]+=res.getAmount();
			reservationRevenuesArray[yearIdx][listingCount][resLength]+=res.getAmount();	// running total
			grandTotAllRevenues+=res.getAmount();
		}
		
		for(int i=0; i < listingCount; i++) {
			System.out.println(anAirbnbReservationCollection.getListingNames().get(i));
			for(int y=0; y < yearCount; y++) {
				System.out.println(startYear+y);
				for(int z=0; z < maxReservationLength; z++) {
					int cnt = reservationLengthsArray[y][i][z];
					float revenue = reservationRevenuesArray[y][i][z];
					
					if (cnt > 0) {
						int revenuePercentage = (int) ((revenue / grandTotAllRevenues) * 100 + .5);
						System.out.printf("%5d x %5d night reservation(s)   $%5.2f \n",cnt,z,revenue);
						//System.out.printf("%5d x %5d night reservation(s)   $%5.2f\n",cnt,zrevenue);
					}
					
				}
			}
		}

		System.out.println("\n\nSummary : ");
		float[] revenuePercentages = new float[maxReservationLength];
				
		for (int z=0; z < maxReservationLength; z++) {
			int resCountTotal = 0;
			float revenueTotal = 0;
			
			for(int y=0; y < yearCount; y++) {
				resCountTotal += reservationLengthsArray[y][listingCount][z];
				revenueTotal +=  reservationRevenuesArray[y][listingCount][z];
			}
			
			if (resCountTotal > 0) {
				int revenuePercentage = (int) ((revenueTotal/grandTotAllRevenues) * 100 + .5);
				float profit = revenueTotal - (float)(resCountTotal * 20);
				System.out.printf("%5d x %5d night reservation(s)   $%5.2f%8.2f %5d%%\n",resCountTotal,z,revenueTotal,profit,revenuePercentage);
			}
		}
	}
	
	public static void printRevenuesByListingAndMonthReport(AirbnbReservationCollection anAirbnbReservationCollection) {

		System.out.println("Revenues By Listing and Month Report");
		System.out.println("-------- -- ------- --- ----- ------");
		
		float yearTotal = 0;
		int startYear = AirbnbGlobals.STARTYEAR;
		int endYear = AirbnbGlobals.ENDYEAR;
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
					} catch (Exception e) {}
	
				}
				yearlyGrid[y-startYear][listingIdx][12] = listingTotal;
			}
			
			for (int x=0; x < 12; x++) {
				String monthName = DateFormatSymbols.getInstance(Locale.US).getMonths()[x];
				monthName = monthName.substring(0, 3);
				yearlyGrid[y-startYear][listingCount][x] = monthlyTotals[x];
			}
			yearlyGrid[y-startYear][listingCount][12] = yearTotal;
		}
		
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
			System.out.println(listing);
			int[] occupancyMonthlyTotals = new int[12];						
			for (int x=0; x < 12; x++) { 
				occupancyMonthlyTotals[x]=(int) 0;
			}
			
			for (int y = AirbnbGlobals.STARTYEAR; y <= AirbnbGlobals.ENDYEAR; y++) {
				System.out.println("---------------------------------------" + y + "-------------------");
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
				
			}
		}
	}

}
