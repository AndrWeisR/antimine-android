package dev.lucasnlm.antimine.level.view

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.view.AreaAdapter
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.common.level.widget.FixedGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class LevelFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    @Inject
    lateinit var dimensionRepository: IDimensionRepository

    private lateinit var viewModel: GameViewModel
    private lateinit var recyclerGrid: RecyclerView
    private lateinit var areaAdapter: AreaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_level, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it, viewModelFactory).get(GameViewModel::class.java)
            areaAdapter = AreaAdapter(it.applicationContext, viewModel)
        }
    }

    override fun onPause() {
        super.onPause()

        GlobalScope.launch {
            viewModel.saveGame()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerGrid = view.findViewById(R.id.recyclerGrid)

        GlobalScope.launch {
            val loadGameUid = checkLoadGameDeepLink()
            val newGameDeepLink = checkNewGameDeepLink()

            val levelSetup = when {
                loadGameUid != null -> viewModel.loadGame(loadGameUid)
                newGameDeepLink != null -> viewModel.startNewGame(newGameDeepLink)
                else -> viewModel.loadLastGame()
            }

            withContext(Dispatchers.Main) {
                recyclerGrid.apply {
                    setHasFixedSize(true)
                    addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
                    isNestedScrollingEnabled = false
                    layoutManager = makeNewLayoutManager(levelSetup.width, levelSetup.height)
                    adapter = areaAdapter
                    alpha = 0.0f

                    animate().apply {
                        alpha(1.0f)
                        duration = DateUtils.SECOND_IN_MILLIS
                    }.start()
                }
            }
        }

        viewModel.run {
            field.observe(viewLifecycleOwner, Observer {
                areaAdapter.bindField(it)
            })

            levelSetup.observe(viewLifecycleOwner, Observer {
                recyclerGrid.layoutManager = makeNewLayoutManager(it.width, it.height)
            })

            fieldRefresh.observe(viewLifecycleOwner, Observer {
                areaAdapter.notifyItemChanged(it)
            })

            eventObserver.observe(viewLifecycleOwner, Observer {
                when (it) {
                    Event.ResumeGameOver,
                    Event.GameOver,
                    Event.Victory,
                    Event.ResumeVictory -> areaAdapter.setClickEnabled(false)
                    Event.Running,
                    Event.ResumeGame,
                    Event.StartNewGame -> areaAdapter.setClickEnabled(true)
                    else -> {
                    }
                }
            })
        }
    }

    private fun checkNewGameDeepLink(): Difficulty? = activity?.intent?.data?.let { uri ->
        if (uri.scheme == DEFAULT_SCHEME) {
            when (uri.schemeSpecificPart.removePrefix(DEEP_LINK_NEW_GAME_HOST)) {
                DEEP_LINK_BEGINNER -> Difficulty.Beginner
                DEEP_LINK_INTERMEDIATE -> Difficulty.Intermediate
                DEEP_LINK_EXPERT -> Difficulty.Expert
                DEEP_LINK_STANDARD -> Difficulty.Standard
                else -> null
            }
        } else {
            null
        }
    }

    private fun checkLoadGameDeepLink(): Int? = activity?.intent?.data?.let { uri ->
        if (uri.scheme == DEFAULT_SCHEME) {
            uri.schemeSpecificPart.removePrefix(DEEP_LINK_LOAD_GAME_HOST).toIntOrNull()
        } else {
            null
        }
    }

    private fun makeNewLayoutManager(boardWidth: Int, boardHeight: Int) =
        FixedGridLayoutManager(boardWidth, calcHorizontalPadding(boardWidth), calcVerticalPadding(boardHeight))

    private fun calcHorizontalPadding(boardWidth: Int): Int =
        ((recyclerGrid.measuredWidth - dimensionRepository.areaSizeWithPadding() * boardWidth) / 2)
            .coerceAtLeast(0.0f)
            .toInt()

    private fun calcVerticalPadding(boardHeight: Int): Int =
        ((recyclerGrid.measuredHeight - dimensionRepository.areaSizeWithPadding() * boardHeight) / 2)
            .coerceAtLeast(0.0f)
            .toInt()

    companion object {
        const val DEFAULT_SCHEME = "antimine"

        const val DEEP_LINK_NEW_GAME_HOST = "//new-game/"
        const val DEEP_LINK_LOAD_GAME_HOST = "//load-game/"
        const val DEEP_LINK_BEGINNER = "beginner"
        const val DEEP_LINK_INTERMEDIATE = "intermediate"
        const val DEEP_LINK_EXPERT = "expert"
        const val DEEP_LINK_STANDARD = "standard"
    }
}
