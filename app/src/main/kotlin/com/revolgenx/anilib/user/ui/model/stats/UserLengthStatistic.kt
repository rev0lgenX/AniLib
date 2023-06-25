package com.revolgenx.anilib.user.ui.model.stats

data class UserLengthStatistic(
    val length: String? = null,
    override val count: Int = 0,
    override val meanScore: Double = 0.0,
    override val minutesWatched: Int = 0,
    override val chaptersRead: Int = 0,
    override val mediaIds: List<Int> = emptyList(),
) : BaseStatisticModel()