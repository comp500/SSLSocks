package link.infra.sslsocks.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import link.infra.sslsocks.R;
import link.infra.sslsocks.service.StunnelProcessManager;

import static link.infra.sslsocks.Constants.CONFIG;
import static link.infra.sslsocks.Constants.PSKSECRETS;

public class ConfigEditorActivity extends AppCompatActivity implements OnItemSelectedListener {

	private static final String TAG = ConfigEditorActivity.class.getSimpleName();
	private EditText editText;
	private String selectedFile = CONFIG;
	private int currentPos = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config_editor);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		Spinner spinner = findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.files_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		openFile();
	}

	public void openFile() {
		editText = findViewById(R.id.editText);
		boolean fileCreated = true;
		if (selectedFile.equals(CONFIG)) {
			fileCreated = StunnelProcessManager.setupConfig(this);
		}
		if (fileCreated) {
			File file = new File(this.getFilesDir().getPath() + selectedFile);
			StringBuilder text = new StringBuilder();

			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;

				while ((line = br.readLine()) != null) {
					text.append(line);
					text.append('\n');
				}
				br.close();
				editText.setText(text.toString());
			} catch (IOException e) {
				Log.e(TAG, "Failed to read config file", e);
				editText.getText().clear();
			}
		} else {
			Log.e(TAG, "Failed to create config file");
			editText.getText().clear();
		}
	}

	@Override
	public void onPause() {
		saveFile();
		super.onPause();
	}

	public void saveFile() {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(this.getFilesDir().getPath() + selectedFile);
			try {
				fileOutputStream.write(editText.getText().toString().getBytes());
				fileOutputStream.close();
				Toast toast = Toast.makeText(this, R.string.config_editor_saved, Toast.LENGTH_SHORT);
				toast.show();
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
	}

	public void onItemSelected(AdapterView<?> parent, View view,
	                           int pos, long id) {
		onSelect(pos);
	}

	public void onNothingSelected(AdapterView<?> parent) {
		onSelect(0);
	}

	public void onSelect(int pos) {
		if (currentPos != pos) {
			saveFile();
			switch (pos) {
				case 0:
					selectedFile = CONFIG;
					break;
				case 1:
					selectedFile = PSKSECRETS;
					break;
				default:
					selectedFile = CONFIG;
			}
			openFile();
		}
		currentPos = pos;
	}

}
