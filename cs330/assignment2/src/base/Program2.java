//J.P. Sohal
//CS 330


package base;



import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Program2 {
	


		public static Connection conn;
		static boolean found = true;
		
		public static void main(String[] args) {
			
			try {
				ArrayList<TickerData> stockDataArray;
				Scanner connStrSource = new Scanner(new File("connString.txt"));
				String[] connString = connStrSource.nextLine().trim().split("\\s+");
				connStrSource.close();
				conn = DriverManager.getConnection(connString[0].trim(), connString[1].trim(), connString[2].trim());
				System.out.println("Database connection established");
				Scanner keyboard = new Scanner(System.in);
				System.out.print("Enter a ticker symbol: ");
				String ticker = keyboard.nextLine().trim();
				while (!ticker.matches("")) {
					displaySplits(ticker);
					if(found){
						stockDataArray = adjustForSplits(ticker);
						portfolioManager(stockDataArray);
					}
					System.out.print("\nEnter a ticker symbol: ");
					ticker = keyboard.nextLine().trim();
				}
				conn.close();
				System.out.println("Database connection closed");
				keyboard.close();
			}
			catch (FileNotFoundException ex) {
				System.out.println("File connString.txt not found");
				return;
			}
			catch (SQLException ex) {
				System.out.println("SQL exception");
				ex.printStackTrace();
				return;
			}
		}
		
		
		/*
		 Pre-Conditions:
		 	- takes in ticker symbol 
		 Post-Conditions
		 	- displays all the splits that have occurred for the company the ticker represents
		*/
		public static void displaySplits(String ticker) {
			NumberFormat formatter = new DecimalFormat ("#0.00");
			ArrayList<TickerData> dataArrayS = new ArrayList<TickerData>();
			
			try {
				Statement stat = conn.createStatement();
				ResultSet rs = stat.executeQuery("select * from company where ticker = " + quote(ticker));
				if (!rs.next()){
					System.out.println("Ticker " + ticker + " not found in database");
					found = false;
				
				}				
				else {
					found = true;
					System.out.println(rs.getString(2));
					rs = stat.executeQuery("select * from pricevolume where ticker = " + quote(ticker) + " order by transDate DESC ");
					if (!rs.next()) {
						System.out.println("\nNo data for ticker " + ticker);
					}	
					else {					
						System.out.println("Adjusting data for splits");
						boolean done = false;
						while (!done) {
							TickerData data = new TickerData();
							data.date = rs.getString(2).trim();							
							data.open = Double.parseDouble(rs.getString(3).trim());
							data.close = Double.parseDouble(rs.getString(6));
							dataArrayS.add(0,data);
							if(dataArrayS.size()>1){
					            if(Math.abs(dataArrayS.get(0).close/dataArrayS.get(1).open - 2.0) < 0.13){
					               System.out.println("2:1 split on " + (data.date + ";     " + formatter.format(dataArrayS.get(0).close) + " -->  " + formatter.format(dataArrayS.get(1).open)));
					            }
					            else if(Math.abs(dataArrayS.get(0).close/dataArrayS.get(1).open - 3.0) < 0.13){
						               System.out.println("3:1 split on " + (data.date + ";     " + formatter.format(dataArrayS.get(0).close) + " -->  " + formatter.format(dataArrayS.get(1).open)));
                                }
	     			            else if(Math.abs(dataArrayS.get(0).close/dataArrayS.get(1).open - 1.5) < 0.13){
						               System.out.println("3:2 split on " + (data.date + ";     " + formatter.format(dataArrayS.get(0).close) + " -->  " + formatter.format(dataArrayS.get(1).open)));
						        }
						    }							
							if (!rs.next()){
								done = true;
							}
						}				
					}
				}
			}
			catch (SQLException ex) {
				System.out.println("SQL exception in processTicker");
			}
		}
			
		/*
		 Pre-conditions:
		 	- takes in the ticker symbol 
		 Post-conditions
		  	- calculates the splits that have occurred for the stock of the company represented by the ticker
		  	- adjust the stock data based on those splits
		  	- returns a new array that has been adjusted for the splits
		 */
		
		public static ArrayList<TickerData> adjustForSplits(String ticker) {
			double splitFactor = 1.0;
			ArrayList<TickerData> dataArray = new ArrayList<TickerData>();
			
			try {
				Statement stat = conn.createStatement();
				ResultSet	rs = stat.executeQuery("select * from pricevolume where ticker = " + quote(ticker) + " order by transDate DESC ");
					if (!rs.next()) {
						System.out.println("\nNo data for ticker " + ticker);
					}	
					else {					
						boolean done = false;
						while (!done) {
							TickerData data = new TickerData();
							data.date = rs.getString(2).trim();
							data.open = Double.parseDouble(rs.getString(3).trim())*splitFactor;
							data.close = Double.parseDouble(rs.getString(6))*splitFactor;
							dataArray.add(0,data);
							if(dataArray.size()>1){
					            if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 2.0) < 0.13){
					                splitFactor = splitFactor*0.5;
					                dataArray.remove(0);
					            	data.open = data.open/2;
					            	data.close = data.close/2;
					            	dataArray.add(0,data);				               
					            }
					            else if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 3.0) < 0.13){
					            	splitFactor = splitFactor*.3333333;
						            dataArray.remove(0);
						            data.open = data.open/3;
						            data.close = data.close/3;
						            dataArray.add(0,data);				
					            }
					            else if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 1.5) < 0.13){
					            	splitFactor = splitFactor*.6666666;
						            dataArray.remove(0);
						            data.open = data.open/1.5;
						            data.close = data.close/1.5;
						            dataArray.add(0,data);					   
					            }				        
						  }
						  if(!rs.next()){
							  done = true;
						  } 						
					   }			
					}
			}
		
			catch (SQLException ex) {
				System.out.println("SQL exception in processTicker");
			}
				
			return dataArray;		
		}
			
		/*
		  Pre-conditions:
		  	- takes in the ticker string 
		  Post-conditions:
		  	- returns a string in quote form to be used with the sql query
		 */
		
		public static String quote(String str) {
			return "'" + str + "'";
		}
		
		
		
		/*
		 Pre-conditions
		 	- takes in the adjusted stockDataArray and the index of the day you want to calculate the average for;
		 Post-conditions
		 	- returns an average of the close value for the previous 50 days from the day you are possibly trading on
		 */
		
		
		public static double calculateAverage (ArrayList <TickerData> dataArray, int index){
			double average = 0.0;
			for(int i = index-1;  (index - 50)<=i; i--){
				average = average + dataArray.get(i).close;
			}
			average = average/50;					
			return average;		
		}
		
		
		/*
		  Pre-conditions
		 	-takes in the adjusted stock data array
		  Post-Conditions
		  	- calculates if you want to buy stock, sell stock or do neither
		  	- executes a trade(buy/sell) or neither based of those calculations
		  	- displays the number of transactions(buy or sell) that have occurred
		  	- displays the net gain achieved through buying and selling stock 
		 */
		
		
		public static void portfolioManager (ArrayList <TickerData> dataArray){
				double average = 0.0;
				ArrayList <TickerData> stockData = dataArray;
				double cash = 0.0;
				int numShares = 0;
				double transactionFee = 8.0;
				int transactions = 0;
				NumberFormat formatter = new DecimalFormat ("#0.00");
				
			for(int i = 50; i < dataArray.size()-1; i++){
				average = calculateAverage(stockData, i);
				if((dataArray.get(i).close < average) && (dataArray.get(i).open - dataArray.get(i).close >= dataArray.get(i).open*.03)){
					numShares = numShares + 100;
					cash = cash - (100*dataArray.get(i+1).open) - transactionFee;
					transactions++;	
				}
				else if((numShares >= 100) && (dataArray.get(i).open > average) && (dataArray.get(i).open - dataArray.get(i-1).close >= dataArray.get(i-1).close*.01)){					
					numShares = numShares - 100;
					cash = cash + (100*((dataArray.get(i).open + dataArray.get(i).close)/2)) - transactionFee;
					transactions++;
				}
		    }
			double netgain = (cash + numShares*dataArray.get(dataArray.size()-1).open);
			System.out.println();
			System.out.println("Executing investment strategy");
			System.out.println("Transactions executed: " + transactions);
			System.out.println("Net gain: " + formatter.format(netgain));
	  }
}





