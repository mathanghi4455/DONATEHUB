package com.example.donatehub_server.repository;

import com.example.donatehub_server.model.DistributionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistributionRecordRepository extends JpaRepository<DistributionRecord, Long> {

    List<DistributionRecord> findByDriveIdOrderByDistributionDateDesc(Long driveId);

    List<DistributionRecord> findByDonatedItemIdOrderByDistributionDateDesc(Long itemId);

    List<DistributionRecord> findByRecipientIdOrderByDistributionDateDesc(Long recipientId);

    List<DistributionRecord> findAllByOrderByDistributionDateDesc();

    @Query("SELECT COALESCE(SUM(d.distributedQuantity), 0) FROM DistributionRecord d WHERE d.drive.id = :driveId")
    int sumDistributedQuantityByDriveId(@Param("driveId") Long driveId);

    @Query("SELECT COALESCE(SUM(d.distributedQuantity), 0) FROM DistributionRecord d")
    int sumDistributedQuantityAll();

    @Query("SELECT COUNT(d) FROM DistributionRecord d WHERE d.drive.id = :driveId")
    long countDistributionsByDriveId(@Param("driveId") Long driveId);
}
