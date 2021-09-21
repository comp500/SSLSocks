/*
 * Copyright (C) 2017-2021 comp500
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining
 * it with OpenSSL (or a modified version of that library), containing parts
 * covered by the terms of the OpenSSL License, the licensors of this Program
 * grant you additional permission to convey the resulting work.
 */

package link.infra.sslsocks.gui.keymgmt;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import link.infra.sslsocks.R;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class KeyEditActivity extends AppCompatActivity {
	private EditText fileContents;
	private EditText fileName;
	private String existingFileName;
	public static final String ARG_EXISTING_FILE_NAME = "EXISTING_FILE_NAME";

	private static final String TAG = KeyEditActivity.class.getSimpleName();
	private boolean showDelete = true;

	private final ActivityResultLauncher<Intent> importFileRequestLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == RESULT_OK) {
			Intent resultData = result.getData();
			if (resultData != null) {
				Uri fileData = resultData.getData();
				if (fileData != null) {
					InputStream inputStream;
					try {
						// TODO: this doesn't seem to work on Android 4.4.x, for some reason
						inputStream = getContentResolver().openInputStream(fileData);
						if (inputStream == null) { // Just to keep the linter happy that I'm doing null checks
							throw new FileNotFoundException();
						}
					} catch (FileNotFoundException e) {
						Log.e(TAG, "Failed to read imported file", e);
						Toast.makeText(this, R.string.file_read_fail, Toast.LENGTH_SHORT).show();
						return;
					}
					try (BufferedSource in = Okio.buffer(Okio.source(inputStream))) {
						fileContents.setText(in.readUtf8());
					} catch (IOException e) {
						Log.e(TAG, "Failed to read imported file", e);
						Toast.makeText(this, R.string.file_read_fail, Toast.LENGTH_SHORT).show();
						return;
					}
					fileName.setText(getFileName(fileData));
				}
			}
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_edit);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

		fileContents = findViewById(R.id.file_contents);
		fileName = findViewById(R.id.file_name);

		Intent intent = getIntent();
		if (intent != null) {
			existingFileName = intent.getStringExtra(ARG_EXISTING_FILE_NAME);
		}
		if (existingFileName == null) {
			getSupportActionBar().setTitle(R.string.title_activity_key_create);
			findViewById(R.id.import_button).setVisibility(View.VISIBLE);
			showDelete = false;
			invalidateOptionsMenu();
		} else {
			fileName.setText(existingFileName);
			openFile();
		}

		// Add event listeners in code, because onClick doesn't work on 4.4.x for some reason
		// https://stackoverflow.com/a/54060752/816185
		findViewById(R.id.import_button).setOnClickListener(view -> importExternalFile());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_key_edit, menu);
		if (!showDelete) {
			menu.findItem(R.id.action_delete).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_save) {
			saveFile();
			return true;
		}
		if (id == android.R.id.home) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}
		if (id == R.id.action_delete) {
			deleteFile();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void importExternalFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		importFileRequestLauncher.launch(Intent.createChooser(intent, getString(R.string.title_activity_config_editor)));
	}

	// Get the file name for importing a file from a Uri
	private String getFileName(Uri uri) {
		String result = null;
		if ("content".equals(uri.getScheme())) {
			try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
				if (cursor != null && cursor.moveToFirst()) {
					result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				}
			}
		}
		if (result == null) {
			result = Objects.requireNonNull(uri.getPath());
			int cut = result.lastIndexOf('/');
			if (cut != -1) {
				result = result.substring(cut + 1);
			}
		}
		return result;
	}

	private void saveFile() {
		String fileNameString = fileName.getText().toString();
		if (fileNameString.length() < 1) {
			Toast.makeText(this, R.string.file_name_required, Toast.LENGTH_SHORT).show();
			return;
		}
		if (!fileNameString.endsWith(".pem") && !fileNameString.endsWith(".p12")) {
			Toast.makeText(this, R.string.file_name_ext, Toast.LENGTH_SHORT).show();
			return;
		}
		if (fileNameString.contains("/")) {
			Toast.makeText(this, R.string.file_name_slashes, Toast.LENGTH_SHORT).show();
			return;
		}

		File file = new File(getFilesDir().getPath() + "/" + fileNameString);
		try (BufferedSink out = Okio.buffer(Okio.sink(file))) {
			String pendingContent = fileContents.getText().toString();
			out.writeUtf8(pendingContent);
			out.close();
			// If renamed, delete old file
			if (existingFileName != null && existingFileName.length() > 0 && !existingFileName.equals(fileNameString)) {
				File existingFile = new File(getFilesDir().getPath() + "/" + existingFileName);
				//noinspection ResultOfMethodCallIgnored
				existingFile.delete();
			}
			setResult(RESULT_OK);
			finish();
		} catch (IOException e) {
			Toast.makeText(this, R.string.file_write_fail, Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Failed key file writing: ", e);
		}
	}

	private void deleteFile() {
		if (existingFileName != null) {
			File existingFile = new File(getFilesDir().getPath() + "/" + existingFileName);
			if (!existingFile.exists()) {
				Toast.makeText(this, R.string.file_delete_nexist, Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			if (!existingFile.delete()) {
				Toast.makeText(this, R.string.file_delete_failed, Toast.LENGTH_SHORT).show();
			}
			setResult(RESULT_OK);
			finish();
		} else {
			Toast.makeText(this, R.string.file_delete_err, Toast.LENGTH_SHORT).show();
		}
	}

	private void openFile() {
		File file = new File(getFilesDir().getPath() + "/" + existingFileName);
		try (BufferedSource in = Okio.buffer(Okio.source(file))) {
			fileContents.setText(in.readUtf8());
		} catch (IOException e) {
			Log.e(TAG, "Failed to read key file", e);
			Toast.makeText(this, R.string.file_read_fail, Toast.LENGTH_SHORT).show();
			fileContents.getText().clear();
		}
	}
}
