package com.revolgenx.anilib.presenter.staff

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.otaliastudios.elements.Presenter
import com.revolgenx.anilib.R
import com.revolgenx.anilib.event.BrowseMediaEvent
import com.revolgenx.anilib.event.ListEditorEvent
import com.revolgenx.anilib.meta.ListEditorMeta
import com.revolgenx.anilib.meta.MediaBrowserMeta
import com.revolgenx.anilib.model.StaffMediaRoleModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText
import kotlinx.android.synthetic.main.staff_media_role_presenter.view.*

//staff roles
class StaffMediaRolePresenter(context: Context) : Presenter<StaffMediaRoleModel>(context) {
    override val elementTypes: Collection<Int>
        get() = listOf(0)


    private val mediaFormats by lazy {
        context.resources.getStringArray(R.array.media_format)
    }

    override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.staff_media_role_presenter,
                parent,
                false
            )
        )
    }

    override fun onBind(page: Page, holder: Holder, element: Element<StaffMediaRoleModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return

        holder.itemView.apply {
            staffMediaRoleImageView.setImageURI(item.coverImage?.image(context))
            staffMediaRoleRatingTv.text = item.averageScore?.toString().naText()
            staffMediaRoleTitleTv.text = item.title?.title(context)
            staffMediaRoleTv.text = item.staffRole
            staffMediaRoleFormatYearTv.text =
                context.getString(R.string.media_format_year_s).format(
                    item.format?.let { mediaFormats[it] }.naText(),
                    item.seasonYear?.toString().naText()
                )
            staffMediaRoleContainer.setOnClickListener {
                BrowseMediaEvent(
                    MediaBrowserMeta(
                        item.mediaId,
                        item.type!!,
                        item.title!!.romaji!!,
                        item.coverImage!!.image(context),
                        item.coverImage!!.largeImage,
                        item.bannerImage
                    ), staffMediaRoleImageView
                ).postEvent
            }

            staffMediaRoleContainer.setOnLongClickListener {
                if (context.loggedIn()) {
                    ListEditorEvent(
                        ListEditorMeta(
                            item.mediaId,
                            item.type!!,
                            item.title!!.title(context)!!,
                            item.coverImage!!.image(context),
                            item.bannerImage
                        ), staffMediaRoleImageView
                    ).postEvent
                } else {
                    context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                }
                true
            }
        }
    }
}
