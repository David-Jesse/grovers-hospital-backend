package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.PackageTierInclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageTierInclusionRepository extends JpaRepository<PackageTierInclusion, Long> {

    /**
     * Load every cell that belongs to the given package — i.e. every cell whose tier hangs off the package.
     */
    @Query("""
            SELECT c FROM PackageTierInclusion c
            WHERE c.tier.healthPackage.id = :packageId
            """)
    List<PackageTierInclusion> findAllByPackageId(@Param("packageId") Long packageId);

    /**
     * Bulk-delete every cell belonging to the given package. Used by the atomic-replace endpoint
     * before re-inserting the full grid.
     */
    /**
     * Bulk-delete every cell belonging to the given package. Used by the atomic-replace endpoint
     * before re-inserting the full grid.
     *
     * <p>{@code flushAutomatically = true} ensures any pending inserts are flushed before the DELETE
     * runs, so the DB is consistent when we then save the replacement cells. We deliberately do NOT
     * clear the persistence context: the caller has the package + its tiers + its inclusions loaded
     * and needs them to stay managed while it builds and saves the new {@code PackageTierInclusion}
     * entities, whose @ManyToOne references must point at still-managed parents.</p>
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            DELETE FROM PackageTierInclusion c
            WHERE c.tier.healthPackage.id = :packageId
            """)
    int deleteAllByPackageId(@Param("packageId") Long packageId);
}