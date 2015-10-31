package appLogic;

public class MagicStrings {
	public static String[] getStockDBStrings() {
		String[] retval = new String[3];
		retval[0] = "jdbc:mysql://db.cs.wwu.edu/CS330_201410";
		retval[1] = "sohalj_reader";
		retval[2] = "vKGyqRaD4";
		return retval;
	}
	
	public static String[] getNewDBStrings() {
		String[] retval = new String[3];
		retval[0] = "jdbc:mysql://db.cs.wwu.edu/sohalj_CS330";
		retval[1] = "sohalj_writer";
		retval[2] = "4Mttdkh9";
		return retval;
	}
}
