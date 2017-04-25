package com.test.testtask;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FirstFragment extends Fragment{

    final String baseURL = "https://translate.yandex.net/api/v1.5/tr.json/";
    final String key = "trnsl.1.1.20170322T103835Z.fc5160e6ab8ac804.e2ff9ababe88110695fd9934437dec01b767b125";
    String translatedText = "error", langFrom, langTo;
    ArrayList<String> langsArrayList = new ArrayList<>();
    ArrayList<String> langsCodesArrayList = new ArrayList<>();
    List<HistoryElement> historyElementArrayList = new ArrayList<>();
    List<String> tempWordsHistory;
    Spinner fromSpinner, toSpinner;
    EditText editText;
    TextView textView;
    ImageView clearEditTextButton, swapLangsButton;
    final String PREFS_NAME = "LangsPrefsFile";
    final String HISTORY_PREFS = "HistoryPrefsFile";

    public static FirstFragment newInstance() {
        return new FirstFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Запрос списка поддерживаемыз языков
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

        //Считываем последние использованные языки
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        langFrom = prefs.getString("langFrom", "ru");
        langTo = prefs.getString("langTo", "en");

        //Тулбар
        Toolbar mActionBarToolbar = (Toolbar) aView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mActionBarToolbar);

        editText = (EditText) aView.findViewById(R.id.editText);
        textView = (TextView) aView.findViewById(R.id.textView);
        fromSpinner = (Spinner) aView.findViewById(R.id.fromSpinner);
        toSpinner = (Spinner) aView.findViewById(R.id.toSpinner);

        //Инициализация кнопки для очистки поля ввода
        clearEditTextButton = (ImageView) aView.findViewById(R.id.clearEditTextButton);
        clearEditTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        //Инициализация кнопки для смены местами языков
        swapLangsButton = (ImageView) aView.findViewById(R.id.swapLangsButton);
        swapLangsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = langFrom;
                changeFromLang(langsCodesArrayList.indexOf(langTo));
                changeToLang(langsCodesArrayList.indexOf(temp));
                setSpinnersToLangs();
            }
        });

        //onTextChanged листенер для быстрого перевода
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    final String textToTranslate = s.toString().replace(" ", "+");
                    makeRequest(baseURL + "translate?key=" + key + "&text=" + textToTranslate + "&lang="+ langFrom + "-" + langTo, new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            try {
                                translatedText = result.getString("text").replace("\"", "").replace("[", "").replace("]", "");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            textView.setText(translatedText);
                            saveTextInHistory(textToTranslate, translatedText);
                        }
                    });
                } else {
                    textView.setText("");
                }
            }
        });

        SharedPreferences historyPrefs = getActivity().getSharedPreferences(HISTORY_PREFS, MODE_PRIVATE);
        String tempString = historyPrefs.getString("wordsHistory", null);
        Gson gson = new Gson();
        Type type = new TypeToken<List<HistoryElement>>(){}.getType();

        if (!TextUtils.isEmpty(tempString)) {
            historyElementArrayList = gson.fromJson(tempString, type);
        } else {
            historyElementArrayList = new ArrayList<>();
        }

        return aView;
    }

    public void saveTextInHistory(String textToTranslate, String translatedText) {

        historyElementArrayList.add(new HistoryElement(textToTranslate, translatedText, langFrom, langTo));

        Gson gson = new Gson();
        String tempString = gson.toJson(historyElementArrayList);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(HISTORY_PREFS, MODE_PRIVATE).edit();
        editor.putString("wordsHistory", tempString);
        editor.apply();
    }

    //Инициализация спиннеров выбора языка
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
        setSpinnersToLangs();
    }

    //"обновление" положения выбранного итема для спиннеров
    public void setSpinnersToLangs() {
        fromSpinner.setSelection(langsCodesArrayList.indexOf(langFrom), false);
        toSpinner.setSelection(langsCodesArrayList.indexOf(langTo), false);
    }

    //Смена языков и запись их в SharedPrefs
    public void changeFromLang(int position) {
        langFrom = langsCodesArrayList.get(position);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("langFrom", langFrom);
        editor.apply();
    }

    public void changeToLang(int position) {
        langTo = langsCodesArrayList.get(position);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("langTo", langTo);
        editor.apply();
    }

    //Метод для volley request
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