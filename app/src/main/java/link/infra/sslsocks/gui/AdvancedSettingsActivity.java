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

package link.infra.sslsocks.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import link.infra.sslsocks.BuildConfig;
import link.infra.sslsocks.R;
import link.infra.sslsocks.service.StunnelProcessManager;

public class AdvancedSettingsActivity extends AppCompatActivity {

	private final MutableLiveData<String> stunnelVersionStringMutable = new MutableLiveData<>();
	private final LiveData<String> stunnelVersionString = stunnelVersionStringMutable;
	private Thread checkVersionThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_settings);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		checkVersionThread = new Thread(() -> stunnelVersionStringMutable.postValue(new StunnelProcessManager().checkStunnelVersion(AdvancedSettingsActivity.this)));
		checkVersionThread.start();
	}

	public static class AdvancedSettingsActivityFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.advanced_settings, rootKey);

			Preference appVersionPreference = findPreference("version");
			if (appVersionPreference != null) {
				final String versionString = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
				appVersionPreference.setSummaryProvider((Preference.SummaryProvider<Preference>) preference -> versionString);
			}
		}

		@Override
		public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			final Preference stunnelVersionPreference = findPreference("stunnelVersion");
			final AdvancedSettingsActivity asa = (AdvancedSettingsActivity) getActivity();
			if (stunnelVersionPreference != null && asa != null) {
				asa.stunnelVersionString.observe(getViewLifecycleOwner(), stunnelVersionPreference::setSummary);
			}

			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}

	@Override
	protected void onDestroy() {
		checkVersionThread.interrupt();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
