package com.revolgenx.anilib.data.field.review

import com.revolgenx.anilib.AllReviewQuery
import com.revolgenx.anilib.data.field.BaseSourceField
import com.revolgenx.anilib.type.ReviewSort

class AllReviewField : BaseSourceField<AllReviewQuery>() {
    var reviewSort: Int = ReviewSort.ID_DESC.ordinal
    override fun toQueryOrMutation(): AllReviewQuery {
        return AllReviewQuery.builder()
            .page(page)
            .perPage(perPage)
            .sort(listOf(ReviewSort.values()[reviewSort]))
            .build()
    }
}
