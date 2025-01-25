package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.permissions.PermissionResult

class TestPermissionChecker : PermissionChecker {
    val permissionResults = mutableListOf<PermissionResult>()

    override fun hasPermissions(permissions: List<Permission>): List<PermissionResult> {
        return permissions.map { permission ->
            PermissionResult(
                permission,
                permissionResults.first { it.permission == permission }.granted,
            )
        }
    }

    override fun hasPermission(permission: Permission): Boolean {
        return permissionResults.first { it.permission == permission }.granted
    }
}
