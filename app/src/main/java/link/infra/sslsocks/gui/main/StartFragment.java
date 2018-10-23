package link.infra.sslsocks.gui.main;

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
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

import link.infra.sslsocks.R;
import link.infra.sslsocks.service.ServiceUtils;
import link.infra.sslsocks.service.StunnelIntentService;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {
	private OnFragmentInteractionListener mListener;

	public StartFragment() {
		// empty
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		final TextView statusLabel = Objects.requireNonNull(getView()).findViewById(R.id.statuslabel);

		Button startbutton = getView().findViewById(R.id.startbutton);
		startbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mListener != null) {
					statusLabel.setText(R.string.run_status_label_starting);
					mListener.onFragmentStartInteraction();
				}
			}
		});

		Button stopbutton = getView().findViewById(R.id.stopbutton);
		stopbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Did you press the stop button?
				// No, did you?
				if (mListener != null) {
					statusLabel.setText(R.string.run_status_label_stopping);
					mListener.onFragmentStopInteraction();
				}
			}
		});

		Activity act = getActivity();
		if (act == null) {
			return;
		}
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(act);

		IntentFilter startedIntentFilter = new IntentFilter(ServiceUtils.ACTION_STARTED);
		BroadcastReceiver startedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				statusLabel.setText(R.string.run_status_label_running);
			}
		};
		manager.registerReceiver(startedReceiver, startedIntentFilter);

		IntentFilter stoppedIntentFilter = new IntentFilter(ServiceUtils.ACTION_STOPPED);
		BroadcastReceiver stoppedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				statusLabel.setText(R.string.run_status_label);
			}
		};
		manager.registerReceiver(stoppedReceiver, stoppedIntentFilter);
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment StartFragment.
	 */
	public static StartFragment newInstance(OnFragmentInteractionListener listener) {
		StartFragment fragment = new StartFragment();
		fragment.mListener = listener;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_start, container, false);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		void onFragmentStartInteraction();
		void onFragmentStopInteraction();
	}

	@Override
	public void onResume() {
		super.onResume();
		StunnelIntentService.checkStatus(getActivity());
	}
}
