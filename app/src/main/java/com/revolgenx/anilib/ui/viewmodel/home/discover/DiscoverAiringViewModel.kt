package com.revolgenx.anilib.ui.viewmodel.home.discover

import android.content.Context
import com.otaliastudios.elements.Adapter
import com.revolgenx.anilib.common.preference.getDiscoverAiringField
import com.revolgenx.anilib.data.field.home.AiringMediaField
import com.revolgenx.anilib.infrastructure.service.airing.AiringMediaService
import com.revolgenx.anilib.infrastructure.source.home.airing.AiringSource
import com.revolgenx.anilib.type.AiringSort
import com.revolgenx.anilib.ui.viewmodel.SourceViewModel
import java.time.*

class DiscoverAiringViewModel(private val airingMediaService: AiringMediaService) :
    SourceViewModel<AiringSource, AiringMediaField>() {


    private val startDateTime = ZonedDateTime.now().with(LocalTime.MIN)
    private val endDateTime = ZonedDateTime.now().with(LocalTime.MAX)
    var selectedDay = 0


    override var field: AiringMediaField = AiringMediaField()
        .also {
            it.sort = AiringSort.TIME.ordinal
            it.airingGreaterThan = startDateTime.toEpochSecond().toInt()
            it.airingLessThan = endDateTime.toEpochSecond().toInt()
        }

    var adapter: Adapter? = null

    override fun createSource(): AiringSource {
        source = AiringSource(field, airingMediaService, compositeDisposable)
        return source!!
    }

    fun onNewDaySelected(day: Int) {
        selectedDay = day
        field.airingGreaterThan = startDateTime.plusDays(day.toLong()).toEpochSecond().toInt()
        field.airingLessThan = endDateTime.plusDays(day.toLong()).toEpochSecond().toInt()
        compositeDisposable.clear()
    }


    fun updateField(context: Context) {
        getDiscoverAiringField(context).let {
            field.notYetAired = it.notYetAired
            field.sort = it.sort
            field.showFromPlanning = it.showFromPlanning
            field.showFromWatching = it.showFromWatching
        }
    }


    override fun onCleared() {
        adapter?.releasePages()
        super.onCleared()
    }

}
