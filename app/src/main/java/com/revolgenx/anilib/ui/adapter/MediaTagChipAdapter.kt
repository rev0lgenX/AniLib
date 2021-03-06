package com.revolgenx.anilib.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.revolgenx.anilib.R
import com.revolgenx.anilib.data.model.MediaTagsModel
import com.revolgenx.anilib.data.model.search.filter.MediaSearchFilterModel
import com.revolgenx.anilib.databinding.ChipTagPresenterBinding
import com.revolgenx.anilib.infrastructure.event.BrowseTagEvent

class MediaTagChipAdapter(val onTagInfoClicked: (MediaTagsModel) -> Unit) :
    RecyclerView.Adapter<MediaTagChipAdapter.TagChipViewHolder>() {

    private var currentList: List<MediaTagsModel>? = null
    private var originalList: List<MediaTagsModel>? = null
    private var spoilerShown = false
    private var hasSpoilerTags = false

    fun submitList(list: List<MediaTagsModel>?) {
        originalList = list
        currentList = originalList?.filter { !it.isMediaSpoilerTag }
        hasSpoilerTags = originalList?.any { it.isMediaSpoilerTag } == true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagChipViewHolder {
        return TagChipViewHolder(
            ChipTagPresenterBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            ).also {
                it.chipTagView.setCloseIconResource(R.drawable.ads_ic_info)
                it.chipTagView.setChipIconResource(R.drawable.ic_hide)
            }
        )
    }

    override fun onBindViewHolder(holder: TagChipViewHolder, position: Int) {
        holder.binding.apply {

            if (!hasSpoilerTags || position < itemCount - 1) {
                val tags = currentList?.get(position) ?: return
                chipTagView.text =
                    root.context.getString(R.string.tag_name_percent)
                        .format(tags.name, tags.rank ?: 0)
                chipTagView.isChipIconVisible = tags.isMediaSpoilerTag
                chipTagView.isCloseIconVisible = true

                root.setOnClickListener {
                    BrowseTagEvent(MediaSearchFilterModel().also {
                        it.tags = listOf(tags.name.trim())
                    }).postEvent
                }

                chipTagView.setOnCloseIconClickListener {
                    onTagInfoClicked(tags)
                }
            } else {
                if (hasSpoilerTags) {
                    chipTagView.isCloseIconVisible = false
                    chipTagView.setText(if (spoilerShown) R.string.hide_spoiler else R.string.show_spoiler)
                    chipTagView.isChipIconVisible = true
                    root.setOnClickListener {
                        showHideSpoilerTags()
                    }
                }
            }
        }

    }


    override fun getItemCount(): Int {
        return currentList?.size?.let { if (hasSpoilerTags) it.plus(1) else it } ?: 0
    }

    private fun showHideSpoilerTags() {
        spoilerShown = if (spoilerShown) {
            currentList = originalList?.filter { !it.isMediaSpoilerTag }
            false
        } else {
            currentList = originalList
            true
        }

        notifyDataSetChanged()
    }

    inner class TagChipViewHolder(val binding: ChipTagPresenterBinding) :
        RecyclerView.ViewHolder(binding.root)
}