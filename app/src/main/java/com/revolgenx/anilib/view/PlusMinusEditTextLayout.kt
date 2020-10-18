package com.revolgenx.anilib.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.core.widget.doOnTextChanged
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.pranavpandey.android.dynamic.support.widget.DynamicEditText
import com.pranavpandey.android.dynamic.support.widget.DynamicImageView
import com.pranavpandey.android.dynamic.utils.DynamicUnitUtils
import com.revolgenx.anilib.R
import com.revolgenx.anilib.controller.ThemeController
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.dp

//todo add min max filter
class PlusMinusEditTextLayout(context: Context, attributeSet: AttributeSet?, set: Int = 0) :
    RelativeLayout(context, attributeSet, set) {

    private val accentColor by lazy {
        DynamicTheme.getInstance().get().accentColor
    }
    private val surfaceColor by lazy {
        ThemeController.lightSurfaceColor()
    }

    var max: Double? = null

    var counterHolder = 0.0
    private var textChanged: ((char: CharSequence) -> Unit)? = null

    fun updateDynamicText() {
        when (dynamicInputType) {
            InputType.TYPE_NUMBER_FLAG_DECIMAL -> {
                dynamicNumberEditText.setText(counterHolder.toString())
            }
            InputType.TYPE_CLASS_NUMBER -> {
                dynamicNumberEditText.setText(counterHolder.toInt().toString())
            }
            else -> {
                dynamicNumberEditText.setText(counterHolder.toString())
            }
        }
    }

    var dynamicInputType: Int? = null
        set(value) {
            field = value
            dynamicNumberEditText.inputType = field!!
        }

    private val dynamicNumberEditText by lazy {
        DynamicEditText(context, attributeSet).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(40f)).also {
                it.addRule(LEFT_OF, R.id.decrementButtonId)
                it.addRule(CENTER_VERTICAL)
                it.marginStart = dp(6f)
            }
            this.background = null
            typeface = ResourcesCompat.getFont(context, R.font.open_sans_regular)
            textSize = 14f
        }
    }

    private val dynamicIncrementIv by lazy {
        DynamicImageView(context, attributeSet).apply {
            this.id = R.id.incrementButtonId
            layoutParams = LayoutParams(
                dp(40f),
                dp(40f)
            ).also {
                it.addRule(ALIGN_PARENT_END)
                it.addRule(CENTER_VERTICAL)
            }
            this.setPadding(DynamicUnitUtils.convertDpToPixels(10f))
            setImageResource(R.drawable.ic_plus)
            this.color = accentColor
        }
    }
    private val dynamicDecrementIv by lazy {
        DynamicImageView(context, attributeSet).apply {
            this.id = R.id.decrementButtonId
            layoutParams = LayoutParams(
                DynamicUnitUtils.convertDpToPixels(40f),
                LayoutParams.MATCH_PARENT
            ).also {
                it.addRule(CENTER_VERTICAL)
                it.addRule(LEFT_OF, R.id.incrementButtonId)
            }
            this.setPadding(DynamicUnitUtils.convertDpToPixels(10f))
            setImageResource(R.drawable.ic_minus)
            this.color = accentColor
        }
    }


    constructor(context: Context) : this(context, null)
    constructor(
        context: Context,
        attributeSet: AttributeSet?
    ) : this(context, attributeSet, 0) {
        addView(dynamicNumberEditText)
        addView(dynamicIncrementIv)
        addView(dynamicDecrementIv)

        dynamicNumberEditText.doOnTextChanged { text, _, _, _ ->
            if (text!!.isEmpty()) {
                dynamicNumberEditText.setText("0")
                return@doOnTextChanged
            }
            try {
                counterHolder = text.toString().trim().toDouble()
                textChanged?.invoke(text)
            } catch (e: NumberFormatException) {
                context.makeToast(
                    R.string.invalid_input_type,
                    icon = R.drawable.ic_error
                )
            }
        }
        dynamicInputType = InputType.TYPE_CLASS_NUMBER
        updateDynamicText()

        this.setBackgroundColor(surfaceColor)

        dynamicIncrementIv.setOnClickListener {
            //            counterHolder = (((counterHolder * 10).toInt() + 10) / 10).toDouble()
            counterHolder++
            if (max != null) {
                if (counterHolder > max!!) {
                    counterHolder = max!!
                }
            }
            updateDynamicText()
        }

        dynamicDecrementIv.setOnClickListener {
            //            counterHolder = (((counterHolder * 10).toInt() - 10) / 10).toDouble()
            counterHolder--
            if (counterHolder < 0) {
                counterHolder = 0.0
            }
            updateDynamicText()
        }
    }

    fun textChangeListener(listener: ((char: CharSequence) -> Unit)? = null) {
        this.textChanged = listener
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        dynamicNumberEditText.visibility = visibility
        dynamicIncrementIv.visibility = visibility
        dynamicDecrementIv.visibility = visibility
    }

    fun setCounter(counter: Double?) {
        if (counter == null) {
            return
        }
        counterHolder = counter
        if(max != null) {
            if (counter > max!!) {
                counterHolder = max!!
            }
        }
        updateDynamicText()
    }

    val dynamictext: String
        get() {
            return dynamicNumberEditText.text.toString()
        }

}

