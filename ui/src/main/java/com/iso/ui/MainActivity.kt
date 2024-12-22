package com.iso.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.iso.ui.screens.MainScreen
import com.iso.ui.ui.theme.PursTestProjectTheme
import com.iso.ui.viewmodel.TimeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val viewModel: TimeViewModel by lazy {
        val viewModel: TimeViewModel by viewModels()
        viewModel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PursTestProjectTheme {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)

                insetsController.apply {
                    hide(WindowInsetsCompat.Type.statusBars())
                    hide(WindowInsetsCompat.Type.navigationBars())
                    systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }

                MainScreen(viewModel)

                viewModel.getTime()
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PursTestProjectTheme {
//        MainScreen()
    }
}


//inline fun <T> Flow<T>.collectIn(
//    owner: LifecycleOwner,
//    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
//    coroutineContext: CoroutineContext = EmptyCoroutineContext,
//    crossinline action: suspend (T) -> Unit,
//) = owner.lifecycleScope.launch(coroutineContext) {
//    owner.lifecycle.repeatOnLifecycle(minActiveState) {
//        collect {
//            action(it)
//        }
//    }
//}
//
//
//
//fun Context.lifecycleOwner(): LifecycleOwner? {
//    var curContext = this
//    var maxDepth = 20
//    while (maxDepth-- > 0 && curContext !is LifecycleOwner) {
//        curContext = (curContext as ContextWrapper).baseContext
//    }
//    return if (curContext is LifecycleOwner) {
//        curContext as LifecycleOwner
//    } else {
//        null
//    }
//}