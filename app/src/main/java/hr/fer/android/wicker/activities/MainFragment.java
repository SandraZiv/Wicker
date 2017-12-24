package hr.fer.android.wicker.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import hr.fer.android.wicker.R;


/**
 * Class that represents fragment for swipe implementation
 */
public class MainFragment extends Fragment {
    //not included in WickerConstant
    private final static String PAGE_NUMBER_KEY = "page_number";

    View rootView;

    public MainFragment() {

    }

    /**
     * Method that creates fragment with page number and counterWorking data
     *
     * @param position page number in swipe
     * @return fragment from {@link MainFragment}
     */
    public static MainFragment createFragment(int position) {
        MainFragment fragment = new MainFragment();
        //bundle for page number
        Bundle dataBundle = new Bundle();
        dataBundle.putInt(PAGE_NUMBER_KEY, position);
        fragment.setArguments(dataBundle);
        return fragment;
    }

    /**
     * Method to create setup layout fragment.
     * Depending on which page is currently active displays layout
     *
     * @param inflater           inflater
     * @param container          container
     * @param savedInstanceState savedInstanceState
     * @return rootView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //get page number
        int pageNumber;
        try {
            pageNumber = getArguments().getInt(PAGE_NUMBER_KEY);
        } catch (Exception ex) {
            pageNumber = 1;
        }

        if (pageNumber == 1) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //add components after view has been created
            ((MainActivity) getActivity())
                    .setFragmentMainComponents((TextView) rootView.findViewById(R.id.text_name),
                            (TextView) rootView.findViewById(R.id.text_value),
                            (TextView) rootView.findViewById(R.id.text_step),
                            (Button) rootView.findViewById(R.id.btn_add),
                            (Button) rootView.findViewById(R.id.btn_subtract),
                            (Button) rootView.findViewById(R.id.btn_reset),
                            (Button) rootView.findViewById(R.id.btn_set_num),
                            (Button) rootView.findViewById(R.id.btn_step));
        } else {
            rootView = inflater.inflate(R.layout.fragment_counter_info, container, false);
            //add components after view has been created
            ((MainActivity) getActivity()).setFragmentInfoComponents((ListView) rootView.findViewById(R.id.info_list));
        }
        return rootView;
    }

}

