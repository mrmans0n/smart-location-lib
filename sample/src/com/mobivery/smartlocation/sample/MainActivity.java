package com.mobivery.smartlocation.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.mobivery.smartlocation.HelloWorld;
import com.mobivery.smartlocation.sample.R;

public class MainActivity extends Activity {
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);

        HelloWorld hw = new HelloWorld();
        ((TextView)findViewById(R.id.hello_msg)).setText(hw.getHello());

	}
	
}