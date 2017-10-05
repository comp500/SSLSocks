package link.infra.sslsocks.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import link.infra.sslsocks.R;
import link.infra.sslsocks.service.StunnelProcessManager;

import static link.infra.sslsocks.Constants.CONFIG;
import static link.infra.sslsocks.Constants.HOME;

public class ConfigEditorActivity extends AppCompatActivity {

	private static final String TAG = ConfigEditorActivity.class.getSimpleName();
	private EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config_editor);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		editText = (EditText) findViewById(R.id.editText);
		if (StunnelProcessManager.setupConfig()) {
			File file = new File(HOME + CONFIG);
			StringBuilder text = new StringBuilder();

			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;

				while ((line = br.readLine()) != null) {
					text.append(line);
					text.append('\n');
				}
				br.close();
			} catch (IOException e) {
				Log.e(TAG, "Failed to read config file", e);
			}

			editText.setText(text.toString());
		} else {
			Log.e(TAG, "Failed to create config file");
		}
	}

	@Override
	public void onPause() {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(HOME + CONFIG);
			try {
				fileOutputStream.write(editText.getText().toString().getBytes());
				fileOutputStream.close();
			} catch (IOException e) {
				Log.e(TAG, "Failed config file writing: ", e);
				try {
					// attempt to finally close the file
					fileOutputStream.close();
				} catch (IOException e1) {
					// ignore exception
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed config file writing: ", e);
		}

		super.onPause();
	}

}
