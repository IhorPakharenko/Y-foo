package com.yfoo.presentation.swipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yfoo.presentation.utils.nameRes

@Composable
fun ProviderSettings(
    providerSettings: List<SwipeState.ProviderSetting>,
    onCheckedChange: (SwipeState.ProviderSetting, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        itemsIndexed(providerSettings) { index, setting ->
            ProviderSwitch(
                setting = setting,
                onCheckedChange = { isChecked ->
                    onCheckedChange(setting, isChecked)
                },
            )
            if (index != providerSettings.lastIndex) {
                SettingsDivider()
            }
        }
    }
}

@Composable
private fun ProviderSwitch(
    setting: SwipeState.ProviderSetting,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(enabled = setting.isToggleable) {
                onCheckedChange(!setting.isChecked)
            }
    ) {
        Text(stringResource(setting.provider.nameRes))
        Switch(
            checked = setting.isChecked,
            onCheckedChange = onCheckedChange,
            enabled = setting.isToggleable,
        )
    }
}


@Composable
private fun SettingsDivider(
    modifier: Modifier = Modifier,
) {
    Divider(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    )
}