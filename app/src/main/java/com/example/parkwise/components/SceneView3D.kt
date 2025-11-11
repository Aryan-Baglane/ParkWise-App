//package com.example.parkwise.components
//
//
//
//import android.content.Context
//import android.view.View
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.viewinterop.AndroidView
//import com.example.parkwise.model.ParkingSlot
//
///**
// * SceneView3D is a Compose wrapper around a native 3D view (SceneView/Filament/Sceneform).
// * It loads a GLB model and provides a method to recolor nodes by name (node ids should map to backend slot ids).
// *
// * NOTE: This file contains *pseudocode* for node recoloring — actual API depends on the 3D engine you choose.
// * Recommended libraries:
// * - SceneView (https://github.com/sceneview/sceneview-android)
// * - Filament + glTF loader
// * - Sceneform community fork
// *
// * Steps:
// * 1) Place `parking_area_{areaId}.glb` in assets or remote URL.
// * 2) Ensure its node names are `slot_1`, `slot_2`, ... matching your slot ids.
// * 3) Call updateSlotColors(mapOf(slotNodeName -> colorInt)).
// */
//
//@Composable
//fun SceneView3D(
//    context: Context,
//    glbAssetPath: String, // "models/area_12.glb" or remote url
//    slots: List<ParkingSlot>,
//    modifier: Modifier = Modifier
//) {
//    // Keep a state map of slot node colors
//    val slotColors = remember { mutableStateMapOf<String, Int>() }
//    // Update colors from slots list
//    LaunchedEffect(slots) {
//        slots.forEach { slot ->
//            val nodeName = "slot_${slot.id}"
//            val color = when (slot.status) {
//                com.example.parkwise.model.SlotStatus.AVAILABLE -> 0xFF00C853.toInt() // green
//                com.example.parkwise.model.SlotStatus.OCCUPIED -> 0xFFFF5252.toInt() // red
//                com.example.parkwise.model.SlotStatus.RESERVED -> 0xFF9E9E9E.toInt() // gray
//            }
//            slotColors[nodeName] = color
//        }
//    }
//
//    AndroidView(factory = { ctx ->
//        // Create and return the native 3D view here (scene view / surface)
//        // Example pseudocode for a SceneView instance:
//        val sceneView = createSceneView(ctx) // implement wrapper to instantiate your 3D view
//        loadGlbIntoScene(sceneView, glbAssetPath) // load GLB
//
//        // once loaded, set initial colors
//        sceneView.setOnModelLoadedListener {
//            slotColors.forEach { (nodeName, colorInt) ->
//                // Pseudocode: find node and set material base color
//                // Actual API varies by 3D engine
//                try {
//                    val node = sceneView.findNode(nodeName)
//                    node?.setMaterialColor(colorInt)
//                } catch (e: Exception) { /* handle */ }
//            }
//        }
//
//        // observe changes (this simple example does not wire a live observer to slotColors)
//        sceneView
//    }, update = { view ->
//        // on recomposition, update node colors
//        slotColors.forEach { (nodeName, colorInt) ->
//            try {
//                val node = findNodeOnView(view, nodeName)
//                node?.setMaterialColor(colorInt)
//            } catch (e: Exception) { }
//        }
//    })
//}
//
///** ---------- PSEUDOCODE helper functions - implement per chosen 3D engine ---------- */
//
//private fun createSceneView(ctx: Context): View {
//    // Example: if using sceneform-android:
//    // val sceneView = ArSceneView(ctx)
//    // sceneView.scene.addOnUpdateListener { ... }
//    // return sceneView
//    throw NotImplementedError("Implement createSceneView according to your 3D library")
//}
//
//private fun loadGlbIntoScene(sceneView: View, assetPath: String) {
//    // Implement loading GLB / glTF into the view.
//    // If using SceneView (sceneview-android): sceneView.loadModelGlb(...)
//}
//
//private fun findNodeOnView(view: View, nodeName: String): Any? {
//    // find and return node reference so you can call setMaterialColor() on it.
//    return null
//}
