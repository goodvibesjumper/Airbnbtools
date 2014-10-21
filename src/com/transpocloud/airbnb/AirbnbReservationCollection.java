package com.transpocloud.airbnb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AirbnbReservationCollection {
	
	String csvHeaderLine = null;
	ArrayList<AirbnbReservation> airbnbReservationList = new ArrayList<AirbnbReservation>();
	ArrayList<String> listingNames = new ArrayList<String>();
	
	public AirbnbReservationCollection() {}
	public ArrayList<String> getListingNames() { return listingNames; }
	
	
	public AirbnbReservationCollection(List<String> aFileList) throws IOException, InvalidHeaderLineException, InvalidReservationLineException, ParseException {

		for(Iterator<String> i = aFileList.iterator(); i.hasNext(); ) {
			
			String filePath = i.next();
			System.out.println("File Path : " + filePath);

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
					if (!listingNames.contains(listingName)) listingNames.add(listingName);
				}
					
				//System.out.println(line);
				linesRead++;
			}
			br.close();
			System.out.println(linesRead + " lines read from " + filePath);
			System.out.println(airbnbReservationList.size() + " reservations.");
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
	
	public void printRevenuesByMonthGuestReport() {
		HashMap<String,Integer> hm = getMonthlyReservationNightTotals();
		System.out.println("Reservation Collection Night Totals :\n" + hm.toString());
		{CONFIRM,LISTING,CHECKINDATE,NUMNIGHTS,
		
	}
}
