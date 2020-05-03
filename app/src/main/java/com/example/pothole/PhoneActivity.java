package com.example.pothole;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class PhoneActivity extends AppCompatActivity {

    private CountryCodePicker mCodePicker;
    private EditText number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        mCodePicker = findViewById(R.id.ccp);
        number = findViewById(R.id.phone_input);
        Button button = findViewById(R.id.button_Verify);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = number.getText().toString().trim();
                if( num.length()!=10 ){
                    if(num.isEmpty())
                        number.setError("Required");
                    else
                        number.setError("Enter valid number");
                    number.requestFocus();

                }
                else{
                    String code;
                    code = mCodePicker.getSelectedCountryCode();
                    if(code.isEmpty())
                    {
                        Toast.makeText(PhoneActivity.this, "Select country Code", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent intent = new Intent(PhoneActivity.this, VerifyPhoneNumber.class);
                        String send = "+" + code + num ;
                        Toast.makeText(PhoneActivity.this, send , Toast.LENGTH_SHORT).show();
                        intent.putExtra("number", send);

                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }
}
