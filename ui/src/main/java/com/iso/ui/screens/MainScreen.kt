package com.iso.ui.screens


import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iso.ui.R
import com.iso.ui.viewmodel.TimeViewModel


@Composable
fun MainScreen(viewModel: TimeViewModel) {

    val stateValue by viewModel.data.collectAsState()
    val stateLocationValue by viewModel.locationName.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    BackgroundImg(expanded)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
    ) {
        Text(
            text = "$stateLocationValue", style = TextStyle(
                fontSize = 54.sp, fontWeight = FontWeight(900), color = Color.White
            ), modifier = Modifier.padding(top = 20.dp)

        )

        expanded = operatingHoursBox(stateValue)

        if (!expanded) Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )


                Text(
                    text = stringResource(R.string.view_menu), color = Color.White
                )
            }
        }
    }

}

@Composable
fun BackgroundImg(isBlur: Boolean) {
    AsyncImage(
        model = stringResource(R.string.bg_url),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .then(if (isBlur) Modifier.blur(10.dp, 10.dp) else Modifier),
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center
    )
}

@SuppressLint("InvalidColorHexValue")
@Composable
internal fun operatingHoursBox(
    stateValue: List<Map<String, Any>>,
    modifier: Modifier = Modifier,
    onExpand: (Boolean) -> Unit = {}
): Boolean {
    var expanded by remember { mutableStateOf(false) }
    val expandIconAngle = remember { Animatable(90f) }

    LaunchedEffect(key1 = expanded) {
        expandIconAngle.animateTo(
            targetValue = (expandIconAngle.value + 90) % 180, animationSpec = tween(
                durationMillis = 300, delayMillis = 0, easing = LinearOutSlowInEasing
            )
        )
    }

    Column(
        modifier = modifier.padding(top = 20.dp)
    ) {
        val customColor = Color(0xFFC2F7F3F3)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    customColor, shape = if (expanded) {
                        RoundedCornerShape(
                            topStart = 8.dp, topEnd = 8.dp
                        )
                    } else {
                        RoundedCornerShape(8.dp)
                    }
                )
                .clickable(interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(
                        radius = 8.dp, color = customColor
                    ),
                    onClick = {
                        expanded = !expanded
                        onExpand.invoke(expanded)
                    })
                .padding(16.dp),

            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
//                        location.status?.let { status ->
//                            LocationStatusText(
//                                status,
//                                modifier = Modifier.padding(end = DefaultPadding)
//                            )
//                        }
//                        Spacer(modifier = Modifier.width(SmallPadding))
//                        location.status?.let { status ->
//                            LocationStatusBadge(status)
//                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.rotate(expandIconAngle.value)
                    )
                }
                Text(
                    text = stringResource(R.string.see_full_hours), fontSize = 12.sp, color = Color.Gray
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        customColor, shape = RoundedCornerShape(
                            bottomStart = 12.dp, bottomEnd = 12.dp
                        )
                    )
                    .padding(16.dp)
            ) {
                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn {
                    items(stateValue.size) { schedule ->
                        val workingHour = stateValue.toList()[schedule]
                        WorkingHourView(workingHour)
                    }
                }
            }
        }
    }
    return expanded
}

@Composable
fun WorkingHourView(workingDay: Map<String, Any>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        workingDay.forEach {
            when (it.key.toString()) {
                stringResource(R.string.day_of_week) -> {
                    Text(text = it.value.toString())
                }

                stringResource(R.string.intervals) -> {
                    val listOfTime = it.value as List<*>
                    Column {
                        listOfTime.forEach {
                            Text(
                                text = it.toString(), modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }

                else -> {

                }
            }
        }
    }
}

//val textWeight = boldTodayText(it.value.toString())
//style = MaterialTheme.typography.bodyLarge.copy(fontWeight = textWeight)

//@Composable
//fun boldTodayText(dayOfWeek: String): FontWeight {
//    return if (dayOfWeek == getCurrentDayOfWeek()) {
//        FontWeight.Bold
//    } else {
//        FontWeight.Normal
//    }
//}
//
//fun getCurrentDayOfWeek(): String {
//    val calendar = Calendar.getInstance()
//    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
//
//    // Преобразуем день недели в строку
//    return when (dayOfWeek) {
//        Calendar.MONDAY -> "Monday"
//        Calendar.TUESDAY -> "Tuesday"
//        Calendar.WEDNESDAY -> "Wednesday"
//        Calendar.THURSDAY -> "Thursday"
//        Calendar.FRIDAY -> "Friday"
//        Calendar.SATURDAY -> "Saturday"
//        Calendar.SUNDAY -> "Sunday"
//        else -> "Unknown"
//    }
//}
