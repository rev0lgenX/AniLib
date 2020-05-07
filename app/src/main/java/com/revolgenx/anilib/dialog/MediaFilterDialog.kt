package com.revolgenx.anilib.dialog

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.pranavpandey.android.dynamic.support.adapter.DynamicSpinnerImageAdapter
import com.pranavpandey.android.dynamic.support.dialog.DynamicDialog
import com.pranavpandey.android.dynamic.support.dialog.fragment.DynamicDialogFragment
import com.pranavpandey.android.dynamic.support.model.DynamicSpinnerItem
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.pranavpandey.android.dynamic.support.widget.DynamicButton
import com.revolgenx.anilib.R
import com.revolgenx.anilib.field.home.SeasonField
import com.revolgenx.anilib.field.home.TrendingMediaField
import com.revolgenx.anilib.field.media.MediaField
import com.revolgenx.anilib.preference.getSeasonField
import com.revolgenx.anilib.util.onItemSelected
import kotlinx.android.synthetic.main.season_filter_layout.*
import java.util.*
import kotlin.math.round

class MediaFilterDialog : DynamicDialogFragment() {

    var onDoneListener: (() -> Unit)? = null

    private val field: MediaField by lazy {
        when (mediaFilterType) {
            MediaFilterType.SEASON.ordinal -> {
                getSeasonField(requireContext())
            }
            else -> {
                MediaField()
            }
        }
    }

    private val mediaFilterType by lazy {
        arguments?.getInt(mediaFilterTypeKey)
    }

    private val listFormatItems by lazy {
        requireContext().resources.getStringArray(R.array.advance_search_format).map {
            DynamicSpinnerItem(
                null, it
            )
        }
    }

    private val listStatusItems by lazy {
        requireContext().resources.getStringArray(R.array.advance_search_status).map {
            DynamicSpinnerItem(
                null, it
            )
        }
    }
    private val seasons by lazy {
        requireContext().resources.getStringArray(if (mediaFilterType == MediaFilterType.SEASON.ordinal) R.array.media_season else R.array.advance_search_season)
            .map {
                DynamicSpinnerItem(
                    null, it
                )
            }
    }

    companion object {
        fun newInstance(type: Int): MediaFilterDialog {
            return MediaFilterDialog().also {
                it.arguments = bundleOf(mediaFilterTypeKey to type)
            }
        }

        const val mediaFilterTypeKey = "mediaFilterTypeKey"
    }

    enum class MediaFilterType {
        SEASON, TRENDING, POPULAR
    }


    override fun onCustomiseBuilder(
        dialogBuilder: DynamicDialog.Builder,
        savedInstanceState: Bundle?
    ): DynamicDialog.Builder {
        with(dialogBuilder) {
            setTitle(R.string.season_filter)
            setView(R.layout.season_filter_layout)
            setPositiveButton(R.string.done) { dialogInterface, _ ->
                if (dialogInterface is DynamicDialog) {
                    if (field is SeasonField)
                        (field as SeasonField).saveSeasonField(requireContext())
                    else if (field is TrendingMediaField) {
                        (field as TrendingMediaField).saveTrendingField(requireContext())
                    }
                    onDoneListener?.invoke()
                }
            }

            setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            isAutoDismiss = false
        }
        return super.onCustomiseBuilder(dialogBuilder, savedInstanceState)
    }

    override fun onCustomiseDialog(
        alertDialog: DynamicDialog,
        savedInstanceState: Bundle?
    ): DynamicDialog {
        with(alertDialog) {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                    (it as DynamicButton).isAllCaps = false
                }

                getButton(AlertDialog.BUTTON_NEGATIVE)?.let {
                    (it as DynamicButton).isAllCaps = false
                }

                seasonStatusSpinner.adapter = makeSpinnerAdapter(listStatusItems)
                seasonFormatSpinner.adapter = makeSpinnerAdapter(listFormatItems)
                seasonSpinner.adapter = makeSpinnerAdapter(seasons)



                field.status?.let {
                    seasonStatusSpinner.setSelection(it + 1)
                }
                field.format?.let {
                    seasonFormatSpinner.setSelection(it + 1)
                }
                field.season?.let {
                    seasonSpinner.setSelection(if (field is SeasonField) it else it + 1)
                }

                seasonYearSeekBar.setIndicatorTextDecimalFormat("0")
                seasonYearSeekBar.setTypeface(
                    ResourcesCompat.getFont(
                        context,
                        R.font.open_sans_light
                    )
                )

                DynamicTheme.getInstance().get().accentColor.let {
                    seasonYearSeekBar.progressColor = it
                    seasonYearSeekBar.leftSeekBar?.indicatorBackgroundColor = it
                }

                val currentYear = Calendar.getInstance().get(Calendar.YEAR) + 1f

                seasonYearSeekBar.setRange(1950f, currentYear)

                field.seasonYear?.toFloat()?.let {
                    seasonYearSeekBar.setProgress(it)
                }
                seasonStatusSpinner.onItemSelected {
                    field.status = it.minus(1).takeIf { it >= 0 }
                }
                seasonFormatSpinner.onItemSelected {
                    field.format = it.minus(1).takeIf { it >= 0 }
                }
                seasonSpinner.onItemSelected {
                    field.season = if (field is SeasonField) it else it.minus(1).takeIf { it >= 0 }
                }
                seasonYearSeekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
                    override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                    }

                    override fun onRangeChanged(
                        view: RangeSeekBar?,
                        leftValue: Float,
                        rightValue: Float,
                        isFromUser: Boolean
                    ) {
                    }

                    override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                        view?.leftSeekBar?.progress?.let {
                            field.seasonYear = round(it).toInt()
                        }
                    }
                })
            }
            return super.onCustomiseDialog(alertDialog, savedInstanceState)
        }
    }


    private fun makeSpinnerAdapter(items: List<DynamicSpinnerItem>) =
        DynamicSpinnerImageAdapter(
            requireContext(),
            R.layout.ads_layout_spinner_item,
            R.id.ads_spinner_item_icon,
            R.id.ads_spinner_item_text, items
        )

}

