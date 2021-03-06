package com.revolgenx.anilib.data.model

open class StaffModel : BaseModel() {
    var staffId: Int? = null
        set(value) {
            field = value
            baseId = value
        }
    var staffName: StaffNameModel? = null
    var staffImage: StaffImageModel? = null
    var description: String? = null
    var favourites: Int? = null
    var language: Int? = null
    var isFavourite = false
    var siteUrl: String? = null
}
