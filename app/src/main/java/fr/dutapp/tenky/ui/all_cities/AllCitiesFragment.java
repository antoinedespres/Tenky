package fr.dutapp.tenky.ui.all_cities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import fr.dutapp.tenky.R;

public class AllCitiesFragment extends Fragment {

    private AllCitiesViewModel allCitiesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        allCitiesViewModel =
                new ViewModelProvider(this).get(AllCitiesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_all_cities, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        allCitiesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}