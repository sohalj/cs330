  //J.P. Sohal
  //Csci330
  //Assignment1
  
  
  
  import java.io.*;
  import java.util.*;
  import java.lang.*;
  import java.text.DecimalFormat;
  import java.text.NumberFormat;

 

 
/*
   Usage: java DataMining fileName numberDays
   
   where the argumuents are
      fileName = the name of the  file where the stock data is stored
      numberDays = the number of days you have data for, which in this case would be 44515 days

*/


public class DataMining {
   public static void main(String[] args) {
       if ( args.length != 2 ) {
            System.err.println("Error: Wrong number of arguments.");
            System.exit(2);
        }  
              
        String fileName = args[0];
        int numberDays = Integer.parseInt(args[1]);
        TradingDayInfo[] dataArray;
        dataArray = loadDataFile(fileName,numberDays);  
        calculateSplits(dataArray); 
   }  
   
   /*
      Preconditions:
         - dataFile is the readable file containing the stock data
         - numDays is the number of days of data contained in the file
         
      Postconditions:
         - A scanner scans the dataFile and a array called tdiArray is created. The tdiArray is an array of the object type TradingDayInfo.
           The object TradingDayInfo contains the stock information for each individual day in the stock data file.  
   */
   
   public static TradingDayInfo[] loadDataFile(String dataFile,int numDays) {
         TradingDayInfo [] tdiArray = new TradingDayInfo [numDays];
         String[] tokenSplit = null;
         String sName = null;
         int i = 0;
      try {Scanner input = new Scanner(new File(dataFile)); 
            while (input.hasNextLine()){
               String token = input.nextLine();
               tokenSplit = token.split(";");
               tdiArray[i] = new TradingDayInfo(tokenSplit[0],tokenSplit[1],Double.parseDouble(tokenSplit[2]),Double.parseDouble(tokenSplit[3]),Double.parseDouble(tokenSplit[4]),Double.parseDouble(tokenSplit[5]));
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
         
        return tdiArray;
    }
    
    
    /*
      Preconditions:
         - tdiArray is the array containing the stock data for each day 
      Postconditions:
         - After looping through the tdiArray the stock splits that occur are calculated for each company. As splits are calulated they
           are outputted to the screen along with the total number of splits for that company. 
    */
   
   public static void calculateSplits(TradingDayInfo [] tdiArray){
         NumberFormat formatter = new DecimalFormat ("#0.00");
         String ticker = "ticker";
         int splits = 0;
      for(int i = 0; i<tdiArray.length-1; i++){
         if(!ticker.equals(tdiArray[i].getTicker())){
            ticker = tdiArray[i].getTicker();
            System.out.println("Processing " + tdiArray[i].getTicker() + "...");
            splits = 0;
         }    
      
         if(tdiArray[i].getTicker().equals(tdiArray[i+1].getTicker())){
            if(Math.abs(tdiArray[i+1].getClosingPrice()/tdiArray[i].getOpeningPrice() - 2.0) < 0.05){
               System.out.println("2:1 split on " + tdiArray[i+1].getDate() + "     " + formatter.format(tdiArray[i+1].getClosingPrice()) + " -->  " + formatter.format(tdiArray[i].getOpeningPrice()));
               splits++;
            }
            else if(Math.abs(tdiArray[i+1].getClosingPrice()/tdiArray[i].getOpeningPrice() - 3.0) < 0.05){
               System.out.println("3:1 split on " + tdiArray[i+1].getDate() + "     " + formatter.format(tdiArray[i+1].getClosingPrice()) + " -->  " + formatter.format(tdiArray[i].getOpeningPrice()));
               splits++;
            }
            else if(Math.abs(tdiArray[i+1].getClosingPrice()/tdiArray[i].getOpeningPrice() - 1.5) < 0.05){
               System.out.println("3:2 split on " + tdiArray[i+1].getDate() + "     " + formatter.format(tdiArray[i+1].getClosingPrice()) + " -->  " + formatter.format(tdiArray[i].getOpeningPrice()));
               splits++;
            }
         }
         
         if(!tdiArray[i].getTicker().equals(tdiArray[i+1].getTicker())){
            System.out.println("splits: " + splits);
         }
         
      }
   
      if(splits>0){
         System.out.println("splits: " + splits);
      }     
   }
}    