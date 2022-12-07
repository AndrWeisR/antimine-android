package dev.lucasnlm.antimine.about

import android.os.Bundle
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import dev.lucasnlm.antimine.about.views.AboutInfoFragment
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import kotlinx.android.synthetic.main.activity_container.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutActivity : ThematicActivity(R.layout.activity_container) {
    private val aboutViewModel: AboutViewModel by viewModel()

    private val toolbar: MaterialToolbar by lazy {
        findViewById(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToolbar(toolbar)

        supportFragmentManager.commit(allowStateLoss = true) {
            replace(R.id.content, AboutInfoFragment())
        }
    }
}
