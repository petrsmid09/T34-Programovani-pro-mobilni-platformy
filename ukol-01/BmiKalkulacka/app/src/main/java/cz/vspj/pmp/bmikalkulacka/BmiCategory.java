package cz.vspj.pmp.bmikalkulacka;

/**
 * BMI categories for adults according to the WHO classification.
 *
 * <p>The order of the constants matches the order of the items in the {@code bmi_category_*}
 * resource arrays (res/values/arrays.xml), so {@link #ordinal()} is the index into those arrays.
 */
public enum BmiCategory {
    UNDERWEIGHT(Double.NEGATIVE_INFINITY, 18.5),
    NORMAL(18.5, 25.0),
    OVERWEIGHT(25.0, 30.0),
    OBESITY_I(30.0, 35.0),
    OBESITY_II(35.0, 40.0),
    OBESITY_III(40.0, Double.POSITIVE_INFINITY);

    /** Lower bound of the category (inclusive). */
    private final double min;
    /** Upper bound of the category (exclusive). */
    private final double max;

    BmiCategory(double min, double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Returns the category the given BMI value belongs to.
     *
     * @param bmi BMI value (should already be rounded to one decimal place, see
     *            {@link BmiCalculator#roundToOneDecimal(double)})
     * @throws IllegalArgumentException if the value is not a finite positive number
     */
    public static BmiCategory fromBmi(double bmi) {
        if (!Double.isFinite(bmi) || bmi <= 0) {
            throw new IllegalArgumentException("Invalid BMI value: " + bmi);
        }
        for (BmiCategory category : values()) {
            if (bmi >= category.min && bmi < category.max) {
                return category;
            }
        }
        // Unreachable: the ranges cover the whole positive axis.
        throw new IllegalStateException("No category for BMI " + bmi);
    }
}
