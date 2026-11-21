package cz.vspj.pmp.bmikalkulacka;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.MaterialColors;

/**
 * Overview of BMI categories shown as a table. The table rows are generated from
 * the resource arrays {@code bmi_category_names}, {@code bmi_category_ranges},
 * {@code bmi_category_descriptions}, {@code bmi_category_icons} and {@code bmi_category_colors}.
 */
public class CategoriesActivity extends AppCompatActivity {

    /** Optional extra (double): BMI calculated on the main screen; its category is highlighted. */
    public static final String EXTRA_BMI = "cz.vspj.pmp.bmikalkulacka.EXTRA_BMI";

    /** Alpha (0–255) of the category color used as the background of the highlighted row. */
    private static final int HIGHLIGHT_ALPHA = 0x40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        int highlightedIndex = showUserBmi();
        fillTable(highlightedIndex);

        // The "exit" button returns to the main activity.
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Shows the BMI passed from the main screen (if any).
     *
     * @return index of the user's category, or -1 if no BMI was passed
     */
    private int showUserBmi() {
        double bmi = getIntent().getDoubleExtra(EXTRA_BMI, Double.NaN);
        if (Double.isNaN(bmi)) {
            return -1;
        }
        double rounded = BmiCalculator.roundToOneDecimal(bmi);
        int index = BmiCategory.fromBmi(rounded).ordinal();
        String categoryName = getResources().getStringArray(R.array.bmi_category_names)[index];

        TextView tvYourBmi = findViewById(R.id.tvYourBmi);
        tvYourBmi.setText(getString(R.string.categories_your_bmi,
                getString(R.string.bmi_value_format, rounded), categoryName));
        tvYourBmi.setVisibility(View.VISIBLE);
        return index;
    }

    /** Adds one table row per BMI category, reading all values from resources. */
    private void fillTable(int highlightedIndex) {
        Resources res = getResources();
        String[] names = res.getStringArray(R.array.bmi_category_names);
        String[] ranges = res.getStringArray(R.array.bmi_category_ranges);
        String[] descriptions = res.getStringArray(R.array.bmi_category_descriptions);
        TypedArray icons = res.obtainTypedArray(R.array.bmi_category_icons);
        TypedArray colors = res.obtainTypedArray(R.array.bmi_category_colors);

        TableLayout table = findViewById(R.id.tableCategories);
        LayoutInflater inflater = getLayoutInflater();
        int stripeColor = MaterialColors.getColor(table,
                com.google.android.material.R.attr.colorSurfaceContainerLow);
        try {
            for (int i = 0; i < names.length; i++) {
                TableRow row = (TableRow) inflater.inflate(R.layout.row_category, table, false);
                ImageView ivIcon = row.findViewById(R.id.ivCategoryIcon);
                TextView tvRange = row.findViewById(R.id.tvCategoryRange);
                TextView tvName = row.findViewById(R.id.tvCategoryName);
                TextView tvDescription = row.findViewById(R.id.tvCategoryDescription);

                ivIcon.setImageResource(icons.getResourceId(i, 0));
                tvRange.setText(ranges[i]);
                tvName.setText(names[i]);
                tvDescription.setText(descriptions[i]);

                if (i == highlightedIndex) {
                    row.setBackgroundColor(
                            ColorUtils.setAlphaComponent(colors.getColor(i, 0), HIGHLIGHT_ALPHA));
                } else if (i % 2 == 1) {
                    row.setBackgroundColor(stripeColor);
                }
                table.addView(row);
            }
        } finally {
            icons.recycle();
            colors.recycle();
        }
    }
}
