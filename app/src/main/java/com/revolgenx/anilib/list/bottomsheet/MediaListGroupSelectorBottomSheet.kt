package com.revolgenx.anilib.list.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.otaliastudios.elements.Adapter
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ui.bottomsheet.BottomSheetFragment
import com.revolgenx.anilib.databinding.SelectorBottomSheetBinding
import com.revolgenx.anilib.databinding.SelectorItemLayoutBinding

class MediaListGroupSelectorBottomSheet :
    BottomSheetFragment<SelectorBottomSheetBinding>() {

    private var callback: ((selected: String) -> Unit)? = null

    companion object {
        private const val media_list_group_selector_arg_key = "media_list_group_selector_arg_key"
        fun newInstance(
            groupNameList: List<Pair<Pair<String, Int>, Boolean>>,
            callback: (selected: String) -> Unit
        ) =
            MediaListGroupSelectorBottomSheet().also {
                it.arguments = bundleOf(media_list_group_selector_arg_key to groupNameList)
                it.callback = callback
            }
    }

    private val groupNameList get() = arguments?.get(media_list_group_selector_arg_key) as? List<Pair<Pair<String, Int>, Boolean>>
    override fun bindView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): SelectorBottomSheetBinding {
        return SelectorBottomSheetBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val groupNameSelectorList = groupNameList ?: return

        Adapter.builder(viewLifecycleOwner)
            .addPresenter(
                Presenter.simple<Pair<Pair<String, Int>, Boolean>>(
                    requireContext(),
                    R.layout.selector_item_layout,
                    0
                ) { v, item ->
                    SelectorItemLayoutBinding.bind(v).apply {
                        val groupNameWithCount = item.first
                        val isSelected = item.second

                        selectorItemCb.text = "%s %d".format(groupNameWithCount.first, groupNameWithCount.second)
                        selectorItemCb.isChecked = isSelected
                        selectorItemCb.setOnCheckedChangeListener { _, _ ->
                            callback?.invoke(groupNameWithCount.first)
                            dismiss()
                        }
                    }
                })
            .addSource(Source.fromList(groupNameSelectorList))
            .into(binding.selectorRecyclerView)
    }
}