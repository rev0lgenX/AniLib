package com.revolgenx.anilib.presenter.search

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
import com.revolgenx.anilib.model.search.*
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText
import kotlinx.android.synthetic.main.search_character_layout.view.*
import kotlinx.android.synthetic.main.search_media_layout.view.*
import kotlinx.android.synthetic.main.search_staff_layout.view.*
import kotlinx.android.synthetic.main.search_studio_layout.view.*

class SearchPresenter(context: Context, private val lifecycleOwner: LifecycleOwner) :
    Presenter<BaseModel>(context) {
    override val elementTypes: Collection<Int>
        get() = SearchTypes.values().map { it.ordinal }

    private val statusColors by lazy {
        context.resources.getStringArray(R.array.status_color)
    }


    private val mediaFormats by lazy {
        context.resources.getStringArray(R.array.media_format)
    }

    private val mediaStatus by lazy {
        context.resources.getStringArray(R.array.media_status)
    }


    override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
        val v = when (elementType) {
            SearchTypes.ANIME.ordinal, SearchTypes.MANGA.ordinal -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_media_layout, parent, false)
            }
            SearchTypes.CHARACTER.ordinal -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_character_layout, parent, false)
            }
            SearchTypes.STAFF.ordinal -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_staff_layout, parent, false)
            }
            SearchTypes.STUDIO.ordinal -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_studio_layout, parent, false)
            }
            SearchTypes.USER.ordinal -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_user_layout, parent, false)
            }
            else -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_media_layout, parent, false)
            }
        }

        return Holder(v)
    }

    override fun onBind(page: Page, holder: Holder, element: Element<BaseModel>) {
        super.onBind(page, holder, element)

        val item = element.data ?: return
        when (holder.elementType) {
            SearchTypes.ANIME.ordinal, SearchTypes.MANGA.ordinal -> {
                holder.itemView.updateMedia(item as MediaSearchModel)
            }
            SearchTypes.CHARACTER.ordinal -> {
                holder.itemView.updateCharacter(item as CharacterSearchModel)
            }
            SearchTypes.STAFF.ordinal -> {
                holder.itemView.updateStaff(item as StaffSearchModel)
            }
            SearchTypes.STUDIO.ordinal -> {
                holder.itemView.updateStudio(item as StudioSearchModel)
            }
            SearchTypes.USER.ordinal -> {
                holder.itemView.updateUser(item as UserSearchModel)
            }
        }
    }


    private fun View.updateMedia(item: MediaSearchModel) {
        searchMediaImageView.setImageURI(item.coverImage?.image(context))
        searchMediaTitleTv.text = item.title?.title(context)
        searchMediaRatingTv.text = item.averageScore?.toString().naText()
        searchMediaFormatTv.text =
            context.getString(R.string.media_format_year_s).format(item.format?.let {
                mediaFormats[it]
            }.naText(), item.seasonYear?.toString().naText())
        searchMediaStatusTv.text = item.status?.let {
            searchMediaStatusTv.color = Color.parseColor(statusColors[it])
            mediaStatus[it]
        }

        setOnClickListener {
            BrowseMediaEvent(
                MediaBrowserMeta(
                    item.mediaId,
                    item.type!!,
                    item.title!!.romaji!!,
                    item.coverImage!!.image(context),
                    item.coverImage!!.largeImage,
                    item.bannerImage
                ), searchMediaImageView
            ).postEvent
        }

        setOnLongClickListener {
            if (context.loggedIn()) {
                ListEditorEvent(
                    ListEditorMeta(
                        item.mediaId,
                        item.type!!,
                        item.title!!.title(context)!!,
                        item.coverImage!!.image(context),
                        item.bannerImage
                    ), searchMediaImageView
                ).postEvent
            } else {
                context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
            }
            true
        }

    }

    private fun View.updateCharacter(item: CharacterSearchModel) {
        searchCharacterImageView.setImageURI(item.characterImageModel?.image)
        searchCharacterNameTv.text = item.name?.full
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

    private fun View.updateStaff(item: StaffSearchModel) {
        searchStaffImageView.setImageURI(item.staffImage?.image)
        searchStaffNameTv.text = item.staffName?.full
        setOnClickListener {
            BrowseStaffEvent(StaffMeta(item.staffId ?: -1, item.staffImage?.image)).postEvent
        }
    }

    private fun View.updateStudio(item: StudioSearchModel) {
        searchStudioHeader.text = item.studioName + " >>"
        searchStudioMedia.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        searchStudioHeader.setOnClickListener {
            BrowseStudioEvent(StudioMeta(item.studioId)).postEvent
        }

        Adapter.builder(lifecycleOwner)
            .addSource(Source.fromList(item.studioMedia!!))
            .addPresenter(BrowseStudioMediaPresenter())
            .into(searchStudioMedia)
    }


    private fun View.updateUser(item: UserSearchModel) {
        searchCharacterImageView.setImageURI(item.avatar?.image)
        searchCharacterNameTv.text = item.userName
        setOnClickListener {
            UserBrowseEvent(item.userId).postEvent
        }
    }


    inner class BrowseStudioMediaPresenter : Presenter<MediaSearchModel>(context) {
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

        override fun onBind(page: Page, holder: Holder, element: Element<MediaSearchModel>) {
            super.onBind(page, holder, element)
            val item = element.data ?: return
            holder.itemView.updateMedia(item)
        }
    }
}

