package com.revolgenx.anilib.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.revolgenx.anilib.R
import com.revolgenx.anilib.activity.MainActivity
import com.revolgenx.anilib.fragment.base.BasePresenterFragment
import com.revolgenx.anilib.model.CommonMediaModel
import com.revolgenx.anilib.model.field.SeasonField
import com.revolgenx.anilib.presenter.MediaPresenter
import com.revolgenx.anilib.util.makeToast
import com.revolgenx.anilib.viewmodel.SeasonViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SeasonFragment : BasePresenterFragment<CommonMediaModel>() {

    private val viewModel by viewModel<SeasonViewModel>()
    override val basePresenter: Presenter<CommonMediaModel> by lazy {
        MediaPresenter(context!!)
    }

    override val baseSource: Source<CommonMediaModel>
        get() {
            return viewModel.seasonSource
        }

    override fun createSource(): Source<CommonMediaModel> {
        return viewModel.createSource(SeasonField.create(context!!))
    }


    override fun onResume() {
        (activity as AppCompatActivity).invalidateOptionsMenu()
        setHasOptionsMenu(true)
        if (!visibleToUser)
            invalidateAdapter()
        super.onResume()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.season_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.prevSeasonId -> {

                true
            }
            R.id.nextSeasonId -> {

                true
            }
            R.id.filterSeasonId -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
