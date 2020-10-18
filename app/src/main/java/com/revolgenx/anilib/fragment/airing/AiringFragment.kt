package com.revolgenx.anilib.fragment.airing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.activity.ContainerActivity
import com.revolgenx.anilib.event.ListEditorResultEvent
import com.revolgenx.anilib.fragment.base.BasePresenterFragment
import com.revolgenx.anilib.model.DateModel
import com.revolgenx.anilib.model.airing.AiringMediaModel
import com.revolgenx.anilib.presenter.airing.AiringPresenter
import com.revolgenx.anilib.viewmodel.airing.AiringViewModel
import kotlinx.android.synthetic.main.airing_fragment_layout.view.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*

class AiringFragment : BasePresenterFragment<AiringMediaModel>() {
    override val basePresenter: Presenter<AiringMediaModel>
        get() = AiringPresenter(requireContext())
    override val baseSource: Source<AiringMediaModel>
        get() = viewModel.source ?: createSource()

    private val viewModel by viewModel<AiringViewModel>()

    override fun createSource(): Source<AiringMediaModel> {
        return viewModel.createSource()
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val airingView = inflater.inflate(R.layout.airing_fragment_layout, container, false)
        with(activity as ContainerActivity) {
            setSupportActionBar(airingView.dynamicToolbar)
            this.supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        airingView.airingContainerFrameLayout.addView(view)
        return airingView
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.airing_menu, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishActivity()
                true
            }
            R.id.airing_next -> {
                viewModel.next()
                updateToolbarTitle()
                createSource()
                invalidateAdapter()
                true
            }

            R.id.airing_previous -> {
                viewModel.previous()
                updateToolbarTitle()
                createSource()
                invalidateAdapter()
                true
            }
            R.id.airing_custom -> {
                val datePickerDialog = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                    { _, year, month, dayOfMonth ->
                        viewModel.updateDate(
                            ZonedDateTime.of(
                                year,
                                month + 1,
                                dayOfMonth,
                                0,
                                0,
                                0,
                                0,
                                ZoneOffset.UTC
                            )
                        )
                    },
                    viewModel.startDateTime.year,
                    viewModel.startDateTime.month.ordinal,
                    viewModel.startDateTime.dayOfMonth
                )

                datePickerDialog.accentColor = DynamicTheme.getInstance().get().accentColor
                datePickerDialog.show(childFragmentManager, "AiringFragmentDatePickerTag")

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateToolbarTitle()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onListEditorEvent(event: ListEditorResultEvent) {
        event.listEditorResultMeta.let {
            viewModel.updateMediaProgress(it.mediaId, it.progress)
        }
        adapter?.notifyDataSetChanged()
        EventBus.getDefault().removeStickyEvent(event)
    }

    private fun updateToolbarTitle() {
        (activity as? ContainerActivity)?.let {
            it.supportActionBar?.title = viewModel.startDateTime.dayOfWeek.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            )
            it.supportActionBar?.subtitle =
                viewModel.startDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        }
    }

}
