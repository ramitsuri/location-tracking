package com.ramitsuri.locationtracking.permissions

interface PermissionChecker {
    fun hasPermissions(permissions: List<Permission>): List<PermissionResult>
    fun hasPermission(permission: Permission): Boolean
}
