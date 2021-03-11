package fr.dutapp.tenky;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Map;

public class AddDialog extends DialogFragment {
    private int mSize;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.fragment_add, null))
                // Add action buttons
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = ((EditText) getActivity().findViewById(R.id.CityName)).getText().toString();
                        if(name == null){
                            new NoNameAlertDialog().show(getParentFragmentManager(), null);
                        }

                        SharedPreferences s = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = s.edit();
                        edit.putString("ville"+s.getInt("nbrCities", 0)+"", name);
                        edit.putInt("nbrCities", s.getInt("nbrCities",0)+1);
                        Log.d("alertdialog", s.getAll().toString());
                        edit.apply();
                        Intent intent = new Intent(getActivity(), AllCitiesActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
