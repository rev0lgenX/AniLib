package com.revolgenx.anilib.infrastructure.source

import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.revolgenx.anilib.common.infrastruture.source.BaseRecyclerSource
import com.revolgenx.anilib.data.field.character.CharacterMediaField
import com.revolgenx.anilib.data.model.character.CharacterMediaModel
import com.revolgenx.anilib.infrastructure.service.character.CharacterService
import io.reactivex.disposables.CompositeDisposable

class CharacterMediaSource(
    field: CharacterMediaField,
    private val characterService: CharacterService,
    private val compositeDisposable: CompositeDisposable
) :
    BaseRecyclerSource<CharacterMediaModel, CharacterMediaField>(field) {

    override fun areItemsTheSame(first: CharacterMediaModel, second: CharacterMediaModel): Boolean {
        return first.mediaId == second.mediaId
    }

    override fun onPageOpened(page: Page, dependencies: List<Element<*>>) {
        super.onPageOpened(page, dependencies)
        field.page = pageNo
        characterService.getCharacterMediaInfo(field, compositeDisposable) {
            postResult(page, it)
        }
    }
}
