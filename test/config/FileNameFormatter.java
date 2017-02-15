package test.config;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FileNameFormatter {
	private DecimalFormat decimalFormat;

	public FileNameFormatter() {
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('-');
		this.decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
	}

	public String formatDoubleForFileName(double d) {
		return this.decimalFormat.format(d);
	}

}
