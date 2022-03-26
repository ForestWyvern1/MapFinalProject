package com.example.maptest

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import com.example.maptest.databinding.ActivityMainBinding
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val startPoint = GeoPoint(55.801139, 49.177280)
    private val endPoint = GeoPoint(55.802256, 49.185435)

    private val pointsArray = arrayOf(endPoint, GeoPoint(55.796231, 49.180902))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Фикс ошибки с сетью
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build()
        )

        // Обязательно
        Configuration.getInstance().load(this, getSharedPreferences("ws", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "My user agent"

        // Tile карты
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        // Два пальца
        binding.mapView.setMultiTouchControls(true)
        // Отключение кнопок зума
        binding.mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        // настройки камеры и т.п.
        val mapController = binding.mapView.controller
        mapController.setCenter(startPoint)
        mapController.setZoom(18)

        // начальный маркер
        val startMarker = Marker(binding.mapView)
        startMarker.icon = resources.getDrawable(R.drawable.ic_baseline_location_on_24)
        startMarker.position = startPoint
        binding.mapView.overlays.add(startMarker)

        // обработка нажатия на карту
        binding.mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                // создание маркера по нажатию
                val marker = Marker(binding.mapView)
                marker.icon = resources.getDrawable(R.drawable.ic_baseline_location_on_24)
                marker.position = p!!
                binding.mapView.overlays.add(marker)

                setRoad(p)

                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }))

        // построение маркеров из массива
        for (i in pointsArray) {
            val marker = Marker(binding.mapView)
            marker.icon = resources.getDrawable(R.drawable.ic_baseline_location_on_24)
            marker.position = i

            // обработка нажатия на маркер
            marker.setOnMarkerClickListener { marker, mapView ->
                setRoad(marker.position)

                true
            }

            binding.mapView.overlays.add(marker)
        }
    }

    // добавление маршрута
    private fun setRoad(point: GeoPoint) {
        val roadManager = OSRMRoadManager(this, "My user agent")
        val road = roadManager.getRoad(arrayListOf(startPoint, point))
        val roadOverlay = OSRMRoadManager.buildRoadOverlay(road)

        // настройка линии
        roadOverlay.outlinePaint.apply {
            color = Color.RED
            strokeWidth = 10f
        }

        binding.mapView.overlays.add(roadOverlay)

        // AHTUNG!!!
        // AHTUNG!!!
        // AHTUNG!!!
        // ОБЯЗАТЕЛЬНО ПОСЛЕ ЛЮБОГО ИЗМЕНЕНИЯ ОВЕРЛЕЕВ
        binding.mapView.invalidate()
    }

}