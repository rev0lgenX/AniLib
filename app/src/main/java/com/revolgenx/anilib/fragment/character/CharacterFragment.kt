package com.revolgenx.anilib.fragment.character

import android.os.Bundle
import android.view.*
import androidx.lifecycle.observe
import com.revolgenx.anilib.R
import com.revolgenx.anilib.field.ToggleFavouriteField
import com.revolgenx.anilib.field.character.CharacterField
import com.revolgenx.anilib.fragment.base.BaseFragment
import com.revolgenx.anilib.markwon.MarkwonImpl
import com.revolgenx.anilib.meta.CharacterMeta
import com.revolgenx.anilib.model.character.CharacterModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.repository.util.Status.*
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.openLink
import com.revolgenx.anilib.util.prettyNumberFormat
import com.revolgenx.anilib.viewmodel.character.CharacterViewModel
import kotlinx.android.synthetic.main.character_fragment_layout.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.resource_status_container_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharacterFragment : BaseFragment() {
    companion object {
        const val CHARACTER_META_KEY = "character_meta_key"
    }

    private val viewModel by viewModel<CharacterViewModel>()

    private var characterModel: CharacterModel? = null
    private lateinit var characterMeta: CharacterMeta
    private val characterField by lazy {
        CharacterField().also {
            it.characterId = characterMeta.characterId
        }
    }

    private val toggleFavouriteField by lazy {
        ToggleFavouriteField().also {
            it.characterId = characterMeta.characterId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.character_fragment_layout, container, false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.character_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.character_share_menu -> {
                characterModel?.siteUrl?.let {
                    requireContext().openLink(it)
                } ?: makeToast(R.string.invalid)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.classLoader = CharacterMeta::class.java.classLoader
        characterMeta = arguments?.getParcelable(CHARACTER_META_KEY) ?: return

        characterIv.setImageURI(characterMeta.characterUrl)
        viewModel.characterInfoLiveData.observe(viewLifecycleOwner) { res ->
            when (res.status) {
                SUCCESS -> {
                    resourceStatusContainer.visibility = View.GONE
                    progressLayout.visibility = View.VISIBLE
                    errorLayout.visibility = View.GONE
                    updateView(res.data!!)
                }
                ERROR -> {
                    resourceStatusContainer.visibility = View.VISIBLE
                    progressLayout.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                }
                LOADING -> {
                    resourceStatusContainer.visibility = View.VISIBLE
                    progressLayout.visibility = View.VISIBLE
                    errorLayout.visibility = View.GONE
                }
            }
        }

        viewModel.toggleFavouriteLiveData.observe(viewLifecycleOwner) { res ->
            when (res.status) {
                SUCCESS -> {
                    characterFavIv.showLoading(false)
                    if (res.data == true) {
                        characterModel?.isFavourite = characterModel?.isFavourite?.not() ?: false
                        characterFavIv.setImageResource(
                            if (characterModel?.isFavourite == true) {
                                R.drawable.ic_favorite
                            } else {
                                R.drawable.ic_not_favourite
                            }
                        )
                    }
                }
                ERROR -> {
                    characterFavIv.showLoading(false)
                    makeToast(R.string.failed_to_toggle, icon = R.drawable.ads_ic_error)
                }
                LOADING -> {
                    characterFavIv.showLoading(true)
                }
            }
        }

        initListener()

        if (savedInstanceState == null) {
            viewModel.getCharacterInfo(characterField)
        }
    }

    private fun initListener() {
        characterFavLayout.setOnClickListener {
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

    private fun updateView(item: CharacterModel) {
        characterModel = item
        characterNameTv.text = item.name?.full
        characterFavCountTv.text = item.favourites?.toLong()?.prettyNumberFormat()

        if (characterMeta.characterUrl == null) {
            characterIv.setImageURI(characterModel?.characterImageModel?.image)
        }

        item.name?.native?.let {
            nativeNameTv.subtitle = it
        } ?: let {
            nativeNameTv.visibility = View.GONE
        }

        item.name?.alternative?.let {
            alternativeNameTv.subtitle = it.joinToString()
        } ?: let {
            alternativeNameTv.visibility = View.GONE
        }

        if (item.isFavourite) {
            characterFavIv.setImageResource(R.drawable.ic_favorite)
        }

        MarkwonImpl.createHtmlInstance(requireContext()).setMarkdown(characterDescriptionTv, item.description ?: "")
    }
}