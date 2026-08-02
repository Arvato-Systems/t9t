package com.arvatosystems.t9t.core.be.impl;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import jakarta.annotation.Nonnull;

import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.core.services.IIbanChecker;

@Singleton
public class IbanChecker implements IIbanChecker {
    private static final ConcurrentHashMap<String, IbanSpec> IBAN_SPECS = new ConcurrentHashMap<>(128);
    private static void add(@Nonnull final IbanSpec ibanSpec) {
        IBAN_SPECS.put(ibanSpec.countryCode(), ibanSpec);
    }
    static {
        add(new IbanSpec("AD", "Andorra", 24, 4, 4, 12, Pattern.compile("^AD[0-9]{2}[0-9]{4}[0-9]{4}[A-Z0-9]{12}$")));
        add(new IbanSpec("AE", "United Arab Emirates (The)", 23, 3, 0, 16, Pattern.compile("^AE[0-9]{2}[0-9]{3}[0-9]{16}$")));
        add(new IbanSpec("AL", "Albania", 28, 3, 5, 16, Pattern.compile("^AL[0-9]{2}[0-9]{8}[A-Z0-9]{16}$")));
        add(new IbanSpec("AT", "Austria", 20, 5, 0, 11, Pattern.compile("^AT[0-9]{2}[0-9]{5}[0-9]{11}$")));
        add(new IbanSpec("AZ", "Azerbaijan", 28, 4, 0, 20, Pattern.compile("^AZ[0-9]{2}[A-Z]{4}[A-Z0-9]{20}$")));
        add(new IbanSpec("BA", "Bosnia and Herzegovina", 20, 3, 3, 10, Pattern.compile("^BA[0-9]{2}[0-9]{3}[0-9]{3}[0-9]{8}[0-9]{2}$")));
        add(new IbanSpec("BE", "Belgium", 16, 3, 0, 9, Pattern.compile("^BE[0-9]{2}[0-9]{3}[0-9]{7}[0-9]{2}$")));
        add(new IbanSpec("BG", "Bulgaria", 22, 4, 4, 10, Pattern.compile("^BG[0-9]{2}[A-Z]{4}[0-9]{4}[0-9]{2}[A-Z0-9]{8}$")));
        add(new IbanSpec("BH", "Bahrain", 22, 4, 0, 14, Pattern.compile("^BH[0-9]{2}[A-Z]{4}[A-Z0-9]{14}$")));
        add(new IbanSpec("BI", "Burundi", 27, 5, 5, 13, Pattern.compile("^BI[0-9]{2}[0-9]{5}[0-9]{5}[0-9]{11}[0-9]{2}$")));
        add(new IbanSpec("BR", "Brazil", 29, 8, 5, 12, Pattern.compile("^BR[0-9]{2}[0-9]{8}[0-9]{5}[0-9]{10}[A-Z]{1}[A-Z0-9]{1}$")));
        add(new IbanSpec("BY", "Belarus", 28, 4, 0, 20, Pattern.compile("^BY[0-9]{2}[A-Z0-9]{4}[0-9]{4}[A-Z0-9]{16}$")));
        add(new IbanSpec("CH", "Switzerland", 21, 5, 0, 12, Pattern.compile("^CH[0-9]{2}[0-9]{5}[A-Z0-9]{12}$")));
        add(new IbanSpec("CR", "Costa Rica", 22, 4, 0, 14, Pattern.compile("^CR[0-9]{2}[0-9]{4}[0-9]{14}$")));
        add(new IbanSpec("CY", "Cyprus", 28, 3, 5, 16, Pattern.compile("^CY[0-9]{2}[0-9]{3}[0-9]{5}[A-Z0-9]{16}$")));
        add(new IbanSpec("CZ", "Czechia", 24, 4, 0, 16, Pattern.compile("^CZ[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{10}$")));
        add(new IbanSpec("DE", "Germany", 22, 8, 0, 10, Pattern.compile("^DE[0-9]{2}[0-9]{8}[0-9]{10}$")));
        add(new IbanSpec("DJ", "Djibouti", 27, 5, 5, 13, Pattern.compile("^DJ[0-9]{2}[0-9]{5}[0-9]{5}[0-9]{11}[0-9]{2}$")));
        add(new IbanSpec("DK", "Denmark", 18, 4, 0, 10, Pattern.compile("^DK[0-9]{2}[0-9]{4}[0-9]{9}[0-9]{1}$")));
        add(new IbanSpec("DO", "Dominican Republic", 28, 4, 0, 20, Pattern.compile("^DO[0-9]{2}[A-Z0-9]{4}[0-9]{20}$")));
        add(new IbanSpec("EE", "Estonia", 20, 2, 0, 14, Pattern.compile("^EE[0-9]{2}[0-9]{2}[0-9]{14}$")));
        add(new IbanSpec("EG", "Egypt", 29, 4, 4, 17, Pattern.compile("^EG[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{17}$")));
        add(new IbanSpec("ES", "Spain", 24, 4, 4, 12, Pattern.compile("^ES[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{1}[0-9]{1}[0-9]{10}$")));
        add(new IbanSpec("FI", "Finland", 18, 3, 0, 11, Pattern.compile("^FI[0-9]{2}[0-9]{3}[0-9]{11}$")));
        add(new IbanSpec("FK", "Falkland Islands (Malvinas)", 18, 2, 0, 12, Pattern.compile("^FK[0-9]{2}[A-Z]{2}[0-9]{12}$")));
        add(new IbanSpec("FO", "Faroe Islands", 18, 4, 0, 10, Pattern.compile("^FO[0-9]{2}[0-9]{4}[0-9]{9}[0-9]{1}$")));
        add(new IbanSpec("FR", "France", 27, 5, 0, 18, Pattern.compile("^FR[0-9]{2}[0-9]{5}[0-9]{5}[A-Z0-9]{11}[0-9]{2}$")));
        add(new IbanSpec("GB", "United Kingdom", 22, 4, 6, 8, Pattern.compile("^GB[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}$")));
        add(new IbanSpec("GE", "Georgia", 22, 2, 0, 16, Pattern.compile("^GE[0-9]{2}[A-Z]{2}[0-9]{16}$")));
        add(new IbanSpec("GI", "Gibraltar", 23, 4, 0, 15, Pattern.compile("^GI[0-9]{2}[A-Z]{4}[A-Z0-9]{15}$")));
        add(new IbanSpec("GL", "Greenland", 18, 4, 0, 10, Pattern.compile("^GL[0-9]{2}[0-9]{4}[0-9]{9}[0-9]{1}$")));
        add(new IbanSpec("GR", "Greece", 27, 3, 4, 16, Pattern.compile("^GR[0-9]{2}[0-9]{3}[0-9]{4}[A-Z0-9]{16}$")));
        add(new IbanSpec("GT", "Guatemala", 28, 4, 0, 20, Pattern.compile("^GT[0-9]{2}[A-Z0-9]{4}[A-Z0-9]{20}$")));
        add(new IbanSpec("HN", "Honduras", 28, 4, 0, 20, Pattern.compile("^HN[0-9]{2}[A-Z]{4}[0-9]{20}$")));
        add(new IbanSpec("HR", "Croatia", 21, 7, 0, 10, Pattern.compile("^HR[0-9]{2}[0-9]{7}[0-9]{10}$")));
        add(new IbanSpec("HU", "Hungary", 28, 3, 4, 17, Pattern.compile("^HU[0-9]{2}[0-9]{3}[0-9]{4}[0-9]{1}[0-9]{15}[0-9]{1}$")));
        add(new IbanSpec("IE", "Ireland", 22, 4, 6, 8, Pattern.compile("^IE[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}$")));
        add(new IbanSpec("IL", "Israel", 23, 3, 3, 13, Pattern.compile("^IL[0-9]{2}[0-9]{3}[0-9]{3}[0-9]{13}$")));
        add(new IbanSpec("IQ", "Iraq", 23, 4, 3, 12, Pattern.compile("^IQ[0-9]{2}[A-Z]{4}[0-9]{3}[0-9]{12}$")));
        add(new IbanSpec("IS", "Iceland", 26, 2, 2, 18, Pattern.compile("^IS[0-9]{2}[0-9]{4}[0-9]{2}[0-9]{6}[0-9]{10}$")));
        add(new IbanSpec("IT", "Italy", 27, 5, 5, 13, Pattern.compile("^IT[0-9]{2}[A-Z]{1}[0-9]{5}[0-9]{5}[A-Z0-9]{12}$")));
        add(new IbanSpec("JO", "Jordan", 30, 4, 4, 18, Pattern.compile("^JO[0-9]{2}[A-Z]{4}[0-9]{4}[A-Z0-9]{18}$")));
        add(new IbanSpec("KW", "Kuwait", 30, 4, 0, 22, Pattern.compile("^KW[0-9]{2}[A-Z]{4}[A-Z0-9]{22}$")));
        add(new IbanSpec("KZ", "Kazakhstan", 20, 3, 0, 13, Pattern.compile("^KZ[0-9]{2}[0-9]{3}[A-Z0-9]{13}$")));
        add(new IbanSpec("LB", "Lebanon", 28, 4, 0, 20, Pattern.compile("^LB[0-9]{2}[0-9]{4}[A-Z0-9]{20}$")));
        add(new IbanSpec("LC", "Saint Lucia", 32, 4, 0, 24, Pattern.compile("^LC[0-9]{2}[A-Z]{4}[A-Z0-9]{24}$")));
        add(new IbanSpec("LI", "Liechtenstein", 21, 5, 0, 12, Pattern.compile("^LI[0-9]{2}[0-9]{5}[A-Z0-9]{12}$")));
        add(new IbanSpec("LT", "Lithuania", 20, 5, 0, 11, Pattern.compile("^LT[0-9]{2}[0-9]{5}[0-9]{11}$")));
        add(new IbanSpec("LU", "Luxembourg", 20, 3, 0, 13, Pattern.compile("^LU[0-9]{2}[0-9]{3}[A-Z0-9]{13}$")));
        add(new IbanSpec("LV", "Latvia", 21, 4, 0, 13, Pattern.compile("^LV[0-9]{2}[A-Z]{4}[A-Z0-9]{13}$")));
        add(new IbanSpec("LY", "Libya", 25, 3, 3, 15, Pattern.compile("^LY[0-9]{2}[0-9]{3}[0-9]{3}[0-9]{15}$")));
        add(new IbanSpec("MC", "Monaco", 27, 5, 5, 13, Pattern.compile("^MC[0-9]{2}[0-9]{5}[0-9]{5}[A-Z0-9]{11}[0-9]{2}$")));
        add(new IbanSpec("MD", "Moldova, Republic of", 24, 2, 0, 18, Pattern.compile("^MD[0-9]{2}[A-Z0-9]{2}[A-Z0-9]{18}$")));
        add(new IbanSpec("ME", "Montenegro", 22, 3, 0, 15, Pattern.compile("^ME[0-9]{2}[0-9]{3}[0-9]{13}[0-9]{2}$")));
        add(new IbanSpec("MK", "North Macedonia", 19, 3, 0, 12, Pattern.compile("^MK[0-9]{2}[0-9]{3}[A-Z0-9]{10}[0-9]{2}$")));
        add(new IbanSpec("MN", "Mongolia", 20, 4, 0, 12, Pattern.compile("^MN[0-9]{2}[0-9]{4}[0-9]{12}$")));
        add(new IbanSpec("MR", "Mauritania", 27, 5, 5, 13, Pattern.compile("^MR[0-9]{2}[0-9]{5}[0-9]{5}[0-9]{11}[0-9]{2}$")));
        add(new IbanSpec("MT", "Malta", 31, 4, 5, 18, Pattern.compile("^MT[0-9]{2}[A-Z]{4}[0-9]{5}[A-Z0-9]{18}$")));
        add(new IbanSpec("MU", "Mauritius", 30, 6, 2, 18, Pattern.compile("^MU[0-9]{2}[A-Z]{4}[0-9]{2}[0-9]{2}[0-9]{12}[0-9]{3}[A-Z]{3}$")));
        add(new IbanSpec("NI", "Nicaragua", 28, 4, 0, 20, Pattern.compile("^NI[0-9]{2}[A-Z]{4}[0-9]{20}$")));
        add(new IbanSpec("NL", "Netherlands (The)", 18, 4, 0, 10, Pattern.compile("^NL[0-9]{2}[A-Z]{4}[0-9]{10}$")));
        add(new IbanSpec("NO", "Norway", 15, 4, 0, 7, Pattern.compile("^NO[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{1}$")));
        add(new IbanSpec("OM", "Oman", 23, 3, 0, 16, Pattern.compile("^OM[0-9]{2}[0-9]{3}[A-Z0-9]{16}$")));
        add(new IbanSpec("PK", "Pakistan", 24, 4, 0, 16, Pattern.compile("^PK[0-9]{2}[A-Z]{4}[A-Z0-9]{16}$")));
        add(new IbanSpec("PL", "Poland", 28, 8, 0, 16, Pattern.compile("^PL[0-9]{2}[0-9]{8}[0-9]{16}$")));
        add(new IbanSpec("PS", "Palestine, State of", 29, 4, 0, 21, Pattern.compile("^PS[0-9]{2}[A-Z]{4}[A-Z0-9]{21}$")));
        add(new IbanSpec("PT", "Portugal", 25, 4, 0, 17, Pattern.compile("^PT[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{11}[0-9]{2}$")));
        add(new IbanSpec("QA", "Qatar", 29, 4, 0, 21, Pattern.compile("^QA[0-9]{2}[A-Z]{4}[A-Z0-9]{21}$")));
        add(new IbanSpec("RO", "Romania", 24, 4, 0, 16, Pattern.compile("^RO[0-9]{2}[A-Z]{4}[A-Z0-9]{16}$")));
        add(new IbanSpec("RS", "Serbia", 22, 3, 0, 15, Pattern.compile("^RS[0-9]{2}[0-9]{3}[0-9]{13}[0-9]{2}$")));
        add(new IbanSpec("RU", "Russian Federation", 33, 9, 5, 15, Pattern.compile("^RU[0-9]{2}[0-9]{9}[0-9]{5}[A-Z0-9]{15}$")));
        add(new IbanSpec("SA", "Saudi Arabia", 24, 2, 0, 18, Pattern.compile("^SA[0-9]{2}[0-9]{2}[A-Z0-9]{18}$")));
        add(new IbanSpec("SC", "Seychelles", 31, 6, 2, 19, Pattern.compile("^SC[0-9]{2}[A-Z]{4}[0-9]{2}[0-9]{2}[0-9]{16}[A-Z]{3}$")));
        add(new IbanSpec("SD", "Sudan", 18, 2, 0, 12, Pattern.compile("^SD[0-9]{2}[0-9]{2}[0-9]{12}$")));
        add(new IbanSpec("SE", "Sweden", 24, 3, 0, 17, Pattern.compile("^SE[0-9]{2}[0-9]{3}[0-9]{16}[0-9]{1}$")));
        add(new IbanSpec("SI", "Slovenia", 19, 5, 0, 10, Pattern.compile("^SI[0-9]{2}[0-9]{5}[0-9]{8}[0-9]{2}$")));
        add(new IbanSpec("SK", "Slovakia", 24, 4, 0, 16, Pattern.compile("^SK[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{10}$")));
        add(new IbanSpec("SM", "San Marino", 27, 5, 5, 13, Pattern.compile("^SM[0-9]{2}[A-Z]{1}[0-9]{5}[0-9]{5}[A-Z0-9]{12}$")));
        add(new IbanSpec("SO", "Somalia", 23, 4, 3, 12, Pattern.compile("^SO[0-9]{2}[0-9]{4}[0-9]{3}[0-9]{12}$")));
        add(new IbanSpec("ST", "Sao Tome and Principe", 25, 4, 4, 13, Pattern.compile("^ST[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{11}[0-9]{2}$")));
        add(new IbanSpec("SV", "El Salvador", 28, 4, 0, 20, Pattern.compile("^SV[0-9]{2}[A-Z]{4}[0-9]{20}$")));
        add(new IbanSpec("TL", "Timor-Leste", 23, 3, 0, 16, Pattern.compile("^TL[0-9]{2}[0-9]{3}[0-9]{14}[0-9]{2}$")));
        add(new IbanSpec("TN", "Tunisia", 24, 2, 3, 15, Pattern.compile("^TN[0-9]{2}[0-9]{2}[0-9]{3}[0-9]{13}[0-9]{2}$")));
        add(new IbanSpec("TR", "Turkiye", 26, 5, 0, 17, Pattern.compile("^TR[0-9]{2}[0-9]{5}[0-9]{1}[A-Z0-9]{16}$")));
        add(new IbanSpec("UA", "Ukraine", 29, 6, 0, 19, Pattern.compile("^UA[0-9]{2}[0-9]{6}[A-Z0-9]{19}$")));
        add(new IbanSpec("VA", "Holy See", 22, 3, 0, 15, Pattern.compile("^VA[0-9]{2}[0-9]{3}[0-9]{15}$")));
        add(new IbanSpec("VG", "Virgin Islands (British)", 24, 4, 0, 16, Pattern.compile("^VG[0-9]{2}[A-Z]{4}[0-9]{16}$")));
        add(new IbanSpec("XK", "Kosovo", 20, 2, 2, 12, Pattern.compile("^XK[0-9]{2}[0-9]{4}[0-9]{10}[0-9]{2}$")));
        add(new IbanSpec("YE", "Yemen", 30, 4, 4, 18, Pattern.compile("^YE[0-9]{2}[A-Z]{4}[0-9]{4}[A-Z0-9]{18}$")));
    }

    @Override
    public void registerIbanSpec(final IbanSpec ibanSpec) {
        add(ibanSpec);
    }

    @Override
    public boolean isValidIban(final String iban) {
        if (iban.length() < 5) {
            return false;
        }
        final IbanSpec spec = IBAN_SPECS.get(iban.substring(0, 2));
        // check for existing country, correct length
        if (spec == null || iban.length() != spec.totalLength()) {
            return false;
        }
        // check country specific structure
        if (!spec.pattern().matcher(iban).matches()) {
            return false;
        }
        // check MOD-97 checksum
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder(rearranged.length() * 2);
        for (int i = 0; i < rearranged.length(); i++) {
            final char ch = rearranged.charAt(i);
            if (ch >= '0' && ch <= '9') {
                numeric.append(ch);
            } else if (ch >= 'A' && ch <= 'Z') {
                numeric.append(ch - 'A' + 10);
            } else {
                return false;
            }
        }
        return new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    @Override
    public boolean isValidBankCodeAndAccount(final String countryCode, final String bankCode, final String accountNumber) {
        final IbanSpec spec = IBAN_SPECS.get(countryCode.substring(0, 2));
        // check for existing country, correct length
        if (spec == null) {
            return true; // no spec, cannot check, but also cannot say it is invalid
        }
        if (bankCode != null && bankCode.length() > spec.bankCodeLength() + spec.secondaryBankCodeLength()) {
            return false;
        }
        if (accountNumber != null && accountNumber.length() > spec.accountNumberLength()) {
            return false;
        }
        return true;
    }
}
