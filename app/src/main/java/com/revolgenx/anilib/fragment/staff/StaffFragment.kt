package com.revolgenx.anilib.fragment.staff

import android.os.Bundle
import android.view.*
import androidx.lifecycle.observe
import com.revolgenx.anilib.R
import com.revolgenx.anilib.field.ToggleFavouriteField
import com.revolgenx.anilib.field.staff.StaffField
import com.revolgenx.anilib.fragment.base.BaseFragment
import com.revolgenx.anilib.markwon.MarkwonImpl
import com.revolgenx.anilib.meta.StaffMeta
import com.revolgenx.anilib.model.StaffModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.repository.util.Status
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.openLink
import com.revolgenx.anilib.util.prettyNumberFormat
import com.revolgenx.anilib.viewmodel.staff.StaffViewModel
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.resource_status_container_layout.*
import kotlinx.android.synthetic.main.staff_fragment_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class StaffFragment : BaseFragment() {
    companion object {
        const val STAFF_META_KEY = "staff_meta_key"
    }

    private val viewModel by viewModel<StaffViewModel>()


    private var staffModel: StaffModel? = null
    private lateinit var staffMeta: StaffMeta
    private val staffField by lazy {
        StaffField().also {
            it.staffId = staffMeta.staffId
        }
    }

    private val toggleFavouriteField by lazy {
        ToggleFavouriteField().also {
            it.staffId = staffMeta.staffId
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.staff_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.staff_share_menu -> {
                staffModel?.siteUrl?.let {
                    requireContext().openLink(it)
                } ?: makeToast(R.string.invalid)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.staff_fragment_layout, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.classLoader = StaffMeta::class.java.classLoader
        staffMeta = arguments?.getParcelable(STAFF_META_KEY) ?: return
        staffIv.setImageURI(staffMeta.staffUrl)
        viewModel.staffInfoLiveData.observe(viewLifecycleOwner) { res ->
            when (res.status) {
                Status.SUCCESS -> {
                    resourceStatusContainer.visibility = View.GONE
                    progressLayout.visibility = View.VISIBLE
                    errorLayout.visibility = View.GONE
                    updateView(res.data!!)
                }
                Status.ERROR -> {
                    resourceStatusContainer.visibility = View.VISIBLE
                    progressLayout.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                }
                Status.LOADING -> {
                    resourceStatusContainer.visibility = View.VISIBLE
                    progressLayout.visibility = View.VISIBLE
                    errorLayout.visibility = View.GONE
                }
            }
        }

        viewModel.toggleStaffFavLiveData.observe(viewLifecycleOwner) { res ->
            when (res.status) {
                Status.SUCCESS -> {
                    staffFavIv.showLoading(false)
                    if (res.data == true) {
                        staffModel?.isFavourite = staffModel?.isFavourite?.not() ?: false
                        staffFavIv.setImageResource(
                            if (staffModel?.isFavourite == true) {
                                R.drawable.ic_favorite
                            } else {
                                R.drawable.ic_not_favourite
                            }
                        )
                    }
                }
                Status.ERROR -> {
                    staffFavIv.showLoading(false)
                    makeToast(R.string.failed_to_toggle, icon = R.drawable.ads_ic_error)
                }
                Status.LOADING -> {
                    staffFavIv.showLoading(true)
                }
            }
        }
        initListener()

        if (savedInstanceState == null) {
            viewModel.getStaffInfo(staffField)
        }
    }

    private fun initListener() {
        staffFavLayout.setOnClickListener {
            if (requireContext().loggedIn()) {
                viewModel.toggleCharacterFav(toggleFavouriteField)
            } else {
                makeToast(R.string.please_log_in, null, R.drawable.ic_person)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionMenu()
        setHasOptionsMenu(true)
    }

    private fun updateView(item: StaffModel) {
        staffModel = item
        staffNameTv.text = item.staffName?.full
        staffFavCountIv.text = item.favourites?.toLong()?.prettyNumberFormat()

        if (staffMeta.staffUrl == null) {
            staffIv.setImageURI(staffModel?.staffImage?.image)
        }

        item.staffName?.native?.let {
            nativeNameTv.subtitle = it
        } ?: let {
            nativeNameTv.visibility = View.GONE
        }

        item.staffName?.alternative?.let {
            alternativeNameTv.subtitle = it.joinToString()
        } ?: let {
            alternativeNameTv.visibility = View.GONE
        }

        item.language?.let {
            languageTv.subtitle =
                requireContext().resources.getStringArray(R.array.staff_language)[it]
        } ?: let {
            languageTv.visibility = View.GONE
        }

        if (item.isFavourite) {
            staffFavIv.setImageResource(R.drawable.ic_favorite)
        }

        MarkwonImpl.createHtmlInstance(requireContext()).setMarkdown(staffDescriptionTv, item.description ?: "")
    }
}