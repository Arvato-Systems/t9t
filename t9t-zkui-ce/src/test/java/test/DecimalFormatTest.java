/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class DecimalFormatTest {


    public static void main(String[] args) {

        formatDecimals();
        formatDecimalsWithScale();

    }

    public static void formatDecimalsWithScale() {
        String pattern;
        Locale locale = new Locale("en", "US");
        //        Locale locale = new Locale("de", "DE");
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);

        System.out.println("*************************************************");
        System.out.println(new BigDecimal("123135.3456").setScale(4, RoundingMode.HALF_UP));
        System.out.println(new BigDecimal("12335.3456").setScale(2, RoundingMode.HALF_UP));
        System.out.println(new BigDecimal("12335.3456").setScale(6, RoundingMode.HALF_UP));

        BigDecimal bigDecimal = new BigDecimal("123123.3456").setScale(0, RoundingMode.HALF_UP);
        pattern = "###,###";
        df.applyPattern(pattern);
        df.setMinimumFractionDigits(bigDecimal.scale());
        heading(pattern, df);
        format(df, bigDecimal);
    }

    public static DecimalFormat formatDecimals() {
        String pattern = "";
        Locale locale = new Locale("en", "US");
        //        Locale locale = new Locale("de", "DE");
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);

        System.out.println("Using locale: " + locale);


        System.out.println("\n'min' case");
        pattern = "###,###.00";
        df.applyPattern(pattern);
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(6);
        heading(pattern, df);
        test(df);

        pattern = "######";
        df.applyPattern(pattern);
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(6);
        heading(pattern, df);
        test(df);


        System.out.println("\n'max' case");
        pattern = "###,###.00";
        df.applyPattern(pattern);
        df.setMinimumFractionDigits(6);
        heading(pattern, df);
        test(df);

        pattern = "######";
        df.applyPattern(pattern);
        df.setMinimumFractionDigits(6);
        heading(pattern, df);
        test(df);


        System.out.println("\n'currency' case : KWD");
        pattern = "###,###";
        df.applyPattern(pattern);
        df.setCurrency(Currency.getInstance("KWD"));
        df.setMinimumFractionDigits(df.getCurrency().getDefaultFractionDigits());
        heading(pattern, df);
        test(df);


        System.out.println("\n'currency' case : USD");
        pattern = "###,###";
        df.applyPattern(pattern);
        df.setCurrency(Currency.getInstance("USD"));
        df.setMinimumFractionDigits(df.getCurrency().getDefaultFractionDigits());
        heading(pattern, df);
        test(df);
        return df;
    }

    public static void test(DecimalFormat df) {
        format(df, "123123         ");
        format(df, "123123.1       ");
        format(df, "123123.1000    ");
        format(df, "123123.109     ");
        format(df, "123123.129123  ");
        format(df, "123123.1239999 ");
    }

    public static void heading(String pattern, DecimalFormat df) {
        System.out.println(String.format("    MinimumFractionDigits:%s - MaximumFractionDigits:%s >> applyedPattern:%s", df.getMinimumFractionDigits(),
                df.getMaximumFractionDigits(), pattern));
    }

    private static void format(DecimalFormat df, String number) {
        System.out.println(String.format("    >> %s -> %s", number, df.format(new BigDecimal(number.trim()))));
    }

    private static void format(DecimalFormat df, BigDecimal number) {
        System.out.println(String.format("    >> %s -> %s", number, df.format(number)));
    }
}
