package com.revolgenx.anilib.presenter.user

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.elements.*
import com.revolgenx.anilib.R
import com.revolgenx.anilib.constant.SearchTypes
import com.revolgenx.anilib.event.*
import com.revolgenx.anilib.meta.*
import com.revolgenx.anilib.model.BaseModel
import com.revolgenx.anilib.model.user.CharacterFavouriteModel
import com.revolgenx.anilib.model.user.MediaFavouriteModel
import com.revolgenx.anilib.model.user.StaffFavouriteModel
import com.revolgenx.anilib.model.user.StudioFavouriteModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText
import kotlinx.android.synthetic.main.search_character_layout.view.*
import kotlinx.android.synthetic.main.search_media_layout.view.*
import kotlinx.android.synthetic.main.search_staff_layout.view.*
import kotlinx.android.synthetic.main.search_studio_layout.view.*

//todo://studio rotation
class UserFavouritePresenter(requireContext: Context, private val lifecycleOwner: LifecycleOwner) :
    Presenter<BaseModel>(requireContext) {
    override val elementTypes: Collection<Int>
        get() = SearchTypes.values().map { it.ordinal }


    override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
        val holderRes = when (elementType) {
            SearchTypes.ANIME.ordinal, SearchTypes.MANGA.ordinal -> {
                R.layout.search_media_layout
            }
            SearchTypes.CHARACTER.ordinal -> {
                R.layout.search_character_layout
            }
            SearchTypes.STAFF.ordinal -> {
                R.layout.search_staff_layout
            }
            SearchTypes.STUDIO.ordinal -> {
                R.layout.search_studio_layout
            }
            else -> {
                R.layout.empty_layout
            }
        }

        return Holder(LayoutInflater.from(parent.context).inflate(holderRes, parent, false))
    }


    private val statusColors by lazy {
        context.resources.getStringArray(R.array.status_color)
    }


    private val mediaFormats by lazy {
        context.resources.getStringArray(R.array.media_format)
    }

    private val mediaStatus by lazy {
        context.resources.getStringArray(R.array.media_status)
    }


    override fun onBind(page: Page, holder: Holder, element: Element<BaseModel>) {
        super.onBind(page, holder, element)

        val item = element.data ?: return
        when (holder.elementType) {
            SearchTypes.ANIME.ordinal, SearchTypes.MANGA.ordinal -> {
                holder.itemView.updateMedia(item)
            }
            SearchTypes.CHARACTER.ordinal -> {
                holder.itemView.updateCharacter(item)
            }
            SearchTypes.STAFF.ordinal -> {
                holder.itemView.updateStaff(item)
            }
            SearchTypes.STUDIO.ordinal -> {
                holder.itemView.updateStudio(item)
            }
        }
    }


    private fun View.updateMedia(item: BaseModel) {
        val data = item as MediaFavouriteModel
        searchMediaImageView.setImageURI(data.coverImage?.image(context))
        searchMediaTitleTv.text = data.title?.title(context)
        searchMediaRatingTv.text = data.averageScore?.toString().naText()
        searchMediaFormatTv.text =
            context.getString(R.string.media_format_year_s).format(data.format?.let {
                mediaFormats[it]
            }.naText(), data.seasonYear?.toString().naText())
        searchMediaStatusTv.text = data.status?.let {
            searchMediaStatusTv.color = Color.parseColor(statusColors[it])
            mediaStatus[it]
        }

        setOnClickListener {
            BrowseMediaEvent(
                MediaBrowserMeta(
                    data.mediaId,
                    data.type!!,
                    data.title!!.romaji!!,
                    data.coverImage!!.image(context),
                    data.coverImage!!.largeImage,
                    data.bannerImage
                ), searchMediaImageView
            ).postEvent
        }

        setOnLongClickListener {
            if (context.loggedIn()) {
                ListEditorEvent(
                    ListEditorMeta(
                        data.mediaId,
                        data.type!!,
                        data.title!!.title(context)!!,
                        data.coverImage!!.image(context),
                        data.bannerImage
                    ), searchMediaImageView
                ).postEvent
            } else {
                context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
            }
            true
        }

    }

    private fun View.updateCharacter(item: BaseModel) {
        val data = item as CharacterFavouriteModel
        searchCharacterImageView.setImageURI(data.characterImageModel?.image)
        searchCharacterNameTv.text = data.name?.full
        setOnClickListener {
            BrowseCharacterEvent(
                CharacterMeta(
                    item.characterId,
                    item.characterImageModel?.image
                ),
                searchCharacterImageView
            ).postEvent
        }
    }

    private fun View.updateStaff(item: BaseModel) {
        val data = item as StaffFavouriteModel
        searchStaffImageView.setImageURI(data.staffImage?.image)
        searchStaffNameTv.text = data.staffName?.full
        setOnClickListener {
            BrowseStaffEvent(StaffMeta(item.staffId ?: -1, item.staffImage?.image)).postEvent
        }
    }

    private fun View.updateStudio(item: BaseModel) {
        val data = item as StudioFavouriteModel
        searchStudioHeader.text = data.studioName + " >>"
        searchStudioMedia.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        searchStudioHeader.setOnClickListener {
            BrowseStudioEvent(StudioMeta(data.studioId)).postEvent
        }

        Adapter.builder(lifecycleOwner)
            .addSource(Source.fromList(data.studioMedia!!))
            .addPresenter(BrowseStudioMediaPresenter())
            .into(searchStudioMedia)
    }


    inner class BrowseStudioMediaPresenter : Presenter<MediaFavouriteModel>(context) {
        override val elementTypes: Collection<Int>
            get() = listOf(0)

        override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
            return Holder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.browse_studio_media_presenter,
                    parent,
                    false
                )
            )
        }

        override fun onBind(page: Page, holder: Holder, element: Element<MediaFavouriteModel>) {
            super.onBind(page, holder, element)
            val item = element.data ?: return
            holder.itemView.updateMedia(item)
        }
    }

}
