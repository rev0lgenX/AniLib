package com.revolgenx.anilib.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.view.FrescoImageViewFactory
import com.github.piasy.biv.view.ImageSaveCallback
import com.pranavpandey.android.dynamic.support.activity.DynamicSystemActivity
import com.revolgenx.anilib.R
import com.revolgenx.anilib.controller.ThemeController
import com.revolgenx.anilib.meta.DraweeViewerMeta
import com.revolgenx.anilib.preference.getApplicationLocale
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.openLink
import com.thefuntasty.hauler.setOnDragDismissedListener
import kotlinx.android.synthetic.main.simple_drawee_viewer_activity.*
import timber.log.Timber
import java.util.*

class SimpleDraweeViewerActivity : DynamicSystemActivity() {

    companion object {
        fun openActivity(
            context: Context,
            draweeMeta: DraweeViewerMeta,
            option: ActivityOptionsCompat? = null
        ) {
            context.startActivity(Intent(context, SimpleDraweeViewerActivity::class.java).also {
                it.putExtra(simpleDraweeMetaKey, draweeMeta)
            }, option?.toBundle())
        }

        const val simpleDraweeMetaKey = "simple_drawee_meta_key"
    }

    override fun getThemeRes(): Int {
        return ThemeController.appStyle
    }

    override fun onCustomiseTheme() {
        ThemeController.setLocalTheme()
    }

    override fun getLocale(): Locale? {
        return Locale(getApplicationLocale())
    }

    private var draweeMeta:DraweeViewerMeta? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_drawee_viewer_activity)
        setSupportActionBar(draweeViewerToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        draweeMeta = intent.getParcelableExtra(simpleDraweeMetaKey) ?: return
        haulerView.setBackgroundColor(Color.BLACK)
        bigImageViewer.setImageViewFactory(FrescoImageViewFactory())
        bigImageViewer.setProgressIndicator(ProgressPieIndicator())
        bigImageViewer.setImageSaveCallback(object : ImageSaveCallback {
            override fun onFail(p0: Throwable?) {
                makeToast(R.string.failed_to_save, icon = R.drawable.ic_error)
            }

            override fun onSuccess(p0: String?) {
                makeToast(R.string.saved_to_gallary)
            }
        })

        haulerView.setOnDragDismissedListener {
            finishAfterTransition()
        }
        bigImageViewer.showImage(draweeMeta!!.url?.toUri())
    }

    override fun onSupportNavigateUp(): Boolean {
        finishAfterTransition()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.simple_drawee_viewer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.downloadImageMenu -> {
//                bigImageViewer.saveImageIntoGallery()
//                true
//            }
            R.id.imageShareMenu->{
                openLink(draweeMeta?.url)
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onDestroy() {
        BigImageViewer.imageLoader().cancelAll()
        super.onDestroy()
    }
}