package base;

public class TradingDayInfo {
	private String ticker;          
    private String date;
    private double openingPrice;
    private double highPrice;
    private double lowPrice;
    private double closingPrice;
        

    // CONSTRUCTOR

    // TradingDayInfo
    // Preconditions:
    //     - None
    // Post-conditions
    //     - The object's fields are set to the provided values
    public TradingDayInfo(String ticker, String date, double openingPrice, double highPrice, double lowPrice, double closingPrice) {
        this.ticker = ticker;
        this.date = date;
        this.openingPrice = openingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closingPrice = closingPrice;
        
      
        return;
    }

    // ACCESSORS

    // getName
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the name
    public String getTicker() {
        return this.ticker;
    }
    
    // getDate
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the date
    public String getDate() {
        return this.date;
    }
    
    // getOpeningPrice
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the opening price
    public double getOpeningPrice() {
        return this.openingPrice;
    }
    
    // getHighPrice
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the high price
    public double getHighPrice() {
        return this.highPrice;
    }

    // getLowPrice
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the low price
    public double getLowPrice() {
        return this.lowPrice;
    }
    
    // getClosingPrice
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the closing price
    public double getClosingPrice() {
        return this.closingPrice;
    }

}
