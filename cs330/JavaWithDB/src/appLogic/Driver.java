package appLogic;

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

public class Driver {
	public static Connection conn;
	
	public static void main(String[] args) {
		try {
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
				calculateSplits(ticker);
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
	
	
	private static void displaySplits(String ticker) {
	
		NumberFormat formatter = new DecimalFormat ("#0.00");
		 class TickerData {
				public String date;
				public double open;
				public double close;
			}
		
		ArrayList<TickerData> dataArray = new ArrayList<TickerData>();
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from company where ticker = " + quote(ticker));
			if (!rs.next()){
				System.out.println("Ticker " + ticker + " not found in database");
			}	
			
			else {
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
						dataArray.add(0,data);
						if(dataArray.size()>1){
				            if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 2.0) < 0.13){
				               System.out.println("2:1 split on " + (data.date + "     " + formatter.format(dataArray.get(0).close) + " -->  " + formatter.format(dataArray.get(1).open)));
				            }
				            else if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 3.0) < 0.13){
					               System.out.println("3:1 split on " + (data.date + "     " + formatter.format(dataArray.get(0).close) + " -->  " + formatter.format(dataArray.get(1).open)));

				            }
				            else if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 1.5) < 0.13){
					               System.out.println("3:2 split on " + (data.date + "     " + formatter.format(dataArray.get(0).close) + " -->  " + formatter.format(dataArray.get(1).open)));
					             
				            }
				            else{  
				            	//dataArray.add(0,data);
				            }
				            
					  }
					
						
						
						
						if (!rs.next())
							done = true;
					}
					System.out.println("\nData tuples stored: " + dataArray.size());
					for (int i = 0; i < dataArray.size(); i++) {
						//System.out.println(String.format("     %s, %7.2f, %7.2f", dataArray.get(i).date, dataArray.get(i).open, dataArray.get(i).close));
					}
						System.out.println(dataArray.size());
					
				}
			}
		}
			catch (SQLException ex) {
				System.out.println("SQL exception in processTicker");
			}
		
			
				
	}
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	private static void calculateSplits(String ticker) {
		int twoOneSplits = 0;
		int threeOneSplits = 0;
		int threeTwoSplits = 0;
		double splitFactor = 1.0;
		NumberFormat formatter = new DecimalFormat ("#0.00");
		 class TickerData {
				public String date;
				public double open;
				public double close;
			}
		
		ArrayList<TickerData> dataArray = new ArrayList<TickerData>();
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from company where ticker = " + quote(ticker));
			if (!rs.next()){
				System.out.println("Ticker " + ticker + " not found in database");
			}	
			else {
				rs = stat.executeQuery("select * from pricevolume where ticker = " + quote(ticker) + " order by transDate DESC ");
				if (!rs.next()) {
					System.out.println("\nNo data for ticker " + ticker);
				}	
				else {					
					boolean done = false;
					while (!done) {
						TickerData data = new TickerData();
						data.date = rs.getString(2).trim();
						//System.out.print(data.date);
						data.open = Double.parseDouble(rs.getString(3).trim())/splitFactor;
						data.close = Double.parseDouble(rs.getString(6))/splitFactor;
						dataArray.add(0,data);
						if(dataArray.size()>1){
				            if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 2.0) < 0.13){
				               //System.out.println("2:1 split on " + (data.date + "     " + formatter.format(dataArray.get(0).close) + " -->  " + formatter.format(data.open)));
				               switch(twoOneSplits){
				               		case 0: splitFactor = 2.0;
				               			break;
				               		case 1: splitFactor = 4.0;
			               				break;
				               		case 2: splitFactor = 8.0;
			               				break;
				               		case 3: splitFactor = 16.0;
			               				break;
				               		case 4: splitFactor = 32.0;
			               				break;
				               		case 5: splitFactor = 64.0;
			               				break;
				               		case 6: splitFactor = 128.0;
			               				break;
				               }
				               dataArray.remove(0);
				               data.open = data.open/splitFactor;
				               data.close = data.close/splitFactor;
				               dataArray.add(0,data);
				               twoOneSplits++;
				            }
				            else if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 3.0) < 0.13){
					               //System.out.println("3:1 split on " + (data.date + "     " + formatter.format(dataArray.get(0).close) + " -->  " + formatter.format(dataArray.get(1).open)));
					               switch(threeOneSplits){
				               		case 0: splitFactor = 3.0;
				               			break;
				               		case 1: splitFactor = 6.0;
			               				break;
				               		case 2: splitFactor = 12.0;
			               				break;
				               		case 3: splitFactor = 24.0;
			               				break;
				               		case 4: splitFactor = 48.0;
			               				break;
				               		case 5: splitFactor = 96.0;
			               				break;
				               		case 6: splitFactor = 192.0;
			               				break;
				               }
					           dataArray.remove(0);
					           data.open = data.open/splitFactor;
					           data.close = data.close/splitFactor;
					           dataArray.add(0,data);    
				               threeOneSplits++;
				            }
				            else if(Math.abs(dataArray.get(0).close/dataArray.get(1).open - 1.5) < 0.13){
					               //System.out.println("3:2 split on " + (data.date + "     " + formatter.format(dataArray.get(0).close) + " -->  " + formatter.format(data.open)));
					               switch(threeTwoSplits){
				               		case 0: splitFactor = 1.5;
				               			break;
				               		case 1: splitFactor = 3.0;
			               				break;
				               		case 2: splitFactor = 6.0;
			               				break;
				               		case 3: splitFactor = 12.0;
			               				break;
				               		case 4: splitFactor = 24.0;
			               				break;
				               		case 5: splitFactor = 48.0;
			               				break;
				               		case 6: splitFactor = 96.0;
			               				break;
				               }
					           dataArray.remove(0);   
					           data.open = data.open/splitFactor;
					           data.close = data.close/splitFactor;
					           dataArray.add(0,data);
					           threeTwoSplits++;
				            }
				            else{  
				            	//dataArray.add(0,data);
				            }
				            
					  }
					//dataArray.add(0,data);	
						
						
						
						if (!rs.next())
							done = true;
					}
					System.out.println("\nData tuples stored: " + dataArray.size());
					for (int i = 0; i < dataArray.size(); i++) {
						System.out.println(String.format("     %s, %7.2f, %7.2f", dataArray.get(i).date, dataArray.get(i).open, dataArray.get(i).close));
					}
						System.out.println(dataArray.size());
					
				}
			}
		}
			catch (SQLException ex) {
				System.out.println("SQL exception in processTicker");
			}
		
			
				
	}
		
			
	
	private static String quote(String str) {
		return "'" + str + "'";
	}
	
}


