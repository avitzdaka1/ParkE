package com.omeryaari.parke.ui;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.omeryaari.parke.R;
import com.omeryaari.parke.logic.ParkingRule;

import java.util.ArrayList;


public class ParkingMarkFragment extends Fragment {

    private GridLayout gridLayout;
    private ArrayList<ParkingRule> paidParkingRules;

    private enum TableProps {
        PARKING_ROWS_MAX(4), PARKING_COLUMNS_MAX(3);
        private int value;

        TableProps(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public ParkingMarkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_placeholder_park_mark, container, false);
        paidParkingRules = new ArrayList<>();
        gridLayout = (GridLayout) mainView.findViewById(R.id.parking_gridlayout);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        //initGrid(width);
        return mainView;
    }

    private void initGrid(int width) {
        gridLayout.setColumnCount(TableProps.PARKING_COLUMNS_MAX.getValue());
        gridLayout.setRowCount(TableProps.PARKING_ROWS_MAX.getValue());
        int cellSize = (width - 150) / 5;

    }

}
