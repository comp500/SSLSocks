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
