package com.revolgenx.anilib.ui.dialog

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.viewbinding.ViewBinding
import com.pranavpandey.android.dynamic.support.dialog.DynamicDialog
import com.pranavpandey.android.dynamic.support.widget.DynamicButton
import com.revolgenx.anilib.common.ui.dialog.BaseDialogFragment

class MessageDialog : BaseDialogFragment<ViewBinding>() {
    companion object {

        const val titleKey = "titleKey"
        const val messageKey = "messageKey"
        const val messageResKey = "messageResKey"
        const val positiveKey = "positiveKey"
        const val negativeKey = "negativeKey"
        const val neutralKey = "neutralKey"
        const val viewKey = "viewKey"
        val messageDialogTag = MessageDialog::class.java.simpleName

        class Builder {

            var titleRes: Int? = null
            var messageRes: Int? = null
            var message: String? = null
            var view: Int? = null
            var positiveTextRes: Int? = null
            var negativeTextRes: Int? = null
            var neutralTextRes: Int? = null

            fun build(): MessageDialog {
                return MessageDialog().also {
                    it.arguments = bundleOf(
                        titleKey to titleRes,
                        messageResKey to messageRes,
                        messageKey to message,
                        positiveKey to positiveTextRes,
                        negativeKey to negativeTextRes,
                        neutralKey to neutralTextRes,
                        viewKey to view
                    )
                }
            }
        }
    }

    override fun bindView(): ViewBinding? {
        return null
    }

    override val titleRes: Int? get() = arguments?.getInt(titleKey).takeIf { it != 0 }
    override val positiveText: Int? get() = arguments?.getInt(positiveKey).takeIf { it != 0 }
    override val negativeText: Int? get() = arguments?.getInt(negativeKey).takeIf { it != 0 }
    override val neutralText: Int? get() = arguments?.getInt(neutralKey).takeIf { it != 0 }

    override fun onCustomiseBuilder(
        dialogBuilder: DynamicDialog.Builder,
        savedInstanceState: Bundle?
    ): DynamicDialog.Builder {
        with(dialogBuilder) {
            arguments?.apply {
                if (getInt(messageResKey) != 0) {
                    setMessage(getInt(messageResKey))
                }
                getString(messageKey)?.let {
                    setMessage(it)
                }
                if (getInt(viewKey) != 0) {
                    val viewRes = getInt(viewKey)
                    setView(viewRes)
                }
            }
        }

        return super.onCustomiseBuilder(dialogBuilder, savedInstanceState)
    }

    override fun onCustomiseDialog(
        alertDialog: DynamicDialog,
        savedInstanceState: Bundle?
    ): DynamicDialog {
        with(alertDialog) {
            setOnShowListener {
                findViewById<TextView>(android.R.id.message)?.textSize = 14f
                getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                    (it as DynamicButton).isAllCaps = false
                }
                getButton(AlertDialog.BUTTON_NEGATIVE)?.let {
                    (it as DynamicButton).isAllCaps = false
                }
            }
            return super.onCustomiseDialog(alertDialog, savedInstanceState)
        }
    }
}