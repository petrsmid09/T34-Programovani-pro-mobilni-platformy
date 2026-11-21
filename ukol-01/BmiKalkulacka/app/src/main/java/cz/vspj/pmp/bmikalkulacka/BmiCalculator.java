package cz.vspj.pmp.bmikalkulacka;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BMI calculation and input validation. Pure Java without Android dependencies,
 * so it can be covered by local unit tests.
 */
public final class BmiCalculator {

    /** Smallest accepted height in centimetres. */
    public static final int MIN_HEIGHT_CM = 50;
    /** Largest accepted height in centimetres. */
    public static final int MAX_HEIGHT_CM = 250;
    /** Smallest accepted weight in kilograms. */
    public static final int MIN_WEIGHT_KG = 10;
    /** Largest accepted weight in kilograms. */
    public static final int MAX_WEIGHT_KG = 350;

    private BmiCalculator() {
        // Utility class, no instances.
    }

    /** Returns {@code true} if the height (cm) is a number in the accepted range. NaN is rejected. */
    public static boolean isHeightValid(double heightCm) {
        return heightCm >= MIN_HEIGHT_CM && heightCm <= MAX_HEIGHT_CM;
    }

    /** Returns {@code true} if the weight (kg) is a number in the accepted range. NaN is rejected. */
    public static boolean isWeightValid(double weightKg) {
        return weightKg >= MIN_WEIGHT_KG && weightKg <= MAX_WEIGHT_KG;
    }

    /**
     * Calculates the body mass index: BMI = weight [kg] / (height [m])².
     *
     * @param heightCm height in centimetres
     * @param weightKg weight in kilograms
     * @return BMI value (not rounded)
     * @throws IllegalArgumentException if the height or weight is outside the accepted range
     */
    public static double calculate(double heightCm, double weightKg) {
        if (!isHeightValid(heightCm)) {
            throw new IllegalArgumentException("Height out of range: " + heightCm);
        }
        if (!isWeightValid(weightKg)) {
            throw new IllegalArgumentException("Weight out of range: " + weightKg);
        }
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /**
     * Rounds the BMI to one decimal place (half up). The rounded value is both displayed
     * and used for the category, so the shown number and the category always agree
     * (e.g. 24.96 is shown as 25.0 and classified as overweight).
     * BigDecimal is used so that values such as 18.45 round to 18.5 despite binary
     * floating-point representation.
     */
    public static double roundToOneDecimal(double bmi) {
        return BigDecimal.valueOf(bmi).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Normalizes user input before {@link Double#parseDouble(String)}: trims whitespace and
     * accepts the Czech decimal comma ("72,5" → "72.5").
     */
    public static String normalizeNumber(String text) {
        return text.trim().replace(',', '.');
    }
}
