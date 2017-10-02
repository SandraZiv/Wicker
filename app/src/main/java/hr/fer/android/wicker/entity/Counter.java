package hr.fer.android.wicker.entity;

import android.content.Context;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hr.fer.android.wicker.R;
import hr.fer.android.wicker.WickerConstant;


//todo change null to ""
public class Counter implements Serializable {
    private Long id;
    private String name;
    private double value;
    private double step;
    private Long dateCreated;
    private Long dateModified;
    private String note = "";

    private DecimalFormat formatting = new DecimalFormat(WickerConstant.DECIMAL_FORMAT);

    public enum CounterDateEnum {
        COUNTER_CREATED_DATE(0),
        COUNTER_MODIFIED_DATE(1);

        private final int counterDateCode;
        private CounterDateEnum(int counterDateCode) {
            this.counterDateCode = counterDateCode;
        }
    }


    public Counter() {
        this.id = WickerConstant.ERROR_CODE_LONG;
        this.name = "";
        this.value = WickerConstant.DEFAULT_VALUE;
        this.step = WickerConstant.DEFAULT_STEP;
        this.dateCreated = Calendar.getInstance().getTimeInMillis();
        this.dateModified = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * This constructor is mostly called after data has been loaded from database
     *
     * @param id    counter id (from database)
     * @param name  counter name
     * @param value counter value
     * @param step  counter step
     */
    public Counter(Long id, String name, double value, double step, Long dateCreated, Long dateModified, String note) {
        this.id = id;
        this.name = name;
        if (step < 1)
            this.step = WickerConstant.DEFAULT_STEP;
        else
            this.step = step;
        if (value < 0)
            this.value = WickerConstant.DEFAULT_VALUE;
        else
            this.value = value;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.dateModified = Calendar.getInstance().getTimeInMillis();
    }

    public double getStep() {
        return step;
    }

    //overflow management done in MainActivity.java
    public void setStep(double step) {
        this.step = step;
        this.dateModified = Calendar.getInstance().getTimeInMillis();
    }

    public double getValue() {
        return value;
    }

    //overflow management done in MainActivity.java
    public void setValue(double value) {
        this.value = value;
        this.dateModified = Calendar.getInstance().getTimeInMillis();
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public Long getDateModified() {
        return dateModified;
    }

    public void setDateModified(Long dateModified) {
        this.dateModified = dateModified;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        this.dateModified = Calendar.getInstance().getTimeInMillis();
    }

    //used when comparing working counter and counter in db to save changes
    //does not include id, dateModified, dateCreated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Counter counter = (Counter) o;

        if (Double.compare(counter.value, value) != 0) return false;
        if (Double.compare(counter.step, step) != 0) return false;
        if (name != null ? !name.equals(counter.name) : counter.name != null) return false;
        return note != null ? note.equals(counter.note) : counter.note == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(step);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (note != null ? note.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name + ',' + value + ',' + step + ',' + dateCreated + ',' + dateModified + ',' + note;
    }

    //in case of overflow returns -1 and values stays untouched
    public double increase() {
        if (Double.MAX_VALUE - this.step < this.value) {
            return WickerConstant.ERROR_CODE;
        }
        this.value += this.step;
        return this.value;
    }

    //problem of negative is solved by not making any changes and leaving user to reset counter or preform similar action
    public double decrease() {
        double tmpValue = this.value - this.step;
        if (tmpValue >= 0) {
            this.value = tmpValue;
        }
        return tmpValue;

    }

    /**
     * Method to create string with important counter data
     *
     * @param context context
     * @return string representation of data
     */
    public String extractData(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getString(R.string.wicker_says)).append("\n")
                .append(this.name.trim().isEmpty() ? context.getString(R.string.value) : this.name).append(": ").append(formatting.format(this.value));
        if (!this.note.trim().isEmpty())
            stringBuilder.append("\n").append(context.getString(R.string.note)).append(": ").append(this.note);
        stringBuilder.append("\n").append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(dateModified)));
        return stringBuilder.toString();
    }

    public String parseDateTime(CounterDateEnum dateCode, boolean showTime) {
        String date;
        if (dateCode == CounterDateEnum.COUNTER_CREATED_DATE) {
            if (showTime)
                date = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(new Date(this.dateCreated));
            else
                date = DateFormat.getDateInstance(DateFormat.FULL).format(new Date(this.dateCreated));
        } else {
            if (showTime)
                date = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(new Date(this.dateModified));
            else
                date = DateFormat.getDateInstance(DateFormat.FULL).format(new Date(this.dateModified));
        }
        return date;
    }

    /**
     * Method to create list of counter data, used in info list view
     *
     * @return list representation of counter data
     */
    public List<String> getCounterDataList() {
        List<String> list = new ArrayList<>();
        list.add(this.name);
        list.add(formatting.format(this.value));
        list.add(formatting.format(this.step));
        list.add(parseDateTime(CounterDateEnum.COUNTER_CREATED_DATE, true));
        list.add(parseDateTime(CounterDateEnum.COUNTER_MODIFIED_DATE, true));
        list.add(this.note);
        return list;
    }
}
