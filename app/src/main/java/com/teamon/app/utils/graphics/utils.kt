package com.teamon.app.utils.graphics

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.RelativeDateTimeFormatter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.get
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.teamon.app.R
import com.teamon.app.prefs
import com.teamon.app.utils.classes.Attachment
import com.teamon.app.utils.classes.Comment
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.History
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.themes.amber.AmberTheme
import com.teamon.app.utils.themes.blue.BlueTheme
import com.teamon.app.utils.themes.cerulean.CeruleanTheme
import com.teamon.app.utils.themes.dodge.DodgeTheme
import com.teamon.app.utils.themes.gorse.GorseTheme
import com.teamon.app.utils.themes.green.GreenTheme
import com.teamon.app.utils.themes.magenta.MagentaTheme
import com.teamon.app.utils.themes.orange.OrangeTheme
import com.teamon.app.utils.themes.pear.PearTheme
import com.teamon.app.utils.themes.persian.PersianTheme
import com.teamon.app.utils.themes.pomegranate.PomegranateTheme
import com.teamon.app.utils.themes.purple.PurpleTheme
import com.teamon.app.utils.themes.robin.RobinTheme
import com.teamon.app.utils.themes.seance.SeanceTheme
import com.teamon.app.utils.themes.sushi.SushiTheme
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.apache.tika.Tika
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

enum class ImageSource { MONOGRAM, CAMERA, LIBRARY, REMOTE }

enum class HistoryIcons { CREATION, TITLE, DESCRIPTION, ENDDATE, STATUS, TAG, COLLABORATORS, PRIORITY }

fun HistoryIcons.getIcon(): Int {
    return when (this) {
        HistoryIcons.CREATION -> R.drawable.outline_new_releases_24
        HistoryIcons.TITLE -> R.drawable.round_text_fields_24
        HistoryIcons.DESCRIPTION -> R.drawable.round_text_fields_24
        HistoryIcons.ENDDATE -> R.drawable.round_outlined_flag_24
        HistoryIcons.STATUS -> R.drawable.outline_change_circle_24
        HistoryIcons.TAG -> R.drawable.round_tag_24
        HistoryIcons.COLLABORATORS -> R.drawable.round_groups_24
        HistoryIcons.PRIORITY -> R.drawable.round_priority_high_24
    }
}

sealed class UploadStatus(
    val progress: Float = 0f,
    val message: String = ""
) {
    data class Success(val downloadUrl: String) : UploadStatus()
    data class Progress(val currentProgress: Float) : UploadStatus(progress = currentProgress)
    data class Error(val errorMessage: String) : UploadStatus(message = errorMessage)
}

sealed class TabItem(
    var title: String,
    var focusedIcon: Int,
    var icon: Int,
) {
    data object ProjectInfo :
        TabItem(
            "Info",
            focusedIcon = R.drawable.round_info_24,
            icon = R.drawable.outline_info_24,
        )

    data object ProjectTasks :
        TabItem(
            "Tasks",
            focusedIcon = R.drawable.round_calendar_today_24,
            icon = R.drawable.outline_calendar_today_24,
        )

    data object ProjectTeams :
        TabItem(
            "Teams",
            focusedIcon = R.drawable.round_groups_24,
            icon = R.drawable.outline_groups_24
        )

    data object ProjectFeedbacks :
        TabItem(
            "Feedbacks",
            focusedIcon = R.drawable.round_feedback_24,
            icon = R.drawable.outline_feedback_24
        )

    data object ProjectPerformance :
        TabItem(
            "Performance",
            focusedIcon = R.drawable.baseline_query_stats_24,
            icon = R.drawable.baseline_query_stats_24
        )

    data object TaskInfo :
        TabItem(
            "Info",
            focusedIcon = R.drawable.round_info_24,
            icon = R.drawable.outline_info_24
        )

    data object TaskHistory :
        TabItem(
            "History",
            focusedIcon = R.drawable.round_history_24,
            icon = R.drawable.round_history_24
        )

    data object TaskComments :
        TabItem(
            "Comments",
            focusedIcon = R.drawable.round_comment_24,
            icon = R.drawable.outline_comment_24
        )

    data object TaskAttachments :
        TabItem(
            "Attachments",
            focusedIcon = R.drawable.round_content_paste_24,
            icon = R.drawable.outline_content_paste_24
        )

    data object TeamInfo :
        TabItem(
            "Info",
            focusedIcon = R.drawable.round_info_24,
            icon = R.drawable.outline_info_24
        )

    data object TeamProject :
        TabItem(
            "Projects",
            focusedIcon = R.drawable.round_dashboard_24,
            icon = R.drawable.rounded_dashboard_24,
        )

    data object TeamChat :
        TabItem(
            "Chat",
            focusedIcon = R.drawable.baseline_chat_bubble_outline_24,
            icon = R.drawable.baseline_chat_bubble_24
        )

    data object TeamMembers :
        TabItem(
            "Members",
            focusedIcon = R.drawable.round_groups_24,
            icon = R.drawable.outline_groups_24
        )

    data object TeamFeedbacks :
        TabItem(
            "Feedbacks",
            focusedIcon = R.drawable.round_feedback_24,
            icon = R.drawable.outline_feedback_24
        )

    data object TeamAchievement :
        TabItem(
            "Achievements",
            focusedIcon = R.drawable.baseline_query_stats_24,
            icon = R.drawable.baseline_query_stats_24
        )

}

fun ProjectColors.toInt(): Int {
    return when (this) {
        ProjectColors.AMBER -> {
            0xFF775A0B.toInt()
        }

        ProjectColors.BLUE -> {
            0xFF4D5C92.toInt()
        }

        ProjectColors.CERULEAN -> {
            0xFF27638A.toInt()
        }

        ProjectColors.DODGE -> {
            0xFF36618E.toInt()
        }

        ProjectColors.GORSE -> {
            0xFF685F12.toInt()
        }

        ProjectColors.GREEN -> {
            0xFF3B6939.toInt()
        }

        ProjectColors.MAGENTA -> {
            0xFF8E4957.toInt()
        }

        ProjectColors.ORANGE -> {
            0xFF855318.toInt()
        }

        ProjectColors.PEAR -> {
            0xFF5C631D.toInt()
        }

        ProjectColors.PERSIAN -> {
            0xFF006A60.toInt()
        }

        ProjectColors.POMEGRANATE -> {
            0xFF904A42.toInt()
        }

        ProjectColors.PURPLE -> {
            0xFF68548E.toInt()
        }

        ProjectColors.ROBIN -> {
            0xFF006876.toInt()
        }

        ProjectColors.SEANCE -> {
            0xFF7B4E7F.toInt()
        }

        ProjectColors.SUSHI -> {
            0xFF4B662C.toInt()
        }
    }
}

fun hexToRgb(hex: String): Triple<Int, Int, Int> {
    val r = Integer.valueOf(hex.substring(2, 4), 16)
    val g = Integer.valueOf(hex.substring(4, 6), 16)
    val b = Integer.valueOf(hex.substring(6, 8), 16)

    return Triple(r, g, b)
}

fun diffColors(color1: String, color2: String): Double {
    val rgb1 = hexToRgb(color1)
    val rgb2 = hexToRgb(color2)
    val rDiff = abs(rgb1.first - rgb2.first)
    val gDiff = abs(rgb1.second - rgb2.second)
    val bDiff = abs(rgb1.third - rgb2.third)
    return sqrt(
        rDiff.toDouble().pow(2.toDouble()) + gDiff.toDouble().pow(2.toDouble()) + bDiff.toDouble().pow(2.toDouble())
    )
}

@OptIn(ExperimentalStdlibApi::class)
fun Int.toProjectColor(): ProjectColors {

    val distances = ProjectColors.entries
        .map { Pair(it, diffColors(it.toInt().toHexString(), this.toHexString())) }
        .sortedBy { it.second }
    return distances.map { it.first }[0]

}

@Composable
fun Theme(color: ProjectColors, applyToStatusBar: Boolean, content: @Composable () -> Unit) {
    when (color) {
        ProjectColors.AMBER -> {
            AmberTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.BLUE -> {
            BlueTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.CERULEAN -> {
            CeruleanTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.DODGE -> {
            DodgeTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.GORSE -> {
            GorseTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.GREEN -> {
            GreenTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.MAGENTA -> {
            MagentaTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.ORANGE -> {
            OrangeTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.PEAR -> {
            PearTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.PERSIAN -> {
            PersianTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.POMEGRANATE -> {
            PomegranateTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.PURPLE -> {
            PurpleTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.ROBIN -> {
            RobinTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.SEANCE -> {
            SeanceTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }

        ProjectColors.SUSHI -> {
            SushiTheme(applyToStatusBar = applyToStatusBar) {
                content()
            }
        }
    }
}

fun Timestamp.asDate(): String {
    val instant = Instant.ofEpochSecond(this.toInstant().epochSecond)
    val instantDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return DateTimeFormatter.ofPattern("dd-MM-yyyy").format(instantDate)
}

fun Timestamp.asTime(): String {
    val instant = Instant.ofEpochSecond(this.toInstant().epochSecond)
    val localDateTime =
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) // Use LocalDateTime
    return DateTimeFormatter.ofPattern("HH:mm").format(localDateTime)
}

fun Timestamp.asPastRelativeDateTime(): String {
    val instant = Instant.ofEpochSecond(this.toInstant().epochSecond)
    val now = Instant.now()
    val duration = java.time.Duration.between(instant, now)

    val instantDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()


    return when {
        duration.seconds < 60 -> "Just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
        duration.toHours() < 24 -> "${duration.toHours()} hours ago"
        duration.toDays() < 2 -> RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.LAST,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY
        ).lowercase().replaceFirstChar { it.uppercase() } + ", ${
            DateTimeFormatter.ofPattern("HH:mm").format(instantDateTime)
        }"

        duration.toDays() < 7 -> DateTimeFormatter.ofPattern("EEEE, HH:mm").format(instantDateTime)
            .lowercase().replaceFirstChar { it.uppercase() }

        duration.toDays() < 30 -> "${duration.toDays()} days ago, ${
            DateTimeFormatter.ofPattern("HH:mm").format(instantDateTime)
        }"

        duration.toDays() < 365 -> DateTimeFormatter.ofPattern("EEE d MMMM, HH:mm")
            .format(instantDateTime)

        else -> DateTimeFormatter.ofPattern("EEE d MMMM yyyy, HH:mm").format(instantDateTime)
    }
}

fun Timestamp.asCompactPastRelativeDateTime(): String {

    val instant = Instant.ofEpochSecond(this.toInstant().epochSecond)
    val now = Instant.now()
    val duration = java.time.Duration.between(instant, now)

    val instantDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

    return when {
        duration.seconds < 60 -> "Now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()} mins ago"
        duration.toHours() < 24 -> "${duration.toHours()} h ago"
        duration.toDays() < 2 -> RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.LAST,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY
        ).lowercase().replaceFirstChar { it.uppercase() } + ", ${
            DateTimeFormatter.ofPattern("HH:mm").format(instantDateTime)
        }"

        duration.toDays() < 7 -> DateTimeFormatter.ofPattern("EEE, HH:mm").format(instantDateTime)
            .lowercase().replaceFirstChar { it.uppercase() }

        duration.toDays() < 30 -> "${duration.toDays()} days ago, ${
            DateTimeFormatter.ofPattern("HH:mm").format(instantDateTime)
        }"

        duration.toDays() < 365 -> DateTimeFormatter.ofPattern("d LLL").format(instantDateTime)
        else -> DateTimeFormatter.ofPattern("d LLL yyyy").format(instantDateTime)
    }
}

fun Timestamp.asPastRelativeDate(): String {
    val instant = Instant.ofEpochSecond(this.toInstant().epochSecond)
    val now = Instant.now()
    val duration = java.time.Duration.between(instant, now)

    val instantDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    RelativeDateTimeFormatter.getInstance().format(
        RelativeDateTimeFormatter.Direction.LAST,
        RelativeDateTimeFormatter.AbsoluteUnit.DAY
    )

    return when {
        duration.toDays() < 1 -> RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.THIS,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY
        ).lowercase().replaceFirstChar { it.uppercase() }

        duration.toDays() < 2 -> RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.LAST,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY
        ).lowercase().replaceFirstChar { it.uppercase() }

        duration.toDays() < 7 -> DateTimeFormatter.ofPattern("EEEE").format(instantDate).lowercase()
            .replaceFirstChar { it.uppercase() }

        duration.toDays() < 365 -> DateTimeFormatter.ofPattern("d MMMM").format(instantDate)
        else -> DateTimeFormatter.ofPattern("d MMMM yyyy").format(instantDate)

    }
}

fun Timestamp.asFutureRelativeDate(): String {
    val instant = Instant.ofEpochSecond(this.toInstant().epochSecond)
    val now = Instant.now()
    val duration = java.time.Duration.between(now, instant)

    val instantDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    RelativeDateTimeFormatter.getInstance().format(
        RelativeDateTimeFormatter.Direction.NEXT,
        RelativeDateTimeFormatter.AbsoluteUnit.DAY
    )

    return when {
        duration.toHours() < 24 -> {
            RelativeDateTimeFormatter.getInstance().format(
                RelativeDateTimeFormatter.Direction.THIS,
                RelativeDateTimeFormatter.AbsoluteUnit.DAY
            ).lowercase().replaceFirstChar { it.uppercase() }
        }

        duration.toDays() < 1 -> RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY
        ).lowercase().replaceFirstChar { it.uppercase() }

        duration.toDays() < 2 -> RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY
        ).lowercase().replaceFirstChar { it.uppercase() }

        duration.toDays() < 7 -> DateTimeFormatter.ofPattern("EEEE").format(instantDate).lowercase()
            .replaceFirstChar { it.uppercase() }

        duration.toDays() < 30 -> DateTimeFormatter.ofPattern("d MMMM").format(instantDate)
        else -> DateTimeFormatter.ofPattern("d MMMM yyyy").format(instantDate)

    }
}

fun Timestamp.asCompactFutureRelativeDate(): String {
    val instant = Instant.ofEpochSecond(this.toInstant().epochSecond)
    val now = Instant.now()
    val duration = java.time.Duration.between(now, instant)

    val instantDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    RelativeDateTimeFormatter.getInstance().format(
        RelativeDateTimeFormatter.Direction.NEXT,
        RelativeDateTimeFormatter.AbsoluteUnit.DAY
    )

    return when {
        duration.toHours() < 24 -> {
            RelativeDateTimeFormatter.getInstance().format(
                RelativeDateTimeFormatter.Direction.THIS,
                RelativeDateTimeFormatter.AbsoluteUnit.DAY
            ).lowercase().replaceFirstChar { it.uppercase() }
        }

        duration.toDays() < 1 -> RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY
        ).lowercase().replaceFirstChar { it.uppercase() }

        duration.toDays() < 2 -> RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY
        ).lowercase().replaceFirstChar { it.uppercase() }

        duration.toDays() < 7 -> DateTimeFormatter.ofPattern("EEEE").format(instantDate).lowercase()
            .replaceFirstChar { it.uppercase() }

        duration.toDays() < 30 -> DateTimeFormatter.ofPattern("d MMMM").format(instantDate)
        else -> DateTimeFormatter.ofPattern("d MMM yyyy").format(instantDate)

    }
}

fun String.toTimestamp(): Timestamp {
    return Timestamp(this.toEpochSeconds(), 0)
}

fun String.toEpochSeconds(): Long {
    if (this.isBlank()) return 0
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val date = LocalDate.parse(this, formatter).atStartOfDay()
    val instant = date.atZone(ZoneId.systemDefault()).toInstant()
    return instant.epochSecond
}

fun currentTimeSeconds(): Long {
    return Timestamp.now().toInstant().epochSecond
}

enum class StorageAccess {
    Full, Partial, Denied
}

/**
 * Depending on the version of Android the device is running, the app should request the right
 * storage permissions:
 * Up to Android 12L    -> [READ_EXTERNAL_STORAGE]
 * Android 13           -> [READ_MEDIA_IMAGES]
 * Android 14+          -> Partial access sets only [READ_MEDIA_VISUAL_USER_SELECTED] to granted
 *                      -> Full access sets [READ_MEDIA_IMAGES] and/or [READ_MEDIA_VIDEO] to granted
 */
fun getStorageAccess(context: Context): StorageAccess {
    return if (
        ContextCompat.checkSelfPermission(
            context,
            READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // Full access on Android 13+
        StorageAccess.Full
    } else if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            READ_MEDIA_VISUAL_USER_SELECTED
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // Partial access on Android 13+
        StorageAccess.Partial
    } else if (ContextCompat.checkSelfPermission(
            context,
            READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // Full access up to Android 12
        StorageAccess.Full
    } else {
        // Access denied
        StorageAccess.Denied
    }
}


@SuppressLint("SimpleDateFormat")
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd-MM-yyyy")
    return formatter.format(Date(millis))
}

fun getFileType(file: File): String {
    return Tika().detect(file)
}

fun getFileTypeIcon(fileType: String): Int {
    return when (fileType) {
        "audio/aac" -> R.drawable.filetype_aac
        "image/bmp" -> R.drawable.filetype_bmp
        "text/csv" -> R.drawable.filetype_csv
        "application/msword" -> R.drawable.filetype_docx
        "text/html" -> R.drawable.filetype_html
        "text/x-java-source" -> R.drawable.filetype_java
        "image/jpg", "image/jpeg" -> R.drawable.filetype_jpg
        "application/json" -> R.drawable.filetype_json
        "video/mp4" -> R.drawable.filetype_mp4
        "image/png" -> R.drawable.filetype_png
        "application/vnd.ms-powerpoint" -> R.drawable.filetype_pptx
        "text/x-python" -> R.drawable.filetype_py
        "text/plain" -> R.drawable.filetype_txt
        "text/css" -> R.drawable.filetype_css
        "application/x-sh" -> R.drawable.filetype_sh
        "application/x-dosexec" -> R.drawable.filetype_exe
        "application/xml" -> R.drawable.filetype_xml
        "application/pdf" -> R.drawable.filetype_pdf
        "application/vnd.ms-excel" -> R.drawable.filetype_xls
        else -> R.drawable.file_earmark
    }
}

fun generateQRCode(content: String, size: Int): Bitmap? {
    return try {
        val bitMatrix: Bitmap =
            BarcodeEncoder().encodeBitmap(content, BarcodeFormat.QR_CODE, size, size)

        Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, bitMatrix[x, y])
                }
            }
        }
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }
}

@Composable
fun LoadingOverlay(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f), contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.1f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun TeamOnImage(
    modifier: Modifier = Modifier,
    source: ImageSource,
    name: String = "",
    surname: String = "",
    color: ProjectColors,
    uri: Uri? = null,
    contentScale: ContentScale = ContentScale.Crop,
    description: String
) {
    when (source) {
        ImageSource.MONOGRAM -> {
            Monogram(
                name = name,
                surname = surname,
                color = color,
                modifier = modifier
            )
        }

        else -> {
            val image = rememberAsyncImagePainter(model = ImageRequest.Builder(LocalContext.current).data(uri).size(coil.size.Size.ORIGINAL).build(), contentScale = contentScale)
            when(image.state) {
                is AsyncImagePainter.State.Empty -> {}
                is AsyncImagePainter.State.Loading -> {
                    CircularProgressIndicator(modifier = modifier.wrapContentSize())
                }
                is AsyncImagePainter.State.Error -> {
                    CircularProgressIndicator(modifier = modifier.wrapContentSize())
                }
                is AsyncImagePainter.State.Success -> {
                    Image(
                        modifier = modifier,
                        painter = image,
                        contentScale = contentScale,
                        contentDescription = description
                    )
                }
            }
        }
    }

}

@Composable
fun AnimatedItem(
    index: Int,
    visibles: List<Int>? = null,
    content: @Composable () -> Unit
) {
    val animate = prefs.getBoolean("animate", true)
    AnimatedVisibility(
        visible = if (animate) visibles?.contains(index) ?: true else true,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight } // Start the slide from the bottom
        ) + fadeIn() + scaleIn(),
        exit = slideOutVertically() + fadeOut() + scaleOut()
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedGrid(
    modifier: Modifier = Modifier,
    columns: StaggeredGridCells,
    items: Collection<Any>,
    scrollToLast: Boolean = false,
    content: @Composable (item: Any, index: Int) -> Unit
) {
    val visibles = remember { mutableStateListOf<Int>() }
    val lazyGridState = rememberLazyStaggeredGridState()
    val animate = prefs.getBoolean("animate", true)


    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.layoutInfo.visibleItemsInfo.map { it.index } }
            .distinctUntilChanged()
            .collectLatest { list ->
                visibles.clear()
                visibles.addAll(list)
            }

    }

    LazyVerticalStaggeredGrid(
        state = lazyGridState,
        modifier = modifier,
        columns = columns
    ) {

        itemsIndexed(items.toList(), key = { index, it ->
            when (it) {
                is Project -> it.projectId
                is Task -> it.taskId
                is Team -> it.teamId
                is Comment -> it.commentId
                is History -> it.historyId
                is Attachment -> it.attachmentId
                is Message -> it.messageId
                is Feedback -> it.feedbackId
                else -> index
            }

        }) { index, it ->

            Box(modifier = if (animate) Modifier.animateItemPlacement() else Modifier) {
                content(it, index)
            }

        }
    }
    LaunchedEffect(Unit) {
        if (scrollToLast && items.isNotEmpty()) {
            if (animate)
                lazyGridState.animateScrollToItem(items.size - 1)
            else
                lazyGridState.scrollToItem(items.size - 1)
        }
    }
}

class FilePickerContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}

fun copyUriToFile(context: Context, uri: Uri): File? {
    val outputFile = File.createTempFile("temp", null, context.cacheDir)
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val outputStream = FileOutputStream(outputFile)

    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return outputFile
}

fun saveBitmapAsJpeg(context: Context, bitmap: Bitmap, filename: String): File? {
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
    return try {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        file
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun networkConnectivityFlow(context: Context): Flow<Boolean> = callbackFlow {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            if (networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                trySend(true)
            }
        }

        override fun onLost(network: Network) {
            trySend(false)
        }
    }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

    // Emit initial connectivity status
    val activeNetwork = connectivityManager.activeNetwork
    val initialNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    val isConnected = initialNetworkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    trySend(isConnected)

    awaitClose {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
