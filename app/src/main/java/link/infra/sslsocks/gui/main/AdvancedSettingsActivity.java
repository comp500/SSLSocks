package link.infra.sslsocks.gui.main;

import android.os.Bundle;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import link.infra.sslsocks.R;

public class AdvancedSettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_settings);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
	}

	public static class AdvancedSettingsActivityFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.advanced_settings, rootKey);
		}
	}
}
