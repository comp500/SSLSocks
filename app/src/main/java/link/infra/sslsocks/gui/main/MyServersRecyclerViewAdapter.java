package link.infra.sslsocks.gui.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import link.infra.sslsocks.R;
import link.infra.sslsocks.dummy.DummyContent.DummyItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link ServersFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyServersRecyclerViewAdapter extends RecyclerView.Adapter<MyServersRecyclerViewAdapter.ViewHolder> {

	private final List<DummyItem> mValues;
	private final ServersFragment.OnListFragmentInteractionListener mListener;

	MyServersRecyclerViewAdapter(List<DummyItem> items, ServersFragment.OnListFragmentInteractionListener listener) {
		mValues = items;
		mListener = listener;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.fragment_servers, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
		holder.mItem = mValues.get(position);
		holder.mIdView.setText(mValues.get(position).id);
		holder.mContentView.setText(mValues.get(position).content);

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
		final View mView;
		final TextView mIdView;
		final TextView mContentView;
		DummyItem mItem;

		ViewHolder(View view) {
			super(view);
			mView = view;
			mIdView = view.findViewById(R.id.id);
			mContentView = view.findViewById(R.id.content);
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mContentView.getText() + "'";
		}
	}
}
