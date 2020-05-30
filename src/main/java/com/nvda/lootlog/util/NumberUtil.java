package com.nvda.lootlog.util;

import java.text.DecimalFormat;

public class NumberUtil {

  private static final DecimalFormat decimalFormat = new DecimalFormat("+#;-#");
  private static final DecimalFormat commaFormat = new DecimalFormat("+#,###;-#,###");


  public static String formatNumberShort(long number) {
    if (Math.abs(number) >= 1e9) return decimalFormat.format(Math.round(number / 1e9)) + "B";
    else if (Math.abs(number) >= 1e6) return decimalFormat.format(Math.round(number / 1e6)) + "M";
    else if (Math.abs(number) >= 1e3) return decimalFormat.format(Math.round(number / 1e3)) + "K";

    return String.valueOf(number);
  }

  public static String formatNumberCommas(long number) {
    return commaFormat.format(number);
  }
}
