package fr.dutapp.tenky.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.dutapp.tenky.R
import fr.dutapp.tenky.domain.model.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val useImperial by viewModel.useImperialUnits.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_language)) },
                supportingContent = { Text(stringResource(language.labelRes())) },
                modifier = Modifier.selectable(
                    selected = false,
                    role = Role.Button,
                    onClick = { showLanguageDialog = true },
                ),
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_temperature_unit)) },
                supportingContent = {
                    Text(stringResource(R.string.settings_temperature_unit_desc))
                },
                trailingContent = {
                    Switch(
                        checked = useImperial,
                        onCheckedChange = viewModel::setUseImperialUnits,
                    )
                },
            )
            HorizontalDivider()
            CreditsItem()
        }
    }

    if (showLanguageDialog) {
        LanguageDialog(
            selected = language,
            onSelect = { chosen ->
                showLanguageDialog = false
                // Applied last: this recreates the Activity.
                viewModel.setLanguage(chosen)
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
}

/**
 * Attribution for the bundled icon pack, linking to its page.
 *
 * The link opens in the browser rather than in-app: sending someone to an
 * external site inside a WebView hides the real URL from them.
 */
@Composable
private fun CreditsItem(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val url = stringResource(R.string.credits_url)
    val openLinkLabel = stringResource(R.string.action_open_link)

    ListItem(
        headlineContent = { Text(stringResource(R.string.title_credits)) },
        supportingContent = { Text(stringResource(R.string.title_credits_description)) },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
            )
        },
        modifier = modifier
            .clickable(onClickLabel = openLinkLabel) {
                // Throws when the device has no browser at all.
                runCatching { uriHandler.openUri(url) }
            }
            .semantics { contentDescription = url },
    )
}

@Composable
private fun LanguageDialog(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column(Modifier.selectableGroup()) {
                AppLanguage.entries.forEach { option ->
                    ListItem(
                        headlineContent = { Text(stringResource(option.labelRes())) },
                        leadingContent = {
                            RadioButton(
                                selected = option == selected,
                                onClick = null,
                            )
                        },
                        modifier = Modifier
                            .selectable(
                                selected = option == selected,
                                role = Role.RadioButton,
                                onClick = { onSelect(option) },
                            )
                            .padding(horizontal = 0.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

/**
 * Language names are intentionally left untranslated — a speaker looking for
 * "한국어" should find it whatever the app is currently showing.
 */
private fun AppLanguage.labelRes() = when (this) {
    AppLanguage.SYSTEM -> R.string.settings_language_system
    AppLanguage.ENGLISH -> R.string.settings_language_english
    AppLanguage.FRENCH -> R.string.settings_language_french
    AppLanguage.KOREAN -> R.string.settings_language_korean
}
