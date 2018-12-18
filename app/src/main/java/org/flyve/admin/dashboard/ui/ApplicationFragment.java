package org.flyve.admin.dashboard.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import org.flyve.admin.dashboard.R;
import org.flyve.admin.dashboard.adapter.ApplicationAdapter;
import org.flyve.admin.dashboard.adapter.ApplicationTouchHelper;
import org.flyve.admin.dashboard.model.ApplicationModel;
import org.flyve.admin.dashboard.utils.FlyveLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ApplicationFragment extends Fragment {

    private ProgressBar pb;
    private RecyclerView lst;
    private List<ApplicationModel> data;
    private ApplicationAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_application, container, false);

        pb = v.findViewById(R.id.pb);
        pb.setVisibility(View.VISIBLE);

        lst = v.findViewById(R.id.lst);

        LinearLayoutManager llm = new LinearLayoutManager(ApplicationFragment.this.getActivity());
        lst.setLayoutManager(llm);

        lst.setItemAnimator(new DefaultItemAnimator());
        lst.addItemDecoration(new DividerItemDecoration(ApplicationFragment.this.getContext(), DividerItemDecoration.VERTICAL));

        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ApplicationTouchHelper(0, ItemTouchHelper.LEFT, new ApplicationTouchHelper.RecyclerItemTouchHelperListener() {

            /**
             * callback when recycler view is swiped
             * item will be removed on swiped
             * undo option will be provided in snackbar to restore the item
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (viewHolder instanceof ApplicationAdapter.DataViewHolder) {
                    // get the removed item name to display it in snack bar
                    String name = data.get(viewHolder.getAdapterPosition()).getName();

                    // backup of removed item for undo purpose
                    final ApplicationModel deletedItem = data.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();

                    // remove the item from recycler view
                    mAdapter.removeItem(viewHolder.getAdapterPosition());

                    // showing snack bar with Undo option
                    Snackbar snackbar = Snackbar
                            .make(ApplicationFragment.this.getView(), name + " removed from list", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // undo is selected, restore the deleted item
                            mAdapter.restoreItem(deletedItem, deletedIndex);
                        }
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(lst);

        load(loadJSONFromAsset());

        return v;
    }

    public String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getActivity().getAssets().open("json/packages.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            FlyveLog.e(ex.getMessage());
            return null;
        }
        return json;
    }

    public void load(String jsonStr) {

        data = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(jsonStr);

            JSONArray items = json.getJSONArray("data");
            for (int y = 0; y < items.length(); y++) {

                JSONObject obj = items.getJSONObject(y);

                ApplicationModel model = new ApplicationModel(ApplicationModel.NO_HEADER);

                model.setName(obj.getString("PluginFlyvemdmPackage.alias"));
                model.setPackages(obj.getString("PluginFlyvemdmPackage.name"));
                model.setIcon(obj.getString("PluginFlyvemdmPackage.icon"));
                model.setVersion(obj.getString("PluginFlyvemdmPackage.version"));

                data.add(model);
            }

            pb.setVisibility(View.GONE);

            mAdapter = new ApplicationAdapter(data, new ApplicationAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(ApplicationModel item) {
                    openDetail(item);
                }
            });
            lst.setAdapter(mAdapter);

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            FlyveLog.e(ex.getMessage());
        }
    }

    private void openDetail(ApplicationModel item) {
        Intent miIntent = new Intent(ApplicationFragment.this.getActivity(), ApplicationDetailActivity.class);
        ApplicationFragment.this.startActivity(miIntent);
    }

}
