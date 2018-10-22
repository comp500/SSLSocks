package link.infra.sslsocks.gui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import link.infra.sslsocks.R;
import link.infra.sslsocks.dummy.DummyContent;
import link.infra.sslsocks.service.StunnelIntentService;
import link.infra.sslsocks.service.StunnelProcessManager;

public class MainActivity extends AppCompatActivity {

	private FloatingActionButton fabAdd;
	public final int IMPORT_FILE = 2;
	public static final String CHANNEL_ID = "NOTIFY_CHANNEL_1";
	private WeakReference<ConfigEditorFragment> cfgEditorFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		ViewPager mViewPager = findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		tabLayout.addOnTabSelectedListener(onTabSelectedListener);
		mViewPager.addOnPageChangeListener(onPageChangeListener);

		fabAdd = findViewById(R.id.fab);
		fabAdd.hide();
		fabAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});

		// attempt extraction in activity, to make service start faster
		StunnelProcessManager.checkAndExtract(this);
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

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		public PlaceholderFragment() {
		}

		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			TextView textView = rootView.findViewById(R.id.section_label);
			if (getArguments() != null) {
				textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
			}
			return rootView;
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a Fragment.
			switch (position) {
				case 0:
					Fragment frag = StartFragment.newInstance(new StartFragment.OnFragmentInteractionListener() {
						@Override
						public void onFragmentStartInteraction() {
							StunnelIntentService.start(getApplicationContext());
						}

						@Override
						public void onFragmentStopInteraction() {
							stopStunnelService();
						}
					});
					StunnelIntentService.checkStatus(MainActivity.this);
					return frag;
				case 1:
					return LogFragment.newInstance();
				case 2:
					cfgEditorFragment = new WeakReference<>(ConfigEditorFragment.newInstance());
					return cfgEditorFragment.get();
				case 3:
					return ServersFragment.newInstance(1, new ServersFragment.OnListFragmentInteractionListener() {
						@Override
						public void onListFragmentInteraction(DummyContent.DummyItem item) {
							AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
							alertDialog.setTitle("hi");
							alertDialog.setMessage("this is my app");

							alertDialog.show();
						}
					});
				default:
					return PlaceholderFragment.newInstance(position + 1);
			}
		}

		@Override
		public int getCount() {
			Resources res = getResources();
			String[] tabs = res.getStringArray(R.array.tabs_array);
			return tabs.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Resources res = getResources();
			String[] tabs = res.getStringArray(R.array.tabs_array);
			return tabs[position];
		}
	}

	private int previousPosition = 0;

	private void handleTabChange(int position) {
		switch (position) {
			case 3:
				fabAdd.show();
				break;
			default:
				fabAdd.hide();
				break;
		}
		if (previousPosition == 2) {
			if (cfgEditorFragment != null) {
				ConfigEditorFragment frag = cfgEditorFragment.get();
				if (frag != null) {
					frag.saveFile(); // Ensure the file is saved when the user changes tab
				}
			}
		}
		previousPosition = position;
	}

	TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
		@Override
		public void onTabSelected(TabLayout.Tab tab) {
			handleTabChange(tab.getPosition());
		}

		@Override
		public void onTabUnselected(TabLayout.Tab tab) {
		} // nothing needed here

		@Override
		public void onTabReselected(TabLayout.Tab tab) {
		} // nothing needed here
	};

	ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		} // nothing needed here

		@Override
		public void onPageSelected(int position) {
			handleTabChange(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		} // nothing needed here
	};

	public void openSettings(MenuItem item) {
		Intent intent = new Intent(this, AdvancedSettingsActivity.class);
		startActivity(intent);
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
						Log.d("data: ", stringBuilder.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void stopStunnelService() {
		Intent intent = new Intent(this, StunnelIntentService.class);
		stopService(intent);
	}

	public void importExternalFile(MenuItem item) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent chooserIntent = Intent.createChooser(intent, getString(R.string.title_activity_config_editor));
		startActivityForResult(chooserIntent, IMPORT_FILE);
	}
}
