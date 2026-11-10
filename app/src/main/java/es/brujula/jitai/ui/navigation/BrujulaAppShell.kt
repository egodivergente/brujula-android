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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.HorizontalDivider
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
        label = "Check-in",
        discreetLabel = "Pulso",
        icon = Icons.Outlined.Tune
    ),
    PrimaryNavTab(
        routePrefix = "zones",
        targetRoute = "zones",
        label = "Mis zonas",
        discreetLabel = "Mapa",
        icon = Icons.Outlined.Map
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
        label = "Apoyo",
        discreetLabel = "Recursos",
        icon = Icons.Outlined.FavoriteBorder
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
            BrujulaCompactTopBar(
                state = state,
                currentRoute = currentRoute,
                onBrandClick = { navigateTopLevel("home") },
                onBackClick = { navController.popBackStack() },
                onOpenProgress = { navigateTopLevel("progress") },
                onOpenSettings = { navigateTopLevel("settings") }
            )
        },
        bottomBar = {
            BrujulaCompactBottomBar(
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
                    border = BorderStroke(0.8.dp, colors.jadePrimary.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Cerrar aviso",
                                tint = colors.onJadeWash,
                                modifier = Modifier.size(16.dp)
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
                            },
                            onLogGhb = viewModel::addVoluntaryGhbLog,
                            onDeleteGhb = viewModel::removeVoluntaryGhbLog,
                            onClearGhb = viewModel::clearAllVoluntaryGhbLogs
                        )
                    }

                    composable("checkin") {
                        CheckInScreen(
                            state = state,
                            onSubmitCheckIn = viewModel::submitEmaCheckIn,
                            onOpenCopingSupport = {
                                navController.navigate("support?section=coping") {
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
                            onSaveNurseProfile = viewModel::saveNurseNavigatorProfile,
                            onLogGhb = viewModel::addVoluntaryGhbLog,
                            onDeleteGhb = viewModel::removeVoluntaryGhbLog,
                            onClearGhb = viewModel::clearAllVoluntaryGhbLogs
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
private fun BrujulaCompactTopBar(
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        haptics.click()
                        if (isSecondaryScreen) onBackClick() else onBrandClick()
                    }
                    .padding(horizontal = 4.dp, vertical = 3.dp)
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.settings.discreetModeEnabled) "ORIENTACIÓN" else "BRÚJULA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.surfaceSubtle,
                    border = BorderStroke(0.7.dp, colors.borderHairline)
                ) {
                    Text(
                        text = "Sem ${state.settings.currentProgramWeek}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopActionChip(
                    icon = Icons.Outlined.Insights,
                    label = "Progreso",
                    selected = inProgress,
                    onClick = {
                        haptics.click()
                        onOpenProgress()
                    }
                )
                TopActionChip(
                    icon = Icons.Outlined.Settings,
                    label = "Ajustes",
                    selected = inSettings,
                    onClick = {
                        haptics.click()
                        onOpenSettings()
                    }
                )
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = colors.borderHairline)
    }
}

@Composable
private fun TopActionChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val bg = if (selected) colors.jadeWash else colors.surfaceElevated
    val border = if (selected) colors.jadePrimary.copy(alpha = 0.65f) else colors.borderHairline
    val fg = if (selected) colors.jadePrimary else colors.inkSecondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg, RoundedCornerShape(50))
            .border(0.8.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = fg,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = fg
        )
    }
}

@Composable
private fun CompassMarkGlyph(
    primaryColor: androidx.compose.ui.graphics.Color,
    ringColor: androidx.compose.ui.graphics.Color
) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f - 1.dp.toPx()
        drawCircle(
            color = ringColor,
            radius = r,
            center = c,
            style = Stroke(width = 1.2.dp.toPx())
        )
        val needle = Path().apply {
            moveTo(c.x, c.y - r * 0.72f)
            lineTo(c.x + r * 0.24f, c.y)
            lineTo(c.x, c.y + r * 0.72f)
            lineTo(c.x - r * 0.24f, c.y)
            close()
        }
        drawPath(path = needle, color = primaryColor)
    }
}

@Composable
private fun BrujulaCompactBottomBar(
    currentRoute: String,
    discreetMode: Boolean,
    onSelectTab: (PrimaryNavTab) -> Unit
) {
    val colors = BrujulaDesign.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(thickness = 0.8.dp, color = colors.borderHairline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryTabs.forEach { tab ->
                val isSelected = currentRoute.startsWith(tab.routePrefix)
                val fg = if (isSelected) colors.jadePrimary else colors.inkMuted
                val pillBg = if (isSelected) colors.jadeWash else androidx.compose.ui.graphics.Color.Transparent

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(28.dp)
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
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.1.sp
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
