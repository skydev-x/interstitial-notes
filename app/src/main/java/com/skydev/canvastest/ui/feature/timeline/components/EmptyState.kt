package com.skydev.canvastest.ui.feature.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydev.canvastest.ui.theme.Accent
import com.skydev.canvastest.ui.theme.AccentSoft
import com.skydev.canvastest.ui.theme.Border
import com.skydev.canvastest.ui.theme.TextPri
import com.skydev.canvastest.ui.theme.TextSec

@Composable
fun EmptyState(onNewNote: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(AccentSoft)
                .border(1.dp, Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Edit, contentDescription = null, tint = Accent, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text("nothing here yet", color = TextPri, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("your notes will appear as a timeline", color = TextSec, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onNewNote,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            modifier = Modifier.height(48.dp),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("create first note", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
