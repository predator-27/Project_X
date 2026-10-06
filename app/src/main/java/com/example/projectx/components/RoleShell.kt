package com.projectx.app.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projectx.app.model.UserRole
import com.projectx.app.theme.CampusTokens
import kotlinx.coroutines.launch

/**
 * Shared drawer-and-content scaffold used by the four non-student role shells
 * (Faculty, Lost&Found Staff, College Admin, Super Admin). The student shell has its
 * own bespoke scaffold with a bottom nav and keeps living in MainActivity.
 *
 * [content] receives:
 *   - `activeId` : the currently-selected drawer item
 *   - `onMenuClick` : opens the drawer (pass this into every top bar)
 *   - `setActive` : programmatic switch (rarely used by callers)
 */
@Composable
fun RoleShell(
    role: UserRole,
    initialItemId: String = "home",
    displayName: String? = null,
    rollNumber: String? = null,
    email: String? = null,
    onItemSelected: (String) -> Unit = {},
    onSignOut: () -> Unit = {},
    content: @Composable (activeId: String, onMenuClick: () -> Unit, setActive: (String) -> Unit) -> Unit,
) {
    val c = CampusTokens.colors
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var active by remember { mutableStateOf(initialItemId) }

    // Back-press closes the drawer first if open.
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = c.sidebar,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(320.dp),
            ) {
                SideNavDrawerContent(
                    activeItemId = active,
                    displayName = displayName,
                    rollNumber = rollNumber,
                    email = email,
                    role = role,
                    onItemClick = { id ->
                        active = id
                        onItemSelected(id)
                        scope.launch { drawerState.close() }
                    },
                    onLogoutClick = {
                        onSignOut()
                        scope.launch { drawerState.close() }
                    },
                )
            }
        },
    ) {
        content(
            active,
            { scope.launch { drawerState.open() } },
            { id -> active = id },
        )
    }
}
