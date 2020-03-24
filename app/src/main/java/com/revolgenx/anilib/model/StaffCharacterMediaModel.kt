package com.revolgenx.anilib.model

import com.revolgenx.anilib.model.character.CharacterNameModel

class StaffCharacterMediaModel : CommonMediaModel() {
    var characterId: Int? = null
    var seasonYear: Int? = null
    var characterName: CharacterNameModel? = null
    var characterImageModel: CharacterImageModel? = null
    var mediaRole: Int? = null
}
