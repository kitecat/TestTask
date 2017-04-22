package com.test.testtask;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FirstFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    final String baseURL = "https://translate.yandex.net/api/v1.5/tr.json/";
    final String key = "trnsl.1.1.20170322T103835Z.fc5160e6ab8ac804.e2ff9ababe88110695fd9934437dec01b767b125";
    String translatedText = "error";
    ArrayList<String> langsArrayList = new ArrayList<>();
    Spinner fromSpinner;

    public static FirstFragment newInstance() {
        return new FirstFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        makeRequest(baseURL + "getLangs?key=" + key + "&ui=ru", new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                JSONArray valuesJSONArray = new JSONArray();
                try {
                    JSONObject langsJSONObject = result.getJSONObject("langs");
                    JSONArray keysJSONArray = langsJSONObject.names();
                    valuesJSONArray = langsJSONObject.toJSONArray(keysJSONArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (valuesJSONArray != null) {
                    for (int i = 0 ; i < valuesJSONArray.length() ; i++){
                        try {
                            langsArrayList.add(valuesJSONArray.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    langsArrayList.add("error");
                }
                setFromSpinner();
            }
        });

        View aView = inflater.inflate(R.layout.fragment_first, container, false);
        final EditText editText = (EditText) aView.findViewById(R.id.editText);
        final TextView textView = (TextView) aView.findViewById(R.id.textView);
        fromSpinner = (Spinner) aView.findViewById(R.id.fromSpinner);

//        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
//                Object item = parent.getItemAtPosition(selectedItemPosition);
//                if (item != null) {
//                    Toast.makeText(getContext(), item.toString(),
//                            Toast.LENGTH_SHORT).show();
//                }
//                Toast.makeText(getContext(), "Selected",
//                        Toast.LENGTH_SHORT).show();
//            }
//
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    makeRequest(baseURL + "translate?key=" + key + "&text=" + s.toString().replace(" ", "+") + "&lang=en-ru", new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            try {
                                translatedText = result.getString("text").replace("\"", "").replace("[", "").replace("]", "");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            textView.setText(translatedText);
                        }
                    });
                } else {
                    textView.setText(null);
                }
            }
        });
        return aView;
    }

    public void onViewCreated(View aView, Bundle savedInstanceState){
        super.onViewCreated(aView, savedInstanceState);

        Spinner toSpinner = (Spinner) aView.findViewById(R.id.toSpinner);

//        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // On selecting a spinner item
//                String item = parent.getItemAtPosition(position).toString();
//
//
//                // Showing selected spinner item
//                Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                Toast.makeText(parent.getContext(), "Nothing selected", Toast.LENGTH_LONG).show();
//            }
//        });
    }

    public void setFromSpinner() {
        ArrayAdapter<String> fromSpinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, langsArrayList);
        fromSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromSpinnerAdapter);
        fromSpinner.setSelection(0, false);
        fromSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Toast.makeText(getActivity(), langsArrayList.get(pos), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        Toast.makeText(getActivity(), "Nincs kiválasztott járat", Toast.LENGTH_LONG).show();
    }

    public void makeRequest(String url, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private interface VolleyCallback{
        void onSuccess(JSONObject result);
    }
}