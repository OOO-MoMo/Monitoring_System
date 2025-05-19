package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.momo.monitoring.store.entities.SensorData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, UUID> {

    List<SensorData> findBySensorIdAndTimestampBetweenOrderByTimestampAsc(
            UUID sensorId,
            LocalDateTime timestampStart,
            LocalDateTime timestampEnd
    );

    // --- Агрегация AVG (Native Query) ---
    @Query(value = "SELECT " +
            "    date_trunc(:granularity, sd.timestamp) as interval_start, " +
            "    AVG(CAST(NULLIF(regexp_replace(REPLACE(sd.value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision)) as aggregated_value, " +
            "    NULL as aggregated_status " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime " +
            "GROUP BY interval_start " +
            "ORDER BY interval_start ASC", nativeQuery = true)
    List<Object[]> findNativeAggregatedAvgByGranularity(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("granularity") String granularity
    );

    // --- Агрегация MIN (Native Query) ---
    @Query(value = "SELECT " +
            "    date_trunc(:granularity, sd.timestamp) as interval_start, " +
            "    MIN(CAST(NULLIF(regexp_replace(REPLACE(sd.value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision)) as aggregated_value, " + // <--- ИЗМЕНЕНО
            "    NULL as aggregated_status " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime " +
            "GROUP BY interval_start " +
            "ORDER BY interval_start ASC", nativeQuery = true)
    List<Object[]> findNativeAggregatedMinByGranularity(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("granularity") String granularity
    );

    // --- Агрегация MAX (Native Query) ---
    @Query(value = "SELECT " +
            "    date_trunc(:granularity, sd.timestamp) as interval_start, " +
            "    MAX(CAST(NULLIF(regexp_replace(REPLACE(sd.value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision)) as aggregated_value, " + // <--- ИЗМЕНЕНО
            "    NULL as aggregated_status " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime " +
            "GROUP BY interval_start " +
            "ORDER BY interval_start ASC", nativeQuery = true)
    List<Object[]> findNativeAggregatedMaxByGranularity(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("granularity") String granularity
    );

    // --- Агрегация SUM (Native Query) ---
    @Query(value = "SELECT " +
            "    date_trunc(:granularity, sd.timestamp) as interval_start, " +
            "    SUM(CAST(NULLIF(regexp_replace(REPLACE(sd.value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision)) as aggregated_value, " + // <--- ИЗМЕНЕНО
            "    NULL as aggregated_status " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime " +
            "GROUP BY interval_start " +
            "ORDER BY interval_start ASC", nativeQuery = true)
    List<Object[]> findNativeAggregatedSumByGranularity(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("granularity") String granularity
    );

    // --- Агрегация COUNT (Native Query) - остается без изменений ---
    @Query(value = "SELECT " +
            "    date_trunc(:granularity, sd.timestamp) as interval_start, " +
            "    COUNT(sd.id) as aggregated_value, " +
            "    NULL as aggregated_status " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime " +
            "GROUP BY interval_start " +
            "ORDER BY interval_start ASC", nativeQuery = true)
    List<Object[]> findNativeAggregatedCountByGranularity(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("granularity") String granularity
    );

    // --- Агрегация LAST (Native Query) - уже был правильный CAST для value ---
    @Query(value = "WITH RankedSensorData AS (" +
            "    SELECT " +
            "        date_trunc(:granularity, sd.timestamp) as interval_start, " +
            "        sd.value as original_value, " +
            "        sd.status as original_status, " +
            "        sd.timestamp as original_timestamp, " +
            "        ROW_NUMBER() OVER (PARTITION BY date_trunc(:granularity, sd.timestamp) ORDER BY sd.timestamp DESC) as rn " +
            "    FROM sensor_data sd " +
            "    WHERE sd.sensor_id = :sensorId " +
            "      AND sd.timestamp >= :fromTime " +
            "      AND sd.timestamp < :toTime " +
            ") " +
            "SELECT " +
            "    rsd.interval_start, " +
            "    CAST(NULLIF(regexp_replace(REPLACE(rsd.original_value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision) as agg_value, " + // Этот CAST уже был корректен
            "    CAST(rsd.original_status AS text) as agg_status " +
            "FROM RankedSensorData rsd " +
            "WHERE rsd.rn = 1 " +
            "ORDER BY rsd.interval_start ASC", nativeQuery = true)
    List<Object[]> findNativeAggregatedLastByGranularity(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("granularity") String granularity
    );

    // --- Агрегация FIRST (Native Query) - уже был правильный CAST для value ---
    @Query(value = "WITH RankedSensorData AS (" +
            "    SELECT " +
            "        date_trunc(:granularity, sd.timestamp) as interval_start, " +
            "        sd.value as original_value, " +
            "        sd.status as original_status, " +
            "        sd.timestamp as original_timestamp, " +
            "        ROW_NUMBER() OVER (PARTITION BY date_trunc(:granularity, sd.timestamp) ORDER BY sd.timestamp ASC) as rn " +
            "    FROM sensor_data sd " +
            "    WHERE sd.sensor_id = :sensorId " +
            "      AND sd.timestamp >= :fromTime " +
            "      AND sd.timestamp < :toTime " +
            ") " +
            "SELECT " +
            "    rsd.interval_start, " +
            "    CAST(NULLIF(regexp_replace(REPLACE(rsd.original_value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision) as agg_value, " + // Этот CAST уже был корректен
            "    CAST(rsd.original_status AS text) as agg_status " +
            "FROM RankedSensorData rsd " +
            "WHERE rsd.rn = 1 " +
            "ORDER BY rsd.interval_start ASC", nativeQuery = true)
    List<Object[]> findNativeAggregatedFirstByGranularity(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("granularity") String granularity
    );

    @Query(value = "SELECT CAST(sd.status AS text) as status_text, COUNT(sd.id) as count " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId " +
            "  AND sd.timestamp >= :fromTime " +
            "  AND sd.timestamp < :toTime " +
            "  AND sd.status IN ('CRITICAL', 'WARNING') " +
            "GROUP BY sd.status", nativeQuery = true)
    List<Object[]> countSensorStatusesInPeriod(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query(value = "SELECT AVG(CAST(NULLIF(regexp_replace(REPLACE(sd.value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision)) " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime", nativeQuery = true)
    Optional<Double> findAvgValueInPeriod(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query(value = "SELECT MIN(CAST(NULLIF(regexp_replace(REPLACE(sd.value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision)) " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime", nativeQuery = true)
    Optional<Double> findMinValueInPeriod(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query(value = "SELECT MAX(CAST(NULLIF(regexp_replace(REPLACE(sd.value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision)) " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime", nativeQuery = true)
    Optional<Double> findMaxValueInPeriod(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    // Для LAST значения и статуса
    @Query(value = "SELECT " +
            "    CAST(NULLIF(regexp_replace(REPLACE(sd.value, ',', '.'), '[^0-9.-]+', '', 'g'), '') AS double precision) as last_value, " + // Первый элемент Object[0]
            "    CAST(sd.status AS text) as last_status " +
            "FROM sensor_data sd " +
            "WHERE sd.sensor_id = :sensorId AND sd.timestamp >= :fromTime AND sd.timestamp < :toTime " +
            "ORDER BY sd.timestamp DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<Object[]> findLastValueAndStatusInPeriod(
            @Param("sensorId") UUID sensorId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

}