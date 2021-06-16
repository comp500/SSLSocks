package link.infra.sslsocks.gui.main;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.ref.WeakReference;

import link.infra.sslsocks.R;
import link.infra.sslsocks.gui.AdvancedSettingsActivity;
import link.infra.sslsocks.gui.OpenVPNIntegrationHandler;
import link.infra.sslsocks.gui.keymgmt.KeyEditActivity;
import link.infra.sslsocks.gui.keymgmt.KeyFragment;
import link.infra.sslsocks.gui.keymgmt.KeyRecyclerViewAdapter;
import link.infra.sslsocks.service.StunnelIntentService;
import link.infra.sslsocks.service.StunnelProcessManager;

import static link.infra.sslsocks.gui.keymgmt.KeyEditActivity.ARG_EXISTING_FILE_NAME;

public class MainActivity extends AppCompatActivity implements KeyFragment.OnListFragmentInteractionListener {

	private FloatingActionButton fabAdd;
	public static final String CHANNEL_ID = "NOTIFY_CHANNEL_1";
	private WeakReference<KeyFragment> keysFragment;
	private OpenVPNIntegrationHandler openVPNIntegrationHandler = null;

	private final ActivityResultLauncher<Intent> keyEditRequestLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == RESULT_OK) {
			if (keysFragment != null) {
				KeyFragment frag = keysFragment.get();
				if (frag != null) {
					frag.updateList(this); // Ensure list is up to date
				}
			}
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ViewPager2 viewPager = findViewById(R.id.container);
		viewPager.setAdapter(new SectionsPagerAdapter(this));

		TabLayout tabLayout = findViewById(R.id.tabs);
		String[] tabNames = getResources().getStringArray(R.array.tabs_array);
		new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabNames[position])).attach();

		fabAdd = findViewById(R.id.fab);
		fabAdd.setOnClickListener(view ->
				keyEditRequestLauncher.launch(new Intent(MainActivity.this, KeyEditActivity.class)));

		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				if (position == 3) {
					fabAdd.show();
				} else {
					fabAdd.hide();
				}
			}
		});

		// attempt extraction in activity, to make service start faster
		StunnelProcessManager.prepareStunnel(this);
		StunnelProcessManager.setupConfig(this);

		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = getString(R.string.notification_channel);
			String description = getString(R.string.notification_desc);
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(channel);
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, AdvancedSettingsActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	class SectionsPagerAdapter extends FragmentStateAdapter {
		public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
			super(fragmentActivity);
		}

		@Override
		public int getItemCount() {
			return 4;
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			switch (position) {
				case 0:
					Fragment frag = StartFragment.newInstance(new StartFragment.OnFragmentInteractionListener() {
						@Override
						public void onFragmentStartInteraction() {
							StunnelIntentService.start(getApplicationContext());
							String openVpnProfile = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("open_vpn_profile", "");
							if (openVpnProfile.trim().length() > 0) {
								openVPNIntegrationHandler = new OpenVPNIntegrationHandler(MainActivity.this, () -> {}, openVpnProfile, false);
								openVPNIntegrationHandler.bind();
							}
						}

						@Override
						public void onFragmentStopInteraction() {
							stopStunnelService();
							if (openVPNIntegrationHandler != null) {
								openVPNIntegrationHandler.disconnect();
							}
						}
					});
					StunnelIntentService.checkStatus(MainActivity.this);
					return frag;
				case 1:
					return LogFragment.newInstance();
				case 2:
					return ConfigEditorFragment.newInstance();
				case 3:
					keysFragment = new WeakReference<>(KeyFragment.newInstance());
					return keysFragment.get();
			}

			throw new RuntimeException("Invalid fragment reached");
		}
	}

	private void stopStunnelService() {
		Intent intent = new Intent(this, StunnelIntentService.class);
		stopService(intent);
	}

	public void onListFragmentInteraction(KeyRecyclerViewAdapter.KeyItem item) {
		Intent intent = new Intent(MainActivity.this, KeyEditActivity.class);
		intent.putExtra(ARG_EXISTING_FILE_NAME, item.filename);
		keyEditRequestLauncher.launch(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == OpenVPNIntegrationHandler.PERMISSION_REQUEST) {
			if (resultCode == RESULT_OK && openVPNIntegrationHandler != null) {
				openVPNIntegrationHandler.doVpnPermissionRequest();
			}
		} else if (requestCode == OpenVPNIntegrationHandler.VPN_PERMISSION_REQUEST) {
			if (resultCode == RESULT_OK && openVPNIntegrationHandler != null) {
				openVPNIntegrationHandler.connectProfile();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		StunnelIntentService.checkStatus(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (openVPNIntegrationHandler != null) {
			openVPNIntegrationHandler.unbind();
		}
	}
}
