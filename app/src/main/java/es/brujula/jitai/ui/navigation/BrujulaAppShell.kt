package es.brujula.jitai.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.EditorialSerif
import es.brujula.jitai.ui.BrujulaUiState
import es.brujula.jitai.ui.BrujulaViewModel
import es.brujula.jitai.ui.screens.CheckInScreen
import es.brujula.jitai.ui.screens.HomeScreen
import es.brujula.jitai.ui.screens.MyDayScreen
import es.brujula.jitai.ui.screens.ProgressScreen
import es.brujula.jitai.ui.screens.SettingsScreen
import es.brujula.jitai.ui.screens.SupportScreen
import es.brujula.jitai.ui.screens.ZonesScreen

private data class PrimaryNavTab(
    val routePrefix: String,
    val targetRoute: String,
    val label: String,
    val discreetLabel: String,
    val icon: ImageVector
)

private val PrimaryTabs = listOf(
    PrimaryNavTab(
        routePrefix = "home",
        targetRoute = "home",
        label = "Brújula",
        discreetLabel = "Inicio",
        icon = Icons.Outlined.Explore
    ),
    PrimaryNavTab(
        routePrefix = "checkin",
        targetRoute = "checkin",
        label = "Sentir",
        discreetLabel = "Pulso",
        icon = Icons.Outlined.Tune
    ),
    PrimaryNavTab(
        routePrefix = "zones",
        targetRoute = "zones",
        label = "Zonas",
        discreetLabel = "Mapa",
        icon = Icons.Outlined.Place
    ),
    PrimaryNavTab(
        routePrefix = "myday",
        targetRoute = "myday",
        label = "Mi día",
        discreetLabel = "Agenda",
        icon = Icons.Outlined.WbTwilight
    ),
    PrimaryNavTab(
        routePrefix = "support",
        targetRoute = "support",
        label = "Calma",
        discreetLabel = "Pausa",
        icon = Icons.Outlined.Air
    )
)

@Composable
fun BrujulaAppShell(
    viewModel: BrujulaViewModel,
    initialDestination: String? = null,
    onDestinationConsumed: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "home"

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = !route.startsWith("support?section=")
        }
    }

    LaunchedEffect(initialDestination) {
        if (initialDestination == "checkin") {
            navigateTopLevel("checkin")
            onDestinationConsumed()
        }
    }

    Scaffold(
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BrujulaMinimalTopBar(
                state = state,
                currentRoute = currentRoute,
                onBrandClick = { navigateTopLevel("home") },
                onBackClick = { navController.popBackStack() },
                onOpenProgress = { navigateTopLevel("progress") },
                onOpenSettings = { navigateTopLevel("settings") }
            )
        },
        bottomBar = {
            BrujulaFloatingDock(
                currentRoute = currentRoute,
                discreetMode = state.settings.discreetModeEnabled,
                onSelectTab = { tab ->
                    haptics.tick()
                    if (tab.routePrefix == "checkin") {
                        viewModel.enterReevaluationMode(false)
                    }
                    navigateTopLevel(tab.targetRoute)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = !state.statusBannerMessage.isNullOrBlank(),
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                val msg = state.statusBannerMessage.orEmpty()
                Surface(
                    color = colors.jadeWash,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onJadeWash,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = viewModel::clearBannerMessage,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Cerrar",
                                tint = colors.onJadeWash,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            state = state,
                            onNavigateToCheckIn = { reeval ->
                                viewModel.enterReevaluationMode(reeval)
                                navigateTopLevel("checkin")
                            },
                            onNavigateToZones = { navigateTopLevel("zones") },
                            onNavigateToMyDay = { navigateTopLevel("myday") },
                            onNavigateToSupport = { section ->
                                val target = if (section.isNullOrBlank()) {
                                    "support"
                                } else {
                                    "support?section=$section"
                                }
                                navController.navigate(target) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToProgress = { navigateTopLevel("progress") },
                            onTogglePlanCompleted = viewModel::togglePlanItemDone,
                            onRejectSuggestion4h = viewModel::rejectCurrentSuggestionWithPause,
                            onAcceptSuggestion = viewModel::acceptCurrentSuggestion,
                            onResumeNotifications = viewModel::resumeAutomaticNotificationsNow,
                            onDismissReeval = viewModel::dismissTwentyMinReeval,
                            onUpdateMotto = { newMotto ->
                                viewModel.saveOrientationSettings(
                                    motto = newMotto,
                                    goal = state.settings.programGoal,
                                    week = state.settings.currentProgramWeek
                                )
                            }
                        )
                    }

                    composable("checkin") {
                        CheckInScreen(
                            state = state,
                            onSubmitCheckIn = viewModel::submitEmaCheckIn,
                            onOpenCopingSupport = {
                                navController.navigate("support?section=breathing") {
                                    launchSingleTop = true
                                }
                            },
                            onOpenNurseContact = {
                                navController.navigate("support?section=nurse") {
                                    launchSingleTop = true
                                }
                            },
                            onRejectSuggestion4h = viewModel::rejectCurrentSuggestionWithPause,
                            onDoneReturnHome = { navigateTopLevel("home") }
                        )
                    }

                    composable("zones") {
                        ZonesScreen(
                            state = state,
                            onToggleOptionalGps = viewModel::setOptionalGps,
                            onCheckLocalGps = viewModel::checkLocalGpsAgainstZones,
                            onSelectManualZone = viewModel::selectManualZoneContext,
                            onSaveZone = viewModel::savePersonalZone,
                            onDeleteZone = viewModel::deletePersonalZone,
                            onNavigateToCheckIn = {
                                viewModel.enterReevaluationMode(false)
                                navigateTopLevel("checkin")
                            }
                        )
                    }

                    composable("myday") {
                        MyDayScreen(
                            state = state,
                            onSavePlanItem = viewModel::savePlanItem,
                            onToggleCompleted = viewModel::togglePlanItemDone,
                            onDeleteItem = viewModel::deletePlanItem,
                            onTriggerVulnerableSlot = viewModel::triggerVulnerableSlotSupport,
                            onNavigateToCheckIn = {
                                viewModel.enterReevaluationMode(false)
                                navigateTopLevel("checkin")
                            }
                        )
                    }

                    composable(
                        route = "support?section={section}",
                        arguments = listOf(
                            navArgument("section") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { entry ->
                        val sectionKey = entry.arguments?.getString("section")
                        SupportScreen(
                            state = state,
                            initialSectionKey = sectionKey,
                            onCompletePractice = viewModel::logCompletedCopingSession,
                            onSaveNurseProfile = viewModel::saveNurseNavigatorProfile
                        )
                    }

                    composable("progress") {
                        ProgressScreen(
                            state = state,
                            onSaveOrientation = viewModel::saveOrientationSettings,
                            onSaveMilestone = viewModel::saveMilestone,
                            onNavigateToCheckIn = {
                                viewModel.enterReevaluationMode(false)
                                navigateTopLevel("checkin")
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            state = state,
                            onSaveExperimentalRules = viewModel::saveExperimentalRules,
                            onToggleDiscreetMode = viewModel::setDiscreetMode,
                            onToggleOptionalGps = viewModel::setOptionalGps,
                            onToggleNotifications = viewModel::setNotifications,
                            onToggleHaptics = viewModel::setHaptics,
                            onToggleLargeText = viewModel::setLargeText,
                            onSelectTheme = viewModel::setTheme,
                            onTestNeutralNotification = viewModel::triggerTestNeutralNotification,
                            onResumeFourHourPause = viewModel::resumeAutomaticNotificationsNow,
                            onGenerateSummaryExport = viewModel::generateAgreedSummaryExport,
                            onDismissSummaryExport = viewModel::clearExportedJson,
                            onWipeAllLocalData = viewModel::deleteAllDataFromDevice
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrujulaMinimalTopBar(
    state: BrujulaUiState,
    currentRoute: String,
    onBrandClick: () -> Unit,
    onBackClick: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    val isSecondaryScreen = currentRoute == "progress" || currentRoute == "settings"
    val inProgress = currentRoute == "progress"
    val inSettings = currentRoute == "settings"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable {
                    haptics.click()
                    if (isSecondaryScreen) onBackClick() else onBrandClick()
                }
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            if (isSecondaryScreen) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = colors.inkPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                CompassMarkGlyph(
                    primaryColor = colors.jadePrimary,
                    ringColor = colors.borderStrong
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = if (state.settings.discreetModeEnabled) "Orientación" else "Brújula",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.3.sp
                ),
                color = colors.inkPrimary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleHeaderAction(
                icon = Icons.Outlined.Insights,
                contentDescription = "Mi progreso",
                selected = inProgress,
                onClick = {
                    haptics.click()
                    onOpenProgress()
                }
            )
            CircleHeaderAction(
                icon = Icons.Outlined.Settings,
                contentDescription = "Configuración",
                selected = inSettings,
                onClick = {
                    haptics.click()
                    onOpenSettings()
                }
            )
        }
    }
}

@Composable
private fun CircleHeaderAction(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val bg = if (selected) colors.jadeWash else colors.surfaceElevated
    val fg = if (selected) colors.jadePrimary else colors.inkSecondary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bg, CircleShape)
            .border(
                0.6.dp,
                if (selected) colors.jadePrimary.copy(alpha = 0.5f) else colors.borderHairline.copy(alpha = 0.6f),
                CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = fg,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CompassMarkGlyph(
    primaryColor: Color,
    ringColor: Color
) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f - 1.dp.toPx()
        drawCircle(
            color = ringColor.copy(alpha = 0.7f),
            radius = r,
            center = c,
            style = Stroke(width = 1.dp.toPx())
        )
        val needle = Path().apply {
            moveTo(c.x, c.y - r * 0.72f)
            lineTo(c.x + r * 0.22f, c.y)
            lineTo(c.x, c.y + r * 0.72f)
            lineTo(c.x - r * 0.22f, c.y)
            close()
        }
        drawPath(path = needle, color = primaryColor)
    }
}

@Composable
private fun BrujulaFloatingDock(
    currentRoute: String,
    discreetMode: Boolean,
    onSelectTab: (PrimaryNavTab) -> Unit
) {
    val colors = BrujulaDesign.colors
    val dockShape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = dockShape,
            color = colors.surfaceElevated,
            border = BorderStroke(0.7.dp, colors.borderHairline.copy(alpha = 0.7f)),
            shadowElevation = if (colors.isDark) 10.dp else 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryTabs.forEach { tab ->
                    val isSelected = currentRoute.startsWith(tab.routePrefix)
                    val fg = if (isSelected) colors.jadePrimary else colors.inkMuted
                    val pillBg = if (isSelected) colors.jadeWash else Color.Transparent

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 3.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(30.dp)
                                .width(48.dp)
                                .background(pillBg, CircleShape)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = fg,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (discreetMode) tab.discreetLabel else tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                letterSpacing = 0.2.sp
                            ),
                            color = fg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
