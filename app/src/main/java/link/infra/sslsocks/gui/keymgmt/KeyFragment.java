package link.infra.sslsocks.gui.keymgmt;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import link.infra.sslsocks.R;

/**
 * A fragment representing the key list
 */
public class KeyFragment extends Fragment {

	private OnListFragmentInteractionListener mListener;
	private RecyclerView recyclerView;
	private TextView emptyView;
	private List<KeyRecyclerViewAdapter.KeyItem> items = new ArrayList<>();
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public KeyFragment() {
	}

	public static KeyFragment newInstance() {
		return new KeyFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_key_list, container, false);

		// Set the adapter
		Context context = view.getContext();

		recyclerView = view.findViewById(R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setAdapter(new KeyRecyclerViewAdapter(items, mListener));

		emptyView = view.findViewById(R.id.empty_view);

		updateList(context);
		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnListFragmentInteractionListener) {
			mListener = (OnListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
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
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnListFragmentInteractionListener {
		void onListFragmentInteraction(KeyRecyclerViewAdapter.KeyItem item);
	}

	public void updateList(Context context) {
		items.clear();
		File folder = context.getFilesDir();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getPath().endsWith(".p12") || fileEntry.getPath().endsWith(".pem")) { // Only show .p12 or .pem files
				items.add(new KeyRecyclerViewAdapter.KeyItem(fileEntry.getName()));
			}
		}
		recyclerView.getAdapter().notifyDataSetChanged();

		// Show text if there are no items
		if (items.isEmpty()) {
			recyclerView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		} else {
			recyclerView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
		}
	}

}
