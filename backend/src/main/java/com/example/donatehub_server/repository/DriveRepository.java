package com.example.donatehub_server.repository;

import com.example.donatehub_server.model.Drive;
import com.example.donatehub_server.model.DriveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {

    List<Drive> findAllByOrderByStartDateDesc();

    List<Drive> findByStatus(DriveStatus status);
}
