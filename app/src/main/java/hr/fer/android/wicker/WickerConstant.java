package hr.fer.android.wicker;

public class WickerConstant {

    //Used in HomeScreenActivity
    public static final int REQUEST_CODE = 1;

    //Used in MainActivity
    public static final String INCREASE = "INCREASE";
    public static final String DECREASE = "DECREASE";
    public static final String SAVE = "SAVE";

    //from Counter
    public final static double DEFAULT_VALUE = 0.0;
    public final static double DEFAULT_STEP = 1.0;
    public final static int ERROR_CODE = -1;
    public final static String COUNTER_BUNDLE_KEY = "counter";
    public final static String COUNTER_WORKING_STATE_KEY = "working";
    public static final String COUNTER_ORIGINAL_STATE_KEY = "original";

    public final static Long ERROR_CODE_LONG = (long) -1;

    //Settings
    public static final String PREF_NOTIFICATION = "pref_notification";

    public static final String PREFS_ORDER = "OrderBy";
    public static final String ORDER = "order";

    //encryption
    public static final String ENCRYPTION_PASSWORD = "Wicker";

    //formatting
    public static final String DECIMAL_FORMAT = "#,###,###,###.##";

    //info indexes
    public static final int INFO_NAME = 0;
    public static final int INFO_VALUE = 1;
    public static final int INFO_STEP = 2;
    public static final int INFO_NOTE = 5;
}
