package com.revolgenx.anilib.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.elements.Adapter
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.preference.loadBioByDefault
import com.revolgenx.anilib.common.ui.fragment.BaseLayoutFragment
import com.revolgenx.anilib.constant.UserConstant
import com.revolgenx.anilib.user.data.meta.UserMeta
import com.revolgenx.anilib.databinding.ResourceStatusContainerLayoutBinding
import com.revolgenx.anilib.databinding.UserActivityGenrePresenterBinding
import com.revolgenx.anilib.databinding.UserOverviewFragmentLayoutBinding
import com.revolgenx.anilib.infrastructure.repository.util.Status
import com.revolgenx.anilib.social.factory.AlMarkwonFactory
import com.revolgenx.anilib.social.markwon.AlStringUtil.anilify
import com.revolgenx.anilib.user.viewmodel.UserProfileViewModel
import com.revolgenx.anilib.util.getOrDefault
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserOverviewFragment : BaseLayoutFragment<UserOverviewFragmentLayoutBinding>() {

    private val userProfileViewModel by viewModel<UserProfileViewModel>()

    companion object{
        fun newInstance(userMeta: UserMeta) = UserOverviewFragment().also {
            it.arguments = bundleOf(UserConstant.USER_META_KEY to userMeta)
        }
    }

    private val userMeta get()= arguments?.getParcelable<UserMeta?>(UserConstant.USER_META_KEY)

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): UserOverviewFragmentLayoutBinding {
        return UserOverviewFragmentLayoutBinding.inflate(inflater, parent, false)
    }

    override fun onResume() {
        super.onResume()
        if (!visibleToUser) {
            userProfileViewModel.getProfile()
        }
        visibleToUser = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val user = userMeta?: return
        userProfileViewModel.userProfileLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.resourceStatusLayout.statusSuccess()

                    val data = it.data ?: return@observe
                    binding.apply {
                        episodesWatchedTv.title = data.episodesWatched.getOrDefault().toString()
                        daysWatchedTv.title = "%.1f".format(data.daysWatched.getOrDefault())
                        animeMeanScoreTv.title = data.animeMeanScore.getOrDefault().toString()

                        volumeReadTv.title = data.volumeRead.getOrDefault().toString()
                        chaptersReadTv.title = data.chaptersRead.getOrDefault().toString()
                        mangaMeanScoreTv.title = data.mangaMeanScore.getOrDefault().toString()

                        tagGenreRecyclerView.layoutManager =
                            GridLayoutManager(requireContext(), 2, RecyclerView.HORIZONTAL, false)

                        Adapter.builder(viewLifecycleOwner)
                            .addSource(Source.fromList(data.genreOverView.toList()))
                            .addPresenter(
                                Presenter.simple<Pair<String, Int>>(
                                    requireContext(),
                                    R.layout.user_activity_genre_presenter,
                                    0
                                ) { view, genres ->
                                    val genreBind = UserActivityGenrePresenterBinding.bind(view)
                                    genreBind.userGenreHeader.title = genres.first
                                    genreBind.userGenreHeader.subtitle = genres.second.toString()
                                }).into(tagGenreRecyclerView)


                        if (loadBioByDefault()) {
                            loadBioCardVew.visibility = View.GONE
                            aboutContainerLayout.visibility = View.VISIBLE
                            if(data.about.isBlank()){
                                userAboutTv.setText(R.string.no_description)
                            }else{
                                AlMarkwonFactory.getMarkwon().setMarkdown(userAboutTv, anilify(data.about))
                            }
                        } else {
                            aboutContainerLayout.visibility = View.GONE
                            loadBioCardVew.visibility = View.VISIBLE
                            loadBioCardVew.setOnClickListener {
                                aboutContainerLayout.visibility = View.VISIBLE
                                if(data.about.isBlank()){
                                    userAboutTv.setText(R.string.no_description)
                                }else{
                                    AlMarkwonFactory.getMarkwon().setMarkdown(userAboutTv, anilify(data.about))
                                }
                                loadBioCardVew.visibility = View.GONE
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    binding.resourceStatusLayout.statusError()
                }
                Status.LOADING -> {
                    binding.resourceStatusLayout.statusLoading()
                }
            }
        }


        if (savedInstanceState == null) {
            with(userProfileViewModel.userField) {
                userId = user.userId
                userName = user.userName
            }
        }
    }

    private fun ResourceStatusContainerLayoutBinding.statusSuccess() {
        resourceStatusContainer.visibility = View.GONE
        resourceProgressLayout.progressLayout.visibility = View.VISIBLE
        resourceErrorLayout.errorLayout.visibility = View.GONE
    }

    private fun ResourceStatusContainerLayoutBinding.statusError() {
        resourceStatusContainer.visibility = View.VISIBLE
        resourceProgressLayout.progressLayout.visibility = View.GONE
        resourceErrorLayout.errorLayout.visibility = View.VISIBLE
    }

    private fun ResourceStatusContainerLayoutBinding.statusLoading() {
        resourceStatusContainer.visibility = View.VISIBLE
        resourceProgressLayout.progressLayout.visibility = View.VISIBLE
        resourceErrorLayout.errorLayout.visibility = View.GONE
    }
}