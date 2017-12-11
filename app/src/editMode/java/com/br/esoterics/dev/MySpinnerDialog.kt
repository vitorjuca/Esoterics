package com.br.esoterics.dev

import android.app.Dialog
import android.app.DialogFragment
import android.app.ProgressDialog
import android.os.Bundle

/**
 * Created by vaniajuca on 08/11/17.
 */


class MySpinnerDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = ProgressDialog(activity)
        this.setStyle(android.app.DialogFragment.STYLE_NO_TITLE, theme)
        dialog.setMessage("Loading...")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
}