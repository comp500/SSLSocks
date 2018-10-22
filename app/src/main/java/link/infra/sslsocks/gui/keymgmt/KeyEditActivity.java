package link.infra.sslsocks.gui.keymgmt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import link.infra.sslsocks.R;

public class KeyEditActivity extends AppCompatActivity {

	public final int IMPORT_FILE = 2;
	private EditText fileContents;
	private String existingFileName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_edit);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		fileContents = findViewById(R.id.file_contents);

		if (existingFileName == null) {
			toolbar.setTitle(R.string.title_activity_key_create);
			findViewById(R.id.import_button).setVisibility(View.VISIBLE);
			findViewById(R.id.delete_button).setVisibility(View.GONE);
		}
	}


	public void importExternalFile(View view) {
		importExternalFile();
	}

	public void importExternalFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent chooserIntent = Intent.createChooser(intent, getString(R.string.title_activity_config_editor));
		startActivityForResult(chooserIntent, IMPORT_FILE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == IMPORT_FILE) {
			if (resultCode == RESULT_OK) {
				Uri fileData = data.getData();
				if (fileData != null) {
					Log.d("test", fileData.getPath());
					try {
						InputStream inputStream = getContentResolver().openInputStream(fileData);
						if (inputStream == null) {
							return;
						}
						BufferedReader reader = new BufferedReader(new InputStreamReader(
								inputStream));
						StringBuilder stringBuilder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							stringBuilder.append(line);
						}
						inputStream.close();
						fileContents.setText(stringBuilder.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
