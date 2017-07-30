package hr.fer.android.wicker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hr.fer.android.wicker.R;
import hr.fer.android.wicker.entity.Counter;


public class HomeScreenListAdapter extends BaseAdapter {
    private List<Counter> counterList;
    private Context context;

    public HomeScreenListAdapter(Context context, List<Counter> list) {
        this.counterList = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return counterList.size();
    }

    @Override
    public Object getItem(int position) {
        return counterList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View homeScreenRowView = LayoutInflater.from(context).inflate(R.layout.home_screen_list_layout, parent, false);

        Counter counter = counterList.get(position);

        TextView twTitle = (TextView) homeScreenRowView.findViewById(R.id.tw_home_screen_title);
        TextView twValue = (TextView) homeScreenRowView.findViewById(R.id.tw_home_screen_value);
        TextView twDate = (TextView) homeScreenRowView.findViewById(R.id.tw_home_screen_date);

        twTitle.setText(counter.getName());
        twValue.setText(context.getString(R.string.value) + ": " + counter.getValue());
        String date;
        try {
            date = counter.parseDateTime(Counter.CounterDateEnum.COUNTER_MODIFIED_DATE, false);
        } catch (NullPointerException e) {
            date = "Unknown";
        }
        twDate.setText(date);

        return homeScreenRowView;
    }
}
