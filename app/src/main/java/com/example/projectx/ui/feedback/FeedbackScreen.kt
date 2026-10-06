package com.projectx.app.ui.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.components.AppScaffold
import com.projectx.app.components.StatusPill
import com.projectx.app.components.StatusTone
import com.projectx.app.model.FeedbackCategory
import com.projectx.app.model.FeedbackItem
import com.projectx.app.theme.CampusTokens
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.ui.auth.AuthViewModel
import com.projectx.app.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val COMMENT_MAX = 400

@Composable
fun FeedbackScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    viewModel: FeedbackViewModel = viewModel(),
) {
    val c = CampusTokens.colors
    val submissions by viewModel.submissions.collectAsState()
    val lastSubmission by viewModel.lastSubmission.collectAsState()
    val displayName = (authViewModel?.sessionState?.collectAsState()?.value as? AuthSessionState.Authenticated)
        ?.publicProfile?.displayName

    var category by remember { mutableStateOf(FeedbackCategory.ACADEMICS) }
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(false) }

    AppScaffold(title = "Institutional Feedback", onMenuClick = onMenuClick, modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { CategoryPicker(selected = category, onSelect = { category = it }) }
            item { RatingPicker(rating = rating, onRate = { rating = it }) }
            item { CommentBox(value = comment, onChange = { if (it.length <= COMMENT_MAX) comment = it }) }
            item { AnonymousToggle(checked = anonymous, onChange = { anonymous = it }) }
            item {
                SubmitBar(
                    disabled = rating == 0 || comment.trim().length < 10,
                    lastSubmission = lastSubmission,
                    onSubmit = {
                        val ok = viewModel.submit(category, rating, comment, anonymous, displayName)
                        if (ok) {
                            rating = 0
                            comment = ""
                            anonymous = false
                            category = FeedbackCategory.ACADEMICS
                        }
                    },
                    onDismiss = { viewModel.clearLastSubmission() },
                )
            }
            item {
                Text(
                    text = "My previous feedback",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.heading,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            if (submissions.isEmpty()) {
                item {
                    Text(
                        text = "No feedback submitted yet.",
                        fontSize = 13.sp,
                        color = c.mutedText,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                    )
                }
            } else {
                items(submissions, key = { it.id }) { s -> PreviousRow(s) }
            }
        }
    }
}

@Composable
private fun CategoryPicker(selected: FeedbackCategory, onSelect: (FeedbackCategory) -> Unit) {
    val c = CampusTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.heading)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FeedbackCategory.entries.forEach { cat ->
                val active = cat == selected
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) c.primary else c.surface,
                    border = BorderStroke(1.dp, if (active) c.primary else c.surfaceBorder),
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onSelect(cat) },
                ) {
                    Text(
                        text = cat.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active) c.onPrimary else c.bodyText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingPicker(rating: Int, onRate: (Int) -> Unit) {
    val c = CampusTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Rating", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.heading)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 1..5) {
                val filled = i <= rating
                IconButton(onClick = { onRate(i) }) {
                    Icon(
                        imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$i star",
                        tint = if (filled) c.warningAmber else c.mutedText,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentBox(value: String, onChange: (String) -> Unit) {
    val c = CampusTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Comment", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.heading)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("Share what worked, what did not, and what to improve…", color = c.mutedText) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = c.heading,
                unfocusedTextColor = c.heading,
                focusedBorderColor = c.primary,
                unfocusedBorderColor = c.surfaceBorder,
            ),
        )
        Text(
            text = "${value.length} / $COMMENT_MAX",
            fontSize = 12.sp,
            color = c.mutedText,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun AnonymousToggle(checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = CampusTokens.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = c.mutedText)
        Column(Modifier.weight(1f)) {
            Text("Submit anonymously", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.heading)
            Text("Your name is not stored or displayed with this feedback.", fontSize = 12.sp, color = c.mutedText)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SubmitBar(
    disabled: Boolean,
    lastSubmission: Resource<FeedbackItem>,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = CampusTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
            onClick = onSubmit,
            enabled = !disabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
        ) {
            Text("Submit Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        when (val s = lastSubmission) {
            is Resource.Success -> SuccessBanner(onDismiss)
            is Resource.Error -> ErrorBanner(message = s.message, onDismiss = onDismiss)
            else -> Unit
        }
    }
}

@Composable
private fun SuccessBanner(onDismiss: () -> Unit) {
    val c = CampusTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(c.successGreenBg)
            .padding(10.dp)
            .clickable { onDismiss() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = c.successGreen)
        Text("Feedback submitted. Thanks!", color = c.heading, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    val c = CampusTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(c.dangerRedBg)
            .padding(10.dp)
            .clickable { onDismiss() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, color = c.dangerRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PreviousRow(item: FeedbackItem) {
    val c = CampusTokens.colors
    val stamp = remember(item.createdAt) {
        SimpleDateFormat("d MMM, h:mm a", Locale.ENGLISH).format(Date(item.createdAt))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(1.dp, c.surfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(text = item.category.label, tone = StatusTone.NEUTRAL)
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                repeat(item.rating) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = c.warningAmber, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Text(stamp, fontSize = 12.sp, color = c.mutedText)
        }
        Text(item.comment, fontSize = 13.sp, color = c.bodyText)
        Text(
            text = item.submittedByName?.let { "— $it" } ?: "— Anonymous",
            fontSize = 12.sp,
            color = c.mutedText,
            fontWeight = FontWeight.Medium,
        )
    }
}

