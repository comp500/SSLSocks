package link.infra.sslsocks.gui.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import link.infra.sslsocks.R;
import link.infra.sslsocks.service.StunnelProcessManager;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static link.infra.sslsocks.Constants.CONFIG;
import static link.infra.sslsocks.Constants.PSKSECRETS;

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
	public static ConfigEditorFragment newInstance() {
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

	public void saveFile() {
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
