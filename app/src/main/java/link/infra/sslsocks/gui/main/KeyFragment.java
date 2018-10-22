package link.infra.sslsocks.gui.main;

import android.content.Context;
import android.os.Bundle;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_key_list, container, false);

		// Set the adapter
		Context context = view.getContext();

		List<KeyRecyclerViewAdapter.KeyItem> items = new ArrayList<>();
		File folder = context.getFilesDir();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getPath().endsWith(".p12") || fileEntry.getPath().endsWith(".pem")) { // Only show .p12 or .pem files
				items.add(new KeyRecyclerViewAdapter.KeyItem(fileEntry.getName()));
			}
		}

		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setAdapter(new KeyRecyclerViewAdapter(items, this));

		TextView emptyView = (TextView) view.findViewById(R.id.empty_view);

		// Show text if there are no items
		if (items.isEmpty()) {
			recyclerView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		} else {
			recyclerView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
		}
		return view;
	}

	public void onListFragmentInteraction(KeyRecyclerViewAdapter.KeyItem mItem) {

	}

	public void addKey() {

	}
}
