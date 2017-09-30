package link.infra.sslsocks;

import android.content.Intent;
import android.os.Bundle;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import link.infra.sslsocks.dummy.DummyContent;

public class MainActivity extends AppCompatActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private FloatingActionButton fabAdd;
	public final int VPN_PERMISSION = 1;
	private StunnelBackgroundService stunnelService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		tabLayout.addOnTabSelectedListener(onTabSelectedListener);
		mViewPager.addOnPageChangeListener(onPageChangeListener);

		fabAdd = (FloatingActionButton) findViewById(R.id.fab);
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
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			TextView textView = (TextView) rootView.findViewById(R.id.section_label);
			textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a Fragment.
			switch (position) {
				case 0:
					return StartFragment.newInstance(new StartFragment.OnFragmentInteractionListener() {
						@Override
						public void onFragmentStartInteraction() {
							Intent intent = StunnelBackgroundService.prepare(getApplicationContext());
							if (intent != null) {
								startActivityForResult(intent, VPN_PERMISSION);
							} else {
								onActivityResult(VPN_PERMISSION, RESULT_OK, null); // already have permission
							}
						}

						@Override
						public void onFragmentStopInteraction() {
							stopStunnelService();
						}
					});
				case 1:
					return LogFragment.newInstance("", "");
				case 2:
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
			// Show 4 total pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "HOME";
				case 1:
					return "LOG";
				case 2:
					return "SERVERS";
				case 3:
					return "SETTINGS";
			}
			return null;
		}
	}

	private void animateFab(int position) {
		switch (position) {
			case 2:
				fabAdd.show();
				break;
			default:
				fabAdd.hide();
				break;
		}
	}

	TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
		@Override
		public void onTabSelected(TabLayout.Tab tab) {
			animateFab(tab.getPosition());
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
			animateFab(position);
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

		if (requestCode == VPN_PERMISSION) {
			if (resultCode == RESULT_OK) {
				Intent intent = new Intent(this, StunnelBackgroundService.class);
				startService(intent);
			}
		}
	}

	private void stopStunnelService() {
		Intent intent = new Intent(this, StunnelBackgroundService.class);
		stopService(intent);
	}
}
