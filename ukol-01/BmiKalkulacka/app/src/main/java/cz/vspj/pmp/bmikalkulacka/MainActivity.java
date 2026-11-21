package cz.vspj.pmp.bmikalkulacka;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.MaterialColors;

/**
 * Main screen: reads height and weight, validates the input, calculates the BMI
 * and shows the value together with a verbal assessment (category).
 */
public class MainActivity extends AppCompatActivity {

    /** Key for keeping the last calculated BMI across configuration changes (rotation). */
    private static final String STATE_BMI = "state_bmi";

    private EditText etHeight;
    private EditText etWeight;
    private TextView tvResult;

    /** Last calculated (unrounded) BMI, or NaN if no valid result is shown. */
    private double lastBmi = Double.NaN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        tvResult = findViewById(R.id.tvResult);
        Button btnCalculate = findViewById(R.id.btnCalculate);
        ImageButton ibInfo = findViewById(R.id.ibInfo);

        btnCalculate.setOnClickListener(v -> calculateBmi());
        ibInfo.setOnClickListener(v -> openCategories());
        // "Done" on the keyboard in the weight field starts the calculation as well.
        etWeight.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                calculateBmi();
                return true;
            }
            return false;
        });

        // Restore the result after a configuration change (e.g. screen rotation).
        if (savedInstanceState != null) {
            double savedBmi = savedInstanceState.getDouble(STATE_BMI, Double.NaN);
            if (!Double.isNaN(savedBmi)) {
                showResult(savedBmi);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(STATE_BMI, lastBmi);
    }

    /** Button handler: validate inputs, convert them to double, calculate and display BMI. */
    private void calculateBmi() {
        // 1) Read the text values of both fields.
        String heightText = etHeight.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();

        // 2) Empty input check.
        if (heightText.isEmpty() || weightText.isEmpty()) {
            if (weightText.isEmpty()) {
                showInputError(etWeight, getString(R.string.error_empty));
            }
            if (heightText.isEmpty()) {
                showInputError(etHeight, getString(R.string.error_empty));
            }
            return;
        }

        // 3) Conversion String -> double (NumberFormatException handled in parseNumber).
        Double heightCm = parseNumber(etHeight, heightText);
        if (heightCm == null) {
            return;
        }
        Double weightKg = parseNumber(etWeight, weightText);
        if (weightKg == null) {
            return;
        }

        // 4) Range check (also rejects 0, which would mean division by zero).
        if (!BmiCalculator.isHeightValid(heightCm)) {
            showInputError(etHeight, getString(R.string.error_height_range,
                    BmiCalculator.MIN_HEIGHT_CM, BmiCalculator.MAX_HEIGHT_CM));
            return;
        }
        if (!BmiCalculator.isWeightValid(weightKg)) {
            showInputError(etWeight, getString(R.string.error_weight_range,
                    BmiCalculator.MIN_WEIGHT_KG, BmiCalculator.MAX_WEIGHT_KG));
            return;
        }

        // 5) Calculation and output.
        etHeight.setError(null);
        etWeight.setError(null);
        showResult(BmiCalculator.calculate(heightCm, weightKg));
        hideKeyboard();
    }

    /**
     * Converts the text of an input field to double.
     *
     * @return the parsed number, or {@code null} if the text is not a valid number
     *         (an error is then shown on the field)
     */
    @Nullable
    private Double parseNumber(EditText field, String text) {
        try {
            return Double.parseDouble(BmiCalculator.normalizeNumber(text));
        } catch (NumberFormatException e) {
            showInputError(field, getString(R.string.error_invalid));
            return null;
        }
    }

    /** Shows the error message on the given field, focuses it and clears the old result. */
    private void showInputError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        clearResult();
    }

    /** Displays BMI and its category; all texts, icons and colors come from resources. */
    private void showResult(double bmi) {
        lastBmi = bmi;
        double rounded = BmiCalculator.roundToOneDecimal(bmi);
        int index = BmiCategory.fromBmi(rounded).ordinal();

        Resources res = getResources();
        String categoryName = res.getStringArray(R.array.bmi_category_names)[index];
        String bmiText = getString(R.string.bmi_value_format, rounded);
        tvResult.setText(getString(R.string.bmi_result, bmiText, categoryName));

        TypedArray icons = res.obtainTypedArray(R.array.bmi_category_icons);
        TypedArray colors = res.obtainTypedArray(R.array.bmi_category_colors);
        try {
            tvResult.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, icons.getResourceId(index, 0), 0, 0);
            setResultStrokeColor(colors.getColor(index, 0));
        } finally {
            icons.recycle();
            colors.recycle();
        }
    }

    /** Resets the result area to its initial state. */
    private void clearResult() {
        lastBmi = Double.NaN;
        tvResult.setText(R.string.result_placeholder);
        tvResult.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        setResultStrokeColor(MaterialColors.getColor(tvResult,
                com.google.android.material.R.attr.colorOutlineVariant));
    }

    private void setResultStrokeColor(@ColorInt int color) {
        GradientDrawable background = (GradientDrawable) tvResult.getBackground().mutate();
        background.setStroke(getResources().getDimensionPixelSize(R.dimen.result_stroke_width), color);
    }

    private void hideKeyboard() {
        WindowCompat.getInsetsController(getWindow(), tvResult)
                .hide(WindowInsetsCompat.Type.ime());
    }

    /** Opens the BMI categories overview; passes the last BMI so its category gets highlighted. */
    private void openCategories() {
        Intent intent = new Intent(this, CategoriesActivity.class);
        if (!Double.isNaN(lastBmi)) {
            intent.putExtra(CategoriesActivity.EXTRA_BMI, lastBmi);
        }
        startActivity(intent);
    }
}
