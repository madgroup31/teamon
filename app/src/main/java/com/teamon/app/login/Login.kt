package com.teamon.app.login

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.teamon.app.R
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.themes.teamon.TeamOnTheme
import kotlinx.coroutines.delay

@Composable
fun Login(
    state: SignInState,
    onSignInClick: () -> Unit,
    onAnonymousSignInClick: () -> Unit
) {
    TeamOnTheme(applyToStatusBar = true) {

        val context = LocalContext.current
        LaunchedEffect(key1 = state.signInError) {
            state.signInError?.let { error ->
                Toast.makeText(
                    context,
                    error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        var logoVisible by remember { mutableStateOf(false) }
        LaunchedEffect(logoVisible) {
            delay(1000)
            logoVisible = true
        }
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier, contentAlignment = Alignment.TopEnd) {
                TextButton(onClick = { onAnonymousSignInClick(); logoVisible = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Continue without an account")
                        Icon(modifier = Modifier.size(18.dp), imageVector = Icons.Rounded.ArrowForward, contentDescription = "Continue without an account")
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedVisibility(
                    modifier = Modifier, visible = logoVisible,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically()
                ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                                    contentDescription = "TeamOn Logo"
                                )
                                Text(
                                    text = "Welcome to TeamOn!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                AnimatedVisibility(visible = logoVisible,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        OutlinedButton(
                            modifier = Modifier,
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                            onClick = { onSignInClick(); logoVisible = false }) {
                            TeamOnImage(
                                modifier = Modifier.size(24.dp),
                                source = ImageSource.REMOTE,
                                color = ProjectColors.PURPLE,
                                uri =
                                "https://www.deliverlogic.com/wp-content/uploads/2021/04/google-logo-png-webinar-optimizing-for-success-google-business-webinar-13.png".toUri(),
                                description = "Google logo"
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Sign in with Google")
                        }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(modifier = Modifier, text = "By clicking continue, you agree to our Terms of Service and Privacy Policy", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)

                        }
                    }

                }


            }
        }
    }
    }
