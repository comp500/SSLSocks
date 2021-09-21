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

package link.infra.sslsocks.gui.main;

import static link.infra.sslsocks.Constants.CONFIG;
import static link.infra.sslsocks.Constants.PSKSECRETS;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import link.infra.sslsocks.R;
import link.infra.sslsocks.service.StunnelProcessManager;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * A fragment to edit text based configuration files
 */
public class ConfigEditorFragment extends Fragment implements AdapterView.OnItemSelectedListener {

	public ConfigEditorFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment ConfigEditorFragment.
	 */
	static ConfigEditorFragment newInstance() {
		return new ConfigEditorFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_config_editor, container, false);
	}

	private static final String TAG = ConfigEditorFragment.class.getSimpleName();
	private EditText editText;
	private String selectedFile = CONFIG;
	private int currentPos = 0;
	private WeakReference<View> currentView;
	private String existingContent;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Activity act = getActivity();
		if (act == null) return;

		Spinner spinner = view.findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(act,
				R.array.files_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		currentView = new WeakReference<>(view);

		openFile();
	}

	private void openFile() {
		Activity act = getActivity();
		if (act == null) return;

		View view = currentView.get();
		if (view == null) return;

		editText = view.findViewById(R.id.editText);
		boolean fileCreated = true;
		if (selectedFile.equals(CONFIG)) {
			fileCreated = StunnelProcessManager.setupConfig(act);
		}
		if (fileCreated) {
			File file = new File(act.getFilesDir().getPath() + "/" + selectedFile);
			try (BufferedSource in = Okio.buffer(Okio.source(file))){
				String text = in.readUtf8();
				editText.setText(text);
				existingContent = text;
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

	void saveFile() {
		Activity act = getActivity();
		if (act == null) return;
		if (editText.getText().toString().equals(existingContent)) {
			return; // No changes made
		}
		try (BufferedSink out = Okio.buffer(Okio.sink(new File(act.getFilesDir().getPath() + "/" + selectedFile)))) {
			String pendingContent = editText.getText().toString();
			out.writeUtf8(pendingContent);
			existingContent = pendingContent;
			Toast toast = Toast.makeText(act, R.string.config_editor_saved, Toast.LENGTH_SHORT);
			toast.show();
		} catch (IOException e) {
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

	private void onSelect(int pos) {
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
