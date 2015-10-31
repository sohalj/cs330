package appLogic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Driver {
	public static Connection connSource, connDest;
	
	public static void main(String[] args) {
		try {
			String[] connString = MagicStrings.getStockDBStrings();
			connSource = DriverManager.getConnection(connString[0].trim(), connString[1].trim(), connString[2].trim());
			connString = MagicStrings.getNewDBStrings();
			connDest = DriverManager.getConnection(connString[0].trim(), connString[1].trim(), connString[2].trim());
			System.out.println("Database connections established");
			
			Statement st = connDest.createStatement();
			try {
				st.executeUpdate("drop table splits");
			}
			catch(SQLException ex) {
				
			}
			System.out.println("Destination database cleared of tables");

			Statement stat = connDest.createStatement();
			stat.executeUpdate("create table splits (Ticker char(6), Date char(10), SplitRatio char(6), PrevClose char(15), NextOpen char(15))");
			System.out.println("Destination table ready");
			
			PreparedStatement companyStat = connSource.prepareStatement("select * from company where ticker = ?");
			Scanner keyboard = new Scanner(System.in);
			System.out.print("Enter a ticker symbol: ");
			String ticker = keyboard.nextLine().trim();
			while (!ticker.matches("")) {
				companyStat.setString(1, ticker);
				ResultSet companyName = companyStat.executeQuery();
				if (!companyName.next()) 
					System.out.println(ticker + " not found in database");
				else {
					System.out.println("Adjusting data for splits");
					recordSplits(ticker); 
					System.out.println("splits for " + ticker + " recorded");
					System.out.println("Readout of recorded splits is");
					recoverSplits(ticker);
				
				}
				System.out.print("\nEnter a ticker symbol: ");
				ticker = keyboard.nextLine().trim();
			}
			
			connSource.close(); 
			connDest.close();
			System.out.println("Database connections closed");
			keyboard.close();
		}
		catch (SQLException ex) {
			System.out.println("SQL exception");
			ex.printStackTrace();
			return;
		}
	}
	
	private static void recordSplits(String ticker) { // checks for 2:1, 3:1, and 3:2 splits
		TickerData data;
		try {
			PreparedStatement priceStat = connSource.prepareStatement("select * from pricevolume where ticker = ? order by TransDate DESC");
			PreparedStatement splitStat = connDest.prepareStatement("insert into splits values (?, ?, ?, ?, ?)");
			splitStat.setString(1, ticker);
			priceStat.setString(1, ticker);
			ResultSet rs = priceStat.executeQuery();
			if (!rs.next())
				return;
			else {
				data = extractData(rs);
			}
			double epsilon = 0.13; // ratio must be this close to 2, 3, or 1.5 to signal a split
			double nextOpen = data.price[0];
			while (rs.next()) {
				data = extractData(rs);
				double open = nextOpen;
				double close = data.price[3];
				double ratio = close/open;
				nextOpen = data.price[0];
				String splitRatio = "";
				if (Math.abs(ratio - 2.0) < epsilon)
					splitRatio = "2:1 ";
				else if (Math.abs(ratio - 3.0) < epsilon) 
					splitRatio = "3:1 ";
				else if (Math.abs(ratio - 1.5) < epsilon)
					splitRatio = "3:2 ";
				if (splitRatio.length() > 0) {
					System.out.print(splitRatio + "split on " + data.date);
					System.out.print("; " + String.format("%7.2f", close));
					System.out.println(" --> " + String.format("%7.2f", open));
					splitStat.setString(2, data.date);
					splitStat.setString(3, splitRatio);
					splitStat.setString(4, String.format("%12.2f", close).trim());
					splitStat.setString(5, String.format("%12.2f", open).trim());
					splitStat.execute();
				}
			}
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in recordSplits");
		}
	}
	
	private static void recoverSplits(String ticker) {
		try {
			PreparedStatement checkStat = connDest.prepareStatement("select * from splits where ticker = ?");
			checkStat.setString(1, ticker);
			ResultSet rs = checkStat.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(3).trim() + " split on " + rs.getString(2).trim() + ";  " + rs.getString(4).trim() + " --> " + rs.getString(5).trim());
			}
			System.out.println();
		}
		catch(SQLException ex) {
			System.out.println("SQL exception in recoverSplits");
		}
	}
	

	
	private static TickerData extractData(ResultSet rs) {
		TickerData result = new TickerData();
		try {
			result.date = rs.getString(2).trim();
			for (int i = 0; i < 4; i++)
				result.price[i] = Double.parseDouble(rs.getString(i + 3).trim());
			result.volume = Long.parseLong(rs.getString(7).trim());
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in extractStrings");
		}
		return result;
	}

}
