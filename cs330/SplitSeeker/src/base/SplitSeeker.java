//J.P. Sohal
//Csci330
//Assignment1


package base;



import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/*
Usage: java SplitSeeker fileName

where the argumuents are
   fileName = the name of the  file where the stock data is stored
   

*/



public class SplitSeeker {
	   public static void main(String[] args) {
	       if ( args.length != 1) {
	            System.err.println("Error: Wrong number of arguments.");
	            System.exit(2);
	        }  
	              
	        String fileName = args[0];
	        ArrayList<TradingDayInfo> dataList;
	        dataList = loadDataFile(fileName);  
	        calculateSplits(dataList); 
	   }  
	   
	   /*
	      Pre-conditions:
	         - dataFile is the readable file containing the stock data
	         - numDays is the number of days of data contained in the file
	         
	      Post-conditions:
	         - A scanner scans the dataFile and a array called tdiArray is created. The tdiArray is an array of the object type TradingDayInfo.
	           The object TradingDayInfo contains the stock information for each individual day in the stock data file.  
	   */
	   
	   public static ArrayList <TradingDayInfo> loadDataFile(String dataFile) {
	         ArrayList<TradingDayInfo> tdiList = new ArrayList<TradingDayInfo>();
	         String[] tokenSplit = null;
	         int i = 0;
	      try {Scanner input = new Scanner(new File(dataFile)); 
	            while (input.hasNextLine()){
	               String token = input.nextLine();
	               tokenSplit = token.split(";");
	               tdiList.add(i, new TradingDayInfo(tokenSplit[0],tokenSplit[1],Double.parseDouble(tokenSplit[2]),Double.parseDouble(tokenSplit[3]),Double.parseDouble(tokenSplit[4]),Double.parseDouble(tokenSplit[5])));
	               i++;        
	            } 
	               
	            
	         } catch(FileNotFoundException e ) {
	               System.err.println("Error: unable to open file " + dataFile);
	               System.exit(1);
	         } catch(NoSuchElementException e ) {
	               System.err.println("Error: Not able to parse file " + dataFile);
	               System.exit(2);
	         } finally {
	                  
	         }
	         
	        return tdiList;
	    }
	    
	    
	    /*
	      Pre-conditions:
	         - tdiArray is the array containing the stock data for each day 
	      Post-conditions:
	         - After looping through the tdiArray the stock splits that occur are calculated for each company. As splits are calulated they
	           are outputted to the screen along with the total number of splits for that company. 
	    */
	   
	   public static void calculateSplits(ArrayList<TradingDayInfo> tdiList){
	         NumberFormat formatter = new DecimalFormat ("#0.00");
	         String ticker = "ticker";
	         int splits = 0;
	      for(int i = 0; i<tdiList.size() -1; i++){
	         if(!ticker.equals(tdiList.get(i).getTicker())){
	            ticker = tdiList.get(i).getTicker();
	            System.out.println("Processing " + tdiList.get(i).getTicker() + "...");
	            splits = 0;
	         }    
	      
	         if(tdiList.get(i).getTicker().equals(tdiList.get(i+1).getTicker())){
	            if(Math.abs(tdiList.get(i+1).getClosingPrice()/ tdiList.get(i).getOpeningPrice() - 2.0) < 0.05){
	               System.out.println("2:1 split on " + (tdiList.get(i+1).getDate() + "     " + formatter.format(tdiList.get(i+1).getClosingPrice()) + " -->  " + formatter.format(tdiList.get(i).getOpeningPrice())));
	               splits++;
	            }
	            else if(Math.abs(tdiList.get(i+1).getClosingPrice()/tdiList.get(i).getOpeningPrice() - 3.0) < 0.05){
	               System.out.println("3:1 split on " + (tdiList.get(i+1).getDate() + "     " + formatter.format(tdiList.get(i+1).getClosingPrice()) + " -->  " + formatter.format(tdiList.get(i).getOpeningPrice())));
	               splits++;
	            }
	            else if(Math.abs(tdiList.get(i+1).getClosingPrice()/tdiList.get(i).getOpeningPrice() - 1.5) < 0.05){
	               System.out.println("3:2 split on " + (tdiList.get(i+1).getDate() + "     " + formatter.format(tdiList.get(i+1).getClosingPrice()) + " -->  " + formatter.format(tdiList.get(i).getOpeningPrice())));
	               splits++;
	            }
	         }
	         
	         if(!(tdiList.get(i).getTicker().equals(tdiList.get(i+1).getTicker()))){
	            System.out.println("splits: " + splits);
	         }
	         
	      }
	   
	      if(splits>0){
	         System.out.println("splits: " + splits);
	      }     
	   }
	}    
	
	
	
