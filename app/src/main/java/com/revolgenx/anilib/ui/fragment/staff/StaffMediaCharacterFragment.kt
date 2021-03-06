package com.revolgenx.anilib.ui.fragment.staff

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.revolgenx.anilib.common.ui.fragment.BasePresenterFragment
import com.revolgenx.anilib.ui.fragment.staff.StaffFragment.Companion.STAFF_META_KEY
import com.revolgenx.anilib.data.meta.StaffMeta
import com.revolgenx.anilib.data.model.StaffMediaCharacterModel
import com.revolgenx.anilib.ui.presenter.staff.StaffMediaCharacterPresenter
import com.revolgenx.anilib.ui.viewmodel.staff.StaffMediaCharacterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

//voice roles
class StaffMediaCharacterFragment : BasePresenterFragment<StaffMediaCharacterModel>() {
    override val basePresenter: Presenter<StaffMediaCharacterModel>
        get() = StaffMediaCharacterPresenter(
            requireContext()
        )

    override val baseSource: Source<StaffMediaCharacterModel>
        get() = viewModel.source ?: createSource()

    private val viewModel by viewModel<StaffMediaCharacterViewModel>()
    private lateinit var staffmeta: StaffMeta
    override fun createSource(): Source<StaffMediaCharacterModel> {
        return viewModel.createSource()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val span =
            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2
        layoutManager =
            GridLayoutManager(
                this.context,
                span
            ).also {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (adapter?.getItemViewType(position) == 0) {
                            1
                        } else {
                            span
                        }
                    }
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        arguments?.classLoader = StaffMeta::class.java.classLoader
        staffmeta = arguments?.getParcelable(STAFF_META_KEY) ?: return
        viewModel.field.staffId = staffmeta.staffId
        super.onActivityCreated(savedInstanceState)
    }
}