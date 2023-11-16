/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.heuristics

object HeuristicsUtils {
    fun <T : IdentifiableNode> findNearestNodeByParentId(currentField: T, fields: List<T>): T? {
        if (fields.isEmpty()) return null

        // Initialize variables to keep track of the nearest node and the minimum number of jumps.
        var nearest: T = fields.first()
        var minJumps = Int.MAX_VALUE

        // Iterate over each node in the list.
        for (node in fields) {
            // Find the common path between the target node's parentList and the current node's parentList.
            val commonPath = currentField.parentPath.intersect(node.parentPath.toSet()).toList()

            // Calculate the number of jumps needed to reach the common path node.
            val jumps = currentField.parentPath.size - commonPath.size + node.parentPath.size - commonPath.size

            // If the current node requires fewer jumps, update the nearest node.
            if (jumps < minJumps) {
                nearest = node
                minJumps = jumps
            }
        }

        return nearest
    }
}
