package com.example.trailheadmap;

import com.example.trailheadmap.AddMarkerDialog.AddMarkerDialogListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.EditText;

public class RateMarkerDialog extends DialogFragment {
	
	private RateMarkerDialogListener listener;
	
	/** Interface to for parent fragment to use for callbacks. */
	public interface RateMarkerDialogListener {
        public void onRateMarkerClick(DialogFragment dialog);
    }
	
	/** Instantiate the parent fragment as the listener. 
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (RateMarkerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RateMarkerDialogListener");
        }
    }*/
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
		// Use the a builder to construct the dialog,
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.diag_ratemarker_title);
        /*builder.setItems(R.array.diag_rating_button_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            // The 'which' argument contains the index position
            // of the selected item
            }
        });*/
        // Return the AlertDialog object.
        return builder.create();
    }
}
