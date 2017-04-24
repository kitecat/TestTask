package com.test.testtask;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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

public class FirstFragment extends Fragment{

    final String baseURL = "https://translate.yandex.net/api/v1.5/tr.json/";
    final String key = "trnsl.1.1.20170322T103835Z.fc5160e6ab8ac804.e2ff9ababe88110695fd9934437dec01b767b125";
    String translatedText = "error", langFrom = "en", langTo = "ru";
    ArrayList<String> langsArrayList = new ArrayList<>();
    ArrayList<String> langsCodesArrayList = new ArrayList<>();
    Spinner fromSpinner, toSpinner;
    EditText editText;
    TextView textView;
    ImageView clearEditTextButton, swapLangsButton;

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
                JSONArray keysJSONArray = new JSONArray();
                try {
                    JSONObject langsJSONObject = result.getJSONObject("langs");
                    keysJSONArray = langsJSONObject.names();
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

                if (keysJSONArray != null) {
                    for (int i = 0 ; i < keysJSONArray.length() ; i++){
                        try {
                            langsCodesArrayList.add(keysJSONArray.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                setLangSetSpinners();
            }
        });

        View aView = inflater.inflate(R.layout.fragment_first, container, false);
        Toolbar mActionBarToolbar = (Toolbar) aView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mActionBarToolbar);
        editText = (EditText) aView.findViewById(R.id.editText);
        textView = (TextView) aView.findViewById(R.id.textView);
        fromSpinner = (Spinner) aView.findViewById(R.id.fromSpinner);
        toSpinner = (Spinner) aView.findViewById(R.id.toSpinner);

        clearEditTextButton = (ImageView) aView.findViewById(R.id.clearEditTextButton);
        clearEditTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        swapLangsButton = (ImageView) aView.findViewById(R.id.swapLangsButton);
        swapLangsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = langFrom;
                langFrom = langTo;
                langTo = temp;
                fromSpinner.setSelection(langsCodesArrayList.indexOf(langFrom), false);
                toSpinner.setSelection(langsCodesArrayList.indexOf(langTo), false);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    makeRequest(baseURL + "translate?key=" + key + "&text=" + s.toString().replace(" ", "+") + "&lang="+ langFrom + "-" + langTo, new VolleyCallback() {
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
                    textView.setText("");
                }
            }
        });
        return aView;
    }

    public void setLangSetSpinners() {
        ArrayAdapter<String> fromSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, langsArrayList);
        fromSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromSpinnerAdapter);
        fromSpinner.setSelection(0, false);
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeFromLang(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> toSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, langsArrayList);
        toSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(fromSpinnerAdapter);
        toSpinner.setSelection(0, false);
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeToLang(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void changeFromLang(int position) {
        langFrom = langsCodesArrayList.get(position);
        Toast.makeText(getActivity(), langFrom, Toast.LENGTH_LONG).show();
    }

    public void changeToLang(int position) {
        langTo = langsCodesArrayList.get(position);
        Toast.makeText(getActivity(), langTo, Toast.LENGTH_LONG).show();
    }

    public void makeRequest(String url, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
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