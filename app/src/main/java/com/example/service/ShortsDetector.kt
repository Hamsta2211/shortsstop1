package com.example.service

import android.view.accessibility.AccessibilityNodeInfo

object ShortsDetector {

    const val PACKAGE_YOUTUBE = "com.google.android.youtube"
    const val PACKAGE_YOUTUBE_KIDS = "com.google.android.apps.youtube.kids"
    const val PACKAGE_YOUTUBE_MUSIC = "com.google.android.apps.youtube.music"

    val BROWSER_PACKAGES = setOf(
        "com.android.chrome",
        "com.chrome.beta",
        "com.chrome.canary",
        "com.chrome.dev",
        "org.mozilla.firefox",
        "org.mozilla.firefox_beta",
        "org.mozilla.fenix",
        "com.sec.android.app.sbrowser",
        "com.microsoft.emmx",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.opera.gx",
        "com.brave.browser",
        "com.duckduckgo.mobile.android",
        "com.vivaldi.browser",
        "com.kiwibrowser.browser",
        "com.ecosia.android",
        "org.chromium.chrome"
    )

    private val YOUTUBE_SHORTS_VIEW_IDS = listOf(
        "reel_recycler",
        "reel_player_fragment",
        "reel_player_page_tree",
        "reel_watch_fragment",
        "reel_header",
        "reel_root",
        "shorts_container",
        "shorts_player",
        "reel_view_pager",
        "reel_shelf"
    )

    private val YOUTUBE_SHORTS_KEYWORDS = listOf(
        "shorts",
        "youtube shorts",
        "sound aus diesem short",
        "dieses short remixen",
        "in shorts verwenden",
        "remix this video into shorts",
        "use this sound in shorts"
    )

    private val BROWSER_SHORTS_URL_PATTERNS = listOf(
        "youtube.com/shorts",
        "m.youtube.com/shorts",
        "youtu.be/shorts",
        "/shorts/"
    )

    data class DetectionResult(
        val isShorts: Boolean,
        val appName: String,
        val packageName: String,
        val reason: String
    )

    fun isSupportedApp(packageName: CharSequence?): Boolean {
        if (packageName == null) return false
        val pkg = packageName.toString()
        return pkg == PACKAGE_YOUTUBE || BROWSER_PACKAGES.contains(pkg)
    }

    fun getAppDisplayName(packageName: String): String {
        return when {
            packageName == PACKAGE_YOUTUBE -> "YouTube App"
            packageName.contains("chrome") -> "Google Chrome"
            packageName.contains("firefox") || packageName.contains("fenix") -> "Mozilla Firefox"
            packageName.contains("sbrowser") -> "Samsung Internet"
            packageName.contains("emmx") -> "Microsoft Edge"
            packageName.contains("brave") -> "Brave Browser"
            packageName.contains("opera") -> "Opera"
            packageName.contains("duckduckgo") -> "DuckDuckGo"
            else -> "Web Browser"
        }
    }

    fun detectShorts(rootNode: AccessibilityNodeInfo?, currentPackage: String): DetectionResult {
        if (rootNode == null) {
            return DetectionResult(false, "", currentPackage, "")
        }

        if (currentPackage == PACKAGE_YOUTUBE) {
            return detectInYouTube(rootNode, currentPackage)
        } else if (BROWSER_PACKAGES.contains(currentPackage)) {
            return detectInBrowser(rootNode, currentPackage)
        }

        return DetectionResult(false, "", currentPackage, "")
    }

    private fun detectInYouTube(rootNode: AccessibilityNodeInfo, packageName: String): DetectionResult {
        var foundShorts = false
        var reason = ""

        fun traverse(node: AccessibilityNodeInfo?, depth: Int = 0) {
            if (node == null || foundShorts || depth > 25) return

            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            val text = node.text?.toString()?.lowercase() ?: ""
            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""

            for (targetId in YOUTUBE_SHORTS_VIEW_IDS) {
                if (viewId.contains(targetId)) {
                    foundShorts = true
                    reason = "Shorts Player/Feed Element erkannt ($targetId)"
                    return
                }
            }

            // Check if Shorts bottom tab is active
            if ((text == "shorts" || contentDesc == "shorts") && (node.isSelected || node.isFocused)) {
                foundShorts = true
                reason = "Shorts-Tab aktiv"
                return
            }

            // Check specific video player keywords
            for (kw in YOUTUBE_SHORTS_KEYWORDS) {
                if (kw != "shorts") {
                    if (text.contains(kw) || contentDesc.contains(kw)) {
                        foundShorts = true
                        reason = "Shorts-Aktionselement ($kw)"
                        return
                    }
                }
            }

            val childCount = node.childCount
            for (i in 0 until childCount) {
                val child = node.getChild(i) ?: continue
                traverse(child, depth + 1)
                child.recycle()
                if (foundShorts) return
            }
        }

        traverse(rootNode)
        return DetectionResult(
            isShorts = foundShorts,
            appName = "YouTube App",
            packageName = packageName,
            reason = reason
        )
    }

    private fun detectInBrowser(rootNode: AccessibilityNodeInfo, packageName: String): DetectionResult {
        var foundShorts = false
        var reason = ""
        val appName = getAppDisplayName(packageName)

        fun traverse(node: AccessibilityNodeInfo?, depth: Int = 0) {
            if (node == null || foundShorts || depth > 25) return

            val text = node.text?.toString()?.lowercase() ?: ""
            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
            val viewId = node.viewIdResourceName?.lowercase() ?: ""

            // Check URL patterns
            for (pattern in BROWSER_SHORTS_URL_PATTERNS) {
                if (text.contains(pattern) || contentDesc.contains(pattern)) {
                    foundShorts = true
                    reason = "Shorts URL im Browser erkannt ($pattern)"
                    return
                }
            }

            // Title indicator in browser tab
            if (text.contains("shorts - youtube") || contentDesc.contains("shorts - youtube") ||
                text.contains("youtube shorts") || contentDesc.contains("youtube shorts")
            ) {
                foundShorts = true
                reason = "Shorts Webseiten-Titel erkannt"
                return
            }

            val childCount = node.childCount
            for (i in 0 until childCount) {
                val child = node.getChild(i) ?: continue
                traverse(child, depth + 1)
                child.recycle()
                if (foundShorts) return
            }
        }

        traverse(rootNode)
        return DetectionResult(
            isShorts = foundShorts,
            appName = appName,
            packageName = packageName,
            reason = reason
        )
    }

    fun findYouTubeHomeTab(rootNode: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (rootNode == null) return null

        var homeNode: AccessibilityNodeInfo? = null

        fun searchHome(node: AccessibilityNodeInfo?, depth: Int = 0) {
            if (node == null || homeNode != null || depth > 25) return

            val text = node.text?.toString()?.lowercase() ?: ""
            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""

            val isHome = text == "startseite" || text == "home" || text == "start" ||
                    contentDesc.contains("startseite") || contentDesc == "home" || contentDesc.contains("start tab")

            if (isHome && (node.isClickable || node.parent?.isClickable == true)) {
                homeNode = if (node.isClickable) node else node.parent
                return
            }

            val childCount = node.childCount
            for (i in 0 until childCount) {
                val child = node.getChild(i) ?: continue
                searchHome(child, depth + 1)
                if (homeNode != null) return
                child.recycle()
            }
        }

        searchHome(rootNode)
        return homeNode
    }
}
