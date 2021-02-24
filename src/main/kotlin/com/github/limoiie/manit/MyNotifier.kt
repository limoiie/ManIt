package com.github.limoiie.manit

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object MyNotifier {
    private val NOTIFICATION_GROUP = NotificationGroup(
        "Cpp Man",
        NotificationDisplayType.BALLOON, true
    )

    @JvmStatic
    fun notify(project: Project?, content: String, type: NotificationType = NotificationType.INFORMATION) {
        NOTIFICATION_GROUP.createNotification(content, type)
            .notify(project)
    }
}
