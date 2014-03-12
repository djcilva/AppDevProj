package com.example.trailheadmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class AddMarkerDialog extends DialogFragment {
	
	private View layout;
	private AddMarkerDialogListener listener;
	
	private EditText trailNameField;
	private String trailNameText = "";
	
	public static final int RATE_GOOD_INDEX = 0;
	public static final int RATE_BAD_INDEX = 1;
	public static final int DO_NOT_RATE_INDEX = 2;
	
	/** Interface to for parent fragment to use for callbacks. */
	public interface AddMarkerDialogListener {
        public void onAddMarkerClick(DialogFragment dialog);
    }
	
	/** Instantiate the parent fragment as the listener. */
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (AddMarkerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AddMarkerDialogListener");
        }
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        layout = getActivity().getLayoutInflater().inflate(R.layout.diag_addmarker, null);
        trailNameField = (EditText) layout.findViewById(R.id.new_trail_name);
        
		// Use the a builder to construct the dialog,
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.diag_addmarker_title);
        builder.setView(layout);
        builder.setPositiveButton(R.string.diag_add_button, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       if (trailNameField == null)
                    	   Log.d("trails", "EditText field is null");
                	   trailNameText = trailNameField.getText().toString();
                       listener.onAddMarkerClick(AddMarkerDialog.this);
                   }
               });
        builder.setNegativeButton(R.string.diag_cancel_button, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Return nothing to main fragment, just close the dialog
                   }
               });
        // Return the AlertDialog object.
        return builder.create();
    }
	
	/** Method to return the trail head name entered by the user to the main fragment. */
	public String getNewMarkerName() {
		if (this.trailNameText.isEmpty()) {
			return getActivity().getString(R.string.unnamed_trailhead);
		}
		return this.trailNameText;
	}
}
