package com.revolgenx.anilib.ui.fragment.settings

import android.os.Bundle
import android.text.InputType
import android.view.*
import com.pranavpandey.android.dynamic.support.model.DynamicSpinnerItem
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.preference.userId
import com.revolgenx.anilib.common.ui.fragment.BaseLayoutFragment
import com.revolgenx.anilib.data.field.setting.MediaListSettingMutateField
import com.revolgenx.anilib.data.model.entry.AdvancedScore
import com.revolgenx.anilib.data.model.setting.MediaListOptionModel
import com.revolgenx.anilib.databinding.MediaListSettingFragmentBinding
import com.revolgenx.anilib.infrastructure.repository.util.Status
import com.revolgenx.anilib.type.ScoreFormat
import com.revolgenx.anilib.ui.dialog.InputDialog
import com.revolgenx.anilib.ui.view.makeSpinnerAdapter
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.ui.viewmodel.setting.SettingViewModel
import com.revolgenx.anilib.util.onItemSelected
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaListSettingFragment : BaseLayoutFragment<MediaListSettingFragmentBinding>() {


    override var setHomeAsUp: Boolean = true
    override var titleRes: Int? = R.string.list

    private val viewModel by viewModel<SettingViewModel>()

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): MediaListSettingFragmentBinding {
        return MediaListSettingFragmentBinding.inflate(inflater, parent, false)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val scoringSystemList =
            requireContext().resources.getStringArray(R.array.scoring_system_list).map {
                DynamicSpinnerItem(null, it)
            }

        val defaultSpinnerOrder =
            requireContext().resources.getStringArray(R.array.media_list_order_list).map {
                DynamicSpinnerItem(null, it)
            }

        val scoringAdapter = makeSpinnerAdapter(requireContext(), scoringSystemList)
        val listOrderAdapter = makeSpinnerAdapter(requireContext(), defaultSpinnerOrder)

        binding.scoringSystemSpinner.spinnerView.adapter = scoringAdapter
        binding.listOrderSpinner.spinnerView.adapter = listOrderAdapter
        binding.advanceScoreRecyclerView.setFragmentManager(childFragmentManager)

        viewModel.mediaListSettingLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.showLoading(false)
                    val data: MediaListOptionModel = it.data ?: return@observe
                    binding.updateView(data)
                    binding.updateListener(data)
                }
                Status.ERROR -> {
                    binding.showLoading(false)
                    makeToast(R.string.something_went_wrong, icon = R.drawable.ic_error)
                }
                Status.LOADING -> {
                    binding.showLoading(true)
                    val data = it.data!!
                    binding.updateView(data)
                }
            }
        }

        if (savedInstanceState == null) {
            with(viewModel.mediaListSettingField) {
                userId = requireContext().userId()
            }
            viewModel.getListSetting(requireContext())
        }
    }

    private fun MediaListSettingFragmentBinding.updateView(model: MediaListOptionModel) {
        scoringSystemSpinner.spinnerView.setSelection(model.scoreFormat!!)
        listOrderSpinner.spinnerView.setSelection(model.rowOrder!!)

        when (ScoreFormat.values()[model.scoreFormat!!]) {
            ScoreFormat.POINT_100, ScoreFormat.POINT_10_DECIMAL -> {
                advanceScoreSwitch.visibility = View.VISIBLE
                advanceScoreSwitch.isChecked = model.animeList!!.advancedScoringEnabled

                if (model.animeList!!.advancedScoringEnabled) {
                    advanceScoreHeader.visibility = View.VISIBLE
                    advanceScoreRecyclerView.visibility = View.VISIBLE
                } else {
                    advanceScoreHeader.visibility = View.GONE
                    advanceScoreRecyclerView.visibility = View.GONE
                }
                binding.updateAdvanceScore(model)
            }
            else -> {
                advanceScoreSwitch.visibility = View.GONE
                advanceScoreHeader.visibility = View.GONE
                advanceScoreRecyclerView.visibility = View.GONE
            }
        }

    }

    private fun MediaListSettingFragmentBinding.updateListener(model: MediaListOptionModel) {
        scoringSystemSpinner.spinnerView.onItemSelected {
            when (ScoreFormat.values()[it]) {
                ScoreFormat.POINT_100, ScoreFormat.POINT_10_DECIMAL -> {
                    advanceScoreSwitch.visibility = View.VISIBLE
                    advanceScoreSwitch.isChecked = model.animeList!!.advancedScoringEnabled
                    if (model.animeList!!.advancedScoringEnabled) {
                        advanceScoreHeader.visibility = View.VISIBLE
                        advanceScoreRecyclerView.visibility = View.VISIBLE
                    } else {
                        advanceScoreHeader.visibility = View.GONE
                        advanceScoreRecyclerView.visibility = View.GONE
                    }
                    binding.updateAdvanceScore(model)
                }
                else -> {
                    advanceScoreSwitch.visibility = View.GONE
                    advanceScoreHeader.visibility = View.GONE
                    advanceScoreRecyclerView.visibility = View.GONE
                }
            }
        }
        advanceScoreSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                advanceScoreHeader.visibility = View.VISIBLE
                advanceScoreRecyclerView.visibility = View.VISIBLE
            } else {
                advanceScoreHeader.visibility = View.GONE
                advanceScoreRecyclerView.visibility = View.GONE
            }
        }

        advanceScoreHeader.setOnClickListener {
            val inputDialog = InputDialog.newInstance(null, InputType.TYPE_CLASS_TEXT, null, false)
            inputDialog.onInputDoneListener = {
                advanceScoreRecyclerView.addTag(AdvancedScore(it, 0.0))
            }
            inputDialog.show(childFragmentManager, "chip_input_dialog")
        }
    }

    private fun MediaListSettingFragmentBinding.updateAdvanceScore(model: MediaListOptionModel) {
        model.animeList?.advancedScoring?.let {
            advanceScoreRecyclerView.submitTags(it)
        }
    }

    private fun MediaListSettingFragmentBinding.saveMediaListSetting() {
        val model = viewModel.mediaListSettingLiveData.value?.data ?: return

        model.scoreFormat = scoringSystemSpinner.spinnerView.selectedItemPosition
        if(model.scoreFormat == ScoreFormat.POINT_100.ordinal || model.scoreFormat == ScoreFormat.POINT_10_DECIMAL.ordinal){
            model.animeList!!.advancedScoringEnabled = advanceScoreSwitch.isChecked
        }

        model.rowOrder = listOrderSpinner.spinnerView.selectedItemPosition
        viewModel.setMediaListSetting(requireContext(), MediaListSettingMutateField(model))
            .observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.showLoading(false)
                        makeToast(R.string.saved_successfully)
                    }
                    Status.ERROR -> {
                        binding.showLoading(false)
                        makeToast(R.string.error_occured_while_saving)
                    }
                    Status.LOADING -> {
                        binding.showLoading(true)
                    }
                }
            }
    }

    private fun MediaListSettingFragmentBinding.showLoading(b: Boolean) {
        mediaListSettingProgressBar.root.visibility = if (b) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_menu -> {
                binding.saveMediaListSetting()
                true
            }
            else -> {
                false
            }
        }
    }


}
