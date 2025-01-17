package gg.flyte.twilight.scoreboard

import gg.flyte.twilight.string.toMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

class TwilightScoreboard(private val player: Player) {
    private var scoreboard: Scoreboard = Bukkit.getScoreboardManager().newScoreboard
    private var sidebarObjective: Objective? = null
    private var belowNameObjective: Objective? = null
    private val teams = mutableMapOf<String, Team>()

    init {
        player.scoreboard = scoreboard
    }

    fun updateSidebarTitle(title: String) {
        if (sidebarObjective == null) {
            sidebarObjective = scoreboard.registerNewObjective("sidebar", "dummy", title.toMini())
            sidebarObjective?.displaySlot = DisplaySlot.SIDEBAR
        } else {
            sidebarObjective?.displayName(title.toMini())
        }
    }

    fun updateSidebarLines(vararg lines: String) {
        if (sidebarObjective == null) return

        scoreboard.entries.forEach { entry ->
            if (entry.startsWith(" ")) {
                scoreboard.resetScores(entry)
            }
        }

        lines.forEachIndexed { index, line ->
            val score = lines.size - index
            val entry = " ".repeat(score)

            val team = teams.getOrPut("line_$index") {
                scoreboard.getTeam("line_$index") ?: scoreboard.registerNewTeam("line_$index").apply {
                    addEntry(entry)
                }
            }

            team.prefix(line.toMini())
            sidebarObjective?.getScore(entry)?.score = score
        }
    }

    fun updateTabList(header: () -> String, footer: () -> String) {
        player.sendPlayerListHeaderAndFooter(
            header().toMini(),
            footer().toMini()
        )
    }

    fun belowName(title: String) {
        if (belowNameObjective == null) {
            belowNameObjective = scoreboard.registerNewObjective("belowname", "dummy", title.toMini())
            belowNameObjective?.displaySlot = DisplaySlot.BELOW_NAME
        } else {
            belowNameObjective?.displayName(title.toMini())
        }
    }

    fun updateBelowNameScore(target: Player, score: Int) { belowNameObjective?.getScore(target.name)?.score = score }

    fun prefix(target: Player, prefix: String) {
        val teamName = "prefix_${target.uniqueId}"
        val team = teams.getOrPut(teamName) {
            scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)
        }
        team.prefix(prefix.toMini())
        team.addEntry(target.name)
    }

    fun suffix(target: Player, suffix: String) {
        val teamName = "prefix_${target.uniqueId}"
        val team = teams.getOrPut(teamName) {
            scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)
        }
        team.suffix(suffix.toMini())
        team.addEntry(target.name)
    }

    fun player(): Player = player

    fun delete() {
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        sidebarObjective?.unregister()
        belowNameObjective?.unregister()
        teams.values.forEach { it.unregister() }
        teams.clear()
    }
}