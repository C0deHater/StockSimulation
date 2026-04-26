package kr.ac.dankook.StockSimulationGame2026.repository;

import kr.ac.dankook.StockSimulationGame2026.domain.StockMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMetadataRepository extends JpaRepository<StockMetadata, String> {
}
