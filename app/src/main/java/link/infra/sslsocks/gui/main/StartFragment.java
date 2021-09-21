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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import link.infra.sslsocks.R;
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
		final SwitchCompat startSwitch = requireView().findViewById(R.id.start_switch);
		startSwitch.setEnabled(true);
		startSwitch.setText(R.string.run_status_not_running);

		final CompoundButton.OnCheckedChangeListener changeListener = (compoundButton, isChecked) -> {
			if (isChecked) {
				if (mListener != null) {
					startSwitch.setText(R.string.run_status_starting);
					mListener.onFragmentStartInteraction();
				}
			} else {
				if (mListener != null) {
					startSwitch.setText(R.string.run_status_stopping);
					mListener.onFragmentStopInteraction();
				}
			}
		};
		startSwitch.setOnCheckedChangeListener(changeListener);

		FragmentActivity act = getActivity();
		if (act == null) {
			return;
		}
		StunnelIntentService.isRunning.observe(getViewLifecycleOwner(), aBoolean -> {
			if (aBoolean) {
				startSwitch.setText(R.string.run_status_running);
				startSwitch.setOnCheckedChangeListener(null);
				startSwitch.setChecked(true);
				startSwitch.setOnCheckedChangeListener(changeListener);
			} else {
				startSwitch.setText(R.string.run_status_not_running);
				startSwitch.setOnCheckedChangeListener(null);
				startSwitch.setChecked(false);
				startSwitch.setOnCheckedChangeListener(changeListener);
			}
		});
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment StartFragment.
	 */
	static StartFragment newInstance(OnFragmentInteractionListener listener) {
		StartFragment fragment = new StartFragment();
		fragment.mListener = listener;
		return fragment;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_start, container, false);
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
