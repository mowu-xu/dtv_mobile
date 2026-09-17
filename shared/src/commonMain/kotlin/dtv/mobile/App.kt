package dtv.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dtv.mobile.backend.DtvBackend
import dtv.mobile.backend.DtvBackendHolder
import dtv.mobile.backend.createDtvRepository
import dtv.mobile.model.Platform
import dtv.mobile.model.Streamer
import dtv.mobile.repo.DtvRepository
import dtv.mobile.state.Screen
import dtv.mobile.state.SubscriptionStore
import dtv.mobile.state.SubscriptionStoreImpl
import dtv.mobile.state.rememberAppState
import dtv.mobile.ui.screens.HomeScreen
import dtv.mobile.ui.screens.PlayerScreen
import dtv.mobile.ui.screens.PlatformScreen
import dtv.mobile.ui.screens.SearchScreen
import dtv.mobile.ui.screens.SyncScreen
import dtv.mobile.ui.system.FullscreenEffect
import dtv.mobile.ui.system.PlatformBackHandler
import dtv.mobile.theme.DtvTheme

@Composable
fun App() {
  val repo = remember { createDtvRepository() }
  val appState = rememberAppState(repo = repo, subscriptionStore = SubscriptionStoreImpl())

  // 打开 App 立即刷新一次关注列表的直播状态，之后每 5 分钟自动刷新一次。
  // 静默刷新，失败自动忽略。
  LaunchedEffect(Unit) {
    runCatching { appState.refreshFollowedLiveStatus() }
    while (true) {
      kotlinx.coroutines.delay(5 * 60 * 1000L)
      runCatching { appState.refreshFollowedLiveStatus() }
    }
  }

  DtvTheme(themeMode = appState.themeMode) {
    DtvBackendHost {
      when (val screen = appState.currentScreen) {
        Screen.Home -> HomeScreen(appState = appState)
        Screen.Platform -> PlatformScreen(appState = appState)
        Screen.Player -> PlayerScreen(appState = appState, streamer = appState.currentStreamer)
        Screen.Search -> SearchScreen(appState = appState)
        Screen.Sync -> SyncScreen(appState = appState)
      }
    }
  }
}

@Composable
private fun DtvBackendHost(
  content: @Composable () -> Unit,
) {
  val backend = remember { DtvBackendHolder.backend }
  DtvBackendProvider(backend) {
    content()
  }
}

@Composable
private fun DtvBackendProvider(
  backend: DtvBackend,
  content: @Composable () -> Unit,
) {
  // 目前 backend 通过持有者提供，此处仅作为将来扩展预留的包装层。
  content()
}
