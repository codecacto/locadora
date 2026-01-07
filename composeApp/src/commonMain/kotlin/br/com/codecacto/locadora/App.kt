package br.com.codecacto.locadora

import androidx.compose.runtime.Composable
import br.com.codecacto.locadora.core.ui.RootNavigation
import br.com.codecacto.locadora.core.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        RootNavigation()
    }
}
