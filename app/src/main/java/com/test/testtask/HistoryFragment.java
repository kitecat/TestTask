package com.test.testtask;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HistoryFragment extends Fragment {

    final String HISTORY_PREFS = "HistoryPrefsFile";
    ArrayList<HistoryElement> historyElementArrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        SharedPreferences historyPrefs = getActivity().getSharedPreferences(HISTORY_PREFS, MODE_PRIVATE);
        String tempString = historyPrefs.getString("wordsHistory", null);
        Gson gson = new Gson();
        Type type = new TypeToken<List<HistoryElement>>(){}.getType();

        if (!TextUtils.isEmpty(tempString)) {
            historyElementArrayList = gson.fromJson(tempString, type);
        } else {
            historyElementArrayList = new ArrayList<>();
        }

        ListView historyListview = (ListView) view.findViewById(R.id.historyListView);
        MyArrayAdapter myArrayAdapter = new MyArrayAdapter(getActivity().getApplicationContext(), historyElementArrayList);
        historyListview.setAdapter(myArrayAdapter);
        return view;
    }
}
