package base;

//J.P. Sohal
//CS 330


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;






public class Assignment3 {
	public static Connection connSource, connDest;
	
	public static void main(String[] args) {
		IndustryData names;
		ArrayList<IndustryData> industryArray = new ArrayList<IndustryData>();
		
		try {
			TickerData range;
			ArrayList<TickerData> finalStocks;
			String[] connString = MagicStrings.getStockDBStrings();
			connSource = DriverManager.getConnection(connString[0].trim(), connString[1].trim(), connString[2].trim());
			connString = MagicStrings.getNewDBStrings();
			connDest = DriverManager.getConnection(connString[0].trim(), connString[1].trim(), connString[2].trim());
			System.out.println("Database connections established");
			
			Statement st = connDest.createStatement();
			try {
				st.executeUpdate("drop table performance");
			}
			catch(SQLException ex) {
				
			}
			System.out.println("Destination database cleared of tables");

			Statement stat = connDest.createStatement();
			System.out.println("Creating table");
			stat.executeUpdate("create table performance (Industry char(30), Ticker char(6), StartDate char(10), EndDate char(10), TickerReturn char(12), IndustryReturn char(12))");
			System.out.println("Destination table ready");
			
			PreparedStatement companyStat = connSource.prepareStatement("select Industry from company group by Industry order by Industry;");
			ResultSet rs = companyStat.executeQuery();
			if (!rs.next())
				System.out.println("no data");
			else {
				names = extractNames(rs);
				industryArray.add(names);
			}	
			while (rs.next()) {
				names = extractNames(rs);
				industryArray.add(names);
			}
			
			
			
			for(int c = 0; c < industryArray.size(); c++){
				String industry = industryArray.get(c).iName;
				range = calculateDateRange(industry); 
				finalStocks = adjustForDateRange(industry, range);
				fillTable(industry, finalStocks);
				System.out.println("Information for  " + industry + " industry recorded");	
			}
			
			connSource.close(); 
			connDest.close();
			System.out.println("Database connections closed");
		}
		catch (SQLException ex) {
			System.out.println("SQL exception");
			ex.printStackTrace();
			return;
		}
	}
	
	
	/*Pre: takes in the industry name for which you want to enter data into the table for
	 *
	 *Post: returns the date range based off the specifications given in the assignment , which is the max date for startDate and min for endDate
	 */
	
	
	private static TickerData calculateDateRange(String industry)  { 
		TickerData data;
		TickerData dates = new TickerData();
		ArrayList<TickerData> dataArray = new ArrayList<TickerData>();
		String min = null;
		String max = null;
		try {
			PreparedStatement priceStat = connSource.prepareStatement("select Ticker, min(TransDate), max(TransDate), count(distinct TransDate) as tradingDays from company natural join pricevolume where Industry = ? group by Ticker having tradingDays >= 150 order by Ticker");
			priceStat.setString(1, industry);
			ResultSet rs = priceStat.executeQuery();
			if (!rs.next())
				System.out.println("no data");
			else {
				data = extractData(rs);
				dataArray.add(data);
			}	
			while (rs.next()) {
				data = extractData(rs);
				dataArray.add(data);
			}

			min = dataArray.get(0).startDate;
			max = dataArray.get(0).endDate;
			for(int i=0; i < dataArray.size()-1; i++){
				if(min.compareTo(dataArray.get(i+1).startDate) < 0){
					min = dataArray.get(i+1).startDate;
				}
				if((max.compareTo(dataArray.get(i+1).endDate) > 0) || max.compareTo(dataArray.get(i+1).endDate) == 0){
					max = dataArray.get(i).endDate;
				}
			}

			dates.startDate = min;
			dates.endDate = max;
		
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in recordSplits");
		}
		return dates;
	}
	
	
	/* Pre: takes in the industry name for which you want to enter data into the table for and it takes the date range determined by previous function.
	 * 
	 * Post: returns Array of stocks in the industry that have been adjust for the pre determined range
	 */
	
	private static ArrayList<TickerData> adjustForDateRange(String industry, TickerData range) { 
		TickerData data = range;
		String start = data.startDate;
		String end = data.endDate;
		ArrayList<TickerData> dataArray = new ArrayList<TickerData>();
		try {
			PreparedStatement priceStat = connSource.prepareStatement("select Ticker, min(TransDate), max(TransDate), count(distinct TransDate) as tradingDays from company natural join pricevolume where Industry = ? and TransDate>= ? and TransDate <= ? group by Ticker having tradingDays >= 150 order by Ticker");
			priceStat.setString(1, industry);
			priceStat.setString(2, start);
			priceStat.setString(3, end);			
			ResultSet rs = priceStat.executeQuery();
			if (!rs.next())
				System.out.println("no Data");
			else {
				data = extractData(rs);
				dataArray.add(data);
			}	
			while (rs.next()) {
				data = extractData(rs);
				dataArray.add(data);
			}		
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in displaySplits");
		}
		return dataArray;
	}
	
	
	/*Pre: takes in the industry name for which you want to enter data into the table for
	 * 
	 * Post: calculates return for each company in industry as well as the return for all industrys excluding the one being compared to. Puts this in
	 * formation into the performance table
	 * 
	 */
	
	
	
	private static void fillTable(String industry, ArrayList<TickerData> finalStocks) { 
		ArrayList<TickerData> stocks = finalStocks;
		String start = stocks.get(0).startDate;
		String end = stocks.get(0).endDate;
		String ticker;
		double open =0.0;
		double close = 0.0;
		int j = 0;
		TickerPrice priceData;
		ArrayList<TickerPrice> priceArray = new ArrayList<TickerPrice>();
		ArrayList<ReturnValue> returnArray = new ArrayList<ReturnValue>();
		ArrayList<IntervalData> intervalArray = new ArrayList<IntervalData>();
		double iReturn = 0.0;

		try {
			PreparedStatement priceStat = connSource.prepareStatement("select P.ticker, P.TransDate, P.openPrice, P.closePrice from pricevolume P where Ticker = ? and TransDate >= ? and TransDate <= ?");
			PreparedStatement splitStat = connDest.prepareStatement("insert into performance values (?, ?, ?, ?, ?, ?)");
			priceStat.setString(1, stocks.get(0).ticker);
			priceStat.setString(2, start);
			priceStat.setString(3, end);
			ResultSet rs = priceStat.executeQuery();
			if (!rs.next())
				return;
			else {
				priceData = extractDataPrice(rs);
				priceArray.add(priceData);
			}	
			while (rs.next()) {
				priceData = extractDataPrice(rs);
				priceArray.add(priceData);
			}
			
			///Creating a array of all the interval dates based of alphabetically smallest ticker
			
			for(int i = 0; i <stocks.get(0).numDays/60; i++){
					IntervalData interval = new IntervalData();
					interval.open = priceArray.get(j).transDate;
					interval.close = priceArray.get(j+59).transDate;
					intervalArray.add(interval);
					j=j+60;
			}

		
			for(int m = 1; m < stocks.size(); m++){
				PreparedStatement priceStat1 = connSource.prepareStatement("select P.ticker,P.TransDate, P.openPrice, P.closePrice from pricevolume P where Ticker = ? and TransDate >= ? and TransDate <= ?");
				priceStat1.setString(1, stocks.get(m).ticker);
				priceStat1.setString(2, start);
				priceStat1.setString(3, end);
				ResultSet rs1 = priceStat1.executeQuery();
				if (!rs1.next())
					return;
				else {
					priceData = extractDataPrice(rs1);
					priceArray.add(priceData);
				}	
				while (rs1.next()) {
					priceData = extractDataPrice(rs1);
					priceArray.add(priceData);
				}
			}
			
			// Creates a array of the return values for all the stocks in the industry, as well as all the intervals
			for(int s = 0; s <stocks.size(); s++){
				String t = stocks.get(s).ticker;
				for(int k = 0; k <intervalArray.size(); k++){
					ReturnValue rv = new ReturnValue();
					for(int n = 0; n < priceArray.size(); n++){
						if(priceArray.get(n).ticker.equals(t)){
							if(priceArray.get(n).transDate.equals(intervalArray.get(k).open)){
								 open = priceArray.get(n).open;
							}
							if(priceArray.get(n).transDate.equals(intervalArray.get(k).close)){
								 close = priceArray.get(n).close;
							}
						}
					}
					rv.ticker = t;
					rv.iReturn = (close/open)-1;
					returnArray.add(rv);
				}
			}

			
			// loops through the intervals and calculates the industry return of all the stocks that are not being compared to
			// and uploads those values to performance table
			for(int n = 0; n <stocks.size(); n++){
				ticker = stocks.get(n).ticker;
				for(int g = 0; g<intervalArray.size(); g++){
					splitStat.setString(1, industry);
					splitStat.setString(2, ticker);
					splitStat.setString(3, intervalArray.get(g).open);
					splitStat.setString(4, intervalArray.get(g).close);
					splitStat.setString(5, String.format("%10.7f", returnArray.get((intervalArray.size()*n) + g).iReturn));
					for(int e = 0; e<stocks.size(); e++){
						if( e == n){
							continue;
						}
						else{
							iReturn = iReturn + returnArray.get((intervalArray.size()*e) + g).iReturn;
						}
					}
					iReturn = iReturn/(stocks.size()-1);
					splitStat.setString(6, String.format("%10.7f", iReturn));
					splitStat.execute();
					iReturn = 0.0;
				}
			}
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in displaySplits");
		}
	}
	

	


	
	/*Pre: takes in a query
	 * Post: extracts data from table created by query
	 * 
	 */
	
	
	private static TickerPrice extractDataPrice(ResultSet rs) {
		TickerPrice result = new TickerPrice();
		
		
		try {
			result.ticker = rs.getString(1).trim();
			result.transDate = rs.getString(2).trim();
			result.open =  Double.parseDouble(rs.getString(3).trim());
			result.close = Double.parseDouble(rs.getString(4).trim());
		
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in extractStrings");
		}
		return result;
	}


	/*Pre: takes in a query
	 * Post: extracts data from table created by query
	 * 
	 */
	
	
	private static IndustryData extractNames(ResultSet rs) {
		IndustryData result = new IndustryData();
		
		
		try {
			result.iName = rs.getString(1).trim();
		
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in extractStrings");
		}
		return result;
	}

	
	
	/*Pre: takes in a query
	 * Post: extracts data from table created by query
	 * 
	 */
	
	private static TickerData extractData(ResultSet rs) {
		TickerData result = new TickerData();
		try {
			result.ticker = rs.getString(1).trim();
			result.startDate = rs.getString(2).trim();
			result.endDate =  rs.getString(3).trim();
			result.numDays = Integer.parseInt(rs.getString(4).trim());
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in extractStrings");
		}
		return result;
	}

}
