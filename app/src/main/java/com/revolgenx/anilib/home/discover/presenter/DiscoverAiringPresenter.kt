package com.revolgenx.anilib.home.discover.presenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.airing.data.model.AiringMediaModel
import com.revolgenx.anilib.common.preference.loggedIn
import com.revolgenx.anilib.entry.data.meta.EntryEditorMeta
import com.revolgenx.anilib.media.data.meta.MediaInfoMeta
import com.revolgenx.anilib.search.data.model.filter.MediaSearchFilterModel
import com.revolgenx.anilib.databinding.DiscoverAiringPresenterLayoutBinding
import com.revolgenx.anilib.infrastructure.event.OpenMediaInfoEvent
import com.revolgenx.anilib.infrastructure.event.OpenMediaListEditorEvent
import com.revolgenx.anilib.infrastructure.event.OpenSearchEvent
import com.revolgenx.anilib.common.presenter.BasePresenter
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText

class DiscoverAiringPresenter(context: Context) : BasePresenter<DiscoverAiringPresenterLayoutBinding, AiringMediaModel>(context) {
    override val elementTypes: Collection<Int>
        get() = listOf(0)


    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        elementType: Int
    ): DiscoverAiringPresenterLayoutBinding {
        val binding = DiscoverAiringPresenterLayoutBinding.inflate(inflater, parent, false)

        binding.mediaMetaBackground.setBackgroundColor(
            ColorUtils.setAlphaComponent(
                DynamicTheme.getInstance().get().backgroundColor, 220
            )
        )

        return binding
    }

    private val mediaFormats by lazy {
        context.resources.getStringArray(R.array.media_format)
    }


    override fun onBind(page: Page, holder: Holder, element: Element<AiringMediaModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return
        val binding = holder.getBinding()?:return

        binding.apply {
            airingMediaSimpleDrawee.setImageURI(item.coverImage?.image(context))
            mediaRatingTv.text = item.averageScore
            airingMediaTitleTv.text = item.title?.title(context)
            airingFormatTv.text = context.getString(R.string.format_episode_s).format(
                item.format?.let { mediaFormats[it] }.naText(),
                item.episodes.naText()
            )

            airingFormatTv.status = item.mediaEntryListModel?.status

            airingTimeTv.setAiringText(item.airingTimeModel)
            airingGenreLayout.addGenre(item.genres?.take(3)) { genre ->
                OpenSearchEvent(MediaSearchFilterModel().also {
                    it.genre = listOf(genre.trim())
                }).postEvent
            }

            root.setOnClickListener {
                OpenMediaInfoEvent(
                    MediaInfoMeta(
                        item.mediaId,
                        item.type!!,
                        item.title!!.romaji!!,
                        item.coverImage!!.image(context),
                        item.coverImage!!.largeImage,
                        item.bannerImage
                    )
                ).postEvent
            }

            root.setOnLongClickListener {
                if (context.loggedIn()) {
                    OpenMediaListEditorEvent(
                        EntryEditorMeta(
                            item.mediaId,
                            item.type!!,
                            item.title!!.title(context)!!,
                            item.coverImage!!.image(context),
                            item.bannerImage
                        )
                    ).postEvent
                } else {
                    context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                }
                true
            }
        }
    }

}