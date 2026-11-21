package cz.vspj.pmp.bmikalkulacka;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.widget.EditText;
import android.widget.TableLayout;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Instrumented UI tests of both activities (run on an emulator or device). */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private final Context context = ApplicationProvider.getApplicationContext();

    private String str(int resId, Object... args) {
        return context.getString(resId, args);
    }

    /** Expected result text built the same way as in MainActivity. */
    private String expectedResult(double roundedBmi, BmiCategory category) {
        String name = context.getResources().getStringArray(R.array.bmi_category_names)[category.ordinal()];
        return str(R.string.bmi_result, str(R.string.bmi_value_format, roundedBmi), name);
    }

    private void enter(String height, String weight) {
        onView(withId(R.id.etHeight)).perform(replaceText(height));
        onView(withId(R.id.etWeight)).perform(replaceText(weight), closeSoftKeyboard());
        onView(withId(R.id.btnCalculate)).perform(scrollTo(), click());
    }

    @Test
    public void bothFieldsEmpty_showsEmptyErrorOnBoth() {
        enter("", "");
        onView(withId(R.id.etHeight)).check(matches(hasErrorText(str(R.string.error_empty))));
        onView(withId(R.id.etWeight)).check(matches(hasErrorText(str(R.string.error_empty))));
        onView(withId(R.id.tvResult)).check(matches(withText(R.string.result_placeholder)));
    }

    @Test
    public void onlyWeightEmpty_showsErrorOnWeightOnly() {
        enter("180", "");
        onView(withId(R.id.etWeight)).check(matches(hasErrorText(str(R.string.error_empty))));
        activityRule.getScenario().onActivity(a ->
                assertNull(((EditText) a.findViewById(R.id.etHeight)).getError()));
    }

    @Test
    public void invalidNumber_showsInvalidError() {
        // replaceText bypasses the keyboard filter, so text that is not a number can be tested.
        enter("180", ".");
        onView(withId(R.id.etWeight)).check(matches(hasErrorText(str(R.string.error_invalid))));
        enter("abc", "80");
        onView(withId(R.id.etHeight)).check(matches(hasErrorText(str(R.string.error_invalid))));
    }

    @Test
    public void heightOutOfRange_showsRangeError() {
        enter("0", "80");
        onView(withId(R.id.etHeight)).check(matches(hasErrorText(str(R.string.error_height_range,
                BmiCalculator.MIN_HEIGHT_CM, BmiCalculator.MAX_HEIGHT_CM))));
    }

    @Test
    public void weightOutOfRange_showsRangeError() {
        enter("180", "999");
        onView(withId(R.id.etWeight)).check(matches(hasErrorText(str(R.string.error_weight_range,
                BmiCalculator.MIN_WEIGHT_KG, BmiCalculator.MAX_WEIGHT_KG))));
    }

    @Test
    public void validInput_showsBmiAndCategory() {
        enter("180", "81");
        onView(withId(R.id.tvResult))
                .check(matches(withText(expectedResult(25.0, BmiCategory.OVERWEIGHT))));
    }

    @Test
    public void decimalCommaWeight_isAccepted() {
        enter("175", "72,5");
        onView(withId(R.id.tvResult))
                .check(matches(withText(expectedResult(23.7, BmiCategory.NORMAL))));
    }

    @Test
    public void typedDecimalComma_passesKeyFilter() {
        // typeText sends real key events, so the EditText key filter (android:digits) applies.
        onView(withId(R.id.etWeight)).perform(typeText("72,5"), closeSoftKeyboard());
        onView(withId(R.id.etWeight)).check(matches(withText("72,5")));
    }

    @Test
    public void errorAfterValidResult_clearsOldResult() {
        enter("180", "81");
        enter("180", "");
        onView(withId(R.id.tvResult)).check(matches(withText(R.string.result_placeholder)));
    }

    @Test
    public void resultSurvivesActivityRecreation() {
        enter("170", "65");
        activityRule.getScenario().recreate();
        onView(withId(R.id.tvResult))
                .check(matches(withText(expectedResult(22.5, BmiCategory.NORMAL))));
    }

    @Test
    public void infoButton_opensCategories_backButtonReturns() {
        onView(withId(R.id.ibInfo)).perform(click());
        onView(withId(R.id.tableCategories)).check(matches(isDisplayed()));
        onView(withId(R.id.tvYourBmi))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.btnBack)).perform(click());
        onView(withId(R.id.etHeight)).check(matches(isDisplayed()));
    }

    @Test
    public void categoriesTable_hasHeaderAndOneRowPerCategory() {
        try (ActivityScenario<CategoriesActivity> scenario =
                     ActivityScenario.launch(CategoriesActivity.class)) {
            scenario.onActivity(a -> {
                TableLayout table = a.findViewById(R.id.tableCategories);
                assertEquals(1 + BmiCategory.values().length, table.getChildCount());
            });
        }
    }

    @Test
    public void categoriesAfterCalculation_showsUsersBmi() {
        enter("180", "81");
        onView(withId(R.id.ibInfo)).perform(click());
        String name = str(R.string.category_overweight);
        onView(withId(R.id.tvYourBmi)).check(matches(withText(
                str(R.string.categories_your_bmi, str(R.string.bmi_value_format, 25.0), name))));
    }

    @Test
    public void categoryResourceArrays_matchEnum() {
        Resources res = context.getResources();
        int count = BmiCategory.values().length;
        assertEquals(count, res.getStringArray(R.array.bmi_category_names).length);
        assertEquals(count, res.getStringArray(R.array.bmi_category_ranges).length);
        assertEquals(count, res.getStringArray(R.array.bmi_category_descriptions).length);
        TypedArray icons = res.obtainTypedArray(R.array.bmi_category_icons);
        TypedArray colors = res.obtainTypedArray(R.array.bmi_category_colors);
        try {
            assertEquals(count, icons.length());
            assertEquals(count, colors.length());
        } finally {
            icons.recycle();
            colors.recycle();
        }
    }
}
