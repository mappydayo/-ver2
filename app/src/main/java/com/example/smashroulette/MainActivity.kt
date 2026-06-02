package com.example.smashroulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smashroulette.data.fighters
import com.example.smashroulette.ui.theme.SmashRouletteTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmashRouletteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmashRouletteScreen()
                }
            }
        }
    }
}

enum class RouletteState {
    IDLE, ROLLING, FINISHED
}

@Composable
fun SmashRouletteScreen() {
    var state by remember { mutableStateOf(RouletteState.IDLE) }
    var currentFighter by remember { mutableStateOf("？？？") }
    val coroutineScope = rememberCoroutineScope()
    
    // フラッシュ演出用のアルファ値
    val flashAlpha = remember { Animatable(0f) }
    
    // 決定したキャラクターの拡大アニメーション (バウンシーなスプリング)
    val scale by animateFloatAsState(
        targetValue = if (state == RouletteState.FINISHED) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "character_scale"
    )

    // ルーレット開始処理
    fun startRoulette() {
        coroutineScope.launch {
            state = RouletteState.ROLLING
            val totalSteps = 30 // ルーレット更新回数
            
            for (i in 0 until totalSteps) {
                currentFighter = fighters.random()
                // イージングカーブに沿ってディレイを徐々に増やす (減速演出)
                val progress = i.toFloat() / totalSteps
                val delayMillis = (30 + (progress * progress * 270)).toLong()
                delay(delayMillis)
            }
            
            // 最終選出キャラ
            currentFighter = fighters.random()
            state = RouletteState.FINISHED
            
            // フラッシュ演出 (白く光らせてからフェードアウト)
            flashAlpha.snapTo(0.7f)
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 450, easing = LinearOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 背景のうっすらとしたスマッシュマーク (透かし)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            SmashLogo(
                modifier = Modifier
                    .size(350.dp)
                    .padding(24.dp),
                color = Color(0xFF151515), // 非常に薄いグレー
                strokeWidth = 12.dp
            )
        }

        // メインコンテンツ
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 上部：アプリタイトル
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // ロゴアイコン
                SmashLogo(
                    modifier = Modifier.size(50.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "スマブラキャラルーレット",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp
                )
                // タイトル下の斜めの仕切り線
                Spacer(modifier = Modifier.height(8.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(3.dp)
                ) {
                    drawLine(
                        color = Color(0xFF0066FF),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }

            // 中央：キャラクター表示エリア
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(CutCornerShape(topStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color(0xFF0C0C0C))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = if (state == RouletteState.FINISHED) {
                                listOf(Color(0xFFFFD700), Color(0xFFFFA500)) // 決定時はゴールド/オレンジ
                            } else {
                                listOf(Color(0xFF0066FF), Color(0xFF3385FF)) // 通常時は青
                            }
                        ),
                        shape = CutCornerShape(topStart = 24.dp, bottomEnd = 24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 内側の装飾的な薄い枠
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF222222),
                            shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
                        )
                )

                // キャラ表示テキスト
                val textValue = when (state) {
                    RouletteState.IDLE -> "？？？"
                    RouletteState.ROLLING -> currentFighter
                    RouletteState.FINISHED -> "★ $currentFighter ★"
                }

                val textColor = when (state) {
                    RouletteState.FINISHED -> Color(0xFFFFD700) // 決定時はゴールドに輝く
                    else -> Color.White
                }

                Text(
                    text = textValue,
                    color = textColor,
                    fontSize = if (state == RouletteState.FINISHED) 32.sp else 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }

            // 下部：ボタンエリア
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ルーレット開始ボタン (斜めカットのスマブラ風)
                Button(
                    onClick = { startRoulette() },
                    enabled = state != RouletteState.ROLLING,
                    shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0066FF),
                        disabledContainerColor = Color(0xFF1E3C72) // ルーレット中はトーンダウンした青
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = "ルーレット開始",
                        color = if (state == RouletteState.ROLLING) Color.Gray else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // もう一度ボタン
                Button(
                    onClick = { startRoulette() },
                    enabled = state == RouletteState.FINISHED,
                    shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF333333),
                        disabledContainerColor = Color(0xFF111111)
                    ),
                    border = if (state == RouletteState.FINISHED) {
                        BorderStroke(1.dp, Color(0xFF0066FF))
                    } else null,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = "もう一度",
                        color = if (state == RouletteState.FINISHED) Color.White else Color.DarkGray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // フラッシュ用オーバーレイ
        if (flashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha.value))
            )
        }
    }
}

@Composable
fun SmashLogo(
    modifier = Modifier,
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp
) {
    Canvas(modifier = modifier) {
        val center = this.center
        val radius = size.minDimension / 2
        val strokePx = strokeWidth.toPx()

        // 外円
        drawCircle(
            color = color,
            radius = radius - strokePx / 2,
            style = Stroke(width = strokePx)
        )

        // 縦線 (中心より少し左)
        val verticalX = center.x - radius * 0.15f
        drawLine(
            color = color,
            start = Offset(verticalX, center.y - radius),
            end = Offset(verticalX, center.y + radius),
            strokeWidth = strokePx * 1.8f
        )

        // 横線 (中心より少し下)
        val horizontalY = center.y + radius * 0.15f
        drawLine(
            color = color,
            start = Offset(center.x - radius, horizontalY),
            end = Offset(center.x + radius, horizontalY),
            strokeWidth = strokePx * 0.9f
        )
    }
}
