package com.example.currencyconversion;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CurrencyConverterActivity extends AppCompatActivity {

    private EditText amountEditText;
    private Spinner fromCurrencySpinner;
    private Spinner toCurrencySpinner;
    private TextView resultTextView;

    private static final String API_KEY = "a342a11e9abc582ca951457f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        amountEditText = findViewById(R.id.amountEditText);
        fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner);
        toCurrencySpinner = findViewById(R.id.toCurrencySpinner);
        resultTextView = findViewById(R.id.resultTextView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.currency_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fromCurrencySpinner.setAdapter(adapter);
        toCurrencySpinner.setAdapter(adapter);

        fromCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                performConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        toCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                performConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void performConversion() {
        String amount = amountEditText.getText().toString();
        String fromCurrency = fromCurrencySpinner.getSelectedItem().toString();
        String toCurrency = toCurrencySpinner.getSelectedItem().toString();

        if (!amount.isEmpty()) {
            new CurrencyConversionTask().execute(fromCurrency, toCurrency, amount);
        }
    }

    private class CurrencyConversionTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String fromCurrency = params[0];
            String toCurrency = params[1];
            String amount = params[2];

            try {
                String query = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/" + fromCurrency;
                URL url = new URL(query);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();
                    Scanner scanner = new Scanner(in);
                    scanner.useDelimiter("\\A");

                    if (scanner.hasNext()) {
                        String jsonResult = scanner.next();
                        return parseJsonResult(jsonResult, toCurrency, amount);
                    } else {
                        return "Error fetching conversion result";
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                return "Error fetching conversion result";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            resultTextView.setText(result);
        }
    }

    private String parseJsonResult(String jsonResult, String toCurrency, String amount) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONObject rates = jsonObject.getJSONObject("conversion_rates");

            if (rates.has(toCurrency)) {
                double exchangeRate = rates.getDouble(toCurrency);
                double convertedValue = Double.parseDouble(amount) * exchangeRate;
                return String.valueOf(convertedValue);
            } else {
                return "Error: Currency not found in conversion rates";
            }
        } catch (JSONException e) {
            return "Error parsing conversion result";
        }
    }
}