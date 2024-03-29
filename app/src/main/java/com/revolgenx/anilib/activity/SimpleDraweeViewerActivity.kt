package com.revolgenx.anilib.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.view.FrescoImageViewFactory
import com.pranavpandey.android.dynamic.support.activity.DynamicSystemActivity
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.preference.getApplicationLocale
import com.revolgenx.anilib.databinding.SimpleDraweeViewerActivityBinding
import com.revolgenx.anilib.util.openLink
import com.thefuntasty.hauler.setOnDragDismissedListener
import java.util.*


class SimpleDraweeViewerActivity : DynamicSystemActivity() {

    companion object {
        fun openActivity(
            context: Context,
            url: String?,
            option: ActivityOptionsCompat? = null
        ) {
            context.startActivity(Intent(context, SimpleDraweeViewerActivity::class.java).also {
                it.putExtra(simpleDraweeMetaKey, url)
            }, option?.toBundle())
        }

        const val simpleDraweeMetaKey = "simple_drawee_meta_key"
    }


    override fun getLocale(): Locale {
        return Locale(getApplicationLocale())
    }

    private val draweeUrl: String? get() = intent.getStringExtra(simpleDraweeMetaKey)

    override fun getContentView(): View {
        return binding.root
    }

    override fun setStatusBarColor(color: Int) {
        super.setStatusBarColor(color)
        setWindowStatusBarColor(statusBarColor)
    }

    private var _binding: SimpleDraweeViewerActivityBinding? = null
    private val binding: SimpleDraweeViewerActivityBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = SimpleDraweeViewerActivityBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        contentView = binding.root
        statusBarColor = Color.BLACK

        setSupportActionBar(binding.draweeViewerToolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        draweeUrl ?: return
        binding.haulerView.setBackgroundColor(Color.BLACK)
        binding.bigImageViewer.setImageViewFactory(FrescoImageViewFactory())
        binding.bigImageViewer.setProgressIndicator(ProgressPieIndicator())
        binding.haulerView.setOnDragDismissedListener {
            finishAfterTransition()
        }
        binding.bigImageViewer.showImage(draweeUrl?.toUri())
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
            R.id.imageShareMenu -> {
                openLink(draweeUrl)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        _binding = null
        BigImageViewer.imageLoader().cancelAll()
        super.onDestroy()
    }
}