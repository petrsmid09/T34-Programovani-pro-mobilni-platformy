package cz.vspj.pmp.bmikalkulacka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Local unit tests of the BMI formula, validation and categorization. */
public class BmiCalculatorTest {

    private static final double DELTA = 1e-9;

    // --- Formula -------------------------------------------------------------------------

    @Test
    public void calculate_usesWeightDividedByHeightInMetersSquared() {
        // 81 / 1.8² = 81 / 3.24 = 25.0
        assertEquals(25.0, BmiCalculator.calculate(180, 81), DELTA);
    }

    @Test
    public void calculate_typicalValue() {
        // 65 / 1.7² = 22.4913...
        assertEquals(65 / (1.7 * 1.7), BmiCalculator.calculate(170, 65), DELTA);
    }

    @Test
    public void calculate_decimalWeight() {
        // 72.5 / 1.75² = 23.6734...
        assertEquals(72.5 / (1.75 * 1.75), BmiCalculator.calculate(175, 72.5), DELTA);
    }

    @Test
    public void calculate_rejectsZeroHeight() {
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(0, 70));
    }

    @Test
    public void calculate_rejectsOutOfRangeWeight() {
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(180, 5));
    }

    // --- Validation ----------------------------------------------------------------------

    @Test
    public void isHeightValid_boundaries() {
        assertFalse(BmiCalculator.isHeightValid(49.9));
        assertTrue(BmiCalculator.isHeightValid(BmiCalculator.MIN_HEIGHT_CM));
        assertTrue(BmiCalculator.isHeightValid(BmiCalculator.MAX_HEIGHT_CM));
        assertFalse(BmiCalculator.isHeightValid(250.1));
    }

    @Test
    public void isWeightValid_boundaries() {
        assertFalse(BmiCalculator.isWeightValid(9.9));
        assertTrue(BmiCalculator.isWeightValid(BmiCalculator.MIN_WEIGHT_KG));
        assertTrue(BmiCalculator.isWeightValid(BmiCalculator.MAX_WEIGHT_KG));
        assertFalse(BmiCalculator.isWeightValid(350.1));
    }

    @Test
    public void validation_rejectsNanAndInfinity() {
        assertFalse(BmiCalculator.isHeightValid(Double.NaN));
        assertFalse(BmiCalculator.isWeightValid(Double.NaN));
        assertFalse(BmiCalculator.isHeightValid(Double.POSITIVE_INFINITY));
        assertFalse(BmiCalculator.isWeightValid(Double.NEGATIVE_INFINITY));
    }

    // --- Input normalization -------------------------------------------------------------

    @Test
    public void normalizeNumber_acceptsDecimalCommaAndTrims() {
        assertEquals("72.5", BmiCalculator.normalizeNumber(" 72,5 "));
        assertEquals("180", BmiCalculator.normalizeNumber("180"));
    }

    @Test
    public void parsing_invalidTextThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class,
                () -> Double.parseDouble(BmiCalculator.normalizeNumber(".")));
        assertThrows(NumberFormatException.class,
                () -> Double.parseDouble(BmiCalculator.normalizeNumber("1,2,3")));
    }

    // --- Rounding ------------------------------------------------------------------------

    @Test
    public void roundToOneDecimal_roundsHalfUp() {
        assertEquals(22.5, BmiCalculator.roundToOneDecimal(22.4913), DELTA);
        assertEquals(25.0, BmiCalculator.roundToOneDecimal(24.96), DELTA);
        assertEquals(18.5, BmiCalculator.roundToOneDecimal(18.45), DELTA);
    }

    // --- Categories (WHO) ----------------------------------------------------------------

    @Test
    public void fromBmi_categoryBoundaries() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCategory.fromBmi(18.4));
        assertEquals(BmiCategory.NORMAL, BmiCategory.fromBmi(18.5));
        assertEquals(BmiCategory.NORMAL, BmiCategory.fromBmi(24.9));
        assertEquals(BmiCategory.OVERWEIGHT, BmiCategory.fromBmi(25.0));
        assertEquals(BmiCategory.OVERWEIGHT, BmiCategory.fromBmi(29.9));
        assertEquals(BmiCategory.OBESITY_I, BmiCategory.fromBmi(30.0));
        assertEquals(BmiCategory.OBESITY_I, BmiCategory.fromBmi(34.9));
        assertEquals(BmiCategory.OBESITY_II, BmiCategory.fromBmi(35.0));
        assertEquals(BmiCategory.OBESITY_II, BmiCategory.fromBmi(39.9));
        assertEquals(BmiCategory.OBESITY_III, BmiCategory.fromBmi(40.0));
        assertEquals(BmiCategory.OBESITY_III, BmiCategory.fromBmi(80.0));
    }

    @Test
    public void fromBmi_extremeButValidInputs() {
        // Smallest/largest BMI reachable through the validated input ranges.
        double min = BmiCalculator.calculate(BmiCalculator.MAX_HEIGHT_CM, BmiCalculator.MIN_WEIGHT_KG);
        double max = BmiCalculator.calculate(BmiCalculator.MIN_HEIGHT_CM, BmiCalculator.MAX_WEIGHT_KG);
        assertEquals(BmiCategory.UNDERWEIGHT,
                BmiCategory.fromBmi(BmiCalculator.roundToOneDecimal(min)));
        assertEquals(BmiCategory.OBESITY_III,
                BmiCategory.fromBmi(BmiCalculator.roundToOneDecimal(max)));
    }

    @Test
    public void fromBmi_roundedValueAndCategoryAgree() {
        // 24.96 is displayed as 25.0, so it must be classified as overweight.
        double rounded = BmiCalculator.roundToOneDecimal(24.96);
        assertEquals(BmiCategory.OVERWEIGHT, BmiCategory.fromBmi(rounded));
    }

    @Test
    public void fromBmi_rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> BmiCategory.fromBmi(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> BmiCategory.fromBmi(0));
        assertThrows(IllegalArgumentException.class, () -> BmiCategory.fromBmi(-1));
        assertThrows(IllegalArgumentException.class,
                () -> BmiCategory.fromBmi(Double.POSITIVE_INFINITY));
    }
}
