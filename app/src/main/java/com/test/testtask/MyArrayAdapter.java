package com.test.testtask;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyArrayAdapter extends ArrayAdapter<HistoryElement> {

    private static class ViewHolder {
        TextView orignalTextView;
        TextView translationTextView;
    }

    public MyArrayAdapter(Context context, ArrayList<HistoryElement> values) {
        super(context, R.layout.history_item, values);
    }

    @Override
    public @NonNull View getView(int position, View convertView,@NonNull ViewGroup parent) {
        HistoryElement historyElement = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.history_item, parent, false);

            holder = new ViewHolder();

            holder.orignalTextView = (TextView) convertView.findViewById(R.id.originalTextView);
            holder.translationTextView = (TextView) convertView.findViewById(R.id.translationTextView);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.orignalTextView.setText(historyElement.original);
        holder.translationTextView.setText(historyElement.translation);

        return convertView;
    }
}