package link.infra.sslsocks.gui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import link.infra.sslsocks.R;
import link.infra.sslsocks.service.ServiceUtils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogFragment extends Fragment {

	public LogFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment LogFragment.
	 */
	public static LogFragment newInstance() {
		return new LogFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_log, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		final TextView logText = view.findViewById(R.id.logtext);
		Activity act = getActivity();
		if (act == null) {
			return;
		}
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(act);

		IntentFilter statusIntentFilter = new IntentFilter(ServiceUtils.ACTION_LOGBROADCAST);
		BroadcastReceiver statusReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.hasExtra(ServiceUtils.SHOULD_CLEAR_LOG)) {
					logText.setText("");
				}
				if (intent.hasExtra(ServiceUtils.EXTENDED_DATA_LOG)) {
					logText.append(intent.getStringExtra(ServiceUtils.EXTENDED_DATA_LOG));
					logText.append("\n");
				}
			}
		};
		manager.registerReceiver(statusReceiver, statusIntentFilter);

		IntentFilter clearLogIntentFilter = new IntentFilter(ServiceUtils.ACTION_CLEARLOG);
		BroadcastReceiver clearLogReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				logText.setText("");
			}
		};
		manager.registerReceiver(clearLogReceiver, clearLogIntentFilter);
	}

}
