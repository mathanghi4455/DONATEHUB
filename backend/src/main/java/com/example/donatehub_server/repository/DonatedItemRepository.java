package com.example.donatehub_server.repository;

import com.example.donatehub_server.model.DonatedItem;
import com.example.donatehub_server.model.ItemCategory;
import com.example.donatehub_server.model.ItemCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonatedItemRepository extends JpaRepository<DonatedItem, Long> {

    List<DonatedItem> findByDriveIdOrderByReceivedDateDesc(Long driveId);

    List<DonatedItem> findByDonorIdOrderByReceivedDateDesc(Long donorId);

    List<DonatedItem> findAllByOrderByReceivedDateDesc();

    /** Undistributed items in remaining stock (availableQuantity > 0) */
    List<DonatedItem> findByAvailableQuantityGreaterThanOrderByReceivedDateDesc(int qty);

    /** Undistributed items in stock for a specific drive */
    List<DonatedItem> findByDriveIdAndAvailableQuantityGreaterThanOrderByReceivedDateDesc(Long driveId, int qty);

    List<DonatedItem> findByCategory(ItemCategory category);

    List<DonatedItem> findByItemCondition(ItemCondition condition);

    @Query("SELECT COALESCE(SUM(i.totalQuantity), 0) FROM DonatedItem i WHERE i.drive.id = :driveId")
    int sumTotalQuantityByDriveId(@Param("driveId") Long driveId);

    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM DonatedItem i WHERE i.drive.id = :driveId")
    int sumAvailableQuantityByDriveId(@Param("driveId") Long driveId);

    @Query("SELECT COALESCE(SUM(i.totalQuantity), 0) FROM DonatedItem i")
    int sumTotalQuantityAll();

    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM DonatedItem i")
    int sumAvailableQuantityAll();

    @Query("SELECT COUNT(i) FROM DonatedItem i WHERE i.drive.id = :driveId")
    long countItemsByDriveId(@Param("driveId") Long driveId);

    @Query("SELECT COUNT(i) FROM DonatedItem i WHERE i.availableQuantity > 0")
    long countUndistributedItems();
}
