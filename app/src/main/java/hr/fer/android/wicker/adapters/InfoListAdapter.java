package hr.fer.android.wicker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hr.fer.android.wicker.R;


public class InfoListAdapter extends BaseAdapter {
    private List<String> counterDataList;
    private Context context;

    public InfoListAdapter(Context context, List<String> list) {
        this.counterDataList = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return counterDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return counterDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; //put maybe id from table
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View infoRowView = LayoutInflater.from(context).inflate(R.layout.info_list_layout, parent, false);
        String title, data;
        switch (position) {
            case 0: title = context.getString(R.string.name);break;
            case 1: title = context.getString(R.string.value);break;
            case 2: title = context.getString(R.string.step);break;
            case 3: title = context.getString(R.string.created);break;
            case 4: title = context.getString(R.string.last_modified); break;
            case 5: title = context.getString(R.string.note);break;
            default: title = null;
        }
        if(counterDataList.get(position) == null || counterDataList.get(position).isEmpty())
            data = " - ";
        else
            data = counterDataList.get(position);
        TextView twTitle = (TextView) infoRowView.findViewById(R.id.tw_list_title);
        twTitle.setText(title);
        TextView twData = (TextView) infoRowView.findViewById(R.id.tw_list_data);
        twData.setText(data);
        return infoRowView;
    }
}
