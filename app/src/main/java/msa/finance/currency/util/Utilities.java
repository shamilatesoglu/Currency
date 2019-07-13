package msa.finance.currency.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Utilities {
    public static String round(double value, int precision) {
        StringBuilder format = new StringBuilder("#.");
        while (precision-- > 0) {
            format.append("#");
        }
        DecimalFormat decimalFormat = new DecimalFormat(format.toString());
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        return decimalFormat.format(value);
    }
}
