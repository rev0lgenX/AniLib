package com.revolgenx.anilib.fragment.staff

import android.os.Bundle
import android.view.View
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.revolgenx.anilib.event.meta.StaffMeta
import com.revolgenx.anilib.field.StaffMediaCharacterField
import com.revolgenx.anilib.fragment.base.BasePresenterFragment
import com.revolgenx.anilib.fragment.staff.StaffFragment.Companion.STAFF_META_KEY
import com.revolgenx.anilib.model.StaffMediaCharacterModel
import com.revolgenx.anilib.presenter.StaffMediaCharacterPresenter
import com.revolgenx.anilib.viewmodel.StaffMediaCharacterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StaffMediaCharacterFragment : BasePresenterFragment<StaffMediaCharacterModel>() {
    override val basePresenter: Presenter<StaffMediaCharacterModel>
        get() = StaffMediaCharacterPresenter(requireContext())

    override val baseSource: Source<StaffMediaCharacterModel>
        get() = viewModel.source ?: createSource()

    private val viewModel by viewModel<StaffMediaCharacterViewModel>()
    private lateinit var staffmeta: StaffMeta
    private val field by lazy {
        StaffMediaCharacterField().also {
            it.staffId = staffmeta.staffId
        }
    }

    override fun createSource(): Source<StaffMediaCharacterModel> {
        return viewModel.createSource(field)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        layoutManager = FlexboxLayoutManager(context).also { manager ->
            manager.justifyContent = JustifyContent.SPACE_EVENLY
            manager.alignItems = AlignItems.CENTER
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        arguments?.classLoader = StaffMeta::class.java.classLoader
        staffmeta = arguments?.getParcelable(STAFF_META_KEY) ?: return
        super.onActivityCreated(savedInstanceState)
    }
}