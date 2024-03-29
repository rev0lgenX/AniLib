package com.revolgenx.anilib.studio.fragment

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import com.otaliastudios.elements.Adapter
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.otaliastudios.elements.extensions.HeaderSource
import com.otaliastudios.elements.extensions.SimplePresenter
import com.pranavpandey.android.dynamic.support.model.DynamicMenu
import com.revolgenx.anilib.R
import com.revolgenx.anilib.airing.data.model.AiringScheduleModel
import com.revolgenx.anilib.common.ui.fragment.BasePresenterFragment
import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.databinding.StudioFragmentLayoutBinding
import com.revolgenx.anilib.studio.presenter.StudioMediaPresenter
import com.revolgenx.anilib.media.data.model.MediaModel
import com.revolgenx.anilib.studio.data.constant.StudioMediaSort
import com.revolgenx.anilib.studio.source.StudioHeaderSource
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.studio.viewmodel.StudioViewModel
import com.revolgenx.anilib.ui.view.makeSpinnerAdapter
import com.revolgenx.anilib.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudioFragment : BasePresenterFragment<MediaModel>() {
    companion object {
        private const val STUDIO_ID_KEY = "STUDIO_ID_KEY"
        fun newInstance(studioId: Int) = StudioFragment().also {
            it.arguments = bundleOf(STUDIO_ID_KEY to studioId)
        }
    }

    private val studioId get() = arguments?.getInt(STUDIO_ID_KEY)

    override val basePresenter: Presenter<MediaModel>
        get() = StudioMediaPresenter(requireContext())

    override val baseSource: Source<MediaModel>
        get() = viewModel.source ?: createSource()

    private lateinit var studioBinding: StudioFragmentLayoutBinding

    private val viewModel by viewModel<StudioViewModel>()
    private val field get() = viewModel.field
    private val studioModel get() = viewModel.studioModel

    override val setHomeAsUp: Boolean = true
    override val showMenuIcon: Boolean = true
    override val menuRes: Int = R.menu.studio_fragment_menu

    override var gridMaxSpan: Int = 6
    override var gridMinSpan: Int = 3


    override fun createSource(): Source<MediaModel> {
        return viewModel.createSource()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        studioBinding = StudioFragmentLayoutBinding.inflate(inflater, container, false)
        studioBinding.studioContainerLayout.addView(v)
        return studioBinding.root
    }


    override fun getBaseToolbar(): Toolbar {
        return studioBinding.dynamicToolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.field.studioId = studioId ?: return
        viewModel.studioField.studioId = studioId
        viewModel.toggleFavouriteField.studioId = studioId

        val statusLayout = studioBinding.resourceStatusLayout
        viewModel.studioInfoLiveData.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Resource.Success -> {
                    statusLayout.resourceStatusContainer.visibility = View.GONE
                    statusLayout.resourceProgressLayout.progressLayout.visibility = View.VISIBLE
                    statusLayout.resourceErrorLayout.errorLayout.visibility = View.GONE
                    studioBinding.updateView()
                }
                is Resource.Error -> {
                    statusLayout.resourceStatusContainer.visibility = View.VISIBLE
                    statusLayout.resourceProgressLayout.progressLayout.visibility = View.GONE
                    statusLayout.resourceErrorLayout.errorLayout.visibility = View.VISIBLE
                }
                is Resource.Loading -> {
                    statusLayout.resourceStatusContainer.visibility = View.VISIBLE
                    statusLayout.resourceProgressLayout.progressLayout.visibility = View.VISIBLE
                    statusLayout.resourceErrorLayout.errorLayout.visibility = View.GONE
                }
            }
        }
        viewModel.toggleFavouriteLiveData.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Resource.Success -> {
                    studioBinding.studioFavIv.showLoading(false)
                    studioBinding.updateView()
                }
                is Resource.Error -> {
                    studioBinding.studioFavIv.showLoading(false)
                    makeToast(R.string.failed_to_toggle, icon = R.drawable.ic_error)
                }
                is Resource.Loading -> {
                    studioBinding.studioFavIv.showLoading(true)
                }
            }
        }
        studioBinding.bind()
        studioBinding.initListener()
        viewModel.getStudioInfo()
    }

    private fun StudioFragmentLayoutBinding.bind(){
        val statusItems = requireContext().resources.getStringArray(R.array.studio_media_sort).map {
            DynamicMenu(null, it)
        }
        studioMediaSortSpinner.adapter = makeSpinnerAdapter(requireContext(), statusItems)
        studioMediaSortSpinner.setSelection(field.sort.ordinal, false)
        studioMediaOnListCheckbox.isChecked = field.onList
    }

    override fun onToolbarMenuSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.studio_share_menu -> {
                shareText(studioModel?.siteUrl)
                true
            }
            R.id.studio_open_in_browser_menu -> {
                openLink(studioModel?.siteUrl)
                true
            }
            android.R.id.home -> {
                finishActivity()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun StudioFragmentLayoutBinding.initListener() {
        studioFavIv.setOnClickListener {
            loginContinue {
                viewModel.toggleStudioFav(viewModel.toggleFavouriteField)
            }
        }

        studioTitleTv.setOnLongClickListener {
            requireContext().copyToClipBoard(studioModel?.studioName)
            true
        }

        studioMediaSortSpinner.onItemSelected {
            field.sort = StudioMediaSort.values()[it]
            renewAdapter()
        }
        studioMediaOnListCheckbox.setOnCheckedChangeListener(null)
        studioMediaOnListCheckbox.setOnCheckedChangeListener { _, isChecked ->
            field.onList = isChecked
            renewAdapter()
        }
    }

    private fun renewAdapter() {
        createSource()
        invalidateAdapter()
    }

    private fun StudioFragmentLayoutBinding.updateView() {
        val item = studioModel ?: return
        studioTitleTv.text = item.studioName
        studioFavCountTv.text = item.favourites?.prettyNumberFormat()
        val favIcon = if (item.isFavourite) R.drawable.ic_favourite else R.drawable.ic_not_favourite
        studioBinding.studioFavIv.setImageResource(favIcon)
    }

    override fun adapterBuilder(): Adapter.Builder {
        val builder = super.adapterBuilder()

        when (field.sort) {
            StudioMediaSort.START_DATE_DESC, StudioMediaSort.START_DATE -> {
                builder.addSource(StudioHeaderSource())
                builder.addPresenter(SimplePresenter<HeaderSource.Data<AiringScheduleModel, String>>(
                    requireContext(),
                    R.layout.header_presenter_layout,
                    HeaderSource.ELEMENT_TYPE
                ) { v, header ->
                    (v as TextView).text = header.header
                })
            }
            else -> {}
        }

        return builder
    }

}