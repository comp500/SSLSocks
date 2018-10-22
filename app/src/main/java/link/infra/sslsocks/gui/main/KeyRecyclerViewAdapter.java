package link.infra.sslsocks.gui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import link.infra.sslsocks.R;

public class KeyRecyclerViewAdapter extends RecyclerView.Adapter<KeyRecyclerViewAdapter.ViewHolder> {

	public static class KeyItem {
		public final String filename;
		public KeyItem(String filename) {
			this.filename = filename;
		}
	}

	private final List<KeyItem> mValues;
	private final KeyFragment mListener;

	public KeyRecyclerViewAdapter(List<KeyItem> items, KeyFragment listener) {
		mValues = items;
		mListener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.fragment_key, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.mItem = mValues.get(position);
		holder.mFileNameView.setText(mValues.get(position).filename);

		holder.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mListener) {
					// Notify the active callbacks interface (the activity, if the
					// fragment is attached to one) that an item has been selected.
					mListener.onListFragmentInteraction(holder.mItem);
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return mValues.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public final View mView;
		public final TextView mFileNameView;
		public KeyItem mItem;

		public ViewHolder(View view) {
			super(view);
			mView = view;
			mFileNameView = (TextView) view.findViewById(R.id.file_name);
		}
	}
}
