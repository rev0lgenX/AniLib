package com.revolgenx.anilib.home.discover.presenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.airing.data.model.AiringScheduleModel
import com.revolgenx.anilib.media.data.meta.MediaInfoMeta
import com.revolgenx.anilib.databinding.DiscoverAiringPresenterLayoutBinding
import com.revolgenx.anilib.common.event.OpenMediaInfoEvent
import com.revolgenx.anilib.common.event.OpenMediaListEditorEvent
import com.revolgenx.anilib.common.event.OpenSearchEvent
import com.revolgenx.anilib.common.preference.loggedIn
import com.revolgenx.anilib.common.presenter.BasePresenter
import com.revolgenx.anilib.search.data.model.SearchFilterEventModel
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText

class DiscoverAiringPresenter(context: Context) : BasePresenter<DiscoverAiringPresenterLayoutBinding, AiringScheduleModel>(context) {
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


    override fun onBind(page: Page, holder: Holder, element: Element<AiringScheduleModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return
        val binding = holder.getBinding()?:return

        val media = item.media ?: return

        binding.apply {
            airingMediaSimpleDrawee.setImageURI(media.coverImage?.image())
            mediaRatingTv.text = media.averageScore
            airingMediaTitleTv.text = media.title?.title()
            airingFormatTv.text = context.getString(R.string.format_episode_s).format(
                media.format?.let { mediaFormats[it] }.naText(),
                media.episodes.naText()
            )

            airingFormatTv.status = media.mediaListEntry?.status

            airingTimeTv.setAiringText(item)
            airingGenreLayout.addGenre(media.genres?.take(3)) { genre ->
                OpenSearchEvent(SearchFilterEventModel(genre = genre)).postEvent
            }

            root.setOnClickListener {
                OpenMediaInfoEvent(
                    MediaInfoMeta(
                        media.id,
                        media.type!!,
                        media.title!!.romaji!!,
                        media.coverImage!!.image(),
                        media.coverImage!!.largeImage,
                        media.bannerImage
                    )
                ).postEvent
            }

            root.setOnLongClickListener {
                if (loggedIn()) {
                    OpenMediaListEditorEvent(media.id).postEvent
                } else {
                    context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                }
                true
            }
        }
    }

}