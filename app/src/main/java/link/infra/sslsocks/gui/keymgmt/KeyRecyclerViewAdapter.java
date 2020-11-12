package link.infra.sslsocks.gui.keymgmt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import link.infra.sslsocks.R;

public class KeyRecyclerViewAdapter extends RecyclerView.Adapter<KeyRecyclerViewAdapter.ViewHolder> {

	public static class KeyItem {
		public final String filename;
		KeyItem(String filename) {
			this.filename = filename;
		}
	}

	private final List<KeyItem> mValues;
	private final KeyFragment.OnListFragmentInteractionListener mListener;

	KeyRecyclerViewAdapter(List<KeyItem> items, KeyFragment.OnListFragmentInteractionListener listener) {
		mValues = items;
		mListener = listener;
	}

	@Override
	@NonNull
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.fragment_key, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
		holder.mItem = mValues.get(position);
		holder.mFileNameView.setText(mValues.get(position).filename);

		holder.mView.setOnClickListener(v -> {
			if (null != mListener) {
				// Notify the active callbacks interface (the activity, if the
				// fragment is attached to one) that an item has been selected.
				mListener.onListFragmentInteraction(holder.mItem);
			}
		});
	}

	@Override
	public int getItemCount() {
		return mValues.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		final View mView;
		final TextView mFileNameView;
		KeyItem mItem;

		ViewHolder(View view) {
			super(view);
			mView = view;
			mFileNameView = view.findViewById(R.id.file_name);
		}
	}
}
