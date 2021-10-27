package com.memad.imagenotes.utils

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

infix fun Context.loadingDialog(loadingDialogBinding: View): AlertDialog {
    return MaterialAlertDialogBuilder(this).apply {
        setView(loadingDialogBinding)
        setCancelable(false)
    }.create()
}