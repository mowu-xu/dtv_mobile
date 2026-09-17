package dtv.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dtv.mobile.repo.DtvRepository
import dtv.mobile.repo.fake.FakeDtvRepository
import dtv.mobile.state.InMemorySubscriptionStore
import dtv.mobile.state.rememberAppState
import dtv.mobile.state.SubscriptionStore
import dtv.mobile.theme.DtvTheme
import dtv.mobile.ui.components.DtvBackground
import dtv.mobile.ui.RootScaffold

@Composable
fun App(
  repo: DtvRepository = FakeDtvRepository(),
  subscriptionStore: SubscriptionStore = InMemorySubscriptionStore,
) {
  val appState = rememberAppState(repo = repo, subscriptionStore = subscriptionStore)

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
    Surface(modifier = Modifier.fillMaxSize()) { DtvBackground { RootScaffold(appState = appState) } }
  }
}
